#!/usr/bin/perl

#
# verify-code-snippets.pl
#

# Script to check that code snippets on a given web page match the linked code.
# For the script to do anything, the page must declare a link to a source file
# on GitHub, of the form

# Usage: verify-code-snippets.pl http://domain.name/path/to/page-to-check ...

use HTML::Entities;

use strict;

# constants
my $urlChar = '[\w\-\@?^=%&/~\+#\.]';
my $codeLink = "https://github.com/$urlChar*/blob/$urlChar*";
my $lineRange = 10;

# parse environment variables
my $debug = $ENV{'DEBUG'};

# parse command line arguments
my @urls = @ARGV;

# process all URLs given
my $returnValue = 0;
for my $url (@urls) {
	my $result = processURL($url);
	if ($result) {
		$returnValue = $result;
	}
}
exit $returnValue;

# Processes the given URL for code snippets, checking any it finds.
sub processURL($) {
	my ($pageURL) = @_;

	debug("Downloading $pageURL\n");
	my @page = `curl -s "$pageURL"`;

	# decode encoded HTML characters (e.g., '&amp;' -> '&')
	for (my $pageIndex = 0; $pageIndex < @page; $pageIndex++) {
		# HACK: decode '&nbsp;' -> ' ' (not 0xa0)
		$page[$pageIndex] =~ s/&nbsp;/ /g;

		$page[$pageIndex] = decode_entities($page[$pageIndex]);
	}

	# scan document for code links
	my $failCount = 0;
	for (my $pageIndex = 0; $pageIndex < @page; $pageIndex++) {
		my $pageLine = $page[$pageIndex];
		if ($pageLine =~ /$codeLink/) {
			my $codeURL = makeRaw($&);
			debug("Downloading $codeURL");
			my @code = `curl -s $codeURL`;

			# HACK: strip off leading copyright header comment
			while ($code[0] =~ /^[\/ ]\*/ || $code[0] =~ /^$/) { shift @code; }

			# search for matching first line of code within a few lines
			my $codeOffset = findMatch(\@page, $code[0], $pageIndex);

			if ($codeOffset < 0) {
				debug("NO CODE SNIPPET FOUND\n");
				next;
			}

			my $lineNo = $codeOffset + 1;
			debug("Checking code snippet at line $lineNo");
			my $match = valuesEqual(\@page, $codeOffset, \@code);

			if ($match) {
				debug("CODE SNIPPET MATCHES\n");
			}
			else {
				$failCount++;
			}
		}
	}

	# return number of non-matching code snippets
	if ($failCount > 0) {
		error("Found $failCount non-matching code snippets.");
	}
	return $failCount;
}

# Converts a cooked GitHub link to a raw code link
sub makeRaw($) {
	my ($url) = @_;
	$url =~ s/github\.com/raw.github.com/;
	$url =~ s/blob\///;
	return $url;
}

# Searches the given list for a matching string from the specified index.
sub findMatch($$$) {
	my ($list, $string, $index) = @_;
	for (my $i = $index; $i < $index + $lineRange; $i++) {
		if ($$list[$i] eq $string) {
			return $i;
		}
	}
	return -1;
}

# Checks whether the two arrays match (with a1 offset by the given index).
sub valuesEqual($$$) {
	my ($a1, $offset, $a2) = @_;
	my $index = $offset;
	for my $v2 (@$a2) {
		my $v1 = sterilize($$a1[$index++]);
		if ($v1 ne $v2) {
			my $lineNo = $index - $offset + 1;
			debug("LINE $lineNo DOES NOT MATCH:\n\t$v1\t$v2");
			return 0;
		}
	}
	return 1;
}

# Hacky routine to fix discrepancies in HTML code...
sub sterilize($) {
	my ($value) = @_;
	$value =~ s/<\/pre>$//;
	return $value;
}

# Emits the given message on stderr when the debug flag is set.
sub debug($) {
	$debug || return;
	error(@_);
}

# Emits the given message on stderr.
sub error($) {
	my ($message) = @_;
	print STDERR "$message\n";
}
