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

package org.scijava.io.location;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Tests {@link BytesLocation}.
 * 
 * @author Curtis Rueden
 */
public class BytesLocationTest {

	/** Tests {@link BytesLocation#BytesLocation(byte[])}. */
	@Test
	public void testBytes() {
		final byte[] digits = { 3, 1, 4, 1, 5, 9, 2, 6, 5, 3, 5, 8, 9, 7, 9 };
		final BytesLocation loc = new BytesLocation(digits);

		final byte[] testDigits = new byte[digits.length];
		loc.getByteBank().getBytes(0, testDigits);
		assertEquals(digits.length, loc.getByteBank().size());
		assertArrayEquals(digits, testDigits);
	}

	/** Tests {@link BytesLocation#BytesLocation(byte[], int, int)}. */
	@Test
	public void testBytesOffsetLength() {
		final byte[] digits = { 3, 1, 4, 1, 5, 9, 2, 6, 5, 3, 5, 8, 9, 7, 9 };
		final int offset = 3, length = 5;
		final BytesLocation loc = new BytesLocation(digits, offset, length);

		final byte[] testDigits = new byte[digits.length];
		loc.getByteBank().getBytes(0, testDigits);
		assertEquals(length, loc.getByteBank().size());

		final byte[] expectedDigits = new byte[digits.length];
		System.arraycopy(digits, offset, expectedDigits, 0, length);
		assertArrayEquals(expectedDigits, testDigits);
	}

}
