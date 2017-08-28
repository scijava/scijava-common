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

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.junit.Test;
import org.scijava.Context;
import org.scijava.io.location.BytesLocation;
import org.scijava.io.location.Location;

public class WriteBufferHandleTest extends DataHandleTest {

	private Location loc;

	@Override
	public Class<? extends DataHandle<?>> getExpectedHandleType() {
		// not needed
		return null;
	}

	@Override
	@Test
	public void testDataHandle() throws IOException {
		final Context context = new Context(DataHandleService.class);
		final DataHandleService dataHandleService = context.service(
			DataHandleService.class);

		loc = createLocation();
		try (final DataHandle<Location> handle = //
			dataHandleService.create(loc);
				final WriteBufferHandle<Location> buffer = dataHandleService
					.writeBuffer(handle))
		{
			checkWrites(buffer);
		}
	}

	@Override
	public Location createLocation() throws IOException {

		final ByteArrayOutputStream out = new ByteArrayOutputStream();
		populateData(out);
		return new BytesLocation(out.toByteArray());
	}

	@Override
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
		handle.close();

		final Context context = new Context(DataHandleService.class);
		final DataHandleService dataHandleService = context.service(
			DataHandleService.class);

		try (final DataHandle<? extends Location> readHandle = //
			dataHandleService.create(loc))
		{
			readHandle.seek(0);
			for (int i = 0; i < copy.length; i++) {
				assertEquals(msg(i), 0xff & copy[i], readHandle.read());
			}
		}
	}
}
