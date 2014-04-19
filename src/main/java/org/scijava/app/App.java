/*
 * #%L
 * SciJava Common shared library for SciJava software.
 * %%
 * Copyright (C) 2009 - 2014 Board of Regents of the University of
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

import org.scijava.plugin.Plugin;
import org.scijava.plugin.RichPlugin;
import org.scijava.plugin.SingletonPlugin;
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
	String getTitle();

	/**
	 * Gets the version of the application.
	 * <p>
	 * SciJava conforms to the <a href="http://semver.org/">Semantic
	 * Versioning</a> specification.
	 * </p>
	 * 
	 * @return The application version, in {@code major.minor.micro} format.
	 */
	String getVersion();

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
	String getInfo(boolean mem);

}
