/*
 * #%L
 * SciJava Common shared library for SciJava software.
 * %%
 * Copyright (C) 2009 - 2023 SciJava developers.
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

// Portions of this class were derived from the loci.common.DataTools class of
// the Bio-Formats library, licensed according to Simplified BSD, as follows:
//
// Copyright (C) 2005 - 2015 Open Microscopy Environment:
//   - Board of Regents of the University of Wisconsin-Madison
//   - Glencoe Software, Inc.
//   - University of Dundee
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions are met:
//
// 1. Redistributions of source code must retain the above copyright notice,
//    this list of conditions and the following disclaimer.
// 2. Redistributions in binary form must reproduce the above copyright notice,
//    this list of conditions and the following disclaimer in the documentation
//    and/or other materials provided with the distribution.
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
// AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
// IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
// ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
// LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
// CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
// SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
// INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
// CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
// ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
// POSSIBILITY OF SUCH DAMAGE.

package org.scijava.util;

import java.io.File;
import java.text.DecimalFormatSymbols;
import java.util.regex.Pattern;

/**
 * Useful methods for working with {@link String}s.
 *
 * @author Curtis Rueden
 * @author Chris Allan
 * @author Melissa Linkert
 * @author Richard Domander (Royal Veterinary College, London)
 */
public final class StringUtils {

	public static final char DEFAULT_PAD_CHAR = ' ';

	private StringUtils() {
		// NB: prevent instantiation of utility class.
	}

	/**
	 * Splits a string only at separators outside of quotation marks ({@code "}).
	 * Does not handle escaped quotes.
	 */
	public static String[] splitUnquoted(final String s, final String separator) {
		// See https://stackoverflow.com/a/1757107/1919049
		return s.split(Pattern.quote(separator) +
			"(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
	}

	/** Normalizes the decimal separator for the user's locale. */
	public static String sanitizeDouble(String value) {
		value = value.replaceAll("[^0-9,\\.]", "");
		final char separator = new DecimalFormatSymbols().getDecimalSeparator();
		final char usedSeparator = separator == '.' ? ',' : '.';
		value = value.replace(usedSeparator, separator);
		try {
			Double.parseDouble(value);
		}
		catch (final Exception e) {
			value = value.replace(separator, usedSeparator);
		}
		return value;
	}

	/** Removes null bytes from a string. */
	public static String stripNulls(final String toStrip) {
		final StringBuilder s = new StringBuilder();
		for (int i = 0; i < toStrip.length(); i++) {
			if (toStrip.charAt(i) != 0) {
				s.append(toStrip.charAt(i));
			}
		}
		return s.toString().trim();
	}

	/** Checks if two filenames have the same prefix. */
	public static boolean samePrefix(final String s1, final String s2) {
		if (s1 == null || s2 == null) return false;
		final int n1 = s1.indexOf(".");
		final int n2 = s2.indexOf(".");
		if ((n1 == -1) || (n2 == -1)) return false;

		final int slash1 = s1.lastIndexOf(File.pathSeparator);
		final int slash2 = s2.lastIndexOf(File.pathSeparator);

		final String sub1 = s1.substring(slash1 + 1, n1);
		final String sub2 = s2.substring(slash2 + 1, n2);
		return sub1.equals(sub2) || sub1.startsWith(sub2) || sub2.startsWith(sub1);
	}

	/** Removes unprintable characters from the given string. */
	public static String sanitize(final String s) {
		if (s == null) return null;
		StringBuffer buf = new StringBuffer(s);
		for (int i = 0; i < buf.length(); i++) {
			final char c = buf.charAt(i);
			if (c != '\t' && c != '\n' && (c < ' ' || c > '~')) {
				buf = buf.deleteCharAt(i--);
			}
		}
		return buf.toString();
	}

	public static boolean isNullOrEmpty(final String s) {
		return s == null || s.isEmpty();
	}

	/**
	 * Calls {@link #padEnd(String, int, char)} with the {@link #DEFAULT_PAD_CHAR}
	 */
	public static String padEnd(final String s, final int length) {
		return padEnd(s, length, DEFAULT_PAD_CHAR);
	}

	/**
	 * Adds characters to the end of the {@link String} to make it the given
	 * length
	 *
	 * @param s the original string
	 * @param length the length of the string with padding
	 * @param padChar the character added to the end
	 * @return the end padded {@link String}. Null if s is null, s if no padding
	 *         is not necessary
	 */
	public static String padEnd(final String s, final int length,
		final char padChar)
	{
		if (s == null) {
			return null;
		}

		final StringBuilder builder = new StringBuilder(s);
		final int padding = length - s.length();
		for (int i = 0; i < padding; i++) {
			builder.append(padChar);
		}

		return builder.toString();
	}

	/**
	 * Calls {@link #padStart(String, int, char)} with the
	 * {@link #DEFAULT_PAD_CHAR}
	 */
	public static String padStart(final String s, final int length) {
		return padStart(s, length, DEFAULT_PAD_CHAR);
	}

	/**
	 * Adds characters to the start of the {@link String} to make it the given
	 * length
	 *
	 * @param s the original string
	 * @param length the length of the string with padding
	 * @param padChar the character added to the start
	 * @return the start padded {@link String}. Null if s is null, s if no padding
	 *         is not necessary
	 */
	public static String padStart(final String s, final int length,
		final char padChar)
	{
		if (s == null) {
			return null;
		}

		final StringBuilder builder = new StringBuilder();
		final int padding = length - s.length();
		for (int i = 0; i < padding; i++) {
			builder.append(padChar);
		}

		return builder.append(s).toString();
	}
}
