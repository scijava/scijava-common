/*
 * #%L
 * SciJava Common shared library for SciJava software.
 * %%
 * Copyright (C) 2009 - 2014 Board of Regents of the University of
 * Wisconsin-Madison, Broad Institute of MIT and Harvard, and Max Planck
 * Institute of Molecular Cell Biology and Genetics.
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * Useful methods for debugging programs.
 * 
 * @author Curtis Rueden
 */
public final class DebugUtils {

	private DebugUtils() {
		// prevent instantiation of utility class
	}

	/** Extracts the given exception's corresponding stack trace to a string. */
	public static String getStackTrace(final Throwable t) {
		try {
			final ByteArrayOutputStream out = new ByteArrayOutputStream();
			t.printStackTrace(new PrintStream(out, false, "UTF-8"));
			return new String(out.toByteArray(), "UTF-8");
		}
		catch (final IOException exc) {
			return null;
		}
	}

	/**
	 * This method uses reflection to scan the values of the given class's static
	 * fields, returning the first matching field's name.
	 */
	public static String getFieldName(final Class<?> c, final int value) {
		final Field[] fields = c.getDeclaredFields();
		for (int i = 0; i < fields.length; i++) {
			if (!Modifier.isStatic(fields[i].getModifiers())) continue;
			fields[i].setAccessible(true);
			try {
				if (fields[i].getInt(null) == value) return fields[i].getName();
			}
			catch (final IllegalAccessException exc) {
				// no action needed
			}
			catch (final IllegalArgumentException exc) {
				// no action needed
			}
		}
		return "" + value;
	}

}
