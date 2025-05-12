/*
 * #%L
 * SciJava Common shared library for SciJava software.
 * %%
 * Copyright (C) 2009 - 2025 SciJava developers.
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

package org.scijava.service;

import java.util.List;

import org.scijava.object.SortedObjectIndex;

/**
 * Data structure for tracking registered services.
 * 
 * @author Curtis Rueden
 */
public class ServiceIndex extends SortedObjectIndex<Service> {

	public ServiceIndex() {
		super(Service.class);
	}

	// -- ServiceIndex methods --

	/** Gets the first available service compatible with the given class. */
	public <S extends Service> S getService(final Class<S> c) {
		return getService(c, null, 0);
	}

	/**
	 * Gets the highest priority service compatible with class {@code c}, which is
	 * lower priority than the {@code ref} service.
	 */
	public <S extends Service> S getNextService(final Class<S> c,
		final Class<? extends S> ref)
	{
		return getService(c, ref, 1);
	}

	/**
	 * Gets the lowest priority service compatible with class {@code c}, which is
	 * higher priority than the {@code ref} service.
	 */
	public <S extends Service> S getPrevService(final Class<S> c,
		final Class<? extends S> ref)
	{
		return getService(c, ref, -1);
	}

	// -- Helper methods --

	/**
	 * Gets the service compatible with the base class {@code c}, with the
	 * modified priority relative to the given reference service, or the highest
	 * priority service if no reference is given.
	 *
	 * @param c - Base class to find compatible services
	 * @param ref - Reference service class for priority comparisons. If null,
	 *          highest priority service is returned.
	 * @param priorityMod - Relative priority value. If {@code ref} is non-null,
	 *          this value is applied to the {@code ref} service's index in the
	 *          list of compatible services to determine the returned service. For
	 *          example, a value of +1 will return the next lowest priority
	 *          service, and a value of -1 will return the next highest priority
	 *          service - as the services are sorted from highest to lowest
	 *          priority.
	 * @return Service matching the given criteria, or null if no applicable
	 *         service.
	 */
	private <S extends Service> S getService(final Class<S> c,
		final Class<? extends S> ref, final int priorityMod)
	{
		final List<Service> list = get(c);
		if (list.isEmpty()) return null;

		int index = 0;

		if (ref != null) {
			// find the ref class's index
			for (; index < list.size() && !list.get(index).getClass().equals(ref); index++)
			{}

			// ref class wasn't on the list
			if (index == list.size()) return null;

			// Update the index for the desired relative priority
			index += priorityMod;
		}

		// If the priorityMod took the index outside the list bounds, then there is
		// no appropriate
		// service to return.
		if (index < 0 || index >= list.size()) return null;

		@SuppressWarnings("unchecked")
		final S service = (S) list.get(index);

		return service;
	}

}
