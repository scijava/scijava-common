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

package org.scijava.ui.dnd.event;

import org.scijava.display.Display;
import org.scijava.display.event.input.InputEvent;
import org.scijava.input.InputModifiers;
import org.scijava.ui.dnd.DragAndDropData;

/**
 * An event indicating drag-and-drop activity in a display.
 * 
 * @author Curtis Rueden
 */
public abstract class DragAndDropEvent extends InputEvent {

	private final DragAndDropData data;

	public DragAndDropEvent(final Display<?> display) {
		this(display, null);
	}

	public DragAndDropEvent(final Display<?> display, final DragAndDropData data)
	{
		this(display, null, -1, -1, data);
	}

	public DragAndDropEvent(final Display<?> display,
		final InputModifiers modifiers, final int x, final int y,
		final DragAndDropData data)
	{
		super(display, modifiers, x, y);
		this.data = data;
	}

	// -- DragAndDropEvent methods --

	/** Gets the drag-and-drop data. */
	public DragAndDropData getData() {
		return data;
	}

	// -- Object methods --

	@Override
	public String toString() {
		return super.toString() + "\n\tdata = " + data;
	}

}
