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
import java.util.function.Supplier;

import org.junit.Ignore;
import org.junit.Test;
import org.scijava.io.handle.DataHandle.ByteOrder;
import org.scijava.io.location.BytesLocation;
import org.scijava.io.location.Location;

public class WriteBufferDataHandleTest extends DataHandleTest {

	@Override
	public Class<? extends DataHandle<?>> getExpectedHandleType() {
		// not needed
		throw new UnsupportedOperationException();
	}

	@Override
	public Location createLocation() throws IOException {
		// not needed
		throw new UnsupportedOperationException();
	}

	@Override
	public DataHandle<? extends Location> createHandle() {
		final DataHandle<Location> handle = //
			dataHandleService.create(new BytesLocation(new byte[42]));
		return dataHandleService.writeBuffer(handle);
	}

	@Test
	@Override
	public void testReading() throws IOException {
		// nothing to do
	}

	@Test
	@Override
	public void checkSkip() throws IOException {
		// nothing to do
	}

	@Test(expected = IOException.class)
	public void ensureNotReadable() throws IOException {
		createHandle().read();
	}

	@Override
	@Test
	public void testWriting() throws IOException {
		final ByteArrayOutputStream os = new ByteArrayOutputStream(42);
		populateData(os);
		final BytesLocation location = new BytesLocation(os.toByteArray());
		final DataHandle<Location> handle = //
			dataHandleService.create(location);
		final DataHandle<Location> writeHandle = dataHandleService.writeBuffer(
			handle);

		checkBasicWrites(handle, writeHandle);
	}

	@Test
	public void testEndiannessWriting() throws IOException {
		final BytesLocation location = new BytesLocation(new byte[42]);
		final Supplier<DataHandle<Location>> readHandleSupplier =
			() -> dataHandleService.create(location);
		final Supplier<DataHandle<Location>> writeHandleSupplier = () -> {
			final DataHandle<Location> h = dataHandleService.create(location);
			return dataHandleService.writeBuffer(h);
		};

		checkWriteEndianes(readHandleSupplier, writeHandleSupplier,
			ByteOrder.LITTLE_ENDIAN);
		checkWriteEndianes(readHandleSupplier, writeHandleSupplier,
			ByteOrder.LITTLE_ENDIAN);
		checkAdvancedStringWriting(readHandleSupplier, writeHandleSupplier);
	}
}
