#!/bin/sh

# Fail if any pom.xml files have more than one SNAPSHOT reference.
# One reference to the parent is expected, but that's it!
# Otherwise, builds will not be repeatable.
if git grep -c SNAPSHOT $(git ls-files | grep pom.xml) | grep -v ':1$';
then
  echo 'Potential SNAPSHOT references!'
  exit 1
fi
