/*
 * #%L
 * SciJava Common shared library for SciJava software.
 * %%
 * Copyright (C) 2009 - 2013 Board of Regents of the University of
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
 * 
 * The views and conclusions contained in the software and documentation are
 * those of the authors and should not be interpreted as representing official
 * policies, either expressed or implied, of any organization.
 * #L%
 */

package org.scijava.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

/**
 * Combines SezPoz annotations from all JAR files on the classpath.
 * 
 * @author Curtis Rueden
 */
public class CombineAnnotations {

	private static final String PREFIX = "META-INF/json/";
	private static final String LEGACY_PREFIX = "META-INF/annotations/";
	private static final String OUTPUT_DIR = "src/main/assembly/all";

	private final Set<String> annotationFiles;

	public CombineAnnotations() throws IOException {
		annotationFiles = getAnnotationFiles();
	}

	/** Reads in annotations from all available resources and combines them. */
	public void combine() throws IOException, ClassNotFoundException {
		final StringBuilder annotations = new StringBuilder();
		final ClassLoader loader = ClassLoader.getSystemClassLoader();

		log("");
		log("Writing annotations to " + new File(OUTPUT_DIR).getAbsolutePath());

		new File(OUTPUT_DIR, PREFIX).mkdirs();

		for (final String annotationFile : annotationFiles) {
			annotations.setLength(0);

			// read in annotations from all classpath resources
			final Enumeration<URL> resources = loader.getResources(annotationFile);
			while (resources.hasMoreElements()) {
				final URL resource = resources.nextElement();
				final BufferedReader reader = new BufferedReader(new InputStreamReader(resource.openStream()));
				while (true) {
					final String line = reader.readLine();
					if (line == null) break;
					annotations.append(line);
				}
				reader.close();
			}

			// write out annotations to combined file on disk
			final File outputFile = new File(OUTPUT_DIR, annotationFile);
			final PrintStream out =
				new PrintStream(new FileOutputStream(outputFile));
			out.print(annotations.toString());
			out.close();
			log(outputFile.getName() + ": " + annotations.length() + " bytes");
		}
	}

	/** Scans for annotations files in every resource on the classpath. */
	public Set<String> getAnnotationFiles() throws IOException {
		final HashSet<String> files = new HashSet<String>();

		for (final String prefix : new String[] { PREFIX, LEGACY_PREFIX }) {
			final Enumeration<URL> directories = Thread.currentThread()
					.getContextClassLoader().getResources(prefix);
			while (directories.hasMoreElements()) {
				final URL url = directories.nextElement();
				for (final URL annotationIndexURL : FileUtils.listContents(url)) {
					String string = annotationIndexURL.toString();
					if (string.endsWith("/")) {
						continue;
					}
					final int length = string.length();
					add(files, PREFIX + string.substring(
							string.lastIndexOf('/', length - 1) + 1, length));
				}
			}
		}
		return files;
	}

	public static void main(final String[] args) throws Exception {
		new CombineAnnotations().combine();
	}

	// -- Helper methods --

	private void add(final HashSet<String> set, final String item) {
		log("\t" + item);
		set.add(item);
	}

	private void log(final String msg) {
		System.out.println(msg);
	}

}
