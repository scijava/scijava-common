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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.Map;
import java.util.jar.Attributes;

import org.scijava.Versioned;

/**
 * Helper class for working with JAR manifests.
 * 
 * @author Curtis Rueden
 */
public class Manifest implements Versioned {

	/** The JAR manifest backing this object. */
	private final java.util.jar.Manifest manifest;

	/** Creates a new instance wrapping the given JAR manifest. */
	private Manifest(final java.util.jar.Manifest manifest) {
		this.manifest = manifest;
	}

	// -- Manifest methods --

	public String getArchiverVersion() {
		return get("Archiver-Version");
	}

	public String getBuildJdk() {
		return get("Build-Jdk");
	}

	public String getBuiltBy() {
		return get("Built-By");
	}

	public String getCreatedBy() {
		return get("Created-By");
	}

	public String getImplementationBuild() {
		return get("Implementation-Build");
	}

	public String getImplementationDate() {
		return get("Implementation-Date");
	}

	public String getImplementationTitle() {
		return get("Implementation-Title");
	}

	public String getImplementationVendor() {
		return get("Implementation-Vendor");
	}

	public String getImplementationVendorId() {
		return get("Implementation-Vendor-Id");
	}

	public String getImplementationVersion() {
		return get("Implementation-Version");
	}

	public String getManifestVersion() {
		return get("Manifest-Version");
	}

	public String getPackage() {
		return get("Package");
	}

	public String getSpecificationTitle() {
		return get("Specification-Title");
	}

	public String getSpecificationVendor() {
		return get("Specification-Vendor");
	}

	public String getSpecificationVersion() {
		return get("Specification-Version");
	}

	public String get(final String key) {
		if (manifest == null) return null;
		final Attributes mainAttrs = manifest.getMainAttributes();
		if (mainAttrs == null) return null;
		return mainAttrs.getValue(key);
	}

	public Map<Object, Object> getAll() {
		if (manifest == null) return null;
		final Attributes mainAttrs = manifest.getMainAttributes();
		if (mainAttrs == null) return null;
		return Collections.unmodifiableMap(mainAttrs);
	}

	// -- Utility methods --

	/** Gets the JAR manifest associated with the given class. */
	public static Manifest getManifest(final Class<?> c) {
		try {
			return getManifest(new URL("jar:" + ClassUtils.getLocation(c) + "!/"));
		}
		catch (final IOException e) {
			return null;
		}
	}

	/**
	 * Gets the JAR manifest associated with the given XML document. Assumes the
	 * XML document was loaded as a resource from inside a JAR.
	 */
	public static Manifest getManifest(final XML xml) throws IOException {
		final String path = xml.getPath();
		if (path == null || !path.startsWith("file:")) return null;
		final int dotJAR = path.indexOf(".jar!/");
		return getManifest(new File(path.substring(5, dotJAR + 4)));
	}

	/** Gets the JAR manifest associated with the given JAR file. */
	public static Manifest getManifest(final File jarFile) throws IOException {
		if (!jarFile.exists()) throw new FileNotFoundException();
		return getManifest(new URL("jar:file:" + jarFile.getAbsolutePath() + "!/"));
	}

	private static Manifest getManifest(final URL jarURL) throws IOException {
		final JarURLConnection conn = (JarURLConnection) jarURL.openConnection();
		return new Manifest(conn.getManifest());
	}

	// -- Versioned methods --

	@Override
	public String getVersion() {
		final String v = getBaseVersion();
		if (v == null || !v.endsWith("-SNAPSHOT")) return v;

		// append commit hash to differentiate between development versions
		final String buildNumber = getImplementationBuild();
		return buildNumber == null ? v : v + "-" + buildNumber;
	}

	// -- Helper methods --

	private String getBaseVersion() {
		final String manifestVersion = getImplementationVersion();
		if (manifestVersion != null) return manifestVersion;
		return getSpecificationVersion();
	}

}
