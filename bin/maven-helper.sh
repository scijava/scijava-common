#!/bin/sh

# This script uses the ImageJ Maven repository at http://maven.imagej.net/
# to fetch an artifact, or to determine the state of it.

# error out whenever a command fails
set -e

root_url=http://maven.imagej.net/content/repositories

die () {
	echo "$*" >&2
	exit 1
}

# Helper (thanks, BSD!)

get_mtime () {
	stat -c %Y "$1"
}
case "$(uname -s 2> /dev/null)" in
MINGW*)
	get_mtime () {
		date -r "$1" +%s
	}
	;;
Darwin)
	get_mtime () {
		stat -f %m "$1"
	}
	;;
esac

# Parse <groupId>:<artifactId>:<version> triplets (i.e. GAV parameters)

groupId () {
	echo "${1%%:*}"
}

artifactId () {
	result="${1#*:}"
	echo "${result%%:*}"
}

version () {
	result="${1#*:}"
	case "$result" in
	*:*)
		echo "${1##*:}"
		;;
	esac
}

# Given an xml, extract the first <tag>

extract_tag () {
	result="${2#*<$1>}"
	case "$result" in
	"$2")
		;;
	*)
		echo "${result%%</$1>*}"
		;;
	esac
}

# Given an xml, skip all <tag> sections

skip_tag () {
	result="$2"
	while true
	do
		case "$result" in
		*"<$1>"*)
			result="${result%%<$1>*}${result#*</$1>}"
			;;
		*)
			break
			;;
		esac
	done
	echo "$result"
}

# Given a GAV parameter, determine the base URL of the project

project_url () {
	gav="$1"
	artifactId="$(artifactId "$gav")"
	infix="$(groupId "$gav" | tr . /)/$artifactId"
	version="$(version "$gav")"
	case "$version" in
	*SNAPSHOT)
		echo "$root_url/snapshots/$infix"
		;;
	*)
		# Release could be in either releases or thirdparty; try releases first
		project_url="$root_url/releases/$infix"
		header=$(curl -Is "$project_url/")
		case "$header" in
		"HTTP/1.1 200 OK"*)
			;;
		*)
			project_url="$root_url/thirdparty/$infix"
			;;
		esac
		echo "$project_url"
		;;
	esac
}

# Given a GAV parameter, determine the URL of the .jar file

jar_url () {
	gav="$1"
	artifactId="$(artifactId "$gav")"
	version="$(version "$gav")"
	infix="$(groupId "$gav" | tr . /)/$artifactId/$version"
	case "$version" in
	*-SNAPSHOT)
		url="$root_url/snapshots/$infix/maven-metadata.xml"
		metadata="$(curl -s "$url")"
		timestamp="$(extract_tag timestamp "$metadata")"
		buildNumber="$(extract_tag buildNumber "$metadata")"
		version=${version%-SNAPSHOT}-$timestamp-$buildNumber
		echo "$root_url/snapshots/$infix/$artifactId-$version.jar"
		;;
	*)
		echo "$root_url/releases/$infix/$artifactId-$version.jar"
		;;
	esac
}

# Given a GAV parameter, return the URL to the corresponding .pom file

pom_url () {
	url="$(jar_url "$1")"
	echo "${url%.jar}.pom"
}

# Given a POM file, find its GAV parameter

gav_from_pom () {
	pom="$(cat "$1")"
	parent="$(extract_tag parent "$pom")"
	pom="$(skip_tag parent "$pom")"
	pom="$(skip_tag dependencies "$pom")"
	pom="$(skip_tag profiles "$pom")"
	pom="$(skip_tag build "$pom")"
	groupId="$(extract_tag groupId "$pom")"
	test -n "$groupId" || groupId="$(extract_tag groupId "$parent")"
	artifactId="$(extract_tag artifactId "$pom")"
	version="$(extract_tag version "$pom")"
	test -n "$version" || version="$(extract_tag version "$parent")"
	echo "$groupId:$artifactId:$version"
}

# Given a POM file, find its parent's GAV

parent_gav_from_pom () {
	pom="$(cat "$1")"
	parent="$(extract_tag parent "$pom")"
	groupId="$(extract_tag groupId "$parent")"
	artifactId="$(extract_tag artifactId "$parent")"
	version="$(extract_tag version "$parent")"
	echo "$groupId:$artifactId:$version"
}

# Given a POM file, extract its packaging

packaging_from_pom () {
	pom="$(cat "$1")"
	pom="$(skip_tag parent "$pom")"
	pom="$(skip_tag dependencies "$pom")"
	pom="$(skip_tag profiles "$pom")"
	pom="$(skip_tag build "$pom")"
	packaging="$(extract_tag packaging "$pom")"
	echo "${packaging:-jar}"
}

# Given a GAV parameter possibly lacking a version, determine the latest version

latest_version () {
	metadata="$(curl -s "$(project_url "$1")"/maven-metadata.xml)"
	latest="$(extract_tag release "$metadata")"
	test -n "$latest" || latest="$(extract_tag latest "$metadata")"
	test -n "$latest" || latest="$(extract_tag version "$metadata")"
	echo "$latest"
}

# Generate a temporary file; not thread-safe

tmpfile () {
	i=1
	while test -f /tmp/precompiled.$i"$1"
	do
		i=$(($i+1))
	done
	echo /tmp/precompiled.$i"$1"
}

# Given a GAV or a path, read the POM

read_pom () {
	case "$1" in
	pom.xml|*/pom.xml|*\\pom.xml)
		cat "$1"
		;;
	*)
		curl -s "$(pom_url "$1")"
		;;
	esac
}

# Given a GAV parameter (or pom.xml path) and a name, resolve a property (falling back to parents)

get_property () {
	gav="$1"
	key="$2"
	case "$key" in
	imagej1.version)
		latest_version net.imagej:ij
		return
		;;
	project.groupId)
		groupId "$gav"
		return
		;;
	project.version)
		version "$gav"
		return
		;;
	esac
	while test -n "$gav"
	do
		pom="$(read_pom "$gav")"
		properties="$(extract_tag properties "$pom")"
		property="$(extract_tag "$key" "$properties")"
		if test -n "$property"
		then
			echo "$property"
			return
		fi
		parent="$(extract_tag parent "$pom")"
		groupId="$(extract_tag groupId "$parent")"
		artifactId="$(extract_tag artifactId "$parent")"
		version="$(extract_tag version "$parent")"
		gav="$groupId:$artifactId:$version"
	done
	die "Could not resolve \${$2} in $1"
}

# Given a GAV parameter and a string, expand properties

expand () {
	gav="$1"
	string="$2"
	result=
	while true
	do
		case "$string" in
		*'${'*'}'*)
			result="$result${string%%\$\{*}"
			string="${string#*\$\{}"
			key="${string%\}*}"
			result="$result$(get_property "$gav" "$key")"
			string="${string#$key\}}"
			;;
		*)
			echo "$result$string"
			break
			;;
		esac
	done
}

# Given a GAV parameter, make a list of its dependencies (as GAV parameters)

get_dependencies () {
	pom="$(read_pom "$1")"
	while true
	do
		case "$pom" in
		*'<dependency>'*)
			dependency="$(extract_tag dependency "$pom")"
			scope="$(extract_tag scope "$dependency")"
			case "$scope" in
			''|compile)
				groupId="$(expand "$1" "$(extract_tag groupId "$dependency")")"
				artifactId="$(extract_tag artifactId "$dependency")"
				version="$(expand "$1" "$(extract_tag version "$dependency")")"
				echo "$groupId:$artifactId:$version"
				;;
			esac
			pom="${pom#*</dependency>}"
			;;
		*)
			break;
		esac
	done
}

# Given a GAV parameter and a space-delimited list of GAV parameters, expand
# the list by the first parameter and its dependencies (unless the list already
# contains said parameter)

get_all_dependencies () {
	case " $2 " in
	*" $1 "*)
		;; # list already contains the depdendency
	*)
		gav="$1"
		set "" "$2 $1"
		for dependency in $(get_dependencies "$gav")
		do
			set "" "$(get_all_dependencies "$dependency" "$2")"
		done
		;;
	esac
	echo "$2"
}

# Given a GAV parameter, download the .jar file

get_jar () {
	url="$(jar_url "$1")"
	tmpfile="$(tmpfile .jar)"
	curl -s "$url" > "$tmpfile"
	test "<html" != "$(head -c 5 "$tmpfile")" ||
	curl -s "${url%.jar}.nar" > "$tmpfile"
	test PK = "$(head -c 2 "$tmpfile")"
	echo "$tmpfile"
}

# Given a GAV parameter, get the commit from the manifest of the deployed .jar

commit_from_gav () {
	jar="$(get_jar "$1")"
	unzip -p "$jar" META-INF/MANIFEST.MF |
	sed -n -e 's/^Implementation-Build: *//pi' |
	tr -d '\r'
	rm "$jar"
}

# Given a GAV parameter, determine whether the .jar file is already in plugins/
# or jars/

is_jar_installed () {
	artifactId="$(artifactId "$1")"
	version="$(version "$1")"
	file=$artifactId-$version.jar
	test -f "$file" || file=../plugins/$file
	test -f "$file" || return 1
	case "$version" in
	*-SNAPSHOT)
		# is the file younger than a day?
		mtime="$(get_mtime "$file")"
		test "$(($mtime-$(date +%s)))" -gt -86400
		;;
	esac
}

# Given a .jar file, determine whether it is an ImageJ 1.x plugin

is_ij1_plugin () {
	unzip -l "$1" plugins.config > /dev/null 2>&1
}

# Given a GAV parameter, download the .jar file and its dependencies as needed
# and install them into plugins/ or jars/, respectively

install_jar () {
	for gav in $(get_all_dependencies "$1")
	do
		if ! is_jar_installed "$gav"
		then
			tmp="$(get_jar "$gav")"
			name="$(artifactId "$gav")-$(version "$gav").jar"
			if test -d ../plugins && is_ij1_plugin "$tmp"
			then
				mv "$tmp" "../plugins/$name"
			else
				mv "$tmp" "$name"
			fi
		fi
	done
}

# Determine whether a local project (specified as pom.xml) needs to be deployed

is_deployed () {
	gav="$(gav_from_pom "$1")" &&
	commit="$(commit_from_gav "$gav")" &&
	test -n "$commit" &&
	dir="$(dirname "$1")" &&
	(cd "$dir" &&
	 git diff --quiet "$commit".. -- .)
}

# The main part

case "$1" in
commit)
	commit_from_gav "$2"
	;;
deps|dependencies)
	get_dependencies "$2"
	;;
all-deps|all-dependencies)
	get_all_dependencies "$2" |
	tr ' ' '\n' |
	grep -v '^$'
	;;
latest-version)
	latest_version "$2"
	;;
gav-from-pom)
	gav_from_pom "$2"
	;;
parent-gav-from-pom)
	parent_gav_from_pom "$2"
	;;
packaging-from-pom)
	packaging_from_pom "$2"
	;;
property-from-pom|get-property|property)
	if test $# -lt 3
	then
		get_property pom.xml "$2"
	else
		get_property "$2" "$3"
	fi
	;;
install)
	install_jar "$2"
	;;
is-deployed)
	is_deployed "$2"
	;;
*)
	die "Usage: $0 [command] [argument...]"'

Commands:

commit <groupId>:<artifactId>:<version>
	Gets the commit from which the given artifact was built.

dependencies <groupId>:<artifactId>:<version>
	Lists the direct dependencies of the given artifact.

all-dependencies <groupId>:<artifactId>:<version>
	Lists all dependencies of the given artifact, including itself and
	transitive dependencies.

latest-version <groupId>:<artifactId>[:<version>]
	Prints the current version of the given artifact (if "SNAPSHOT" is
	passed as version, it prints the current snapshot version rather
	than the release one).

gav-from-pom <pom.xml>
	Prints the GAV parameter described in the given pom.xml file.

parent-gav-from-pom <pom.xml>
	Prints the GAV parameter of the parent project of the pom.xml file.

packaging-from-pom <pom.xml>
	Prints the packaging type of the given project.

property-from-pom <pom.xml> <property-name>
	Prints the property specified in the pom.xml file (or in its parents).

install <groupId>:<artifactId>:<version>
	Installs the given artifact and all its dependencies; if the artifact
	or dependency to install is an ImageJ 1.x plugin and the parent
	directory contains a subdirectory called "plugins", it will be
	installed there, otherwise into the current directory.

is-deployed <pom.xml>
	Tests whether the specified project is deployed alright. Fails
	with exit code 1 if not.
'
	;;
esac
