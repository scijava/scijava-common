/*-
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

package org.scijava.download;

import java.io.IOException;

import org.scijava.io.handle.DataHandle;
import org.scijava.io.location.Location;

/**
 * An object which knows how to convert a slow (typically remote)
 * {@link Location} to a faster (typically local) one.
 *
 * @author Curtis Rueden
 */
public interface LocationCache {

	/** Gets whether the given location can be cached by this cache. */
	boolean canCache(Location source);

	/**
	 * Gets the cache location of a given data source.
	 *
	 * @return A {@link Location} where the source data is, or would be, cached.
	 * @throws IllegalArgumentException if the given source cannot be cached (see
	 *           {@link #canCache}).
	 */
	Location cachedLocation(Location source);

	/**
	 * Loads the checksum value which corresponds to the cached location.
	 *
	 * @param source The source location for which the cached checksum is desired.
	 * @return The loaded checksum, or null if one is not available.
	 * @see DataHandle#checksum()
	 * @throws IOException If something goes wrong accessing the checksum.
	 */
	String loadChecksum(Location source) throws IOException;

	/**
	 * Associates the given checksum value with the specified source location.
	 *
	 * @param source The source location for which the checksum should be cached.
	 * @param checksum The checksum value to cache.
	 * @see DataHandle#checksum()
	 * @throws IOException If something goes wrong caching the checksum.
	 */
	void saveChecksum(Location source, String checksum) throws IOException;
}
