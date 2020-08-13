/*
 * #%L
 * SciJava Common shared library for SciJava software.
 * %%
 * Copyright (C) 2009 - 2020 SciJava developers.
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

package org.scijava.io;

import java.io.IOException;

import org.scijava.io.location.FileLocation;
import org.scijava.io.location.Location;
import org.scijava.plugin.HandlerService;
import org.scijava.service.SciJavaService;

/**
 * Interface for high-level data I/O: opening and saving data of a specific type.
 *
 * @author Curtis Rueden
 * @author Deborah Schmidt
 */
public interface TypedIOService<D> extends HandlerService<Location, IOPlugin<D>>,
		SciJavaService
{

	/**
	 * Gets the most appropriate {@link IOPlugin} for opening data from the given
	 * location.
	 */
	default IOPlugin<D> getOpener(final String source) {
		return getOpener(new FileLocation(source));
	}

	/**
	 * Gets the most appropriate {@link IOPlugin} for opening data from the given
	 * location.
	 */
	default IOPlugin<D> getOpener(Location source) {
		for (final IOPlugin<D> handler : getInstances()) {
			if (handler.supportsOpen(source)) return handler;
		}
		return null;
	}

	/**
	 * Gets the most appropriate {@link IOPlugin} for saving data to the given
	 * location.
	 */
	default IOPlugin<D> getSaver(final D data, final String destination) {
		return getSaver(data, new FileLocation(destination));
	}

	/**
	 * Gets the most appropriate {@link IOPlugin} for saving data to the given
	 * location.
	 */
	default IOPlugin<D> getSaver(D data, Location destination) {
		for (final IOPlugin<?> handler : getInstances()) {
			if (handler.supportsSave(data, destination)) {
				return (IOPlugin<D>) handler;
			}
		}
		return null;
	}

	/**
	 * Loads data from the given source. For extensibility, the nature of the
	 * source is left intentionally general, but two common examples include file
	 * paths and URLs.
	 * <p>
	 * The opener to use is automatically determined based on available
	 * {@link IOPlugin}s; see {@link #getOpener(String)}.
	 * </p>
	 *
	 * @param source The source (e.g., file path) from which to data should be
	 *          loaded.
	 * @return An object representing the loaded data, or null if the source is
	 *         not supported.
	 * @throws IOException if something goes wrong loading the data.
	 */
	D open(String source) throws IOException;

	/**
	 * Loads data from the given location.
	 * <p>
	 * The opener to use is automatically determined based on available
	 * {@link IOPlugin}s; see {@link #getOpener(Location)}.
	 * </p>
	 *
	 * @param source The location from which to data should be loaded.
	 * @return An object representing the loaded data, or null if the source is
	 *         not supported.
	 * @throws IOException if something goes wrong loading the data.
	 */
	D open(Location source) throws IOException;

	/**
	 * Saves data to the given destination. The nature of the destination is left
	 * intentionally general, but the most common example is a file path.
	 * <p>
	 * The saver to use is automatically determined based on available
	 * {@link IOPlugin}s; see {@link #getSaver(Object, String)}.
	 * </p>
	 *
	 * @param data The data to be saved to the destination.
	 * @param destination The destination (e.g., file path) to which data should
	 *          be saved.
	 * @throws IOException if something goes wrong saving the data.
	 */
	void save(D data, String destination) throws IOException;

	/**
	 * Saves data to the given location.
	 * <p>
	 * The saver to use is automatically determined based on available
	 * {@link IOPlugin}s; see {@link #getSaver(Object, Location)}.
	 * </p>
	 *
	 * @param data The data to be saved to the destination.
	 * @param destination The destination location to which data should be saved.
	 * @throws IOException if something goes wrong saving the data.
	 */
	void save(D data, Location destination) throws IOException;

	boolean canOpen(String source);

	boolean canOpen(Location source);

	boolean canSave(D data, String destination);

	boolean canSave(D data, Location destination);

	// -- HandlerService methods --

	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	default Class<IOPlugin<D>> getPluginType() {
		return (Class) IOPlugin.class;
	}

	@Override
	default Class<Location> getType() {
		return Location.class;
	}
}
