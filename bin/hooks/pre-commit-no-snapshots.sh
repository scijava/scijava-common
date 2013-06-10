#!/bin/sh

# Fail if any new SNAPSHOT references are introduced into pom.xml files.
# In general, SNAPSHOT references result in unstable/nonrepeatable builds.
if git diff --staged -U0 $(git ls-files | grep pom.xml) | grep '^\+.*SNAPSHOT';
then
  echo 'Potential SNAPSHOT references!'
  exit 1
fi
