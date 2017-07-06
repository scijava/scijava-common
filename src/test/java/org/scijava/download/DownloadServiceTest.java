/*-
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
package org.scijava.download;

import static org.junit.Assert.assertArrayEquals;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.scijava.Context;
import org.scijava.io.location.FileLocation;
import org.scijava.io.location.Location;
import org.scijava.util.FileUtils;
import org.scijava.util.MersenneTwisterFast;

/**
 * Tests {@link DownloadService}.
 * 
 * @author Curtis Rueden
 */
public class DownloadServiceTest {

	private DownloadService downloadService;

	@Before
	public void setUp() {
		final Context ctx = new Context(DownloadService.class);
		downloadService = ctx.service(DownloadService.class);
	}

	@After
	public void tearDown() {
		downloadService.context().dispose();
	}

	@Test
	public void testDownload() throws IOException, InterruptedException,
		ExecutionException
	{
		final byte[] data = randomBytes(0xbabebabe);

		final String prefix = getClass().getName();
		final File inFile = File.createTempFile(prefix, "testDownloadIn");
		final File outFile = File.createTempFile(prefix, "testDownloadOut");

		try {
			FileUtils.writeFile(inFile, data);

			final Location src = new FileLocation(inFile);
			final Location dest = new FileLocation(outFile);

			final Download download = downloadService.download(src, dest);
			download.task().waitFor();

			final byte[] result = FileUtils.readFile(outFile);
			assertArrayEquals(data, result);
		}
		finally {
			inFile.delete();
			outFile.delete();
		}
	}

	// -- Helper methods --

	private byte[] randomBytes(final long seed) {
		final MersenneTwisterFast r = new MersenneTwisterFast(seed);
		final byte[] data = new byte[2938740];
		for (int i = 0; i < data.length; i++) {
			data[i] = r.nextByte();
		}
		return data;
	}
}
