
package org.scijava.io.handle;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.AdditionalMatchers.aryEq;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;

import org.scijava.io.location.DummyLocation;
import org.scijava.io.location.Location;

import static org.mockito.Mockito.*;

import java.io.IOException;

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

		// read the first byte
		buf.read();
		verify(mock, times(0)).seek(0);
		// buffer should read a whole page
		verify(mock).read(aryEq(byteArrayLen10));

		buf.seek(0);
		// ensure seek was not called again
		verify(mock, times(0)).seek(0);

		// read over the edge of the current page
		buf.read(new byte[12]);
		verify(mock, times(0)).seek(anyLong());
		verify(mock, times(2)).read(aryEq(byteArrayLen10));

		assertEquals(12, buf.offset());

		// read the last page
		buf.read(new byte[12]);
		verify(mock, times(0)).seek(anyLong());
		verify(mock, times(3)).read(aryEq(byteArrayLen10));

		// first page should no longer be buffered, must be reread in
		buf.seek(0);
		buf.read();
		verify(mock).seek(0);
		verify(mock, times(4)).read(aryEq(byteArrayLen10));
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

		// read the first byte
		buf.read();
		verify(mock, times(0)).seek(anyLong());
		verify(mock).read(aryEq(byteArrayLen10));

		// skip the second page
		buf.seek(30l);
		buf.read();

		// read the third page
		verify(mock).seek(30l);
		verify(mock, times(2)).read(aryEq(byteArrayLen10));

		// go back to already buffered page
		buf.seek(0l);
		buf.read();

		verify(mock, times(1)).seek(anyLong());

		// go back to third page
		buf.seek(35);
		buf.read();
		verify(mock, times(1)).seek(anyLong());
		verify(mock, times(2)).read(any());
	}
}
