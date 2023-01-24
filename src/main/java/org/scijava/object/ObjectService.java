/*
 * #%L
 * SciJava Common shared library for SciJava software.
 * %%
 * Copyright (C) 2009 - 2023 SciJava developers.
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

package org.scijava.object;

import java.util.List;

import org.scijava.Named;
import org.scijava.event.EventService;
import org.scijava.object.event.ObjectsAddedEvent;
import org.scijava.object.event.ObjectsRemovedEvent;
import org.scijava.service.SciJavaService;

/**
 * Interface for object management service.
 * 
 * @author Curtis Rueden
 */
public interface ObjectService extends SciJavaService {

	default EventService eventService() {
		return context().getService(EventService.class);
	}

	/** Gets the index of available objects. */
	NamedObjectIndex<Object> getIndex();

	/** Gets a list of all registered objects compatible with the given type. */
	default <T> List<T> getObjects(final Class<T> type) {
		final List<Object> list = getIndex().get(type);
		@SuppressWarnings("unchecked")
		final List<T> result = (List<T>) list;
		return result;
	}

	/**
	 * Gets the name belonging to a given object.
	 * <p>
	 * If no explicit name was provided at registration time, the name will be
	 * derived from {@link Named#getName()} if the object implements
	 * {@link Named}, or from the {@link Object#toString()} otherwise. It is
	 * guaranteed that this method will not return {@code null}.
	 * </p>
	 **/
	default String getName(final Object obj) {
		if (obj == null) throw new NullPointerException();
		final String name = getIndex().getName(obj);
		if (name != null) return name;
		if (obj instanceof Named) {
			final String n = ((Named) obj).getName();
			if (n != null) return n;
		}
		final String s = obj.toString();
		if (s != null) return s;
		return obj.getClass().getName() + "@" + Integer.toHexString(obj.hashCode());
	}

	/** Registers an object with the object service. */
	default void addObject(Object obj) {
		addObject(obj, null);
	}

	/** Registers a named object with the object service. */
	default void addObject(final Object obj, final String name) {
		getIndex().add(obj, name);
		eventService().publish(new ObjectsAddedEvent(obj));
	}

	/** Deregisters an object with the object service. */
	default void removeObject(final Object obj) {
		getIndex().remove(obj);
		eventService().publish(new ObjectsRemovedEvent(obj));
	}

	// -- Deprecated methods --

	/** @deprecated Use {@link #eventService()} instead. */
	@Deprecated
	default EventService getEventService() {
		return eventService();
	}
}
