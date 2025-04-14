/*
 * #%L
 * SciJava Common shared library for SciJava software.
 * %%
 * Copyright (C) 2009 - 2017 Board of Regents of the University of
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

package org.scijava.io.handle;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.GZIPOutputStream;

import org.junit.Ignore;
import org.junit.Test;
import org.scijava.io.location.FileLocation;
import org.scijava.io.location.GZipLocation;
import org.scijava.io.location.Location;

/**
 * Tests {@link GZipHandle}.
 *
 * @author Gabriel Einsdorf
 */
public class GZipHandleTest extends DataHandleTest {

	@Override
	public Class<? extends DataHandle<?>> getExpectedHandleType() {
		return GZipHandle.class;
	}

	@Override
	public Location createLocation() throws IOException {
		// create and populate a temp file
		final File tmpFile = File.createTempFile("FileHandleTest",
			"test-file.gzip");
		tmpFile.deleteOnExit();

		try (GZIPOutputStream out = new GZIPOutputStream(new FileOutputStream(
			tmpFile)))
		{
			populateData(out);
		}

		return new GZipLocation(new FileLocation(tmpFile));
	}

	@Test
	@Override
	public void testWriting() throws IOException {
		// no Op
	}

	@Override
	@Test
	public void testReading() throws IOException {
		try (final DataHandle<? extends Location> handle = createHandle()) {
			checkBasicReadMethods(handle, false);
			checkEndiannessReading(handle);
		}
	}
}
