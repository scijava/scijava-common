#!/bin/sh
curl -fsLO https://raw.githubusercontent.com/scijava/scijava-scripts/master/travis-build.sh
sh travis-build.sh $encrypted_d2fbfc37eea9_key $encrypted_d2fbfc37eea9_iv
