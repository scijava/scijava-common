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

package org.scijava.input;

import org.scijava.display.Display;
import org.scijava.display.event.input.MsButtonEvent;
import org.scijava.event.EventService;
import org.scijava.service.SciJavaService;

/**
 * Interface for service that tracks the current status of input devices
 * (keyboard and mouse in particular).
 * 
 * @author Barry DeZonia
 * @author Curtis Rueden
 */
public interface InputService extends SciJavaService {

	default EventService eventService() {
		return context().getService(EventService.class);
	}

	InputModifiers getModifiers();

	boolean isAltDown();

	boolean isAltGrDown();

	boolean isCtrlDown();

	boolean isMetaDown();

	boolean isShiftDown();

	boolean isKeyDown(KeyCode code);

	/**
	 * Gets the display associated with the last observed mouse cursor.
	 * 
	 * @return The display in question, or null if the display has been deleted,
	 *         or the mouse cursor is outside all known displays, or no mouse
	 *         events have ever been observed.
	 */
	Display<?> getDisplay();

	/**
	 * Gets the last observed X coordinate of the mouse cursor, relative to a
	 * specific display.
	 * 
	 * @see #getDisplay()
	 */
	int getX();

	/**
	 * Gets the last observed Y coordinate of the mouse cursor, relative to a
	 * specific display.
	 * 
	 * @see #getDisplay()
	 */
	int getY();

	/**
	 * Gets whether the given mouse button is currently pressed.
	 * 
	 * @param button One of:
	 *          <ul>
	 *          <li>{@link MsButtonEvent#LEFT_BUTTON}</li>
	 *          <li>{@link MsButtonEvent#MIDDLE_BUTTON}</li>
	 *          <li>{@link MsButtonEvent#RIGHT_BUTTON}</li>
	 *          </ul>
	 */
	boolean isButtonDown(int button);

	// -- Deprecated methods --

	/** @deprecated Use {@link #eventService()} instead. */
	@Deprecated
	default EventService getEventService() {
		return eventService();
	}
}
