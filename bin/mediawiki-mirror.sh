#!/bin/sh

if test $# != 2
then
	cat >&2 << EOF
Usage: $0 <original-wiki-URL> <mirror-wiki-URL>

This script tries to keep a mirror of a MediaWiki up-to-date. It does so by
inspecting the recent changes and calling a slightly modified MediaWiki API
to allow for exporting more than one revision of a page.

It is incomplete in that it does not mirror the logs, nor accounts. It is also
prone to miss edits that happened while the script ran previously.

Make sure that the script is run in the directory containing the mirror's
LocalSettings.php.
EOF
	exit 1
fi

normalize_mediawiki_url () {
	case "$1" in
	*/.php*)
		echo "${1%.php*}"
		;;
	*/)
		echo "$1"
		;;
	*)
		echo "${1*/*}"/
		;;
	esac
}

ORIGINAL="$(normalize_mediawiki_url "$1")"/api.php?action=
MIRROR="$(normalize_mediawiki_url "$2")"/api.php?action=

latest_update () {
	curl -s "$1query&list=recentchanges&rclimit=1&format=yaml" |
	sed -n -e '/^      timestamp: |-$/{N;s/.*\n *//p}'
}

pages_with_changes_since () {
	curl -s "$1query&list=recentchanges&rcend=$2&rclimit=500&format=dbg" |
	sed -n "/^        'type' => 'edit'/{N;N;s/.*'title' => //p}" |
	sort |
	uniq
}

urlencode () {
	printf "%s" "$*" |
	xxd -plain |
	tr -d '\n' |
	sed 's/\(..\)/%\1/g'
}

get_dump () {
	curl -s "$1query&export=1&exportnowrap=1&format=xml&offset=$2&titles=$3"
}

# make sure we are in the correct directory

test -f maintenance/importDump.php &&
test -f maintenance/dumpUploads.php &&
test -f LocalSettings.php || {
	echo "Not in a MediaWiki directory!" >&2
	exit 1
}

# when was our local latest update

since="$(latest_update $MIRROR)"

# get updates for these pages

pages="$(pages_with_changes_since $ORIGINAL $since |
	sed -e "s/^'//" -e "s/',$//" -e "s/ /%20/g" |
	tr '\n' ' ')"

test -n "$pages" || exit 0

# export from the original Wiki

for page in $pages
do
	echo "Importing $page incrementally..."
	get_dump $ORIGINAL $since $page |
	php maintenance/importDump.php
done

# copy images

IMAGEURL=${ORIGINAL%index.php?action=}

php maintenance/dumpUploads.php |
while read path
do
	# ignore if there were no changes
	case " $pages " in
	*" File:${path##*/} "*)
		;;
	*)
		continue
		;;
	esac

	# ignore if we have an up-to-date copy
	if test -f "$path"
	then
		local_mtime="$(stat -c %Y "$path")"
		remote_mtime="$(date -d "$(curl -s --head "$IMAGEURL$path" |
			sed -n 's/^Last-Modified: //p')" +%s)"
		if test -z "$remote_mtime" ||
			test $local_mtime -ge "$remote_mtime"
		then
			continue
		fi
	fi

	# make fan-out directories if necessary
	case "$path" in
	*/*)
		directory="${path%/*}"
		mkdir -p "$directory"
		;;
	*)
		directory=.
		;;
	esac

	echo "Downloading $path..."
	(cd "$directory" &&
	 curl -s -O "$IMAGEURL$path")
done

# update recent changes

php maintenance/rebuildrecentchanges.php

