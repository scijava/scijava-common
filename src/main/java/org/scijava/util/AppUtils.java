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

package org.scijava.util;

import java.io.File;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Useful methods for obtaining details of the SciJava application environment.
 * 
 * @author Johannes Schindelin
 * @author Curtis Rueden
 */
public final class AppUtils {

	private static final Class<?> mainClass;

	static {
		// Get the class whose main method launched the application. The heuristic
		// will fail if the main thread has terminated before this class loads.
		final String className = DebugUtils.getMainClassName();
		mainClass = className == null ? null : ClassUtils.loadClass(className);
	}

	private AppUtils() {
		// prevent instantiation of utility class
	}

	/**
	 * Gets the class whose main method launched the application.
	 * 
	 * @return The launching class, or null if the main method terminated before
	 *         the {@code AppUtils} class was loaded.
	 */
	public static Class<?> getMainClass() {
		return mainClass;
	}

	/**
	 * Gets the application root directory. If the given system property is set,
	 * it is used. Otherwise, we scan up the tree from the given class for a
	 * suitable directory.
	 * 
	 * @param sysProp System property which may point at the root directory. If
	 *          this is set to a valid directory, it is used.
	 * @param c The class from which the base directory should be derived.
	 * @param baseSubdirectory A hint for what to expect for a directory structure
	 *          beneath the application base directory. If this value is null
	 *          (i.e., no hint is given), the heuristic scans up the directory
	 *          tree looking for the topmost pom.xml file.
	 * @see AppUtils#getBaseDirectory(File, String)
	 */
	public static File getBaseDirectory(final String sysProp, final Class<?> c,
		final String baseSubdirectory)
	{
		final String property = System.getProperty(sysProp);
		if (property != null) {
			final File dir = new File(property);
			if (dir.isDirectory()) return dir;
		}

		// look for valid base directory relative to this class
		final File basePath = AppUtils.getBaseDirectory(c, baseSubdirectory);
		if (basePath != null) return basePath;

		// NB: Look for valid base directory relative to the main class which
		// launched this environment. We will reach this logic, e.g., if the
		// application is running via "mvn exec:exec". In this case, most classes
		// (including this one) will be located in JARs in the local Maven
		// repository cache (~/.m2/repository), so the corePath will be null.
		// However, the classes of the launching project will be located in
		// target/classes, so we search up the tree from one of those.
		final File appPath = AppUtils.getBaseDirectory(AppUtils.getMainClass());
		if (appPath != null) return appPath;

		// last resort: use current working directory
		return new File(".").getAbsoluteFile();
	}

	/**
	 * Gets the base file system directory containing the given class file.
	 * 
	 * @param c The class from which the base directory should be derived.
	 * @see #getBaseDirectory(File, String)
	 */
	public static File getBaseDirectory(final Class<?> c) {
		return getBaseDirectory(c, null);
	}

	/**
	 * Gets the base file system directory containing the given class file.
	 * 
	 * @param c The class from which the base directory should be derived.
	 * @param baseSubdirectory A hint for what to expect for a directory structure
	 *          beneath the application base directory.
	 * @see #getBaseDirectory(File, String)
	 */
	public static File getBaseDirectory(final Class<?> c,
		final String baseSubdirectory)
	{
		// see: http://stackoverflow.com/a/12733172/1207769

		// step 1: convert Class to URL
		final URL location = ClassUtils.getLocation(c);

		// step 2: convert URL to File
		File baseFile;
		try {
			baseFile = FileUtils.urlToFile(location);
		}
		catch (final IllegalArgumentException exc) {
			// URL can't be converted to a file
			baseFile = null;
		}

		// step 3: get the file's base directory
		return getBaseDirectory(baseFile, baseSubdirectory);
	}

	/**
	 * Gets the base file system directory from the given known class location.
	 * <p>
	 * This method uses heuristics to find the base directory in a variety of
	 * situations. Depending on your execution environment, the class may be
	 * located in one of several different places:
	 * </p>
	 * <ol>
	 * <li><b>In the Maven build directory.</b> Typically this is the
	 * {@code target/classes} folder of a given component. In this case, the class
	 * files reside directly on the file system (not in a JAR file). The base
	 * directory is defined as the toplevel Maven directory for the multi-module
	 * project. For example, if you have checked out {@code scijava-common.git} to
	 * {@code ~/sjc}, the {@code org.scijava.Context} class will be located at
	 * {@code ~/sjc/scijava-common/target/classes/org/scijava/Context.class}.
	 * Asking for the base directory for that class will yield
	 * {@code ~/sjc/scijava-common}, as long as you correctly specify
	 * {@code scijava-common} for the {@code baseSubdirectory}.</li>
	 * <li><b>Within a JAR file in the Maven local repository cache.</b> Typically
	 * this cache is located in {@code ~/.m2/repository}. The location will be
	 * {@code groupId/artifactId/version/artifactId-version.jar} where
	 * {@code groupId}, {@code artifactId} and {@code version} are the <a
	 * href="http://maven.apache.org/pom.html#Maven_Coordinates">Maven GAV
	 * coordinates</a>. Note that in this case, no base directory with respect to
	 * the given class can be found, and this method will return null.</li>
	 * <li><b>Within a JAR file beneath the base directory.</b> Common cases
	 * include running the application from a standalone application bundle (e.g.,
	 * the JARs may all be located within a {@code jars} folder of the application
	 * distribution archive) or using a JAR packaged by Maven and residing in the
	 * Maven build folder (typically {@code target/artifactId-version.jar}).
	 * However, this could potentially be anywhere beneath the base directory.
	 * This method assumes the JAR will be nested exactly one level deep; i.e., it
	 * computes the base directory as the parent directory of the one containing
	 * the JAR file.</li>
	 * </ol>
	 * <p>
	 * As you can see, it is quite complicated to find the base directory properly
	 * in all cases. The specific execution environments we have tested include:
	 * </p>
	 * <ol>
	 * <li><b>Running from Eclipse.</b> How Eclipse structures the classpath
	 * depends on which dependencies are open (i.e., project dependencies) versus
	 * closed (i.e., JAR dependencies). Each project dependency has a classpath
	 * entry <b>in its Maven build directory</b>. Each JAR dependency has a
	 * classpath entry <b>within a JAR file in the Maven local repository
	 * cache</b>.</li>
	 * <li><b>Running via Maven.</b> If you execute the application using Maven
	 * (e.g., {@code mvn exec:exec}, or with a fully Maven-compatible IDE such as
	 * NetBeans or IntelliJ IDEA) then all dependencies will reside <b>within JAR
	 * files in the local Maven repository cache</b>. But the executed project
	 * itself will reside <b>in its Maven build directory</b>. So as long as you
	 * ask for the base directory relative to a class
	 * <em>of the executed project</em> it will be found.</li>
	 * <li><b>Running as an application bundle (e.g., ImageJ).</b> Typically this
	 * means downloading ImageJ from the web site, unpacking it and running the
	 * ImageJ launcher (double-clicking ImageJ-win32.exe on Windows,
	 * double-clicking the {@code ImageJ.app} on OS X, etc.). In this case, all
	 * components reside in the {@code jars} folder of the application bundle, and
	 * the base directory will be found one level above that.</li>
	 * </ol>
	 * 
	 * @param classLocation The location from which the base directory should be
	 *          derived.
	 * @param baseSubdirectory A hint for what to expect for a directory structure
	 *          beneath the application base directory. If this value is null
	 *          (i.e., no hint is given), the heuristic scans up the directory
	 *          tree looking for the topmost pom.xml file.
	 */
	public static File getBaseDirectory(final File classLocation,
		final String baseSubdirectory)
	{
		if (classLocation == null) return null;
		String path = FileUtils.getPath(classLocation).replace('\\', '/');

		if (path.contains("/.m2/repository/")) {
			// NB: The class is in a JAR in the Maven repository cache.
			// We cannot find the application base directory relative to this path.
			return null;
		}

		// check whether the class is in a Maven build directory
		String basePrefix = "/";
		if (baseSubdirectory != null) basePrefix += baseSubdirectory + "/";

		final String targetClassesSuffix = basePrefix + "target/classes";
		final String targetTestClassesSuffix = basePrefix + "target/test-classes";
		final String[] suffixes = {targetClassesSuffix, targetTestClassesSuffix};
		for (final String suffix : suffixes) {
			if (!path.endsWith(suffix)) continue;

			// NB: The class is a file beneath a Maven build directory.
			path = path.substring(0, path.length() - suffix.length());

			File dir = new File(path);
			if (baseSubdirectory == null) {
				// NB: There is no hint as to the directory structure.
				// So we scan up the tree to find the topmost pom.xml file.
				while (dir.getParentFile() != null &&
					new File(dir.getParentFile(), "pom.xml").exists())
				{
					dir = dir.getParentFile();
				}
			}
			return dir;
		}

		final Pattern pattern =
			Pattern.compile(".*(" + Pattern.quote(basePrefix + "target/") +
				"[^/]*\\.jar)");
		final Matcher matcher = pattern.matcher(path);
		if (matcher.matches()) {
			// NB: The class is in the Maven build directory inside a JAR file
			// ("target/[something].jar").
			final int index = matcher.start(1);
			path = path.substring(0, index);
			return new File(path);
		}

		if (path.endsWith(".jar")) {
			final File jarDirectory = classLocation.getParentFile();
			// NB: The class is in a JAR file, which we assume is nested one level
			// deep (i.e., directly beneath the application base directory).
			return jarDirectory.getParentFile();
		}

		// NB: As a last resort, we use the class location directly.
		return classLocation;
	}

}
