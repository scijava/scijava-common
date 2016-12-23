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

package org.scijava.tool;

import org.scijava.display.event.input.KyPressedEvent;
import org.scijava.display.event.input.KyReleasedEvent;
import org.scijava.display.event.input.MsClickedEvent;
import org.scijava.display.event.input.MsDraggedEvent;
import org.scijava.display.event.input.MsMovedEvent;
import org.scijava.display.event.input.MsPressedEvent;
import org.scijava.display.event.input.MsReleasedEvent;
import org.scijava.display.event.input.MsWheelEvent;
import org.scijava.input.MouseCursor;
import org.scijava.plugin.Plugin;
import org.scijava.plugin.RichPlugin;
import org.scijava.plugin.SingletonPlugin;

/**
 * Interface for tools. A tool is a collection of rules binding user input
 * (e.g., keyboard and mouse events) to display and data manipulation in a
 * coherent way.
 * <p>
 * For example, a {@code PanTool} might pan a display when the mouse is dragged
 * or arrow key is pressed, while a {@code PencilTool} could draw hard lines on
 * the data within a display.
 * </p>
 * <p>
 * Tools discoverable at runtime must implement this interface and be annotated
 * with @{@link Plugin} with {@link Plugin#type()} = {@link Tool}.class. While
 * it possible to create a tool merely by implementing this interface, it is
 * encouraged to instead extend {@link AbstractTool}, for convenience.
 * </p>
 * 
 * @author Rick Lentz
 * @author Grant Harris
 * @author Curtis Rueden
 * @see Plugin
 * @see ToolService
 */
public interface Tool extends RichPlugin, SingletonPlugin {

	/** When true, tool has no button but rather is active all the time. */
	default boolean isAlwaysActive() {
		return false;
	}

	/**
	 * When true, tool receives events when the main application frame is active.
	 * When false, tool only receives events when a display window is active.
	 */
	default boolean isActiveInAppFrame() {
		return false;
	}

	/** The tool's mouse pointer. */
	default MouseCursor getCursor() {
		return MouseCursor.DEFAULT;
	}

	/** Informs the tool that it is now active. */
	default void activate() {
		// do nothing by default
	}

	/** Informs the tool that it is no longer active. */
	default void deactivate() {
		// do nothing by default
	}

	/** Occurs when a key on the keyboard is pressed while the tool is active. */
	@SuppressWarnings("unused")
	default void onKeyDown(final KyPressedEvent evt) {
		// do nothing by default
	}

	/** Occurs when a key on the keyboard is released while the tool is active. */
	@SuppressWarnings("unused")
	default void onKeyUp(final KyReleasedEvent evt) {
		// do nothing by default
	}

	/** Occurs when a mouse button is pressed while the tool is active. */
	@SuppressWarnings("unused")
	default void onMouseDown(final MsPressedEvent evt) {
		// do nothing by default
	}

	/** Occurs when a mouse button is released while the tool is active. */
	@SuppressWarnings("unused")
	default void onMouseUp(final MsReleasedEvent evt) {
		// do nothing by default
	}

	/** Occurs when a mouse button is double clicked while the tool is active. */
	@SuppressWarnings("unused")
	default void onMouseClick(final MsClickedEvent evt) {
		// do nothing by default
	}

	/** Occurs when the mouse is moved while the tool is active. */
	@SuppressWarnings("unused")
	default void onMouseMove(final MsMovedEvent evt) {
		// do nothing by default
	}

	/** Occurs when the mouse is dragged while the tool is active. */
	@SuppressWarnings("unused")
	default void onMouseDrag(final MsDraggedEvent evt) {
		// do nothing by default
	}

	/** Occurs when the mouse wheel is moved while the tool is active. */
	@SuppressWarnings("unused")
	default void onMouseWheel(final MsWheelEvent evt) {
		// do nothing by default
	}

	/** Occurs when the user right clicks this tool's icon. */
	default void configure() {
		// do nothing by default
	}

	/** Returns the text the tool provides when mouse hovers over tool */
	default String getDescription() {
		return getInfo().getDescription();
	}
}
