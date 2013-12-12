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
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * Helps Eclipse's lack of support for annotation processing in incremental
 * build mode.
 * <p>
 * Eclipse has a very, let's say, "creative" way to interpret the Java
 * specifications when it comes to annotation processing: while Java mandates
 * that annotation processors need to be run after compiling Java classes,
 * Eclipse cops out of that because it poses a challenge to its incremental
 * compilation (and especially to Eclipse's attempt at compiling .class files
 * even from .java sources that contain syntax errors).
 * </p>
 * <p>
 * So we need to do something about this. Our strategy is to detect when the
 * annotation index was not updated properly and just do it ourselves, whenever
 * {@link Index#load(Class)} is called.
 * </p>
 * <p>
 * Since our aim here is to compensate for Eclipse's shortcoming, we need only
 * care about the scenario where the developer launches either a Java main class
 * or a unit test from within Eclipse, and even then only when the annotation
 * index is to be accessed.
 * </p>
 * <p>
 * The way Eclipse launches Java main classes or unit tests, it makes a single
 * {@link URLClassLoader} with all the necessary class path elements. Crucially,
 * the class path elements corresponding to Eclipse projects will never point to
 * {@code .jar} files but to directories. This allows us to assume that the
 * annotation classes as well as the annotated classes can be loaded using that
 * exact class loader, too.
 * </p>
 * <p>
 * It is quite possible that a developer may launch a main class in a different
 * project than the one which needs annotation indexing, therefore we need to
 * inspect all class path elements.
 * </p>
 * <p>
 * To provide at least a semblance of a performant component, before going all
 * out and indexing the annotations, we verify that the {@code META-INF/json/}
 * directory has an outdated timestamp relative to the {@code .class} files. If
 * that is not the case, we may safely assume that the annotation indexes are
 * up-to-date.
 * </p>
 * <p>
 * To avoid indexing class path elements over and over again which simply do not
 * contain indexable annotations, we make the {@code META-INF/json/} directory
 * nevertheless, updating the timestamp to reflect that we indexed the
 * annotations.
 * </p>
 * 
 * @author Johannes Schindelin
 */
public class EclipseHelper extends DirectoryIndexer {

	/**
	 * Updates the annotation index in the current Eclipse project.
	 * <p>
	 * The assumption is that Eclipse -- after failing to run the annotation
	 * processors correctly -- will launch any tests or main classes with a class
	 * path that contains the project's output directory with the {@code .class}
	 * files (as opposed to a {@code .jar} file). We only need to update that
	 * first class path element (or for tests, the first two), and only if it is a
	 * local directory.
	 * </p>
	 * 
	 * @param loader the class loader whose class path to inspect
	 * @throws IOException
	 */
	public static void updateAnnotationIndex(final ClassLoader loader) {
		if (!(loader instanceof URLClassLoader)) {
			return;
		}
		EclipseHelper helper = null;
		for (final URL url : ((URLClassLoader) loader).getURLs()) {
			if (!"file".equals(url.getProtocol())) {
				continue;
			}
			String path = url.getFile();
			if (path.indexOf(':') >= 0) {
				continue;
			}
			File directory = new File(path);
			if (!directory.isDirectory()) {
				continue;
			}
			if (helper == null) {
				helper = new EclipseHelper();
			}
			helper.index(directory, loader);
		}
	}

	private void index(File directory, ClassLoader loader) {
		if (!directory.canWrite() || upToDate(directory)) {
			return;
		}
		System.err.println("[ECLIPSE HELPER] Indexing annotations...");
		try {
			discoverAnnotations(directory, "", loader);
			write(directory);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		// update the timestamp of META-INF/json/
		final File jsonDirectory = new File(directory, Index.INDEX_PREFIX);
		if (jsonDirectory.isDirectory()) {
			jsonDirectory.setLastModified(System.currentTimeMillis());
		}
		else {
			jsonDirectory.mkdirs();
		}
	}

	private boolean upToDate(final File directory) {
		final File jsonDirectory = new File(directory, Index.INDEX_PREFIX);
		if (!jsonDirectory.isDirectory()) {
			return false;
		}
		return upToDate(directory, jsonDirectory.lastModified());
	}

	private boolean upToDate(File directory, long lastModified) {
		if (directory.lastModified() > lastModified) {
			return false;
		}
		final File[] list = directory.listFiles();
		if (list != null) {
			for (final File file : list) {
				if (file.isFile()) {
					if (file.lastModified() > lastModified) {
						return false;
					}
				}
				else if (file.isDirectory()) {
					if (!upToDate(file, lastModified)) {
						return false;
					}
				}
			}
		}
		return true;
	}
}
