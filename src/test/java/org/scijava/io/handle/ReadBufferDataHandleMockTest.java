/*-
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

package org.scijava.io.handle;

import static org.junit.Assert.assertEquals;
import static org.mockito.AdditionalMatchers.aryEq;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.scijava.io.location.DummyLocation;
import org.scijava.io.location.Location;

public class ReadBufferDataHandleMockTest {

	private DataHandle<Location> mock;
	private AbstractDataHandle<Location> buf;
	private byte[] byteArrayLen10;
	private long innerOffset;

	@SuppressWarnings("unchecked")
	@Before
	public void setup() throws IOException {
		innerOffset = 0l;
		mock = mock(DataHandle.class);

		// needed to get around type checking in AbstractWrapperPlugin
		when(mock.get()).thenReturn(new DummyLocation());
		when(mock.getType()).thenReturn(Location.class);

		buf = new ReadBufferDataHandle(mock, 10, 2);
		byteArrayLen10 = new byte[10];

		// update offset on mock read
		when(mock.read(any(byte[].class))).thenAnswer(inv -> {
			innerOffset += inv.<byte[]> getArgument(0).length;
			return null;
		});

		// update offset on mock seek
		doAnswer(inv -> {
			innerOffset = inv.getArgument(0);
			return null;
		}).when(mock).seek(anyLong());

		// mock offset
		when(mock.offset()).then(inv -> {
			return innerOffset;
		});
	}

	@Test
	public void testBufferingSequence() throws IOException {

		// set length of stubbed handle
		when(mock.length()).thenReturn(30l);
		byte[] value = new byte[10];
		when(mock.read(aryEq(value), eq(0), eq(10))).thenReturn(10);
		when(mock.read(aryEq(value), anyInt(), anyInt())).thenReturn(10);

		// read the first byte
		buf.read();
		verify(mock, times(0)).seek(0);
		// buffer should read a whole page
		verify(mock).read(aryEq(byteArrayLen10), eq(0), eq(10));

		buf.seek(0);
		// ensure seek was not called again
		verify(mock, times(0)).seek(0);

		when(mock.offset()).thenReturn(10l);

		// read over the edge of the current page
		buf.read(new byte[12]);
		verify(mock, times(0)).seek(anyLong());
		verify(mock, times(2)).read(aryEq(byteArrayLen10), eq(0), eq(10));

		assertEquals(12, buf.offset());

		// read the last page
		when(mock.offset()).thenReturn(20l);
		buf.read(new byte[12]);
		verify(mock, times(0)).seek(anyLong());
		verify(mock, times(3)).read(aryEq(byteArrayLen10), eq(0), eq(10));

		// first page should no longer be buffered, must be reread in
		buf.seek(0);
		buf.read();
		verify(mock).seek(0);
		verify(mock, times(4)).read(aryEq(byteArrayLen10), eq(0), eq(10));
	}

	/**
	 * Tests that we do not buffer pages that are not needed and
	 *
	 * @throws IOException
	 */
	@Test
	public void testSkipForward() throws IOException {

		// set length of stubbed handle
		when(mock.length()).thenReturn(40l);
		when(mock.read(any(), anyInt(), anyInt())).thenReturn(10);

		// read the first byte
		buf.read();
		verify(mock, times(0)).seek(anyLong());
		verify(mock, times(1)).read(aryEq(byteArrayLen10), eq(0), eq(10));

		// skip the second page
		buf.seek(30l);
		buf.read();

		// read the third page
		verify(mock).seek(30l);
		verify(mock, times(2)).read(aryEq(byteArrayLen10), eq(0), eq(10));
		when(mock.offset()).thenReturn(40l);

		// go back to already buffered page
		buf.seek(0l);
		buf.read();

		verify(mock, times(1)).seek(anyLong());

		// go back to third page
		buf.seek(35);
		buf.read();
		verify(mock, times(1)).seek(anyLong());
		verify(mock, times(2)).read(any(), anyInt(), anyInt());
	}
}
