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

package org.scijava.app;

import java.io.File;

import org.scijava.plugin.Plugin;
import org.scijava.plugin.RichPlugin;
import org.scijava.plugin.SingletonPlugin;
import org.scijava.util.AppUtils;
import org.scijava.util.Manifest;
import org.scijava.util.POM;

/**
 * Metadata about a SciJava-based application, used by the {@link AppService}.
 * <p>
 * Applications discoverable at runtime must implement this interface and be
 * annotated with @{@link Plugin} with attribute {@link Plugin#type()} =
 * {@link App}.class. While it possible to create an application merely by
 * implementing this interface, it is encouraged to instead extend
 * {@link AbstractApp}, for convenience.
 * </p>
 * 
 * @author Curtis Rueden
 * @see Plugin
 * @see AppService
 */
public interface App extends RichPlugin, SingletonPlugin {

	/** Gets the title of the application. */
	default String getTitle() {
		return getInfo().getName();
	}


	/** The Maven {@code groupId} of the application. */
	String getGroupId();

	/** The Maven {@code artifactId} of the application. */
	String getArtifactId();

	/** Gets the Maven POM containing metadata about the application. */
	POM getPOM();

	/**
	 * Gets the manifest containing metadata about the application.
	 * <p>
	 * NB: This metadata may be null if run in a development environment.
	 * </p>
	 */
	Manifest getManifest();

	/**
	 * Gets a string with information about the application.
	 * 
	 * @param mem If true, memory usage information is included.
	 */
	default String getInfo(final boolean mem) {
		final String appTitle = getTitle();
		final String appVersion = getVersion();
		final String javaVersion = System.getProperty("java.version");
		final String osArch = System.getProperty("os.arch");
		final long maxMem = Runtime.getRuntime().maxMemory();
		final long totalMem = Runtime.getRuntime().totalMemory();
		final long freeMem = Runtime.getRuntime().freeMemory();
		final long usedMem = totalMem - freeMem;
		final long usedMB = usedMem / 1048576;
		final long maxMB = maxMem / 1048576;
		final StringBuilder sb = new StringBuilder();
		sb.append(appTitle + " " + appVersion);
		sb.append("; Java " + javaVersion + " [" + osArch + "]");
		if (mem) sb.append("; " + usedMB + "MB of " + maxMB + "MB");
		return sb.toString();
	}

	/**
	 * A system property which, if set, overrides the base directory of the
	 * application.
	 */
	default String getSystemProperty() {
		return getInfo().getName().toLowerCase() + ".dir";
	}

	/**
	 * Gets the application's root directory. If the application's system property
	 * is set, it is used. Otherwise, we scan up the tree from this class for a
	 * suitable directory.
	 */
	default File getBaseDirectory() {
		return AppUtils.getBaseDirectory(getSystemProperty(), getClass(), null);
	}

	/**
	 * Displays information about the application. Typically this action
	 * takes the form as About dialog in the UI, and/or a message on the console.
	 */
	void about();

	/**
	 * Displays application preferences. The behavior is application-specific, but
	 * typically a preferences dialog will be shown onscreen.
	 */
	default void prefs() {
		// NB: Do nothing.
	}

	/**
	 * Quits the application. Typically this action will prompt the user about any
	 * unsaved work first.
	 */
	default void quit() {
		getContext().dispose();
	}

	// -- Versioned methods --

	/**
	 * Gets the version of the application.
	 * <p>
	 * SciJava conforms to the <a href="http://semver.org/">Semantic
	 * Versioning</a> specification.
	 * </p>
	 * 
	 * @return The application version, in {@code major.minor.micro} format.
	 */
	@Override
	default String getVersion() {
		// NB: We do not use VersionUtils.getVersion(c, groupId, artifactId)
		// because that method does not cache the parsed Manifest and/or POM.
		// We might have them already parsed here, and if not, we want to
		// parse then cache locally, rather than discarding them afterwards.

		// try the manifest first, since it might know its build number
		final Manifest m = getManifest();
		if (m != null) {
			final String v = m.getVersion();
			if (v != null) return v;
		}
		// try the POM
		final POM p = getPOM();
		if (p != null) {
			final String v = p.getVersion();
			if (v != null) return v;
		}
		return "Unknown";
	}
}
