#!/bin/sh
curl -fsLO https://raw.githubusercontent.com/scijava/scijava-scripts/master/ci-build.sh
# NB: Only the Linux CI node should deploy build artifacts.
NO_DEPLOY=$(test "$(uname)" = Linux || echo 1) sh ci-build.sh
