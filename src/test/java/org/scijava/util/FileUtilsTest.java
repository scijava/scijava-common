/*
 * #%L
 * SciJava Common shared library for SciJava software.
 * %%
 * Copyright (C) 2009 - 2014 Board of Regents of the University of
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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collection;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import org.junit.Test;

/**
 * Tests {@link FileUtils}.
 * 
 * @author Curtis Rueden
 * @author Johannes Schindelin
 * @author Grant Harris
 */
public class FileUtilsTest {
	private final static boolean isWindows =
		System.getProperty("os.name").startsWith("Win");

	@Test
	public void testGetPath() {
		// test that Windows-style paths get standardized
		assertEquals("C:/path/to/my-windows-file", FileUtils.getPath(
			"C:\\path\\to\\my-windows-file", "\\"));

		// test that there are no changes to *nix-style paths
		assertEquals("/path/to/my-nix-file", FileUtils.getPath(
			"/path/to/my-nix-file", "/"));

		// test that an already-standardized path stays good on Windows
		assertEquals("/path/to/my-nix-file", FileUtils.getPath(
			"/path/to/my-nix-file", "\\"));
	}

	@Test
	public void testGetExtension() {
		assertEquals("ext", FileUtils.getExtension("/path/to/file.ext"));
		assertEquals("", FileUtils.getExtension("/path/to/file"));
		assertEquals("a", FileUtils.getExtension("/etc/init.d/xyz/file.a"));
		assertEquals("", FileUtils.getExtension("/etc/init.d/xyz/file"));
	}

	@Test
	public void testURLToFile() throws MalformedURLException {
		// verify that 'file:' URL works
		final String jqpublic;
		if (isWindows) {
			jqpublic = "C:/Users/jqpublic/";
		} else {
			jqpublic = "/Users/jqpublic/";
		}
		final String filePath = jqpublic + "imagej/ImageJ.class";
		final String fileURL = new File(filePath).toURI().toURL().toString();
		final File fileFile = FileUtils.urlToFile(fileURL);
		assertEqualsPath(filePath, fileFile.getPath());

		// verify that file path with spaces works
		final File spaceFileOriginal =
			new File(jqpublic.replace("jqpublic", "Spaceman Spiff") + "stun/Blaster.class");
		final URL spaceURL = spaceFileOriginal.toURI().toURL();
		final File spaceFileResult = FileUtils.urlToFile(spaceURL);
		assertEqualsPath(spaceFileOriginal.getPath(), spaceFileResult.getPath());

		// verify that file path with various characters works
		final String alphaLo = "abcdefghijklmnopqrstuvwxyz";
		final String alphaHi = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
		final String numbers = "1234567890";
		final String special = "_~!@#$%^&*()+`-=";
		final File specialFileOriginal = new File(jqpublic.replace("jqpublic", alphaLo) +
			alphaHi + "/" + numbers + "/" + special + "/foo/Bar.class");
		final URL specialURL = specialFileOriginal.toURI().toURL();
		final File specialFileResult = FileUtils.urlToFile(specialURL);
		assertEqualsPath(specialFileOriginal.getPath(), specialFileResult.getPath());

		// verify that 'jar:' URL works
		final String jarPath = "/Users/jqpublic/imagej/ij-core.jar";
		final String jarURL = "jar:file:" + jarPath + "!/imagej/ImageJ.class";
		final File jarFile = FileUtils.urlToFile(jarURL);
		assertEqualsPath(jarPath, jarFile.getPath());

		// verify that OSGi 'bundleresource:' URL fails
		final String bundleURL =
			"bundleresource://346.fwk2106232034:4/imagej/ImageJ.class";
		try {
			final File bundleFile = FileUtils.urlToFile(bundleURL);
			fail("Expected exception not thrown; result=" + bundleFile);
		}
		catch (IllegalArgumentException exc) {
			// NB: Expected behavior.
		}
	}

	@Test
	public void testShortenPath() {
		assertEquals("C:\\Documents and Settings\\"
			+ "All Users\\Application Data\\Apple Computer\\...\\SC Info.txt",
			FileUtils.shortenPath("C:\\Documents and Settings\\All Users"
				+ "\\Application Data\\Apple Computer\\iTunes\\SC Info\\SC Info.txt"));
		assertEquals("C:\\Documents and Settings\\All Users\\Application Data\\"
			+ "Apple Computer\\iTunes\\...\\SC Info.txt", FileUtils.shortenPath(
			"C:\\Documents and Settings\\All Users\\"
				+ "Application Data\\Apple Computer\\iTunes\\SC Info\\SC Info.txt", 5));
		assertEquals("C:\\temp", FileUtils.shortenPath("C:\\temp"));
		assertEquals("C:\\1\\2\\3\\4\\...\\test.txt", FileUtils
			.shortenPath("C:\\1\\2\\3\\4\\5\\test.txt"));
		assertEquals("C:/1/2/test.txt", FileUtils.shortenPath("C:/1/2/test.txt"));
		assertEquals("C:/1/2/3/4/.../test.txt", FileUtils
			.shortenPath("C:/1/2/3/4/5/test.txt"));
		assertEquals("\\\\server\\p1\\p2\\p3\\p4\\...\\p6", FileUtils
			.shortenPath("\\\\server\\p1\\p2\\p3\\p4\\p5\\p6"));
		assertEquals("\\\\server\\p1\\p2\\p3", FileUtils
			.shortenPath("\\\\server\\p1\\p2\\p3"));
		assertEquals("http://www.rgagnon.com/p1/p2/p3/.../pb.html", FileUtils
			.shortenPath("http://www.rgagnon.com/p1/p2/p3/p4/p5/pb.html"));
	}

	@Test
	public void testLimitPath() {
		assertEquals("C:\\Doc...SC Info.txt",
			FileUtils
				.limitPath("C:\\Documents and Settings\\All Users\\"
					+ "Application Data\\Apple Computer\\iTunes\\SC Info\\SC Info.txt",
					20));
		assertEquals("C:\\temp", FileUtils.limitPath("C:\\temp", 20));
		assertEquals("C:\\1\\2\\3\\...test.txt", FileUtils.limitPath(
			"C:\\1\\2\\3\\4\\5\\test.txt", 20));
		assertEquals("...testfile.txt", FileUtils.limitPath("C:/1/2/testfile.txt",
			15));
		assertEquals("C:/1...test.txt", FileUtils.limitPath(
			"C:/1/2/3/4/5/test.txt", 15));
		assertEquals("\\\\server\\p1\\p2\\...p6", FileUtils.limitPath(
			"\\\\server\\p1\\p2\\p3\\p4\\p5\\p6", 20));
		assertEquals("http://www...pb.html", FileUtils.limitPath(
			"http://www.rgagnon.com/p1/p2/p3/p4/p5/pb.html", 20));
	}

	@Test
	public void testListContents() throws IOException, URISyntaxException {
		// verify that listContents on a non-existent file returns the empty set
		File nonExisting;
		int i = 0;
		for (;;) {
			nonExisting = new File("" + i);
			if (!nonExisting.exists()) break;
			i++;
		}
		try {
			Collection<URL> urls = FileUtils.listContents(nonExisting.toURI().toURL());
			assertNotNull(urls);
			assertEquals(0, urls.size());
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}

		// write some items to a temporary .jar file
		final String subDir = "sub 𝄞directory/";
		final String subSubDir = "more 𝄢stuff/";
		final File jarFile = File.createTempFile("listContentsTest", ".jar");
		final FileOutputStream out = new FileOutputStream(jarFile);
		final JarOutputStream jarOut = new JarOutputStream(out);
		try {
			jarOut.putNextEntry(new JarEntry(subDir));
			jarOut.closeEntry();
			jarOut.putNextEntry(new JarEntry(subDir + subSubDir));
			jarOut.closeEntry();
			// NB: This entry is not in the subdirectory, and should not be listed.
			jarOut.putNextEntry(new JarEntry("foo.txt"));
			jarOut.write("bar".getBytes());
			jarOut.closeEntry();
			// NB: The next two entries are directly beneath the subdirectory.
			jarOut.putNextEntry(new JarEntry(subDir + "hello.txt"));
			jarOut.write("world".getBytes());
			jarOut.closeEntry();
			jarOut.putNextEntry(new JarEntry(subDir + "rock.txt"));
			jarOut.write("roll".getBytes());
			jarOut.closeEntry();
			// NB: The last two entries are beneath a second subdirectory,
			// and should only be listed when the recurse flag is set to true.
			jarOut.putNextEntry(new JarEntry(subDir + subSubDir + "fox.txt"));
			jarOut.write("the quick brown fox".getBytes());
			jarOut.closeEntry();
			jarOut.putNextEntry(new JarEntry(subDir + subSubDir + "dog.txt"));
			jarOut.write("jumps over the lazy dog".getBytes());
			jarOut.closeEntry();
			jarOut.close();
		} finally {
			out.close();
		}

		final String uriPath = new URI(null, null, "!/" + subDir, null).toString();
		final String urlPath = "jar:" + jarFile.toURI().toURL() + uriPath;
		final URL url = new URL(urlPath);
		final URL subSubURL = new URL(urlPath + "more%20𝄢stuff/");
		final URL helloURL = new URL(urlPath + "hello.txt");
		final URL rockURL = new URL(urlPath + "rock.txt");
		final URL foxURL = new URL(urlPath + "more%20𝄢stuff/fox.txt");
		final URL dogURL = new URL(urlPath + "more%20𝄢stuff/dog.txt");

		// check listContents: recursive without directories
		final Collection<URL> setTT = FileUtils.listContents(url, true, true);
		final URL[] listTT = setTT.toArray(new URL[setTT.size()]);
		assertArrayEquals(new URL[] { helloURL, rockURL, foxURL, dogURL }, listTT);

		// check listContents: recursive including directories
		final Collection<URL> setTF = FileUtils.listContents(url, true, false);
		final URL[] listTF = setTF.toArray(new URL[setTF.size()]);
		assertArrayEquals(
			new URL[] { subSubURL, helloURL, rockURL, foxURL, dogURL }, listTF);

		// check listContents: non-recursive without directories
		final Collection<URL> setFT = FileUtils.listContents(url, false, true);
		final URL[] listFT = setFT.toArray(new URL[setFT.size()]);
		assertArrayEquals(new URL[] { helloURL, rockURL }, listFT);

		// check listContents: non-recursive including directories
		final Collection<URL> setFF = FileUtils.listContents(url, false, false);
		final URL[] listFF = setFF.toArray(new URL[setFF.size()]);
		assertArrayEquals(new URL[] { subSubURL, helloURL, rockURL }, listFF);

		// clean up
		assertTrue(jarFile.delete());
	}

	private static void assertEqualsPath(final String a, final String b) {
		if (isWindows) {
			assertEquals(a.replace('\\', '/'), b.replace('\\', '/'));
		} else {
			assertEquals(a, b);
		}
	}

	@Test
	public void testStripVersionFromFilename() {
		assertEquals("jars/bio-formats.jar", FileUtils.stripFilenameVersion("jars/bio-formats-4.4-imagej-2.0.0-beta1.jar"));
		assertEquals(FileUtils.stripFilenameVersion("jars/ij-data-2.0.0.1-beta1.jar"), FileUtils.stripFilenameVersion("jars/ij-data-2.0.0.1-SNAPSHOT.jar"));
		assertEquals(FileUtils.stripFilenameVersion("jars/ij-1.44.jar"), FileUtils.stripFilenameVersion("jars/ij-1.46b.jar"));
		assertEquals(FileUtils.stripFilenameVersion("jars/javassist.jar"), FileUtils.stripFilenameVersion("jars/javassist-3.9.0.GA.jar"));
		assertEquals(FileUtils.stripFilenameVersion("jars/javassist.jar"), FileUtils.stripFilenameVersion("jars/javassist-3.16.1-GA.jar"));
		assertEquals(FileUtils.stripFilenameVersion("jars/bsh.jar"), FileUtils.stripFilenameVersion("jars/bsh-2.0b4.jar"));
		assertEquals(FileUtils.stripFilenameVersion("jars/mpicbg.jar"), FileUtils.stripFilenameVersion("jars/mpicbg-20111128.jar"));
		assertEquals(FileUtils.stripFilenameVersion("jars/miglayout-swing.jar"), FileUtils.stripFilenameVersion("jars/miglayout-3.7.3.1-swing.jar"));
	}

	@Test
	public void testGetAllVersions() throws IOException {
		final String withClassifier = "miglayout-3.7.3.1-swing.jar";
		final String withoutClassifier = "miglayout-3.7.3.1.jar";

		final File tmp = FileUtils.createTemporaryDirectory("delete-other-", "");
		try {
			writeEmptyFile(new File(tmp, withClassifier));
			writeEmptyFile(new File(tmp, withoutClassifier));

			assertArrayEquals(new File[] { new File(tmp, withClassifier) },
				FileUtils.getAllVersions(tmp, withClassifier));
			assertArrayEquals(new File[] { new File(tmp, withoutClassifier) },
				FileUtils.getAllVersions(tmp, withoutClassifier));
		}
		finally {
			FileUtils.deleteRecursively(tmp);
		}
	}

	private static void writeEmptyFile(final File file) throws FileNotFoundException, IOException {
		final OutputStream out = new FileOutputStream(file);
		out.flush();
		out.close();
	}
}
