#!/bin/sh

# Script to print version properties for a given pom-scijava release.

# Example: sj-version.sh 1.70

version="$1"

if [ -z "$version" ]
then
  echo "Usage: sj-version.sh version"
fi

repo="http://maven.imagej.net/content/repositories/releases"
url="$repo/org/scijava/pom-scijava/$version/pom-scijava-$version.pom"
curl -s $url | \
	grep '\.version' | \
	sed 's/<\/.*//' | \
	sed 's/^	*<\(.*\)>/\1: /' | \
	sort
