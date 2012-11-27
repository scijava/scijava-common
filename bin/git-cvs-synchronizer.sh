#!/bin/sh

# This script uses rsync and cvs2git for Sourceforge projects, and cvsimport
# for all other CVS projects, to keep a Git mirror of CVS repositories
#
# Usage: git-cvs-synchronizer.sh <CVS-spec> <Git-Push-URL>...
#
# where <CVS-spec> is the the CVS root followed by a colon and the name of the
# CVS module.
#
# Example: git-cvs-synchronizer.sh \
#   :pserver:anonymous@tcljava.cvs.sourceforge.net:/cvsroot/tcljava:tcljava \
#   fiji.sc:/srv/git/tcljava/.git

if test $# -lt 2
then
	echo "Usage: $0 <CVSROOT>:<MODULE> <GIT-PUSH-URL>..." >&2
	exit 1
fi

set -e

CVSROOT="${1%:*}"
CVSMODULE="${1##*:}"
shift

RSYNC_URL="$(echo "$CVSROOT" |
	sed -n 's/^:pserver:anonymous@\([^:]*\.cvs\.\(sourceforge\|sf\)\.net\):\/\(cvsroot\/.*\)/\1::\3/p')"

cvs2git () {
	test -d "$CVS2SVN" || {
		CVS2SVN=../../cvs2svn
		test -d "$CVS2SVN" ||
		git clone git://fiji.sc/cvs2svn "$CVS2SVN"
	}
	"$CVS2SVN"/cvs2git  --blobfile=cvs.blobs --dumpfile=cvs.dump \
	        --username=git-synchronizer cvs-mirror/
	cat cvs.blobs cvs.dump |
	git fast-import
}

if test -n "$RSYNC_URL"
then
	test -d .git || {
		git init
		mkdir -p .git/info
		echo /cvs-mirror/ >> .git/info/exclude
	}
	rsync -va "$RSYNC_URL/$CVSMODULE" cvs-mirror
	rsync -va "$RSYNC_URL/CVSROOT" cvs-mirror
	cvs2git
else
	# TODO: try cvsclone (git://repo.or.cz/cvsclone.git)
	cvs -d "$CVSROOT" co "$CVSMODULE"
	cd "$CVSMODULE"
	case "$CVSROOT" in
	*cvs.dev.java.net*)
                EXTRA_CVSPS_OPTS="-p --no-rlog,--no-cvs-direct"
        ;;
        *cvs.scms.waikato.ac.nz*)
                (cvs rlog $(cat CVS/Repository) |
                 sed "/^The changelog prior to shifting was:$/,/^=\{77\}$/d" \
                 > rlog-patched.out) 2>&1 |
                grep -ve "^rlog" -e "^connect" -e "^cvs r\?log" -e "^creating"
                EXTRA_CVSPS_OPTS="-p --test-log,rlog-patched.out"
        ;;
        *)
                EXTRA_CVSPS_OPTS=
        ;;
        esac
	git cvsimport -i -k $EXTRA_CVSPS_OPTS
fi

refs="$(git for-each-ref --shell --format '%(refname)')"

for remote
do
	eval git push \"$remote\" $refs
done
