/*-
 * #%L
 * SciJava Common shared library for SciJava software.
 * %%
 * Copyright (C) 2009 - 2017 Board of Regents of the University of
 * Wisconsin-Madison, Broad Institute of MIT and Harvard, Max Planck
 * Institute of Molecular Cell Biology and Genetics, University of
 * Konstanz, and KNIME GmbH.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

package org.scijava.util;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Tests for the utility methods in {@link StringUtils}
 *
 * @author Richard Domander (Royal Veterinary College, London)
 */
public class StringUtilsTest {

	/** Tests {@link StringUtils#splitUnquoted}. */
	@Test
	public void testSplitUnquoted() {
		// See https://stackoverflow.com/a/1757107/1919049
		final String line = "foo,bar,c;qual=\"baz,blurb\",d;junk=\"quux,syzygy\"";
		final String[] expected = {
			"foo",
			"bar",
			"c;qual=\"baz,blurb\"",
			"d;junk=\"quux,syzygy\""
		};
		final String[] actual = StringUtils.splitUnquoted(line, ",");
		assertArrayEquals(expected, actual);
	}

	@Test
	public void isNullOrEmptyFalseIfString() throws Exception {
		assertFalse(StringUtils.isNullOrEmpty("Fresh out of Red Leicester"));
	}

	@Test
	public void isNullOrEmpty() throws Exception {
		assertTrue(StringUtils.isNullOrEmpty(null));
		assertTrue(StringUtils.isNullOrEmpty(""));
	}

	@Test
	public void padEndNullStringReturnsNull() throws Exception {
		assertNull(StringUtils.padEnd(null, 5, '*'));
	}

	@Test
	public void padEndEmptyString() throws Exception {
		final int length = 5;
		final char padChar = '*';

		final String padded = StringUtils.padEnd("", length, padChar);
		assertNotNull(padded);
		assertFalse(padded.isEmpty());
		assertEquals(length, padded.length());
		assertTrue(padded.chars().allMatch(c -> c == padChar));
	}

	@Test
	public void padEndLengthSmaller() throws Exception {
		final String s = "Eric the fruit bat";

		final String padded = StringUtils.padEnd(s, 3, '*');

		assertEquals(s, padded);
	}

	@Test
	public void padEndLengthEqual() throws Exception {
		final String s = "Eric the cat";

		final String padded = StringUtils.padEnd(s, s.length(), '*');

		assertEquals(s, padded);
	}

	@Test
	public void padEndLengthNegative() throws Exception {
		final String s = "Eric the dog";

		final String padded = StringUtils.padEnd(s, -1, '~');

		assertEquals(s, padded);
	}

	@Test
	public void padEnd() throws Exception {
		final String s = "Eric the halibut";
		final int newLength = s.length() + 5;

		final String padded = StringUtils.padEnd(s, newLength);

		assertEquals(newLength, padded.length());
		final String padding = padded.substring(padded.length() - 5);
		assertTrue(padding.chars().allMatch(c -> c == StringUtils.DEFAULT_PAD_CHAR));
		assertEquals(s, padded.substring(0, padded.length() - 5));
	}

	@Test
	public void padStartNullStringReturnsNull() throws Exception {
		assertNull(StringUtils.padStart(null, 5, '*'));
	}

	@Test
	public void padStartEmptyString() throws Exception {
		final int length = 5;
		final char padChar = '*';

		final String padded = StringUtils.padStart("", length, padChar);
		assertNotNull(padded);
		assertFalse(padded.isEmpty());
		assertEquals(length, padded.length());
		assertTrue(padded.chars().allMatch(c -> c == padChar));
	}

	@Test
	public void padStartLengthSmaller() throws Exception {
		final String s = "Eric the dog";

		final String padded = StringUtils.padStart(s, 3, '*');

		assertEquals(s, padded);
	}

	@Test
	public void padStartLengthEqual() throws Exception {
		final String s = "Simon the prawn";

		final String padded = StringUtils.padStart(s, s.length(), '*');

		assertEquals(s, padded);
	}

	@Test
	public void padStartLengthNegative() throws Exception {
		final String s = "Norman the pike";

		final String padded = StringUtils.padStart(s, -1, '~');

		assertEquals(s, padded);
	}

	@Test
	public void padStart() throws Exception {
		final String s = "Norman the pike";
		final int newLength = s.length() + 5;

		final String padded = StringUtils.padStart(s, newLength);

		assertEquals(newLength, padded.length());
		final String padding = padded.substring(0, 5);
		assertTrue(padding.chars().allMatch(c -> c == StringUtils.DEFAULT_PAD_CHAR));
		assertEquals(s, padded.substring(5, padded.length()));
	}
}
