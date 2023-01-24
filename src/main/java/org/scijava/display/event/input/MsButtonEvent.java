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

package org.scijava.display.event.input;

import org.scijava.display.Display;
import org.scijava.input.InputModifiers;

/**
 * An event indicating mouse button activity in a display.
 * 
 * @author Curtis Rueden
 * @author Grant Harris
 */
public abstract class MsButtonEvent extends MsEvent {

	// TODO - Use an enum for the buttons. Perhaps an extensible enum (see Axis &
	// Axes)? In scijava-ui-awt, add a mechanism for mapping AWT mouse buttons to
	// and from SciJava mouse buttons (similar to AWTCursors).

	public static final int LEFT_BUTTON = 0;
	public static final int MIDDLE_BUTTON = 1;
	public static final int RIGHT_BUTTON = 2;

	private final int button;
	private final int numClicks;
	private final boolean isPopupTrigger;

	public MsButtonEvent(final Display<?> display,
		final InputModifiers modifiers, final int x, final int y,
		final int button, final int numClicks, final boolean isPopupTrigger)
	{
		super(display, modifiers, x, y);
		this.button = button;
		this.numClicks = numClicks;
		this.isPopupTrigger = isPopupTrigger;
	}

	public int getButton() {
		return button;
	}

	public int getNumClicks() {
		return numClicks;
	}

	public boolean isPopupTrigger() {
		return isPopupTrigger;
	}

	// -- Object methods --

	@Override
	public String toString() {
		return super.toString() + "\n\tbutton = " + button + "\n\tnumClicks = " +
			numClicks + "\n\tisPopupTrigger = " + isPopupTrigger;
	}

}
