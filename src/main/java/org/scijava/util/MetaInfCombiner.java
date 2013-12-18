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
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.scijava.annotations.CombineAnnotations;

/**
 * Accumulates information from the class path to make META-INF/ files
 * appropriate for an uber jar.
 * 
 * @author Johannes Schindelin
 */
public class MetaInfCombiner {

	private final static String SERVICES_PREFIX = "META-INF/services/";

	private static void combineServices(final File outputDirectory) throws IOException {
		final Map<String, StringBuilder> files = new HashMap<String, StringBuilder>();

		final Enumeration<URL> directories = Thread.currentThread()
				.getContextClassLoader().getResources(SERVICES_PREFIX);
		while (directories.hasMoreElements()) {
			for (final URL url : FileUtils.listContents(directories.nextElement())) {
				final String urlString = url.toString();
				if (urlString.endsWith("/")) {
					continue;
				}
				final String fileName = urlString.substring(urlString.lastIndexOf('/') + 1);
				StringBuilder builder = files.get(fileName);
				if (builder == null) {
					builder = new StringBuilder();
					files.put(fileName, builder);
				}
				final InputStreamReader reader = new InputStreamReader(url.openStream());
				final BufferedReader buffered = new BufferedReader(reader);
				for (;;) {
					final String line = buffered.readLine();
					if (line == null) {
						break;
					}
					builder.append(line).append('\n');
				}
				buffered.close();
			}
		}

		if (files.isEmpty()) {
			return;
		}

		final File servicesDirectory = new File(outputDirectory, SERVICES_PREFIX);
		servicesDirectory.mkdirs();
		for (final Entry<String, StringBuilder> entry : files.entrySet()) {
			final FileWriter writer = new FileWriter(new File(servicesDirectory, entry.getKey()));
			writer.write(entry.getValue().toString());
			writer.close();
		}
	}

	public static void main(final String... args) throws IOException,
		ClassNotFoundException {
		if (args.length != 1) {
			throw new RuntimeException("Need an output directory!");
		}

		final String outputDirectory = args[0];
		combineServices(new File(outputDirectory));
		new CombineAnnotations(outputDirectory).combine();
	}
}
