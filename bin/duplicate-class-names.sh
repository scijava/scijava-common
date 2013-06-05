#!/bin/sh

#
# duplicate-class-names.sh
#

# Script to print out classes in different packages with the same name.
#
# Such name clashes are to be avoided, since when more than one of them
# is needed in the same source file, it becomes necessary to reference
# all but one using fully qualified package prefixes.

names=$(git ls-files \*.java |
	sed -e 's|.*/||' -e 's|\.java$||' |
	sort |
	uniq -d)
for name in $names
do
	printf '\t%s\n' $name
	git ls-files \*/$name.java
done
