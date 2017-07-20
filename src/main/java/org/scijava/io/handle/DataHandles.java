/*
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

package org.scijava.io.handle;

import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Utility methods for working with {@link DataHandle}s.
 * 
 * @author Curtis Rueden
 */
public final class DataHandles {

	private static Method utfMethod;

	private DataHandles() {
		// Prevent instantiation of utility class.
	}

	/**
	 * Writes a string to the specified DataOutput using modified UTF-8 encoding
	 * in a machine-independent manner.
	 * <p>
	 * First, two bytes are written to out as if by the {@code writeShort} method
	 * giving the number of bytes to follow. This value is the number of bytes
	 * actually written out, not the length of the string. Following the length,
	 * each character of the string is output, in sequence, using the modified
	 * UTF-8 encoding for the character. If no exception is thrown, the counter
	 * {@code written} is incremented by the total number of bytes written to the
	 * output stream. This will be at least two plus the length of {@code str},
	 * and at most two plus thrice the length of {@code str}.
	 * </p>
	 *
	 * @param str a string to be written.
	 * @param out destination to write to
	 * @return The number of bytes written out.
	 * @throws IOException if an I/O error occurs.
	 */
	public static int writeUTF(final String str, final DataOutput out)
		throws IOException
	{
		// HACK: Strangely, DataOutputStream.writeUTF(String, DataOutput)
		// has package-private access. We work around it via reflection.
		try {
			return (Integer) utfMethod().invoke(null, str, out);
		}
		catch (final IllegalAccessException | IllegalArgumentException
				| InvocationTargetException exc)
		{
			throw new IllegalStateException(
				"Cannot invoke DataOutputStream.writeUTF(String, DataOutput)", exc);
		}
	}

	// -- Helper methods --

	/** Gets the {@link #utfMethod} field, initializing if needed. */
	private static Method utfMethod() {
		if (utfMethod == null) initUTFMethod();
		return utfMethod;
	}

	/** Initializes the {@link #utfMethod} field. */
	private static synchronized void initUTFMethod() {
		if (utfMethod != null) return;
		try {
			final Method m = DataOutputStream.class.getDeclaredMethod("writeUTF",
				String.class, DataOutput.class);
			m.setAccessible(true);
			utfMethod = m;
		}
		catch (final NoSuchMethodException | SecurityException exc) {
			throw new IllegalStateException(
				"No usable DataOutputStream.writeUTF(String, DataOutput)", exc);
		}
	}
}
