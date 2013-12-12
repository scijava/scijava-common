/*
 * #%L
 * Annotation index (processor and index access library).
 * %%
 * Copyright (C) 2009 - 2013 Board of Regents of the University of Wisconsin-Madison.
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

package org.scijava.annotations;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * Indexes a directory of classes.
 * <p>
 * The assumption is that the classes are tested at some stage before packaging.
 * That will be the time when we can index the annotations, even if Eclipse
 * decided not to run any annotation processor.
 * </p>
 * 
 * @author Johannes Schindelin
 */
public class DirectoryIndexer extends AbstractIndexWriter {

	public void index(final File directory) throws IOException {
		try {
			@SuppressWarnings("deprecation")
			final URLClassLoader loader =
				new URLClassLoader(new URL[] { directory.toURL() }, Thread
					.currentThread().getContextClassLoader());
			discoverAnnotations(directory, "", loader);
		}
		catch (MalformedURLException e) {
			throw new IOException(e);
		}
		write(directory);
	}

	protected void discoverAnnotations(final File directory,
		final String classNamePrefix, final ClassLoader loader) throws IOException
	{
		final File[] list = directory.listFiles();
		if (list == null) {
			return;
		}
		for (final File file : list) {
			if (file.isDirectory()) {
				discoverAnnotations(file, classNamePrefix + file.getName() + ".",
					loader);
			}
			else if (file.isFile()) try {
				final String fileName = file.getName();
				if (!fileName.endsWith(".class")) {
					continue;
				}
				final String className =
					classNamePrefix + fileName.substring(0, fileName.length() - 6);
				final Class<?> clazz = loader.loadClass(className);
				for (final Annotation a : clazz.getAnnotations()) {
					add(a, className);
				}
			}
			catch (ClassNotFoundException e) {
				throw new IOException(e);
			}
		}
	}

	protected synchronized <A extends Annotation> void add(final A annotation,
		final String className)
	{
		if (!isIndexable(annotation)) {
			return;
		}
		add(adapt(annotation), annotation.annotationType().getName(), className);
	}

	private static <A extends Annotation> boolean isIndexable(final A annotation)
	{
		return annotation.annotationType().getAnnotation(Indexable.class) != null;
	}

	protected synchronized void write(final File directory) throws IOException {
		write(new StreamFactory() {

			public InputStream openInput(String annotationName) throws IOException {
				final File file =
					new File(directory, Index.INDEX_PREFIX + annotationName);
				if (file.exists()) {
					return new FileInputStream(file);
				}
				else {
					return null;
				}
			}

			public OutputStream openOutput(String annotationName) throws IOException {
				final File file =
					new File(directory, Index.INDEX_PREFIX + annotationName);
				final File directory = file.getParentFile();
				if (directory != null && !directory.isDirectory() &&
					!directory.mkdirs())
				{
					throw new IOException("Could not make directory " + directory);
				}
				return new FileOutputStream(file);
			}
		});
	}
}
