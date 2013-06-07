#!/bin/bash

#
# install-git-hooks.sh
#

# A script to install recommended git hooks into your .git/hooks folder.
# It will append them without overwriting any existing hooks you have.

HOOK_DIR="$(dirname "$0")/hooks"

PRE_COMMIT=.git/hooks/pre-commit

# must be run from toplevel of repository
if [ ! -d .git ];
then
	echo "Must be run from toplevel of a git repository!"
	exit 1
fi

# create pre-commit hook if it does not already exist
if [ ! -e "$PRE_COMMIT" ];
then
	echo "Creating: $PRE_COMMIT"
	echo '#!/bin/sh' >> "$PRE_COMMIT"
	echo >> "$PRE_COMMIT"
	chmod +x "$PRE_COMMIT"
fi

# inject bin/hooks calls into relevant git hook scripts
for f in "$HOOK_DIR/pre-commit-"*;
do
	if ! grep -q "$f" "$PRE_COMMIT"
	then
		echo "Installing: $f"
		echo "$f" >> "$PRE_COMMIT"
	fi
done
