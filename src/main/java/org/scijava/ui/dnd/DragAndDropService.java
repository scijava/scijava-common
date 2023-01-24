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

package org.scijava.ui.dnd;

import java.util.List;

import org.scijava.display.Display;
import org.scijava.plugin.HandlerService;
import org.scijava.service.SciJavaService;

/**
 * Interface for service that handles drag and drop events.
 * 
 * @author Curtis Rueden
 */
public interface DragAndDropService extends
	HandlerService<Object, DragAndDropHandler<Object>>, SciJavaService
{

	/**
	 * Checks whether the given {@link DragAndDropData} can be dropped onto the
	 * specified display. A (data, display) pair is deemed compatible if a
	 * compatible handler exists for them.
	 * 
	 * @see DragAndDropHandler
	 */
	default boolean supports(final DragAndDropData data,
		final Display<?> display)
	{
		return getHandler(data, display) != null;
	}

	/**
	 * Checks whether the given object can be dropped onto the specified display.
	 * An (object, display) pair is deemed compatible if a compatible handler
	 * exists for them.
	 * 
	 * @see DragAndDropHandler
	 */
	default boolean supports(final Object object, final Display<?> display) {
		return getHandler(object, display) != null;
	}

	/**
	 * Performs a drag-and-drop operation in the given display with the specified
	 * {@link DragAndDropData}, using the first available compatible handler.
	 * 
	 * @see DragAndDropHandler
	 * @return true if the drop operation was successful
	 * @throws IllegalArgumentException if the display and/or data object are
	 *           unsupported, or are incompatible with one another.
	 */
	default boolean drop(final DragAndDropData data, final Display<?> display) {
		final DragAndDropHandler<?> handler = getHandler(data, display);
		if (handler == null) return false;
		return handler.dropData(data, display);
	}

	/**
	 * Performs a drag-and-drop operation in the given display with the specified
	 * data object, using the first available compatible handler.
	 * 
	 * @see DragAndDropHandler
	 * @return true if the drop operation was successful
	 * @throws IllegalArgumentException if the display and/or data object are
	 *           unsupported, or are incompatible with one another.
	 */
	default boolean drop(final Object data, final Display<?> display) {
		final DragAndDropHandler<?> handler = getHandler(data, display);
		if (handler == null) return false;
		return handler.dropObject(data, display);
	}

	/**
	 * Gets the drag-and-drop handler which will be used to handle the given
	 * {@link DragAndDropData} dragged onto the specified display.
	 * 
	 * @return The first compatible drag-and-drop handler, or null if none
	 *         available.
	 */
	default DragAndDropHandler<?> getHandler(final DragAndDropData data,
		final Display<?> display)
	{
		for (final DragAndDropHandler<?> handler : getInstances()) {
			if (handler.supportsData(data, display)) return handler;
		}
		return null;
	}

	/**
	 * Gets the drag-and-drop handler which will be used to handle the given
	 * object dragged onto the specified display.
	 * 
	 * @return The first compatible drag-and-drop handler, or null if none
	 *         available.
	 */
	default DragAndDropHandler<?> getHandler(final Object object,
		final Display<?> display)
	{
		for (final DragAndDropHandler<?> handler : getInstances()) {
			if (handler.supportsObject(object, display)) return handler;
		}
		return null;
	}

	// NB: Javadoc overrides.

	// -- SingletonService methods --

	/**
	 * Gets the list of available drag-and-drop handlers, which are used to
	 * perform drag-and-drop operations.
	 */
	@Override
	List<DragAndDropHandler<Object>> getInstances();

	// -- PTService methods --

	@Override
	@SuppressWarnings({"rawtypes", "unchecked"})
	default Class<DragAndDropHandler<Object>> getPluginType() {
		return (Class) DragAndDropHandler.class;
	}

	// -- Typed methods --

	@Override
	default Class<Object> getType() {
		return Object.class;
	}
}
