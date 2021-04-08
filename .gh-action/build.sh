#!/bin/sh
curl -fsLO https://raw.githubusercontent.com/scijava/scijava-scripts/github-actions/github-action-build.sh
sh github-action-build.sh $encrypted_d2fbfc37eea9_key $encrypted_d2fbfc37eea9_iv
