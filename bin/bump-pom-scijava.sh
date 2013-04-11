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
	scijava-common.version --latest
# TODO:
#	imagej.version --latest \
#	imglib2.version --latest \
#	scifio.version --latest \

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

while test $# -ge 2
do
	must_change=t
	if test "a--latest" = "a$2"
	then
		must_change=
		case "$1" in
		imagej1.version)
			ga=net.imagej:ij
			;;
		scijava-common.version)
			ga=org.scijava:scijava-common
			;;
		imagej.version)
			ga=net.imagej:ij-core
			;;
		imglib2.version)
			ga=net.imglib2:imglib2
			;;
		scifio.version)
			ga=loci:scifio
			;;
		*)
			die "Unknown GAV for $1"
			;;
		esac
		set "$1" "$(sh "$maven_helper" latest-version "$ga")"
	fi

	property="$(sed_quote "$1")"
	value="$(sed_quote "$2")"
	sed \
	  -e "/<properties>/,/<\/properties>/s/\(<$property>\)[^<]*\(<\/$property>\)/\1$value\2/" \
	  $pom > $pom.new &&
	if test -n "$must_change" && git diff --quiet --no-index $pom $pom.new
	then
		die "Property $1 not found in $pom"
	fi &&
	mv $pom.new $pom ||
	die "Failed to set property $1 = $2"

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
! git diff --quiet --no-index $pom $pom.new ||
die "Failed to increase version of $pom"

rm $pom.new ||
die "Failed to remove intermediate $pom.new"

commit_and_push "Increase pom-scijava version to $new_version" \
	-m "This changed the property '$1' to '$2'." $pom

test -n "$skip_commit" ||
(cd pom-scijava &&
 mvn -DupdateReleaseInfo=true deploy)
