/*
 * #%L
 * SciJava Common shared library for SciJava software.
 * %%
 * Copyright (C) 2009 - 2024 SciJava developers.
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

import java.util.ArrayList;
import java.util.List;

import org.scijava.object.ObjectService;

/**
 * A service for managing {@link SingletonPlugin}s of a particular type. The
 * {@code SingletonService} creates and maintain a list of singleton instances.
 * <p>
 * Note that like {@link PTService}, {@link TypedService} and
 * {@link WrapperService}, {@code SingletonService} is not a service interface
 * defining API for a specific concrete service implementation, but rather a
 * more general layer in a type hierarchy intended to ease creation of services
 * that fit its pattern.
 * </p>
 * 
 * @author Curtis Rueden
 * @param <PT> Plugin type of the plugins being managed.
 * @see PTService
 */
public interface SingletonService<PT extends SingletonPlugin> extends
	PTService<PT>
{

	/** Gets the {@link ObjectService} upon which this service depends. */
	default ObjectService objectService() {
		return context().getService(ObjectService.class);
	}

	/**
	 * Gets the list of plugin instances. There will be one singleton instance for
	 * each available plugin.
	 */
	List<PT> getInstances();

	/** Gets the singleton plugin instance of the given class. */
	<P extends PT> P getInstance(Class<P> pluginClass);

	/**
	 * Filters the given list of instances by this service's inclusion criteria.
	 * 
	 * @param list the initial list of instances
	 * @return the filtered list of instances
	 */
	default List<? extends PT> filterInstances(final List<PT> list) {
		return list;
	}

	// -- PTService methods --

	@Override
	default <P extends PT> P create(final Class<P> pluginClass) {
		throw new UnsupportedOperationException(
			"Cannot create singleton plugin instance. "
				+ "Use getInstance(Class) instead.");
	}

	// -- Service methods --

	@Override
	default void initialize() {
		// add singleton instances to the object index... IN THE FUTURE!
		objectService().getIndex().addLater(() -> new ArrayList<>(getInstances()));
	}

}
