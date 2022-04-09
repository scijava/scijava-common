/*
 * #%L
 * SciJava Common shared library for SciJava software.
 * %%
 * Copyright (C) 2009 - 2022 SciJava developers.
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

// File path shortening code adapted from:
// from: http://www.rgagnon.com/javadetails/java-0661.html

package org.scijava.util;

import java.util.List;

/**
 * Useful methods for working with {@link List}s.
 * 
 * @author Curtis Rueden
 */
public final class ListUtils {

	private ListUtils() {
		// prevent instantiation of utility class
	}

	/** Gets the first element of the given list, or null if none. */
	public static <T> T first(final List<T> list) {
		if (list == null || list.size() == 0) return null;
		return list.get(0);
	}

	/**
	 * Converts the given list to a string.
	 * <p>
	 * The list elements will be separated by a comma then a space. The list will
	 * be enclosed in square brackets.
	 * </p>
	 * 
	 * @param list The list to stringify.
	 * @see #string(List, String, String, String, boolean)
	 */
	public static String string(final List<?> list) {
		return string(list, true);
	}

	/**
	 * Converts the given list to a string.
	 * <p>
	 * The list elements will be separated by a comma then a space. The list will
	 * be enclosed in square brackets unless it is a singleton with the
	 * {@code encloseSingletons} flag set to false.
	 * </p>
	 * 
	 * @param list The list to stringify.
	 * @param encloseSingletons Whether to enclose singleton lists in brackets.
	 * @return The stringified list.
	 * @see #string(List, String, String, String, boolean)
	 */
	public static String string(final List<?> list,
		final boolean encloseSingletons)
	{
		return string(list, "[", "]", ", ", encloseSingletons);
	}

	/**
	 * Converts the given list to a string.
	 * <p>
	 * The list elements will be comma-separated. It will be enclosed in square
	 * brackets unless the list is a singleton with the {@code encloseSingletons}
	 * flag set to false.
	 * </p>
	 * 
	 * @param list The list to stringify.
	 * @param lDelimiter The left-hand symbol(s) in which to enclose the list.
	 * @param rDelimiter The right-hand symbol(s) in which to enclose the list.
	 * @param separator The symbol(s) to place in between each element.
	 * @param encloseSingletons Whether to enclose singleton lists inside the
	 *          delimiter symbols.
	 * @return The stringified list.
	 */
	public static String string(final List<?> list, //
		final String lDelimiter, final String rDelimiter, //
		final String separator, final boolean encloseSingletons)
	{
		final boolean delimit = encloseSingletons || list.size() != 1;
		final StringBuilder sb = new StringBuilder();
		if (delimit) sb.append(lDelimiter);
		boolean first = true;
		for (final Object e : list) {
			if (first) first = false;
			else sb.append(separator);
			sb.append(e);
		}
		if (delimit) sb.append(rDelimiter);
		return sb.toString();
	}
}
