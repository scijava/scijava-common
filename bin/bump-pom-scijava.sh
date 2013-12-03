#!/bin/sh

die () {
	echo "$*" >&2
	exit 1
}

skip_commit=
bump_parent=
while test $# -gt 0
do
	case "$1" in
	--skip-commit)
		skip_commit=t
		;;
	--bump-parent)
		bump_parent=t
		;;
	--default-properties)
		# handle later
		break
		;;
	-*)
		die "Unknown option: $1"
		;;
	*)
		break
		;;
	esac
	shift
done

require_clean_worktree () {
	test -z "$skip_commit" ||
	return

	git rev-parse HEAD@{u} > /dev/null 2>&1 ||
	die "No upstream configured for the current branch"

	git update-index -q --refresh &&
	git diff-files --quiet --ignore-submodules &&
	git diff-index --cached --quiet --ignore-submodules HEAD -- ||
	die "There are uncommitted changes!"
}

commit_and_push () {
	test -n "$skip_commit" || {
		remote="(none)" &&
		upstream="$(git rev-parse --symbolic-full-name HEAD@{u})" &&
		remote="${upstream#refs/remotes/}" &&
		remote="${remote%%/*}" &&
		git commit -s -m "$@" &&
		git push "$remote" HEAD ||
		die "Could not commit and push to $remote"
	}
}

maven_helper="$(cd "$(dirname "$0")" && pwd)/maven-helper.sh" &&
test -f "$maven_helper" ||
die "Could not find maven-helper.sh"

test -z "$bump_parent" || {
	require_clean_worktree

	test -f pom.xml ||
	die "Not found: pom.xml"

	gav="$(sh "$maven_helper" gav-from-pom pom.xml)" ||
	die "Could not extract GAV from pom.xml"

	case "$gav" in
	*-SNAPSHOT)
		;;
	*)
		die "Not a -SNAPSHOT version: $gav"
		;;
	esac

	gav="$(sh "$maven_helper" parent-gav-from-pom pom.xml)" &&
	version="${gav#org.scijava:pom-scijava:}" &&
	test "$version" != "$gav" ||
	die "Parent is not pom-scijava: $gav"

	latest="$(sh "$maven_helper" latest-version org.scijava:pom-scijava)" &&
	test -n "$latest" ||
	die "Could not determine latest pom-scijava version"

	test $version != $latest || {
		echo "Parent is already the newest pom-scijava version: $version" >&2
		exit 0
	}

	sed "/<parent>/,/<\/parent>/s/\(<version>\)$version\(<\/version>\)/\1$latest\2/" \
		pom.xml > pom.xml.new &&
	mv -f pom.xml.new pom.xml ||
	die "Could not edit pom.xml"

	commit_and_push "Bump to pom-scijava $latest" pom.xml

	exit
}

test "a--default-properties" != "a$*" ||
set imagej1.version --latest \
	imagej.version --latest \
	imagej-launcher.version --latest \
	imagej-maven-plugin.version --latest \
	imglib2.version --latest \
	nar.version --latest \
	ome-formats.version --latest \
	scifio.version --latest \
	scifio-bf-compat.version --latest \
	scifio-lifesci.version --latest \
	scijava-common.version --latest

test $# -ge 2 &&
test 0 = $(($#%2)) ||
die "Usage: $0 [--skip-commit] (--bump-parent | --default-properties | <key> <value>...)"

pom=pom-scijava/pom.xml
cd "$(dirname "$0")/.." &&
test -f $pom ||
die "Could not switch to scijava-common's root directory"

require_clean_worktree

sed_quote () {
        echo "$1" | sed "s/[]\/\"\'\\\\(){}[\!\$  ;]/\\\\&/g"
}

gav="$(sh bin/maven-helper.sh gav-from-pom $pom)"
old_version=${gav##*:}
new_version=${old_version%.*}.$((1 + ${old_version##*.}))

message="$(printf "%s\n" "The following changes were made:")"
while test $# -ge 2
do
	must_change=t
	latest_message=
	property="$1"
	value="$2"
	if test "a--latest" = "a$value"
	then
		must_change=
		case "$property" in
		imagej1.version)
			ga=net.imagej:ij
			;;
		scijava-common.version)
			ga=org.scijava:scijava-common
			;;
		imagej.version)
			ga=net.imagej:ij-core
			;;
		imagej-launcher.version)
			ga=net.imagej:ij-launcher
			;;
		imagej-maven-plugin.version)
			ga=net.imagej:imagej-maven-plugin
			;;
		imglib2.version)
			ga=net.imglib2:imglib2
			;;
		nar.version)
			ga=org.apache.maven.plugins:nar-maven-plugin
			;;
		ome-formats.version)
			ga=io.scif:ome-formats
			;;
		scifio.version)
			ga=io.scif:scifio
			;;
		scifio-bf-compat.version)
			ga=io.scif:scifio-bf-compat
			;;
		scifio-lifesci.version)
			ga=io.scif:scifio-lifesci
			;;
		*)
			die "Unknown GAV for $1"
			;;
		esac
		latest_message=" (latest $ga)"
		value="$(sh "$maven_helper" latest-version "$ga")"
	fi

	p="$(sed_quote "$property")"
	v="$(sed_quote "$value")"
	sed \
	 -e "/<properties>/,/<\/properties>/s/\(<$p>\)[^<]*\(<\/$p>\)/\1$v\2/" \
	  $pom > $pom.new &&
	if ! cmp $pom $pom.new
	then
		message="$(printf '%s\n\t%s = %s%s' \
			"$message" "$property" "$value" "$latest_message")"
	elif test -n "$must_change"
	then
		die "Property $property not found in $pom"
	fi &&
	mv $pom.new $pom ||
	die "Failed to set property $property = $value"

	shift
	shift
done

! git diff --quiet $pom || {
	echo "No properties changed!" >&2
	# help detect when no commit is required by --default-properties
	exit 128
}

mv $pom $pom.new &&
sed \
  -e "s/^\(\\t<version>\)$old_version\(<\/version>\)/\1$new_version\2/" \
  $pom.new > $pom &&
! cmp $pom $pom.new ||
die "Failed to increase version of $pom"

rm $pom.new ||
die "Failed to remove intermediate $pom.new"

commit_and_push "Increase pom-scijava version to $new_version" \
	-m "$message" $pom

test -n "$skip_commit" ||
(cd pom-scijava &&
 mvn -DupdateReleaseInfo=true deploy)
