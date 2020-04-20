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

package org.scijava.common.ui.viewer;

import org.scijava.common.Disposable;
import org.scijava.common.display.Display;
import org.scijava.common.display.event.DisplayActivatedEvent;
import org.scijava.common.display.event.DisplayDeletedEvent;
import org.scijava.common.display.event.DisplayUpdatedEvent;
import org.scijava.common.display.event.DisplayUpdatedEvent.DisplayUpdateLevel;
import org.scijava.common.plugin.Plugin;
import org.scijava.common.plugin.RichPlugin;
import org.scijava.common.ui.UserInterface;

/**
 * A display viewer is a UI widget that shows a display to a user.
 * <p>
 * Display viewers discoverable at runtime must implement this interface and be
 * annotated with @{@link Plugin} with attribute {@link Plugin#type()} =
 * {@link DisplayViewer}.class. While it possible to create a display viewer
 * merely by implementing this interface, it is encouraged to instead extend
 * {@link AbstractDisplayViewer}, for convenience.
 * </p>
 * 
 * @author Lee Kamentsky
 * @author Curtis Rueden
 * @see Plugin
 */
public interface DisplayViewer<T> extends RichPlugin, Disposable {

	/** Returns true if this display viewer can be used with the given UI. */
	boolean isCompatible(final UserInterface ui);

	/**
	 * Returns true if an instance of this display viewer can view the given
	 * display.
	 */
	boolean canView(Display<?> d);

	/**
	 * Begins viewing the given display.
	 * <p>
	 * The default behavior of this method is to ask the given
	 * {@link UserInterface} to create a {@link DisplayWindow} via
	 * {@link UserInterface#createDisplayWindow(Display)} and then pass it to
	 * {@link #view(DisplayWindow, Display)}. Viewers needing to customize details
	 * of the {@link DisplayWindow} creation can do so via this method.
	 * </p>
	 * 
	 * @param ui The user interface with which the viewer will be associated.
	 * @param d the model for the display to show.
	 */
	default void view(final UserInterface ui, final Display<?> d) {
		final DisplayWindow w = ui.createDisplayWindow(d);
		w.setTitle(d.getName());
		view(w, d);
		w.showDisplay(true);
		d.update();
	}

	/**
	 * Begins viewing the given display.
	 * 
	 * @param w The frame / window that will contain the GUI elements
	 * @param d the model for the display to show.
	 */
	void view(DisplayWindow w, Display<?> d);

	/** Gets the display being viewed. */
	Display<T> getDisplay();

	/** Gets the window in which the view is displayed. */
	DisplayWindow getWindow();

	/**
	 * Installs the display panel.
	 * 
	 * @param panel the panel used to host the gui
	 */
	void setPanel(DisplayPanel panel);

	/** Gets the display panel that hosts the gui elements. */
	DisplayPanel getPanel();

	/** Synchronizes the user interface appearance with the display model. */
	default void onDisplayUpdatedEvent(final DisplayUpdatedEvent e) {
		if (getPanel() == null) return;
		if (e.getLevel() == DisplayUpdateLevel.REBUILD) {
			getPanel().redoLayout();
		}
		getPanel().redraw();
	}

	/** Removes the user interface when the display is deleted. */
	@SuppressWarnings("unused")
	default void onDisplayDeletedEvent(final DisplayDeletedEvent e) {
		if (getPanel() == null || getPanel().getWindow() == null) return;
		getPanel().getWindow().close();
	}

	/**
	 * Handles a display activated event directed at this viewer's display. Note
	 * that the event's display may not be the viewer's display, but the active
	 * display will always be the viewer's display.
	 */
	@SuppressWarnings("unused")
	default void onDisplayActivatedEvent(final DisplayActivatedEvent e) {
		if (getPanel() == null || getPanel().getWindow() == null) return;
		getPanel().getWindow().requestFocus();
	}

	// -- Disposable methods --

	@Override
	default void dispose() {
		final DisplayWindow w = getWindow();
		if (w != null) w.close();
	}
}
