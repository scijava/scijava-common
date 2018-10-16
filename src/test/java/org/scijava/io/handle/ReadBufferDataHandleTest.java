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

import static org.junit.Assert.assertArrayEquals;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.junit.Ignore;
import org.junit.Test;
import org.scijava.io.location.BytesLocation;
import org.scijava.io.location.Location;

/**
 * Tests {@link ReadBufferDataHandle}
 *
 * @author Gabriel Einsdorf
 */
public class ReadBufferDataHandleTest extends DataHandleTest {

	@Test
	public void testSmallBuffer() throws IOException {

		final Location loc = createLocation();
		try (final DataHandle<Location> handle = //
			dataHandleService.create(loc);
				AbstractDataHandle<Location> bufferedHandle = //
					new ReadBufferDataHandle(handle, 5))
		{
			// check with small buffersize
			checkBasicReadMethods(bufferedHandle, true);
			checkEndiannessReading(bufferedHandle);
		}
	}

	@Test(expected = IOException.class)
	public void ensureNotWritable() throws IOException {
		createHandle().write(1);
	}

	@Override
	public DataHandle<? extends Location> createHandle() {
		Location loc;
		try {
			loc = createLocation();
		}
		catch (final IOException exc) {
			throw new RuntimeException(exc);
		}
		final DataHandle<Location> handle = //
			dataHandleService.create(loc);
		final AbstractDataHandle<Location> bufferedHandle = //
			new ReadBufferDataHandle(handle, 5);
		return bufferedHandle;
	}

	@Test
	public void testLargeRead() throws Exception {

		final int size = 10_00;
		final byte[] bytes = new byte[size];
		final Random r = new Random(42);
		r.nextBytes(bytes);

		final Location loc = new BytesLocation(bytes);
		try (final DataHandle<Location> handle = //
			dataHandleService.create(loc);
				AbstractDataHandle<Location> bufferedHandle = //
					new ReadBufferDataHandle(handle, 12, 3))
		{
			// check with small buffersize
			final byte[] actual = new byte[size];

			// create evenly sized slice ranges
			final int slices = 60;
			final int range = (size + slices - 1) / slices;
			final List<SimpleEntry<Integer, Integer>> ranges = new ArrayList<>();
			for (int i = 0; i < slices; i++) {
				final int start = range * i;
				final int end = range * (i + 1);
				ranges.add(new SimpleEntry<>(start, end));
			}
			Collections.shuffle(ranges, r);

			for (final SimpleEntry<Integer, Integer> e : ranges) {
				bufferedHandle.seek(e.getKey());
				bufferedHandle.read(actual, e.getKey(), e.getValue() - e.getKey());
			}
			assertArrayEquals(bytes, actual);
		}
	}

	@Test
	@Override
	public void testWriting() throws IOException {
		// nothing to do here
	}

	@Override
	public Class<? extends DataHandle<?>> getExpectedHandleType() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Location createLocation() throws IOException {
		try (final ByteArrayOutputStream out = new ByteArrayOutputStream()) {
			populateData(out);
			return new BytesLocation(out.toByteArray());
		}
	}
}
