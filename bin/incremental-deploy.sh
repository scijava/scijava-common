#!/bin/sh

# This script is intended to be run by Jenkins after a full, successful build.
# It employs maven-helper.sh to find out whether a given artifact's source
# code has changed since the latest deployment.

# error out whenever a command fails
set -e

bin_dir="$(cd "$(dirname "$0")" && pwd)"
helper="$bin_dir/maven-helper.sh"

for pom in $(git ls-files \*pom.xml)
do
	dir=${pom%pom.xml}
	test -n "$dir" || dir=.
	gav="$("$helper" gav-from-pom "$pom")" &&
	case "$gav" in
	net.imagej:ij-launcher:*|\
	sc.fiji:pom-ffmpeg-io:*|\
	sc.fiji:ffmpeg-native:*|\
	sc.fiji:FFMPEG_IO:*)
		# blacklisted; require JNI
		continue;;
	esac &&
	test -n "$gav" &&
	commit="$("$helper" commit "$gav")" &&
	test -n "$commit" &&
	git diff --quiet "$commit".. -- "$dir" ||
	(cd "$dir" &&
	 echo "Deploying $dir" &&
	 mvn -N deploy)
done
