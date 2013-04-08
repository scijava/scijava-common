#!/bin/sh

die () {
	echo "$*" >&2
	exit 1
}

test $# = 1 ||
die "Usage: $0 <release-version>"

test refs/heads/master = "$(git rev-parse --symbolic-full-name HEAD)" ||
die "Not on 'master' branch"

git fetch origin master &&
test "$(git rev-parse HEAD)" = "$(git rev-parse FETCH_HEAD)" ||
die "'master' is not up-to-date"

# Prepare new release without pushing (requires the release plugin >= 2.1)
mvn --batch-mode release:prepare -DpushChanges=false -Dresume=false \
        -DreleaseVersion="$1" &&

# Squash the two commits on the current branch into one
git reset --soft HEAD^^ &&
git commit -s -m "Bump to next development cycle" &&

# push the current branch and the tag
git push origin HEAD &&
git push origin $(sed -n 's/^scm.tag=//p' < release.properties) ||
exit
