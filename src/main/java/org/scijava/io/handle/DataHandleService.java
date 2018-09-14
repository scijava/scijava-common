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

package org.scijava.io.handle;

import java.io.IOException;

import org.scijava.io.IOService;
import org.scijava.io.location.Location;
import org.scijava.plugin.WrapperService;
import org.scijava.service.SciJavaService;

/**
 * Interface for low-level data I/O: reading and writing bytes using
 * {@link DataHandle}s.
 * 
 * @author Curtis Rueden
 * @see IOService
 * @see Location
 */
public interface DataHandleService extends
	WrapperService<Location, DataHandle<Location>>, SciJavaService
{

	// -- PTService methods --

	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	default Class<DataHandle<Location>> getPluginType() {
		return (Class) DataHandle.class;
	}

	// -- Typed methods --

	@Override
	default Class<Location> getType() {
		return Location.class;
	}

	/**
	 * Convenience method to test whether it describes an existing file.
	 *
	 * @param location the location to test
	 * @return The result of {@link DataHandle#exists()} on a newly created handle
	 *         on this location. Also returns {@code false} if the handle can not
	 *         be created.
	 * @throws IOException if the creation of the handle fails exceptionally
	 */
	default boolean exists(final Location location) throws IOException {
		try (DataHandle<Location> handle = create(location)) {
			return handle == null ? false : handle.exists();
		}
	}

	/**
	 * Wraps the provided {@link DataHandle} in a read-only buffer for accelerated
	 * reading.
	 *
	 * @param handle the handle to wrap
	 * @return The handle wrapped in a read-only buffer, or {@code null} if the
	 *         input handle is {@code null}
	 * @see ReadBufferDataHandle#ReadBufferDataHandle(DataHandle)
	 */
	default DataHandle<Location> readBuffer(final DataHandle<Location> handle) {
		return handle == null ? null : new ReadBufferDataHandle(handle);
	}

	/**
	 * Creates a {@link DataHandle} on the provided {@link Location} wrapped in a
	 * read-only buffer for accelerated reading.
	 *
	 * @param location the Location to create a buffered handle on.
	 * @return A {@link DataHandle} on the provided location wrapped in a
	 *         read-only buffer, or {@code null} if no handle could be created for
	 *         the location.
	 * @see ReadBufferDataHandle#ReadBufferDataHandle(DataHandle)
	 */
	default DataHandle<Location> readBuffer(final Location location) {
		final DataHandle<Location> handle = create(location);
		return handle == null ? null : new ReadBufferDataHandle(handle);
	}

	/**
	 * Wraps the provided {@link DataHandle} in a write-only buffer for
	 * accelerated writing.
	 *
	 * @param handle the handle to wrap
	 * @return the handle wrapped in a write-only buffer or {@code null} if the
	 *         provided handle is {@code null}
	 * @see WriteBufferDataHandle#WriteBufferDataHandle(DataHandle)
	 */
	default DataHandle<Location> writeBuffer(final DataHandle<Location> handle) {
		return handle == null ? null : new WriteBufferDataHandle(handle);
	}
}
