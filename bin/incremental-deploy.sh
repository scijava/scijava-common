#!/bin/sh

# This script is intended to be run by Jenkins after a full, successful build.
# It employs maven-helper.sh to find out whether a given artifact's source
# code has changed since the latest deployment.

# error out whenever a command fails
set -e

bin_dir="$(cd "$(dirname "$0")" && pwd)"
helper="$bin_dir/maven-helper.sh"

jar_poms=
aggregate_poms=

for pom in $(git ls-files \*pom.xml)
do
	dir=${pom%pom.xml}
	test -n "$dir" || dir=.
	gav="$(sh "$helper" gav-from-pom "$pom")" &&
	case "$gav" in
	net.imagej:ij-launcher:*|\
	sc.fiji:pom-ffmpeg-io:*|\
	sc.fiji:ffmpeg-native:*|\
	sc.fiji:FFMPEG_IO:*)
		# blacklisted; require JNI
		continue;;
	esac &&
	test -n "$gav" &&
	case "$(sh "$helper" packaging-from-pom "$pom")" in
	pom)
		aggregate_poms="$aggregate_poms $pom"
		;;
	jar)
		test -d "$dir/target" || continue
		commit="$(sh "$helper" commit "$gav")" &&
		test -n "$commit" &&
		git diff --quiet "$commit".. -- "$dir" || {
			jar_poms="$jar_poms $pom" &&
			echo "Deploying $dir" &&
			mvn -N -f "$pom" -DupdateReleaseInfo=true deploy
		}
		;;
	esac
done

# Deploy aggregate POMs only if a child has been deployed
for pom in $aggregate_poms
do
	case "$jar_poms" in
	*" ${pom%pom.xml}"*)
		echo "Deploying aggregate $pom" &&
		mvn -N -f "$pom" -DupdateReleaseInfo=true deploy
		;;
	esac
done
