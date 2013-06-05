#!/bin/sh

die () {
	echo "$*" >&2
	exit 1
}

IMAGEJ_BASE_REPOSITORY=-DaltDeploymentRepository=imagej.releases::default::dav:http://maven.imagej.net/content/repositories
IMAGEJ_RELEASES_REPOSITORY=$IMAGEJ_BASE_REPOSITORY/releases
IMAGEJ_THIRDPARTY_REPOSITORY=$IMAGEJ_BASE_REPOSITORY/thirdparty

BATCH_MODE=--batch-mode
EXTRA_ARGS=
SKIP_PUSH=
SKIP_DEPLOY=
ALT_REPOSITORY=
while test $# -gt 0
do
	case "$1" in
	--no-batch-mode) BATCH_MODE=;;
	--skip-push) SKIP_PUSH=t;;
	--skip-deploy) SKIP_DEPLOY=t;;
	--extra-arg=*|--extra-args=*)
		EXTRA_ARGS="$EXTRA_ARGS ${1#--*=}";;
	--alt-repository=imagej-releases)
		ALT_REPOSITORY=$IMAGEJ_RELEASES_REPOSITORY;;
	--alt-repository=imagej-thirdparty)
		ALT_REPOSITORY=$IMAGEJ_THIRDPARTY_REPOSITORY;;
	--alt-repository=*|--alt-deployment-repository=*)
		ALT_REPOSITORY="${1#--*=}";;
	--thirdparty=imagej)
		BATCH_MODE=
		SKIP_PUSH=t
		ALT_REPOSITORY=$IMAGEJ_THIRDPARTY_REPOSITORY;;
	--skip-gpg)
		EXTRA_ARGS="$EXTRA_ARGS -Dgpg.skip=true";;
	-*) echo "Unknown option: $1" >&2; break;;
	*) break;;
	esac
	shift
done

test $# = 1 && test "a$1" = "a${1#-}" ||
die "Usage: $0 [--no-batch-mode] [--skip-push] [--alt-repository=<repository>] [--thirdparty=imagej] [--skip-gpg] [--extra-arg=<args>] <release-version>"

REMOTE="${REMOTE:-origin}"

git update-index -q --refresh &&
git diff-files --quiet --ignore-submodules &&
git diff-index --cached --quiet --ignore-submodules HEAD -- ||
die "There are uncommitted changes!"

test refs/heads/master = "$(git rev-parse --symbolic-full-name HEAD)" ||
die "Not on 'master' branch"

HEAD="$(git rev-parse HEAD)" &&
git fetch "$REMOTE" master &&
FETCH_HEAD="$(git rev-parse FETCH_HEAD)" &&
test $FETCH_HEAD = HEAD ||
test $FETCH_HEAD = "$(git merge-base $FETCH_HEAD $HEAD)" ||
die "'master' is not up-to-date"

# Prepare new release without pushing (requires the release plugin >= 2.1)
mvn $BATCH_MODE release:prepare -DpushChanges=false -Dresume=false \
        -DreleaseVersion="$1" "-Darguments=${EXTRA_ARGS# }" &&

# Squash the two commits on the current branch into one
git reset --soft HEAD^^ &&
if ! git diff-index --cached --quiet --ignore-submodules HEAD --
then
	git commit -s -m "Bump to next development cycle"
fi &&

# push the current branch and the tag
tag=$(sed -n 's/^scm.tag=//p' < release.properties) &&
test -n "$tag" &&
if test -z "$SKIP_PUSH"
then
	git push "$REMOTE" HEAD &&
	git push "$REMOTE" $tag
fi ||
exit

if test -z "$SKIP_DEPLOY"
then
	git checkout $tag &&
	mvn clean verify &&
	mvn $ALT_REPOSITORY -DupdateReleaseInfo=true deploy &&
	git checkout @{-1}
fi
