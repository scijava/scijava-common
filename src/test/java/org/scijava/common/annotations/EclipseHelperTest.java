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

package org.scijava.common.annotations;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.scijava.common.test.TestUtils.createTemporaryDirectory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLClassLoader;

import org.junit.Test;
import org.scijava.common.annotations.EclipseHelper;
import org.scijava.common.annotations.Index;
import org.scijava.common.annotations.Indexable;

/**
 * Verifies that the {@link EclipseHelper} does its job correctly.
 * 
 * @author Johannes Schindelin
 */
public class EclipseHelperTest {

	@Test
	public void testSkipIndexGeneration() throws Exception {
		final File dir = createTemporaryDirectory("eclipse-test-");
		copyClasses(dir, Complex.class, Simple.class);
		final File jsonDir = new File(dir, Index.INDEX_PREFIX);
		assertFalse(jsonDir.exists());
		final URLClassLoader loader =
			new URLClassLoader(new URL[] { dir.toURI().toURL() }, getClass()
				.getClassLoader().getParent());
		EclipseHelper.indexed.clear();
		EclipseHelper.updateAnnotationIndex(loader);
		assertFalse(jsonDir.exists());
	}

	@Test
	public void testIndexing() throws Exception {
		final File dir = createTemporaryDirectory("eclipse-test-");
		copyClasses(dir, Complex.class, Simple.class, Fruit.class,
			AnnotatedA.class, AnnotatedB.class, AnnotatedC.class, AnnotatedD.class);
		final File jsonDir = new File(dir, Index.INDEX_PREFIX);
		for (final Class<?> clazz : new Class<?>[] { Complex.class, Simple.class })
		{
			assertFalse(new File(jsonDir, clazz.getName()).exists());
		}
		final URLClassLoader loader =
			new URLClassLoader(new URL[] { dir.toURI().toURL() }, getClass()
				.getClassLoader().getParent())
			{

				@Override
				public Class<?> loadClass(final String className)
					throws ClassNotFoundException
				{
					if (className.equals(Indexable.class.getName())) {
						return Indexable.class;
					}
					return super.loadClass(className);
				}
			};
		EclipseHelper.indexed.clear();
		EclipseHelper.updateAnnotationIndex(loader);
		for (final Class<?> clazz : new Class<?>[] { Complex.class, Simple.class })
		{
			assertTrue(new File(jsonDir, clazz.getName()).exists());
		}
		assertEquals(2, jsonDir.list().length);

		// delete the .class files and verify that the annotation indexes are
		// deleted
		jsonDir.setLastModified(123456789);
		for (final Class<?> clazz : new Class<?>[] { AnnotatedA.class,
			AnnotatedB.class, AnnotatedC.class, AnnotatedD.class })
		{
			assertTrue(new File(dir, DirectoryIndexerTest.getResourcePath(clazz))
				.delete());
		}
		long now = System.currentTimeMillis();
		EclipseHelper.indexed.clear();
		EclipseHelper.updateAnnotationIndex(loader);
		assertEquals(0, jsonDir.list().length);
		/*
		 * Most file systems provide the mtime at second granularity, not
		 * milli-second granularity. Hence "now" might be as much as 999
		 * milliseconds ahead of the stored mtime.
		 */
		assertTrue(jsonDir.lastModified() >= now - 999);
	}

	private void copyClasses(final File dir, final Class<?>... classes)
		throws IOException
	{
		final byte[] buffer = new byte[16384];
		for (final Class<?> clazz : classes) {
			final String classPath = DirectoryIndexerTest.getResourcePath(clazz);
			final InputStream in =
				getClass().getResource("/" + classPath).openStream();
			final File outFile = new File(dir, classPath);
			final File parent = outFile.getParentFile();
			assertTrue(parent.isDirectory() || parent.mkdirs());
			final OutputStream out = new FileOutputStream(outFile);
			for (;;) {
				int count = in.read(buffer);
				if (count < 0) {
					break;
				}
				out.write(buffer, 0, count);
			}
			in.close();
			out.close();
		}
	}

}
