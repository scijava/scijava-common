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

package org.scijava.annotations;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.junit.Test;

/**
 * Tests the {@link DirectoryIndexer}.
 * 
 * @author Johannes Schindelin
 */
public class DirectoryIndexerTest {

	@Test
	public void testIndexer() throws Exception {
		final String suffix = getResourcePath(AnnotatedA.class);
		final URL url = getClass().getResource("/" + suffix);
		assumeTrue(url != null);
		String path = url.getFile();
		assumeTrue(path.indexOf(':') < 0);
		assertTrue(path.endsWith("/" + suffix));
		final File directory =
			new File(path.substring(0, path.length() - suffix.length()));

		// delete all the test annotations
		final File jsonDirectory = new File(directory, Index.INDEX_PREFIX);
		final File[] list = jsonDirectory.listFiles();
		if (list != null) {
			for (final File file : list) {
				assertTrue(file.delete());
			}
			assertTrue(jsonDirectory.delete());
		}

		// force the directory indexer to run
		new DirectoryIndexer().index(directory);
		assertTrue(directory.exists());

		// read the index
		final Map<String, IndexItem<Complex>> map =
			readIndex(Complex.class, DirectoryIndexerTest.class.getClassLoader());

		testDefaultAnnotations(map);

		// verify that default values are not written to the serialized annotation index
		final File complex = new File(jsonDirectory, Complex.class.getName());
		final BufferedReader reader = new BufferedReader(new FileReader(complex));
		for (;;) {
			final String line = reader.readLine();
			if (line == null) break;
			assertTrue("Contains default value 'Q' for char0: " + line, line.indexOf('Q') < 0);
		}
		reader.close();
	}

	@Test
	public void testRepeatedClassPathElements() throws Exception {
		final String suffix = getResourcePath(AnnotatedA.class);
		final String classURL =
			getClass().getResource("/" + suffix).toString();
		final URL classPathURL = new URL(classURL.substring(0,
			classURL.length() - suffix.length()));
		final ClassLoader loader = new URLClassLoader(new URL[] {
			classPathURL, classPathURL
		});
		final Set<String> seen = new HashSet<>();
		for (final IndexItem<Simple> item :
				Index.load(Simple.class, loader)) {
			final String name = item.className();
			assertFalse(seen.contains(name));
			seen.add(name);
		}
		assertEquals(3, seen.size());
	}

	public static void
		testDefaultAnnotations(Map<String, IndexItem<Complex>> map)
	{
		assertEquals(4, map.size());
		Complex a = map.get(AnnotatedA.class.getName()).annotation();
		assertEquals("Hello, World!", a.simple().string1());

		Complex c = map.get(AnnotatedC.class.getName()).annotation();
		assertEquals(true, c.bool0());
		assertEquals(-17, c.byte0());
		assertEquals(-19, c.short0());
		assertEquals(-23, c.int0());
		assertEquals(-29, c.long0());
		assertTrue(-31.0f == c.float0());
		assertTrue(-37.0 == c.double0());
		assertEquals((char) -41, c.char0());
		assertEquals("Narf!", c.string());
		assertEquals(Exception.class, c.clazz());
		assertEquals(Fruit.Banana, c.fruit());
		assertEquals("Hello", c.simple().string1());
		assertEquals(3, c.array().length);
		assertEquals("one", c.array()[0].string1());
		assertEquals("two", c.array()[1].string1());
		assertEquals("three", c.array()[2].string1());
		assertEquals(4, c.array1().length);
		assertEquals(-43, c.array1()[0]);
		assertEquals(-47, c.array1()[1]);
		assertEquals(-53, c.array1()[2]);
		assertEquals(-59, c.array1()[3]);

		c = map.get(AnnotatedInnerClass.InnerClass.class.getName()).annotation();
		assertNotNull(c);
	}

	public static String getResourcePath(final Class<?> clazz) {
		return clazz.getName().replace('.', '/') + ".class";
	}

	public static <A extends Annotation> Map<String, IndexItem<A>> readIndex(
		final Class<A> annotationClass, final URL... directories)
	{
		final ClassLoader loader = new ClassLoader() {

			@Override
			public final Enumeration<URL> getResources(final String path)
				throws IOException
			{
				final List<URL> urls = new ArrayList<>();
				for (final URL directory : directories) {
					final URL url = new URL(directory, path);
					final URLConnection connection = url.openConnection();
					if (connection.getLastModified() > 0) {
						urls.add(url);
					}
				}
				return Collections.enumeration(urls);
			}
		};
		return readIndex(annotationClass, loader);
	}

	public static <A extends Annotation> Map<String, IndexItem<A>> readIndex(
		final Class<A> annotationClass, final ClassLoader loader)
	{
		// read the index
		final Map<String, IndexItem<A>> map = new TreeMap<>();
		for (final IndexItem<A> item : Index.load(annotationClass, loader)) {
			map.put(item.className(), item);
		}
		return map;
	}
}
