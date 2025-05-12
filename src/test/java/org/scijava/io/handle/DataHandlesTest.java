/*-
 * #%L
 * SciJava Common shared library for SciJava software.
 * %%
 * Copyright (C) 2009 - 2025 SciJava developers.
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

import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.scijava.Context;
import org.scijava.event.EventService;
import org.scijava.io.location.BytesLocation;
import org.scijava.io.location.Location;
import org.scijava.task.DefaultTask;
import org.scijava.task.Task;
import org.scijava.thread.ThreadService;
import org.scijava.util.MersenneTwisterFast;

/**
 * Tests for utility methods in the {@link DataHandles} class.
 *
 * @author Gabriel Einsdorf
 */
public class DataHandlesTest {

	private static final int TEST_SIZE = 2_938_740;
	private DataHandleService handles;
	private Location inFile;
	private BytesLocation outFile;
	private byte[] data;
	private ThreadService threadService;
	private EventService eventService;

	@Before
	public void classSetup() {
		final Context ctx = new Context(DataHandleService.class,
			ThreadService.class, EventService.class);
		handles = ctx.service(DataHandleService.class);
		threadService = ctx.service(ThreadService.class);
		eventService = ctx.service(EventService.class);

		data = randomBytes(0xbabebabe);
		inFile = new BytesLocation(data);
		outFile = new BytesLocation(TEST_SIZE);
	}

	@After
	public void cleanup() {
		handles.context().dispose();
	}

	@Test
	public void testDefaultCopy() throws IOException {
		try (DataHandle<Location> src = handles.create(inFile);
				final DataHandle<Location> dest = handles.create(outFile))
		{
			DataHandles.copy(src, dest);
			assertHandleEquals(data, dest);
		}
	}

	@Test
	public void testCopyTask() throws IOException {
		try (DataHandle<Location> src = handles.create(inFile);
				final DataHandle<Location> dest = handles.create(outFile))
		{
			final Task t = new DefaultTask(threadService, eventService);
			DataHandles.copy(src, dest, t);
			assertEquals(t.getProgressValue(), src.length());
			assertHandleEquals(data, dest);
		}
	}

	@Test
	public void testCopyLength() throws IOException {
		final int sliceSize = 50_000;
		try (DataHandle<Location> src = handles.create(inFile);
				final DataHandle<Location> dest = handles.create(outFile))
		{
			DataHandles.copy(src, dest, sliceSize);
			final byte[] expected = new byte[sliceSize];
			System.arraycopy(data, 0, expected, 0, sliceSize);
			assertHandleEquals(expected, dest);
		}
	}

	@Test
	public void testCopyLengthTask() throws IOException {
		final int sliceSize = 50_000;
		try (DataHandle<Location> src = handles.create(inFile);
				final DataHandle<Location> dest = handles.create(outFile))
		{
			final Task t = new DefaultTask(threadService, eventService);
			DataHandles.copy(src, dest, sliceSize, t);
			assertEquals(t.getProgressValue(), sliceSize);

			final byte[] expected = new byte[sliceSize];
			System.arraycopy(data, 0, expected, 0, sliceSize);
			assertHandleEquals(expected, dest);
		}
	}

	private void assertHandleEquals(final byte[] expected,
		final DataHandle<Location> handle) throws IOException
	{
		handle.seek(0);
		for (int i = 0; i < expected.length; i++) {
			assertEquals(expected[i], handle.readByte());
		}
	}

	private byte[] randomBytes(final long seed) {
		final MersenneTwisterFast r = new MersenneTwisterFast(seed);
		final byte[] ldata = new byte[TEST_SIZE];
		for (int i = 0; i < ldata.length; i++) {
			ldata[i] = r.nextByte();
		}
		return ldata;
	}
}
