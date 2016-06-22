/*
 * #%L
 * SciJava Common shared library for SciJava software.
 * %%
 * Copyright (C) 2009 - 2016 Board of Regents of the University of
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

package org.scijava.io;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

import org.junit.Test;
import org.scijava.Context;
import org.scijava.util.Bytes;

/**
 * Abstract base class for {@link DataHandle} implementation tests.
 *
 * @author Curtis Rueden
 */
public abstract class DataHandleTest {

	private static final byte[] BYTES = { //
		'H', 'e', 'l', 'l', 'o', ',', ' ', 'w', 'o', 'r', 'l', 'd', '\n', //
			9, 8, 7, 6, 5, 4, 3, 2, 1, 0, 0, -128, 0, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, //
			125, 127, -127, -125, -3, -2, -1 };

	// -- Test methods --

	@Test
	public void testDataHandle() throws IOException {
		final Context context = new Context(DataHandleService.class);
		final DataHandleService dataHandleService =
			context.service(DataHandleService.class);

		final Location loc = createLocation();
		try (final DataHandle<? extends Location> handle = //
			dataHandleService.create(loc))
		{
			assertEquals(getExpectedHandleType(), handle.getClass());

			checkReads(handle);
			checkWrites(handle);
		}
	}

	// -- DataHandleTest methods --

	public abstract Class<? extends DataHandle<?>> getExpectedHandleType();

	public abstract Location createLocation() throws IOException;

	// -- Internal methods --

	protected void populateData(final OutputStream out) throws IOException {
		out.write(BYTES);
		out.close();
	}

	protected <L extends Location> void checkReads(final DataHandle<L> handle)
		throws IOException
	{
		assertEquals(0, handle.offset());
		assertEquals(BYTES.length, handle.length());
		assertEquals("UTF-8", handle.getEncoding());
		assertEquals(ByteOrder.BIG_ENDIAN, handle.getOrder());
		assertEquals(false, handle.isLittleEndian());

		// test read()
		for (int i = 0; i < BYTES.length; i++) {
			assertEquals(msg(i), 0xff & BYTES[i], handle.read());
		}
		assertEquals(-1, handle.read());
		handle.seek(10);
		assertEquals(10, handle.offset());
		assertEquals(BYTES[10], handle.read());

		// test read(byte[])
		final byte[] buf = new byte[10];
		handle.seek(1);
		assertBytesMatch(1, handle.read(buf), buf);

		// test read(ByteBuffer)
		Arrays.fill(buf, (byte) 0);
		final ByteBuffer byteBuffer = ByteBuffer.wrap(buf);
		handle.seek(2);
		assertBytesMatch(2, handle.read(byteBuffer), byteBuffer.array());

		// test readByte()
		handle.seek(0);
		for (int i = 0; i < BYTES.length; i++) {
			assertEquals(msg(i), BYTES[i], handle.readByte());
		}

		// test readShort()
		handle.seek(0);
		for (int i = 0; i < BYTES.length / 2; i += 2) {
			assertEquals(msg(i), Bytes.toShort(BYTES, i, false), handle.readShort());
		}

		// test readInt()
		handle.seek(0);
		for (int i = 0; i < BYTES.length / 4; i += 4) {
			assertEquals(msg(i), Bytes.toInt(BYTES, i, false), handle.readInt());
		}

		// test readLong()
		handle.seek(0);
		for (int i = 0; i < BYTES.length / 8; i += 8) {
			assertEquals(msg(i), Bytes.toLong(BYTES, i, false), handle.readLong());
		}

		// test readFloat()
		handle.seek(0);
		for (int i = 0; i < BYTES.length / 4; i += 4) {
			assertEquals(msg(i), Bytes.toFloat(BYTES, i, false), handle.readFloat(),
				0);
		}

		// test readDouble()
		handle.seek(0);
		for (int i = 0; i < BYTES.length / 8; i += 8) {
			assertEquals(msg(i), Bytes.toDouble(BYTES, i, false),
				handle.readDouble(), 0);
		}

		// test readBoolean()
		handle.seek(0);
		for (int i = 0; i < BYTES.length; i++) {
			assertEquals(msg(i), BYTES[i] == 0 ? false : true, handle.readBoolean());
		}

		// test readChar()
		handle.seek(0);
		for (int i = 0; i < BYTES.length / 2; i += 2) {
			assertEquals(msg(i), (char) Bytes.toInt(BYTES, i, 2, false), handle
				.readChar());
		}

		// test readFully(byte[])
		Arrays.fill(buf, (byte) 0);
		handle.seek(3);
		handle.readFully(buf);
		assertBytesMatch(3, buf.length, buf);

		// test readCString() - _includes_ the null terminator!
		handle.seek(16);
		assertBytesMatch(16, 7, handle.readCString().getBytes());

		// test readLine() - _excludes_ the newline terminator!
		handle.seek(7);
		assertBytesMatch(7, 5, handle.readLine().getBytes());

		// test readString(String) - _includes_ the matching terminator!
		handle.seek(7);
		assertBytesMatch(7, 5, handle.readString("abcdefg").getBytes());

		// test findString(String) - _includes_ the matching terminator!
		handle.seek(1);
		assertBytesMatch(1, 11, handle.findString("world").getBytes());
	}

	protected <L extends Location> void checkWrites(final DataHandle<L> handle)
		throws IOException
	{
		final byte[] copy = BYTES.clone();

		// change the data
		handle.seek(7);
		final String splice = "there";
		for (int i = 0; i < splice.length(); i++) {
			final char c = splice.charAt(i);
			handle.write(c);
			copy[7 + i] = (byte) c;
		}

		// verify the changes
		handle.seek(0);
		for (int i = 0; i < copy.length; i++) {
			assertEquals(msg(i), 0xff & copy[i], handle.read());
		}
	}

	// -- Helper methods --

	private void assertBytesMatch(final int offset, final int length,
		final byte[] b)
	{
		assertEquals(length, b.length);
		for (int i = 0; i < length; i++) {
			assertEquals(msg(i), BYTES[i + offset], b[i]);
		}
	}

	private String msg(final int i) {
		return "[" + i + "]:";
	}

}
