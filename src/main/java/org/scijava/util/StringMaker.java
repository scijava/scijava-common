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

package org.scijava.util;

import java.util.Collection;
import java.util.Iterator;

/**
 * Helper class for building up {@link String}s of key/value pairs.
 * 
 * @author Curtis Rueden
 */
public final class StringMaker {

	private final StringBuilder builder;

	public StringMaker() {
		builder = new StringBuilder();
	}

	public StringMaker(final String s) {
		builder = new StringBuilder(s);
	}

	// -- Object methods --

	@Override
	public String toString() {
		return builder.toString();
	}

	// -- StringMaker methods --

	public void append(final String s) {
		if (builder.length() > 0) builder.append(", ");
		builder.append(s);
	}

	public void append(final String key, final Object value) {
		append(key, value, null);
	}

	public void append(final String key, final Object value,
		final Object defaultValue)
	{
		if (value == null || value.equals(defaultValue)) return;

		final String s = makeString(value);
		if (s == null) return;

		if (builder.length() > 0) builder.append(", ");
		builder.append(key + "=" + s);
	}

	// -- Helper methods --

	private String makeString(final Object value) {
		if (value == null) return null;
		if (value instanceof String) {
			final String s = (String) value;
			if (s.isEmpty()) return null;
			return "'" + s + "'";
		}
		if (value instanceof Class) {
			final Class<?> c = (Class<?>) value;
			return c.getSimpleName();
		}
		if (value instanceof Collection) {
			final Collection<?> c = (Collection<?>) value;
			if (c.isEmpty()) return null;
			final StringBuilder sb = new StringBuilder();
			final Iterator<?> iter = c.iterator();
			while (iter.hasNext()) {
				if (sb.length() > 0) sb.append(", ");
				final Object o = iter.next();
				sb.append(makeString(o));
			}
			return "{" + sb.toString() + "}";
		}
		return value.toString();
	}

}
