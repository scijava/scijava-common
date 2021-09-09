/*
 * #%L
 * SciJava Common shared library for SciJava software.
 * %%
 * Copyright (C) 2009 - 2021 SciJava developers.
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

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.scijava.annotations.EclipseHelper;
import org.w3c.dom.Document;

/**
 * Deprecated class.
 * 
 * @author Johannes Schindelin
 */
@Deprecated
public final class CheckSezpoz {

	@Deprecated
	public static boolean verbose;

	@Deprecated
	public static final String FILE_NAME = "latest-sezpoz-check.txt";

	@Deprecated
	private CheckSezpoz() {
		// NB: Prevent instantiation of utility class.
	}

	/**
	 * Checks the annotations of all CLASSPATH components. Optionally, it only
	 * checks the non-.jar components of the CLASSPATH. This is for Eclipse.
	 * Eclipse fails to run the annotation processor at each incremental build. In
	 * contrast to Maven, Eclipse usually does not build .jar files, though, so we
	 * can have a very quick check at startup if the annotation processor was not
	 * run correctly and undo the damage.
	 * 
	 * @param checkJars whether to inspect .jar components of the CLASSPATH
	 * @return false, when the annotation processor had to be run
	 * @throws IOException
	 * @see EclipseHelper
	 */
	@Deprecated
	public static boolean check(final boolean checkJars) throws IOException {
		EclipseHelper.main();
		return false;
	}

	/**
	 * Checks the annotations of a CLASSPATH component.
	 * 
	 * @param file the CLASSPATH component (.jar file or directory)
	 * @return false, when the annotation processor had to be run
	 * @throws IOException
	 * @see EclipseHelper
	 */
	@Deprecated
	public static boolean check(final File file) throws IOException {
		System.err.println("Warning: Deprecated CheckSezpoz class was called!");
		EclipseHelper.updateAnnotationIndex(new URLClassLoader(new URL[] { file.toURI().toURL() }));
		return false;
	}

	/**
	 * Checks the annotations of a directory in the CLASSPATH.
	 * 
	 * @param classes the CLASSPATH component directory
	 * @return false, when the annotation processor had to be run
	 * @throws IOException
	 * @see EclipseHelper
	 */
	@Deprecated
	public static boolean checkDirectory(final File classes) throws IOException {
		return check(classes);
	}

	/**
	 * Checks whether the annotations are possibly out-of-date.
	 * <p>
	 * This method looks whether there are any {@code .class} files older than
	 * their corresponding {@code .java} files, or whether there are
	 * {@code .class} files that were generated since last time we checked.
	 * </p>
	 * 
	 * @param classes the {@code classes/} directory where Maven puts the
	 *          {@code .class} files
	 * @param source the {@code src/main/java/} directory where Maven expects the
	 *          {@code .java} files
	 * @param youngerThan the date/time when we last checked
	 * @see EclipseHelper
	 */
	@Deprecated
	public static boolean checkDirectory(final File classes, final File source,
		final long youngerThan) throws IOException
	{
		return check(classes);
	}

	/**
	 * Checks a {@code .jar} file for stale annotations.
	 * <p>
	 * This method is broken at the moment since there is no good way to verify
	 * that SezPoz ran before the {@code .jar} file was packaged.
	 * </p>
	 * 
	 * @param file the {@code .jar} file
	 * @see EclipseHelper
	 */
	@Deprecated
	public static void checkJar(final File file) throws IOException {
		check(file);
	}

	/**
	 * Runs SezPoz on the sources, writing the annotations into the classes'
	 * {@code META-INF/annotations/} directory.
	 * 
	 * @param classes the output directory
	 * @param sources the directory containing the source files
	 * @return whether anything in {@code META-INF/annotations/*} changed
	 * @see EclipseHelper
	 */
	@Deprecated
	public static boolean fix(final File classes, final File sources) {
		try {
			return check(classes);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Writes out a DOM as {@code .xml} file.
	 * 
	 * @param xml the DOM
	 * @param file the file to write
	 * @throws TransformerException
	 */
	@Deprecated
	public static void writeXMLFile(final Document xml, final File file)
		throws TransformerException
	{
		final Source source = new DOMSource(xml);
		final Result result = new StreamResult(file);
		final TransformerFactory factory = TransformerFactory.newInstance();
		final Transformer transformer = factory.newTransformer();
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount",
			"4");
		transformer.transform(source, result);
	}
}
