
package org.scijava.util;

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
