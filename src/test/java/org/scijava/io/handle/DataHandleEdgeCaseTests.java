/*-
 * #%L
 * SciJava Common shared library for SciJava software.
 * %%
 * Copyright (C) 2009 - 2024 SciJava developers.
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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.util.Arrays;

import org.junit.Test;
import org.scijava.Context;
import org.scijava.io.location.BytesLocation;
import org.scijava.io.location.Location;

/**
 * Additional Tests for edge case behavior of {@link DataHandle}.
 * 
 * @author Gabriel Einsdorf
 */
public class DataHandleEdgeCaseTests {

	private static final byte[] BYTES = { //
		'H', 'e', 'l', 'l', 'o', ',', ' ', 'w', 'o', 'r', 'l', 'd', '\n', //
		9, 8, 7, 6, 5, 4, 3, 2, 1, 0, 0, -128, 0, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, //
		125, 127, -127, -125, -3, 'h', 'e' };

	/**
	 * Test to ensure {@link DataHandle#findString(String...)} and
	 * {@link DataHandle#readCString()} work with {@link DataHandle}
	 * implementations that have unknown length.
	 * 
	 * @throws IOException
	 */
	@Test
	public void testFindStringsOnUnknonwLengthHandle() throws IOException {
		final Context ctx = new Context();
		final DataHandleService dhs = ctx.getService(DataHandleService.class);
		final DummyHandle dummy = new DummyHandle(dhs.create(new BytesLocation(
			BYTES)));

		assertEquals("Hello,", dummy.findString(","));
		assertEquals(" world\n", dummy.findString("\n"));
		
		dummy.seek(41);
		assertEquals("he", dummy.findString("\n"));

		dummy.seek(16);
		assertArrayEquals(Arrays.copyOfRange(BYTES, 16, 23), dummy.readCString()
			.getBytes());
		dummy.seek(42);
		assertNull(dummy.readCString());
	}

	private class DummyHandle extends AbstractHigherOrderHandle<Location> {

		public DummyHandle(final DataHandle<Location> handle) {
			super(handle);
		}

		@Override
		public long length() throws IOException {
			return -1;
		}

		@Override
		public long offset() throws IOException {
			return handle().offset();
		}

		@Override
		public void seek(final long pos) throws IOException {
			handle().seek(pos);
		}

		@Override
		public void setLength(final long length) throws IOException {
			handle().setLength(length);
		}

		@Override
		public int read(final byte[] b, final int off, final int len)
			throws IOException
		{
			return handle().read(b, off, len);
		}

		@Override
		public byte readByte() throws IOException {
			return handle().readByte();
		}

		@Override
		public void write(final int b) throws IOException {
			handle().write(b);
		}

		@Override
		public void write(final byte[] b, final int off, final int len)
			throws IOException
		{
			handle().write(b, off, len);
		}

		@Override
		protected void cleanup() throws IOException {
			//
		}
	}

}
