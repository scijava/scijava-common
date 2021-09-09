/*
 * #%L
 * SciJava Common shared library for SciJava software.
 * %%
 * Copyright (C) 2009 - 2021 SciJava developers.
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

package org.scijava.io;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

/**
 * Abstract superclass for testing {@link ByteBank} implementations.
 *
 * @author Gabriel Einsdorf
 */
public abstract class ByteBankTest {

	private static byte[] testBytes = { 0, -1, 2, -3, 4, 120, -128, 127, 32, 42 };
	private ByteBank bank;

	/**
	 * @return the ByteBank implementation to test
	 */
	public abstract ByteBank createByteBank();

	@Before
	public void setup() {
		bank = createByteBank();
	}

	@Test
	public void testSetGetBytesArray() {
		// read in full array
		bank.setBytes(0l, testBytes.clone(), 0, testBytes.length);

		// read out full array
		assertEqualRead(testBytes.length, 0);
		assertEqualRead(testBytes.length - 4, 2);
	}

	@Test
	public void testToByteArray() {
		// read in full array
		bank.setBytes(0l, testBytes.clone(), 0, testBytes.length);

		assertArrayEquals(testBytes, bank.toByteArray());

		byte[] actuals = bank.toByteArray(0, testBytes.length);
		assertArrayEquals(actuals, testBytes);
	}

	@Test
	public void testSetGetBytesPartialArray() {
		// read in the partial array
		bank.setBytes(0l, testBytes, 0, testBytes.length - 4);

		// read out the partial array
		assertEqualRead(testBytes.length - 4, 0);
		assertEqualRead(testBytes.length - 4, 2);
	}

	@Test
	public void testSetGetByte() {
		final int numElements = 200_000;
		for (int i = 0; i < numElements; i++) {
			bank.setByte(i, (byte) i);
		}

		for (int i = 0; i < numElements; i++) {
			assertEquals((byte) i, bank.getByte(i));
		}
	}

	@Test
	public void testClear() {
		bank.setBytes(0, testBytes, 0, testBytes.length);
		assertEquals(testBytes.length, bank.size());

		bank.clear();
		assertEquals(0, bank.size());
	}

	@Test
	public void testAppendBytes() {
		// simple append
		bank.appendBytes(testBytes, testBytes.length);
		assertEqualRead(testBytes.length, 0);

		// append to buffer that already contains data
		bank.clear();
		bank.setByte(0l, (byte) 42);
		bank.setByte(1l, (byte) 43);
		bank.appendBytes(testBytes, testBytes.length);

		final byte[] expected = new byte[testBytes.length + 2];
		expected[0] = 42;
		expected[1] = 43;
		System.arraycopy(testBytes, 0, expected, 2, testBytes.length);

		final byte[] actuals = new byte[expected.length];
		bank.getBytes(0, actuals);

		assertArrayEquals(expected, actuals);
	}

	/**
	 * Asserts that {@link #bank} contains the same bytes as {@link #testBytes},
	 * allows to specify an offset.
	 *
	 * @param length how many bytes (starting from the offset) of
	 *          {@link #testBytes} are tested, this allows to test partial reads.
	 * @param offset the offset position
	 */
	private void assertEqualRead(final int length, final int offset) {
		// read from offset up to the length of the given array
		final byte[] bytes = new byte[length];
		final int read = bank.getBytes(offset, bytes);

		final byte[] expected = new byte[length];
		System.arraycopy(testBytes, offset, expected, 0, read);
		assertArrayEquals(expected, bytes);

		// read from offset to offset
		final byte[] offsetBytes = new byte[testBytes.length];
		final int readOffset = bank.getBytes(offset, offsetBytes, offset, length);

		final byte[] offsetExpected = new byte[testBytes.length];
		System.arraycopy(testBytes, offset, offsetExpected, offset, readOffset);
		assertArrayEquals(expected, bytes);
	}
}
