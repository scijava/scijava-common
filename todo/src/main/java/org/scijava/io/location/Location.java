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

package org.scijava.io.location;

import java.net.URI;

import org.scijava.io.handle.DataHandle;

/**
 * A <em>location</em> is a data descriptor, such as a file on disk, a remote
 * URL, or a database connection.
 * <p>
 * Analogous to a <a
 * href="https://en.wikipedia.org/wiki/Uniform_resource_identifier">uniform
 * resource identifier</a> ({@link URI}), a location identifies <em>where</em>
 * the data resides, without necessarily specifying <em>how</em> to access that
 * data. The {@link DataHandle} interface defines a plugin that knows how to
 * read and/or write bytes for a particular kind of location.
 * </p>
 * 
 * @author Curtis Rueden
 * @author Gabriel Einsdorf
 */
public interface Location {

	/**
	 * Gets the location expressed as a {@link URI}, or null if the location
	 * cannot be expressed as such.
	 */
	default URI getURI() {
		return null;
	}

	/**
	 * Gets a (typically short) name expressing this location. This string is not
	 * intended to unambiguously identify the location, but rather act as a
	 * friendly, human-readable name. The precise behavior will depend on the
	 * implementation, but as an example, a file-based location could return the
	 * name of the associated file without its full path.
	 *
	 * @return The name, or an empty string if no name is available.
	 */
	default String getName() {
		return defaultName();
	}

	/**
	 * Gets the default name used when no explicit name is assigned.
	 * <p>
	 * Note: this is mostly intended for debugging, since most kinds of
	 * {@code Location} will assign some non-default name. But in cases where that
	 * does not occur, this value can be useful to detect the situation.
	 * </p>
	 * 
	 * @return The default name string.
	 */
	default String defaultName() {
		return "Location.defaultName";
	}
}
