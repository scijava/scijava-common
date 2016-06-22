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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

import org.scijava.util.Combiner;
import org.scijava.util.FileUtils;

/**
 * Combines annotation indexes from all JAR files on the classpath.
 * 
 * @author Curtis Rueden
 */
public class AnnotationCombiner extends AbstractIndexWriter implements Combiner
{

	private static final String PREFIX = "META-INF/json/";
	private static final String LEGACY_PREFIX = "META-INF/annotations/";

	/** Reads in annotations from all available resources and combines them. */
	@Override
	public void combine(File outputDirectory) throws Exception {
		if (outputDirectory == null) {
			outputDirectory = new File("src/main/assembly/all");
		}
		final Set<String> annotationFiles = getAnnotationFiles();

		final ClassLoader loader = Thread.currentThread().getContextClassLoader();

		log("");
		log("Writing annotations to " + outputDirectory.getAbsolutePath());

		new File(outputDirectory, PREFIX).mkdirs();
		for (final String annotationFile : annotationFiles) {
			final String annotationName = annotationFile.substring(PREFIX.length());
			@SuppressWarnings("unchecked")
			final Class<? extends Annotation> annotation =
				(Class<? extends Annotation>) loader.loadClass(annotationName);
			for (IndexItem<? extends Annotation> item : Index
				.load(annotation, loader))
			{
				add(adapt(item.annotation()), annotationName, item.className());
			}
		}

		write(new AnnotationStreamFactory(outputDirectory));
	}

	/** Scans for annotations files in every resource on the classpath. */
	public Set<String> getAnnotationFiles() throws IOException {
		final HashSet<String> files = new HashSet<>();

		for (final String prefix : new String[] { PREFIX, LEGACY_PREFIX }) {
			final Enumeration<URL> directories =
				Thread.currentThread().getContextClassLoader().getResources(prefix);
			while (directories.hasMoreElements()) {
				final URL url = directories.nextElement();
				for (final URL annotationIndexURL : FileUtils.listContents(url)) {
					String string = annotationIndexURL.toString();
					if (string.endsWith("/")) {
						continue;
					}
					final int length = string.length();
					add(files, PREFIX +
						string.substring(string.lastIndexOf('/', length - 1) + 1, length));
				}
			}
		}
		return files;
	}

	// -- Helper methods --

	private void add(final HashSet<String> set, final String item) {
		log("\t" + item);
		set.add(item);
	}

	private void log(final String msg) {
		System.out.println(msg);
	}

	// -- Helper Class --

	/**
	 * {@link AbstractIndexWriter.StreamFactory} implementation for writing an
	 * annotation.
	 */
	private static class AnnotationStreamFactory implements StreamFactory {

		private final File outputDirectory;

		public AnnotationStreamFactory(final File outputDirectory) {
			this.outputDirectory = outputDirectory;
		}

		@Override
		public InputStream openInput(String annotationName) throws IOException {
			return null;
		}

		@Override
		public OutputStream openOutput(String annotationName) throws IOException {
			final File file = new File(outputDirectory, PREFIX + annotationName);
			return new FileOutputStream(file);
		}

		@Override
		public boolean isClassObsolete(String className) {
			return false;
		}

	}

	// -- Main method --
	
	public static void main(final String[] args) throws Exception {
		new AnnotationCombiner().combine(args.length > 0 ? new File(args[0]) : null);
	}
}
