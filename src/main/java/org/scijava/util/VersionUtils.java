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

package org.scijava.util;

/**
 * Useful methods for retrieving versions from JARs and POMs associated with
 * {@link Class} objects.
 *
 * @author Curtis Rueden
 * @author Mark Hiner
 */
public class VersionUtils {

	/**
	 * Looks up the version of the specified class using any means available,
	 * appending the build number to any {@code SNAPSHOT} version. Will only
	 * search POMs in the base directory.
	 *
	 * @param c - Look up this class's version
	 * @return Version of specified {@link Class} or null if not found.
	 */
	public static String getVersion(final Class<?> c) {
		return getVersion(c, null, null);
	}

	/**
	 * Looks up the version of the specified class using any means available,
	 * appending the build number to any {@code SNAPSHOT} version. The
	 * {@code groupId} and {@code artifactId} parameters allow specification of
	 * the POM lookup path.
	 *
	 * @param c - Look up this class's version
	 * @param groupId - Maven group ID containing class
	 * @param artifactId - Maven artifact ID containing class
	 * @return Version of specified {@link Class} or null if not found.
	 */
	public static String getVersion(final Class<?> c, final String groupId,
		final String artifactId)
	{
		final String version = getVersionFromManifest(c);
		if (version != null) return version;
		return getVersionFromPOM(c, groupId, artifactId);
	}

	/**
	 * Looks up the version of the specified class using a JAR manifest if
	 * available, appending the build number to any {@code SNAPSHOT} version.
	 *
	 * @param c - Look up this class's version
	 * @return Version of specified {@link Class} or null if not found.
	 */
	public static String getVersionFromManifest(final Class<?> c) {
		final Manifest m = Manifest.getManifest(c);
		return m == null ? null : m.getVersion();
	}

	/**
	 * Looks up the version of the specified class using the specified POM, or
	 * base POM directory if {@code groupId} and {@code artifactId} are
	 * {@code null}.
	 *
	 * @param c - Look up this class's version
	 * @param groupId - Maven group ID containing class
	 * @param artifactId - Maven artifact ID containing class
	 * @return Version of specified {@link Class} or null if not found.
	 */
	public static String getVersionFromPOM(final Class<?> c,
		final String groupId, final String artifactId)
	{
		final POM pom = POM.getPOM(c, groupId, artifactId);
		return pom == null ? null : pom.getVersion();
	}

	/**
	 * Looks up the build number (typically an SCM revision) of the specified
	 * class. This information is retrieved from the JAR manifest's
	 * <code>Implementation-Build</code> entry, or null if no such value exists.
	 *
	 * @param c - Look up this class's build number
	 * @return Build number of specified {@link Class} or null if not found.
	 */
	public static String getBuildNumber(final Class<?> c) {
		final Manifest m = Manifest.getManifest(c);
		return m == null ? null : m.getImplementationBuild();
	}

}
