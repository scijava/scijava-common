#!/bin/sh

# This script uses Git for Windows' remote-hg to mirror Mercurial repositories.
# It is meant to be run as a Jenkins job.

set -e

GIT_HG="$(cd ../../ && pwd)"/git-hg
test -d "$GIT_HG"/src || {
	mkdir -p "$GIT_HG"
	git clone git://github.com/msysgit/git "$GIT_HG"/src
}
test -x "$GIT_HG"/bin/git || (
	if ! dpkg -l libcurl4-openssl-dev libexpat1-dev gettext mercurial \
		> /dev/null
	then
		echo "Missing packages" >&2
		exit 1
	fi
	cd "$GIT_HG"/src &&
	git checkout devel &&
	make install prefix="$GIT_HG"
)

export PATH="$GIT_HG"/bin:$PATH

HG_URL="$1"
shift

test -d .git || git init
test a"hg::$HG_URL" = a"$(git config remote.origin.url)" ||
git remote add --mirror=fetch origin hg::"$HG_URL"

git fetch origin

for url
do
	git push --all "$url"
	git push --tags "$url"
done
