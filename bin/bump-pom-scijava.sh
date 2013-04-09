#!/bin/sh

die () {
	echo "$*" >&2
	exit 1
}

skip_commit=
test a--skip-commit != "a$1" || {
	skip_commit=t
	shift
}

test $# = 2 ||
die "Usage: $0 [--skip-commit] <key> <value>"

pom=pom-scijava/pom.xml
cd "$(dirname "$0")/.." &&
test -f $pom ||
die "Could not switch to scijava-common's root directory"

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

test f = "$skip_commit" || {
	git commit -s -m "Increase pom-scijava version to $new_version" \
		-m "This changed the property '$1' to '$2'." $pom &&
	git push origin HEAD
}
