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

package org.scijava.display;

import java.util.List;

import org.scijava.display.event.DisplayCreatedEvent;
import org.scijava.display.event.DisplayDeletedEvent;
import org.scijava.event.EventService;
import org.scijava.object.ObjectService;
import org.scijava.plugin.PluginInfo;
import org.scijava.plugin.PluginService;
import org.scijava.service.SciJavaService;

/**
 * Interface for service that tracks available {@link Display}s.
 * 
 * @author Barry DeZonia
 * @author Curtis Rueden
 * @author Grant Harris
 */
public interface DisplayService extends SciJavaService {

	default EventService eventService() {
		return context().getService(EventService.class);
	}

	default ObjectService objectService() {
		return context().getService(ObjectService.class);
	}

	default PluginService pluginService() {
		return context().getService(PluginService.class);
	}

	/** Gets the currently active display (of any Display type). */
	Display<?> getActiveDisplay();

	/** Gets the most recently active display (of the specified Display type). */
	<D extends Display<?>> D getActiveDisplay(Class<D> displayClass);

	/**
	 * Set the active display.
	 * 
	 * @param display
	 */
	void setActiveDisplay(Display<?> display);

	/** Gets the list of known display plugins. */
	List<PluginInfo<Display<?>>> getDisplayPlugins();

	/**
	 * Gets the display plugin of the given class, or null if none.
	 */
	<D extends Display<?>> PluginInfo<Display<?>> getDisplayPlugin(
		Class<D> pluginClass);

	/**
	 * Gets the display plugin of the given class name, or null if none.
	 * 
	 * @throws ClassCastException if the plugin found is not a display plugin.
	 */
	PluginInfo<Display<?>> getDisplayPlugin(String className);

	/**
	 * Gets the list of display plugins of the given type (e.g.,
	 * {@code ImageDisplay.class}).
	 */
	<DT extends Display<?>> List<PluginInfo<DT>> getDisplayPluginsOfType(
		Class<DT> type);

	/** Gets a list of all available displays. */
	List<Display<?>> getDisplays();

	/**
	 * Gets a list of all available displays of the given type (e.g.,
	 * {@code ImageDisplay.class}).
	 */
	<DT extends Display<?>> List<DT> getDisplaysOfType(Class<DT> type);

	/** Gets a display by its name. */
	Display<?> getDisplay(String name);

	/** Gets a list of displays currently visualizing the given object. */
	List<Display<?>> getDisplays(Object o);

	/**
	 * Checks whether the given name is already taken by an existing display.
	 * 
	 * @param name The name to check.
	 * @return true if the name is available, false if already taken.
	 */
	boolean isUniqueName(String name);

	/**
	 * Creates a display for the given object, publishing a
	 * {@link DisplayCreatedEvent} to notify interested parties. In particular:
	 * <ul>
	 * <li>Visible UIs will respond to this event by showing the display.</li>
	 * <li>The {@link ObjectService} will add the new display to its index, until
	 * a corresponding {@link DisplayDeletedEvent} is later published.</li>
	 * </ul>
	 * <p>
	 * To create a {@link Display} without publishing an event, see
	 * {@link #createDisplayQuietly}.
	 * </p>
	 * 
	 * @param o The object for which a display should be created. The object is
	 *          then added to the display.
	 * @return Newly created {@code Display<?>} containing the given object. The
	 *         Display is typed with ? rather than T matching the Object because
	 *         it is possible for the Display to be a collection of some other
	 *         sort of object than the one being added. For example, ImageDisplay
	 *         is a {@code Display<DataView>} with the DataView wrapping a
	 *         Dataset, yet the ImageDisplay supports adding Datasets directly,
	 *         taking care of wrapping them in a DataView as needed.
	 */
	Display<?> createDisplay(Object o);

	/**
	 * Creates a display for the given object, publishing a
	 * {@link DisplayCreatedEvent} to notify interested parties. In particular:
	 * <ul>
	 * <li>Visible UIs will respond to this event by showing the display.</li>
	 * <li>The {@link ObjectService} will add the new display to its index, until
	 * a corresponding {@link DisplayDeletedEvent} is later published.</li>
	 * </ul>
	 * <p>
	 * To create a {@link Display} without publishing an event, see
	 * {@link #createDisplayQuietly}.
	 * </p>
	 * 
	 * @param name The name to be assigned to the display.
	 * @param o The object for which a display should be created. The object is
	 *          then added to the display.
	 * @return Newly created {@code Display<?>} containing the given object. The
	 *         Display is typed with ? rather than T matching the Object because
	 *         it is possible for the Display to be a collection of some other
	 *         sort of object than the one being added. For example, ImageDisplay
	 *         is a {@code Display<DataView>} with the DataView wrapping a
	 *         Dataset, yet the ImageDisplay supports adding Datasets directly,
	 *         taking care of wrapping them in a DataView as needed.
	 */
	Display<?> createDisplay(String name, Object o);

	/**
	 * Creates a display for the given object, without publishing a
	 * {@link DisplayCreatedEvent}. Hence, the display will not be automatically
	 * shown or tracked.
	 * 
	 * @param o The object for which a display should be created. The object is
	 *          then added to the display.
	 * @return Newly created {@code Display<?>} containing the given object. The
	 *         Display is typed with ? rather than T matching the Object because
	 *         it is possible for the Display to be a collection of some other
	 *         sort of object than the one being added. For example, ImageDisplay
	 *         is a {@code Display<DataView>} with the DataView wrapping a
	 *         Dataset, yet the ImageDisplay supports adding Datasets directly,
	 *         taking care of wrapping them in a DataView as needed.
	 */
	Display<?> createDisplayQuietly(Object o);

	// -- Deprecated methods --

	/** @deprecated Use {@link #eventService()} instead. */
	@Deprecated
	default EventService getEventService() {
		return eventService();
	}

	/** @deprecated Use {@link #objectService()} instead. */
	@Deprecated
	default ObjectService getObjectService() {
		return objectService();
	}

	/** @deprecated Use {@link #pluginService()} instead. */
	@Deprecated
	default PluginService getPluginService() {
		return pluginService();
	}
}
