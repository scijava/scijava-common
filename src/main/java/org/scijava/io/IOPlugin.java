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

package org.scijava.io;

import java.io.IOException;

import org.scijava.plugin.HandlerPlugin;
import org.scijava.plugin.Plugin;

/**
 * A plugin which extends an application's I/O capabilities.
 * <p>
 * I/O plugins discoverable at runtime must implement this interface and be
 * annotated with @{@link Plugin} with attribute {@link Plugin#type()} =
 * {@link IOPlugin}.class. While it possible to create an I/O plugin merely by
 * implementing this interface, it is encouraged to instead extend
 * {@link AbstractIOPlugin}, for convenience.
 * </p>
 * 
 * @author Curtis Rueden
 * @see Plugin
 * @see IOService
 */
public interface IOPlugin<D> extends HandlerPlugin<String> {

	/** The type of data opened and/or saved by the plugin. */
	Class<D> getDataType();

	/** Checks whether the I/O plugin can open data from the given source. */
	@SuppressWarnings("unused")
	default boolean supportsOpen(final String source) {
		return false;
	}

	/** Checks whether the I/O plugin can save data to the given destination. */
	@SuppressWarnings("unused")
	default boolean supportsSave(final String destination) {
		return false;
	}

	/**
	 * Checks whether the I/O plugin can save the given data to the specified
	 * destination.
	 */
	default boolean supportsSave(final Object data, final String destination) {
		return supportsSave(destination) && getDataType().isInstance(data);
	}

	/** Opens data from the given source. */
	@SuppressWarnings("unused")
	default D open(final String source) throws IOException {
		throw new UnsupportedOperationException();
	}

	/** Saves the given data to the specified destination. */
	@SuppressWarnings("unused")
	default void save(final D data, final String destination) throws IOException {
		throw new UnsupportedOperationException();
	}

	// -- Typed methods --

	@Override
	default boolean supports(final String descriptor) {
		return supportsOpen(descriptor) || supportsSave(descriptor);
	}

	@Override
	default Class<String> getType() {
		return String.class;
	}
}
