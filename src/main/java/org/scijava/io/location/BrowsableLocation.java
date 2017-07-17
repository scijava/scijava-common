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

import java.io.IOException;
import java.util.Collections;
import java.util.Set;

/**
 * A {@link Location} that offers methods to browse other locations relative to
 * it.
 *
 * @author Gabriel Einsdorf
 * @author Curtis Rueden
 */
public interface BrowsableLocation extends Location {

	/**
	 * Obtains a location pointing to the parent directory of this one.
	 * 
	 * @return the parent location of this one, or <code>null</code> if this
	 *         location has no parent.
	 * @throws IOException if something goes wrong obtaining the parent.
	 */
	BrowsableLocation parent() throws IOException;

	/**
	 * Obtains a collection of locations for whom this location is the parent.
	 * Note that this will only succeed if calls to {@link #isDirectory()} on this
	 * location return <code>true</code>.
	 * 
	 * @return A set containing the children of this location, or
	 *         {@link Collections#EMPTY_SET} if this location has no children.
	 * @throws IOException if something goes wrong obtaining the children.
	 * @throws IllegalArgumentException if this location is not a directory (i.e.,
	 *           {@link #isDirectory()} returns false).
	 */
	Set<BrowsableLocation> children() throws IOException;

	/**
	 * Obtains a location relative to this one, which will be configured
	 * like the current location, but point to a the file specified by the
	 * <code>path</code> parameter.
	 *
	 * @param path the relative path of the desired location.
	 * @return A location that points to the specified file location.
	 * @throws IOException if something goes wrong obtaining the sibling
	 */
	BrowsableLocation sibling(String path) throws IOException;

	/**
	 * Tests whether this location is a <b>directory</b>, meaning that it can have
	 * children. It is recommended to use this method before calling
	 * {@link #child(String)} or {@link #children()}, to ensure those calls
	 * succeed.
	 * 
	 * @return True iff the location represents a directory.
	 */
	boolean isDirectory();

	/**
	 * Obtains a location with the given name, for whom this location is the
	 * parent. Note that this will only succeed if calls to {@link #isDirectory()}
	 * on this location return <code>true</code>.
	 *
	 * @param name the name of the child
	 * @return a location pointing to the child
	 * @throws IOException if something goes wrong obtaining the child.
	 * @throws IllegalArgumentException if this location is not a directory (i.e.,
	 *           {@link #isDirectory()} returns false).
	 */
	BrowsableLocation child(String name) throws IOException;
}
