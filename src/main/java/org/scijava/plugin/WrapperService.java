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

package org.scijava.plugin;

/**
 * A service for managing {@link WrapperPlugin}s of a particular type. A
 * {@link WrapperPlugin} is a stateful {@link TypedPlugin} which wraps a
 * particular object of its associated data type. For any given data object, the
 * service is capable of wrapping it with the most appropriate wrapper by
 * sequentially querying each {@link WrapperPlugin} on its list for
 * compatibility.
 * <p>
 * Note that like {@link PTService}, {@link SingletonService} and
 * {@link TypedService}, {@code WrapperService} is not a service interface
 * defining API for a specific concrete service implementation, but rather a
 * more general layer in a type hierarchy intended to ease creation of services
 * that fit its pattern.
 * </p>
 * 
 * @author Curtis Rueden
 * @param <DT> Base data type
 * @param <PT> Plugin type
 * @see WrapperPlugin
 * @see PTService
 */
public interface WrapperService<DT, PT extends WrapperPlugin<DT>> extends
	TypedService<DT, PT>
{

	/**
	 * Creates a new plugin instance wrapping the given associated data object.
	 * 
	 * @return An appropriate plugin instance, or null if the data is not
	 *         compatible with any available plugin.
	 */
	default <D extends DT> PT create(final D data) {
		final PT instance = find(data);
		if (instance != null) instance.set(data);
		return instance;
	}

	// -- Service methods --

	@Override
	default void initialize() {
		if (log() != null) {
			log().debug("Found " + getPlugins().size() + " " +
				getPluginType().getSimpleName() + " plugins.");
		}
	}

	// -- Typed methods --

	@Override
	default boolean supports(final DT data) {
		return find(data) != null;
	}
}
