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
	test f = "$skip_commit" || {
		remote="(none)" &&
		upstream="$(git rev-parse --symbolic-full-name HEAD@{u})" &&
		remote="${upstream#refs/remotes/}" &&
		remote="${remote%%/*}" &&
		git commit -s -m "$@" &&
		git push "$remote" HEAD ||
		die "Could not commit and push to $remote"
	}
}

test -z "$bump_parent" || {
	require_clean_worktree

	test -f pom.xml ||
	die "Not found: pom.xml"

	helper="$(cd "$(dirname "$0")" && pwd)/maven-helper.sh" &&
	test -f "$helper" ||
	die "Could not find maven-helper.sh"

	gav="$(sh "$helper" gav-from-pom pom.xml)" ||
	die "Could not extract GAV from pom.xml"

	case "$gav" in
	*-SNAPSHOT)
		;;
	*)
		die "Not a -SNAPSHOT version: $gav"
		;;
	esac

	gav="$(sh "$helper" parent-gav-from-pom pom.xml)" &&
	version="${gav#org.scijava:pom-scijava:}" &&
	test "$version" != "$gav" ||
	die "Parent is not pom-scijava: $gav"

	latest="$(sh "$helper" latest-version org.scijava:pom-scijava)" &&
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

test $# = 2 ||
die "Usage: $0 [--skip-commit] (--parent | <key> <value>)"

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

property="$(sed_quote "$1")"
value="$(sed_quote "$2")"
sed \
  -e "/<properties>/,/<\/properties>/s/\(<$property>\)[^<]*\(<\/$property>\)/\1$value\2/" \
  $pom > $pom.new ||
die "Failed to set property $1 = $2"

git diff --quiet --no-index $pom $pom.new &&
die "Property $1 not found in $pom"

sed \
  -e "s/^\(\\t<version>\)$old_version\(<\/version>\)/\1$new_version\2/" \
  $pom.new > $pom ||
die "Failed to increase version of $pom"

rm $pom.new ||
die "Failed to remove intermediate $pom.new"

commit_and_push "Increase pom-scijava version to $new_version" \
	-m "This changed the property '$1' to '$2'." $pom

test -n "$skip_commit" ||
mvn deploy
