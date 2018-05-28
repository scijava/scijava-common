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

import org.junit.Before;
import org.junit.Test;
import org.scijava.Context;
import org.scijava.io.location.BytesLocation;
import org.scijava.io.location.Location;

/**
 * Tests {@link ReadBufferDataHandle}
 *
 * @author Gabriel Einsdorf
 */
public class ReadBufferDataHandleTest extends DataHandleTest {

	private Context context;
	private DataHandleService dataHandleService;

	@Override
	@Test
	public void testDataHandle() throws IOException {

		final Location loc = createLocation();
		try (final DataHandle<Location> handle = //
			dataHandleService.create(loc);
				AbstractDataHandle<Location> bufferedHandle = //
					new ReadBufferDataHandle(handle))
		{
			checkReads(bufferedHandle);
		}
	}

	@Test
	public void testSmallBuffer() throws IOException {

		final Location loc = createLocation();
		try (final DataHandle<Location> handle = //
			dataHandleService.create(loc);
				AbstractDataHandle<Location> bufferedHandle = //
					new ReadBufferDataHandle(handle, 5))
		{
			// check with small buffersize
			checkReads(bufferedHandle);
		}
	}

	@Test
	public void testLargeRead() throws Exception {

		final int size = 10_00;
		final byte[] bytes = new byte[size];
		Random r = new Random(42);
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
			int slices = 60;
			int range = (size + slices - 1) / slices;
			List<SimpleEntry<Integer, Integer>> ranges = new ArrayList<>();
			for (int i = 0; i < slices; i++) {
				int start = range * i;
				int end = range * (i + 1);
				ranges.add(new SimpleEntry<>(start, end));
			}
			Collections.shuffle(ranges, r);

			for (SimpleEntry<Integer, Integer> e : ranges) {
				bufferedHandle.seek(e.getKey());
				bufferedHandle.read(actual, e.getKey(), e.getValue() - e.getKey());
			}

			assertArrayEquals(bytes, actual);
		}
	}

	@Before
	public void setup() {
		context = new Context(DataHandleService.class);
		dataHandleService = context.service(DataHandleService.class);
	}

	@Override
	public Class<? extends DataHandle<?>> getExpectedHandleType() {
		return null;
	}

	@Override
	public Location createLocation() throws IOException {
		try (final ByteArrayOutputStream out = new ByteArrayOutputStream()) {
			populateData(out);
			return new BytesLocation(out.toByteArray());
		}
	}
}
