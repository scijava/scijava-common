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

package org.scijava.platform;

import java.io.IOException;
import java.net.URL;

import org.scijava.Disposable;
import org.scijava.plugin.Plugin;
import org.scijava.plugin.SingletonPlugin;

/**
 * An interface for configuring a specific deployment platform, defined by
 * criteria such as operating system, machine architecture or Java version.
 * <p>
 * Platforms discoverable at runtime must implement this interface and be
 * annotated with @{@link Plugin} with attribute {@link Plugin#type()} =
 * {@link Platform}.class. While it possible to create a platform merely by
 * implementing this interface, it is encouraged to instead extend
 * {@link AbstractPlatform}, for convenience.
 * </p>
 * 
 * @author Curtis Rueden
 * @see Plugin
 * @see PlatformService
 */
public interface Platform extends SingletonPlugin, Disposable {

	/** Java Runtime Environment vendor to match. */
	default String javaVendor() {
		return null;
	}

	/** Minimum required Java Runtime Environment version. */
	default String javaVersion() {
		return null;
	}

	/** Operating system architecture to match. */
	default String osArch() {
		return null;
	}

	/** Operating system name to match. */
	default String osName() {
		return null;
	}

	/** Minimum required operating system version. */
	default String osVersion() {
		return null;
	}

	/** Determines whether the given platform is applicable to this runtime. */
	default boolean isTarget() {
		if (javaVendor() != null) {
			final String javaVendor = System.getProperty("java.vendor");
			if (!javaVendor.matches(".*" + javaVendor() + ".*")) return false;
		}

		if (javaVersion() != null) {
			final String javaVersion = System.getProperty("java.version");
			if (javaVersion.compareTo(javaVersion()) < 0) return false;
		}

		if (osName() != null) {
			final String osName = System.getProperty("os.name");
			if (!osName.matches(".*" + osName() + ".*")) return false;
		}

		if (osArch() != null) {
			final String osArch = System.getProperty("os.arch");
			if (!osArch.matches(".*" + osArch() + ".*")) return false;
		}

		if (osVersion() != null) {
			final String osVersion = System.getProperty("os.version");
			if (osVersion.compareTo(osVersion()) < 0) return false;
		}

		return true;
	}

	/** Activates and configures the platform. */
	void configure(PlatformService service);

	void open(URL url) throws IOException;

	/**
	 * Informs the platform of a UI's newly created application menu structure.
	 * The platform may choose to do something platform-specific with the menus.
	 * 
	 * @param menus The UI's newly created menu structure
	 * @return true iff the menus should not be added to the UI as normal because
	 *         the platform did something platform-specific with them instead.
	 */
	default boolean registerAppMenus(final Object menus) {
		return false;
	}
}
