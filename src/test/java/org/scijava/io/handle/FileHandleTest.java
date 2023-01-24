/*
 * #%L
 * SciJava Common shared library for SciJava software.
 * %%
 * Copyright (C) 2009 - 2023 SciJava developers.
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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;

import org.junit.Test;
import org.scijava.Context;
import org.scijava.io.location.FileLocation;
import org.scijava.io.location.Location;

/**
 * Tests {@link FileHandle}.
 *
 * @author Curtis Rueden
 */
public class FileHandleTest extends DataHandleTest {

	@Override
	public Class<? extends DataHandle<?>> getExpectedHandleType() {
		return FileHandle.class;
	}

	@Override
	public Location createLocation() throws IOException {
		// create and populate a temp file
		final File tmpFile = File.createTempFile("FileHandleTest", "test-file");
		tmpFile.deleteOnExit();
		populateData(new FileOutputStream(tmpFile));
		return new FileLocation(tmpFile);
	}

	@Test
	public void testExists() throws IOException {
		final Context ctx = new Context();
		final DataHandleService dhs = ctx.service(DataHandleService.class);

		final File nonExistentFile = //
			File.createTempFile("FileHandleTest", "nonexistent-file");
		assertTrue(nonExistentFile.delete());
		assertFalse(nonExistentFile.exists());

		final FileLocation loc = new FileLocation(nonExistentFile);
		try (final DataHandle<?> handle = dhs.create(loc)) {
			assertTrue(handle instanceof FileHandle);
			assertFalse(handle.exists());
			assertEquals(-1, handle.length());

			handle.writeBoolean(true);
			assertTrue(handle.exists());
			assertEquals(1, handle.length());			
		}

		// Clean up.
		assertTrue(nonExistentFile.delete());
	}

	@Test
	public void testNotCreatedByClose() throws IOException {
		final Context ctx = new Context();
		final DataHandleService dhs = ctx.service(DataHandleService.class);

		final File nonExistentFile = //
			File.createTempFile("FileHandleTest", "nonexistent-file");
		assertTrue(nonExistentFile.delete());
		assertFalse(nonExistentFile.exists());

		final FileLocation loc = new FileLocation(nonExistentFile);
		try (final DataHandle<?> handle = dhs.create(loc)) {
			assertTrue(handle instanceof FileHandle);
			assertFalse(handle.exists());
		}
		assertFalse(nonExistentFile.exists());
	}

	@Test
	public void testNotCreatedByRead() throws IOException {
		final Context ctx = new Context();
		final DataHandleService dhs = ctx.service(DataHandleService.class);

		final File nonExistentFile = //
			File.createTempFile("FileHandleTest", "none xistent file");
		final URI uri = nonExistentFile.toURI();
		assertTrue(nonExistentFile.delete());
		assertFalse(nonExistentFile.exists());

		final FileLocation loc = new FileLocation(uri);
		try (final DataHandle<?> handle = dhs.create(loc)) {
			assertFalse(handle.exists());
			handle.read(); // this will fail as there is no underlying file!
			fail("Read successfully from non-existing file!");
		}
		catch (final IOException exc) {
			// should be thrown
		}
		assertFalse(nonExistentFile.exists());
		// reading from the non-existing file should not create it!
	}
}
