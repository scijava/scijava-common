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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutionException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.scijava.Context;
import org.scijava.io.ByteBank;
import org.scijava.io.location.BytesLocation;
import org.scijava.io.location.FileLocation;
import org.scijava.io.location.Location;
import org.scijava.test.TestUtils;
import org.scijava.util.ByteArray;
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

	@Test
	public void testDownloadCache() throws IOException, InterruptedException,
		ExecutionException
	{
		// Create some data.
		final byte[] data = randomBytes(0xcafecafe);

		// Create source location.
		final String prefix = getClass().getName();
		final File inFile = File.createTempFile(prefix, "testDownloadCacheIn");
		final Location src = new FileLocation(inFile);

		// Create destination location.
		final BytesLocation dest = new BytesLocation(data.length);

		// Create a disk cache.
		final File cacheDir = TestUtils.createTemporaryDirectory(
			"testDownloadCacheBase", getClass());
		final DiskLocationCache cache = new DiskLocationCache();
		cache.setBaseDirectory(cacheDir);
		cache.setFileLocationCachingEnabled(true);

		try {
			// Write the data to the source location.
			FileUtils.writeFile(inFile, data);

			// Sanity check: the cache should be empty.
			assertNull(cache.loadChecksum(src));
			final Location cachedSource = cache.cachedLocation(src);
			assertTrue(cachedSource instanceof FileLocation);
			final FileLocation cachedFile = (FileLocation) cachedSource;
			assertFalse(cachedFile.getFile().exists());

			// Download + cache the source.
			final Download download = downloadService.download(src, dest, cache);
			download.task().waitFor();

			// Check that the data was read.
			assertBytesEqual(data, dest.getByteBank());

			// Check that the data was cached.
			assertEquals(cachedSource, cache.cachedLocation(src));
			assertTrue(cachedFile.getFile().exists());
			final byte[] cachedData = FileUtils.readFile(cachedFile.getFile());
			assertArrayEquals(data, cachedData);

			// Check that the cache works, even after the source file is deleted.
			inFile.delete();
			assertFalse(inFile.exists());
			final BytesLocation dest2 = new BytesLocation(data.length);
			final Download download2 = downloadService.download(src, dest2, cache);
			download2.task().waitFor();
			assertBytesEqual(data, dest2.getByteBank());
		}
		finally {
			if (inFile.exists()) inFile.delete();
			FileUtils.deleteRecursively(cacheDir);
		}
	}

	@Test
	public void testDownloadZip() throws IOException {
		final byte[] data = readResource("greetings.zip");
		// START HERE
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

	/** Totally awesome and not-at-all-overcomplicated way to open a resource. */
	private byte[] readResource(final String path) throws IOException {
		final ByteArray bytes = new ByteArray();
		try (final InputStream in = getClass().getResourceAsStream(path)) {
			final byte[] b = new byte[16384];
			int len = 0;
			while (true) {
				final int r = in.read(b);
				if (r < 0) break; // EOF
				bytes.setSize(len + r);
				System.arraycopy(b, 0, bytes.getArray(), len, r);
				len += r;
			}
		}
		return bytes.copyArray();
	}

	private void assertBytesEqual(byte[] data, ByteBank byteBank) {
		for (int i=0; i<data.length; i++) {
			assertEquals(data[i], byteBank.getByte(i));
		}
	}
}
