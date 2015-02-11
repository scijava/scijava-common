/*
 * #%L
 * SciJava Common shared library for SciJava software.
 * %%
 * Copyright (C) 2009 - 2015 Board of Regents of the University of
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

package org.scijava.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.scijava.test.TestUtils.createTemporaryDirectory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;

import org.junit.Test;

/**
 * Tests {@link ClassUtils}.
 * 
 * @author Johannes Schindelin
 * @author Curtis Rueden
 */
public class ClassUtilsTest {

	@Test
	public void testLoadClass() {
		assertLoaded(boolean.class, "boolean");
		assertLoaded(byte.class, "byte");
		assertLoaded(char.class, "char");
		assertLoaded(double.class, "double");
		assertLoaded(float.class, "float");
		assertLoaded(int.class, "int");
		assertLoaded(long.class, "long");
		assertLoaded(short.class, "short");
		assertLoaded(void.class, "void");
		assertLoaded(String.class, "string");
		assertLoaded(Number.class, "java.lang.Number");
		assertLoaded(boolean[].class, "boolean[]");
		assertLoaded(byte[].class, "byte[]");
		assertLoaded(char[].class, "char[]");
		assertLoaded(double[].class, "double[]");
		assertLoaded(float[].class, "float[]");
		assertLoaded(int[].class, "int[]");
		assertLoaded(long[].class, "long[]");
		assertLoaded(short[].class, "short[]");
		assertLoaded(null, "void[]");
		assertLoaded(String[].class, "string[]");
		assertLoaded(Number[].class, "java.lang.Number[]");
		assertLoaded(boolean[][].class, "boolean[][]");
		assertLoaded(byte[][].class, "byte[][]");
		assertLoaded(char[][].class, "char[][]");
		assertLoaded(double[][].class, "double[][]");
		assertLoaded(float[][].class, "float[][]");
		assertLoaded(int[][].class, "int[][]");
		assertLoaded(long[][].class, "long[][]");
		assertLoaded(short[][].class, "short[][]");
		assertLoaded(null, "void[][]");
		assertLoaded(String[][].class, "string[][]");
		assertLoaded(Number[][].class, "java.lang.Number[][]");
		assertLoaded(boolean[].class, "[Z");
		assertLoaded(byte[].class, "[B");
		assertLoaded(char[].class, "[C");
		assertLoaded(double[].class, "[D");
		assertLoaded(float[].class, "[F");
		assertLoaded(int[].class, "[I");
		assertLoaded(long[].class, "[J");
		assertLoaded(short[].class, "[S");
		assertLoaded(null, "[V");
		assertLoaded(String[].class, "[Lstring;");
		assertLoaded(Number[].class, "[Ljava.lang.Number;");
		assertLoaded(boolean[][].class, "[[Z");
		assertLoaded(byte[][].class, "[[B");
		assertLoaded(char[][].class, "[[C");
		assertLoaded(double[][].class, "[[D");
		assertLoaded(float[][].class, "[[F");
		assertLoaded(int[][].class, "[[I");
		assertLoaded(long[][].class, "[[J");
		assertLoaded(short[][].class, "[[S");
		assertLoaded(null, "[[V");
		assertLoaded(String[][].class, "[[Lstring;");
		assertLoaded(Number[][].class, "[[Ljava.lang.Number;");
	}

	@Test
	public void testGetArrayClass() {
		assertSame(boolean[].class, ClassUtils.getArrayClass(boolean.class));
		assertSame(String[].class, ClassUtils.getArrayClass(String.class));
		assertSame(Number[].class, ClassUtils.getArrayClass(Number.class));
		assertSame(boolean[][].class, ClassUtils.getArrayClass(boolean[].class));
		assertSame(String[][].class, ClassUtils.getArrayClass(String[].class));
		assertSame(Number[][].class, ClassUtils.getArrayClass(Number[].class));
		assertNull(ClassUtils.getArrayClass(void.class));
	}

	@Test
	public void testUnpackedClass() throws IOException {
		final File tmpDir = createTemporaryDirectory("class-utils-test-");
		final String path = getClass().getName().replace('.', '/') + ".class";
		final File classFile = new File(tmpDir, path);
		assertTrue(classFile.getParentFile().exists() ||
			classFile.getParentFile().mkdirs());
		copy(getClass().getResource("/" + path).openStream(),
			new FileOutputStream(classFile), true);

		final ClassLoader classLoader =
			new URLClassLoader(new URL[] { tmpDir.toURI().toURL() }, null);
		final URL location = ClassUtils.getLocation(getClass().getName(),
			classLoader);
		assertEquals(tmpDir, FileUtils.urlToFile(location));
		FileUtils.deleteRecursively(tmpDir);
	}

	@Test
	public void testClassInJar() throws IOException {
		final File tmpDir = createTemporaryDirectory("class-utils-test-");
		final File jar = new File(tmpDir, "test.jar");
		final JarOutputStream out = new JarOutputStream(new FileOutputStream(jar));
		final String path = getClass().getName().replace('.', '/') + ".class";
		out.putNextEntry(new ZipEntry(path));
		copy(getClass().getResource("/" + path).openStream(), out, true);

		final ClassLoader classLoader =
			new URLClassLoader(new URL[] { jar.toURI().toURL() }, null);
		final URL location = ClassUtils.getLocation(getClass().getName(), classLoader);
		assertEquals(jar, FileUtils.urlToFile(location));
		jar.deleteOnExit();
	}

	// -- Helper methods --

	/**
	 * Copies bytes from an {@link InputStream} to an {@link OutputStream}.
	 * 
	 * @param in the source
	 * @param out the sink
	 * @param closeOut whether to close the sink after we're done
	 * @throws IOException
	 */
	private void copy(final InputStream in, final OutputStream out,
		final boolean closeOut) throws IOException
	{
		final byte[] buffer = new byte[16384];
		for (;;) {
			final int count = in.read(buffer);
			if (count < 0) break;
			out.write(buffer, 0, count);
		}
		in.close();
		if (closeOut) out.close();
	}

	private void assertLoaded(final Class<?> c, final String name) {
		assertSame(c, ClassUtils.loadClass(name));
	}

	// -- Helper classes --

	public static class Thing<T> {
		public T thing;
	}

	public static class NumberThing<N extends Number> extends Thing<N> {
		// NB: No implementation needed.
	}

	public static class IntegerThing extends NumberThing<Integer> {
		// NB: No implementation needed.
	}

	public static class ComplexThing<T extends Serializable & Cloneable> extends
		Thing<T>
	{
		// NB: No implementation needed.
	}

}
