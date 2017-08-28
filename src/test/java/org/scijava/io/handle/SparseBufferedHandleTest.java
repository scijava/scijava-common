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

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.scijava.Context;
import org.scijava.io.location.BytesLocation;
import org.scijava.io.location.Location;

/**
 * Tests {@link SparseBufferedHandle}
 * @author Gabriel Einsdorf
 */
public class SparseBufferedHandleTest extends DataHandleTest {

	private Context context;
	private DataHandleService dataHandleService;

	@Override
	@Test
	public void testDataHandle() throws IOException {

		Location loc = createLocation();
		try (final DataHandle<Location> handle = //
			dataHandleService.create(loc))
		{
			SparseBufferedHandle bufferedHandle = //
				new SparseBufferedHandle(handle, 100);
			checkReads(bufferedHandle);
		}
	}

	@Test
	public void testSmallBuffer() throws IOException {

		Location loc = createLocation();
		try (final DataHandle<Location> handle = //
			dataHandleService.create(loc))
		{
			// check with small buffersize
			SparseBufferedHandle bufferedHandle = //
				new SparseBufferedHandle(handle, 5);
			checkReads(bufferedHandle);
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
