/*
 * #%L
 * SciJava Common shared library for SciJava software.
 * %%
 * Copyright (C) 2009 - 2016 Board of Regents of the University of
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

package org.scijava.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;

import org.junit.Test;

/**
 * Tests the {@link TestUtils}.
 * 
 * @author Johannes Schindelin
 */
public class TestUtilsTest {

	@Test
	public void testCreateTemporaryDirectory() throws IOException {
		final File tmp1 = TestUtils.createTemporaryDirectory("test-utils-test-");
		assertTrue("Not in target/: " + tmp1.getAbsolutePath(), tmp1
			.getAbsolutePath().replace('\\', '/').contains("/target/"));
		final File tmp2 = TestUtils.createTemporaryDirectory("test-utils-test-");
		assertTrue(!tmp1.getAbsolutePath().equals(tmp2.getAbsolutePath()));

		final File tmp3 =
			TestUtils.createTemporaryDirectory("test-utils-test-", getClass());
		assertTrue("Not in target/: " + tmp3.getAbsolutePath(), tmp3
			.getAbsolutePath().replace('\\', '/').contains("/target/"));
		final File tmp4 =
			TestUtils.createTemporaryDirectory("test-utils-test-", getClass());
		assertTrue(!tmp3.getAbsolutePath().equals(tmp4.getAbsolutePath()));

	}

	@Test
	public void sameDirectoryTwice() throws IOException {
		final FileOutputStream[] out = new FileOutputStream[2];
		for (int i = 0; i < 2; i++) {
			final File tmp = TestUtils.createTemporaryDirectory("same-");
			assertTrue(tmp != null);
			final String[] list = tmp.list();
			assertTrue("Not null: " + Arrays.toString(list), list == null || list.length == 0);
			out[i] = new FileOutputStream(new File(tmp, "hello" + i + ".txt"));
		}
		for (final FileOutputStream stream : out) {
			if (stream != null) stream.close();
		}
	}

	/** Tests {@link TestUtils#createPath(File, String)}. */
	@Test
	public void testCreatePath() throws IOException {
		final File base = TestUtils.createTemporaryDirectory("create-path-");
		final String path = "/my/what/a/nested/directory/structure/you/have/gramma";
		File file = TestUtils.createPath(base, path);
		assertTrue(file.exists());
		assertEquals("gramma", file.getName());
		file = file.getParentFile();
		assertEquals("have", file.getName());
		file = file.getParentFile();
		assertEquals("you", file.getName());
		file = file.getParentFile();
		assertEquals("structure", file.getName());
		file = file.getParentFile();
		assertEquals("directory", file.getName());
		file = file.getParentFile();
		assertEquals("nested", file.getName());
		file = file.getParentFile();
		assertEquals("a", file.getName());
		file = file.getParentFile();
		assertEquals("what", file.getName());
		file = file.getParentFile();
		assertEquals("my", file.getName());
		file = file.getParentFile();
		assertEquals(base, file);
	}

}
