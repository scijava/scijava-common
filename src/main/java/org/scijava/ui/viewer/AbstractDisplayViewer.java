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

package org.scijava.ui.viewer;

import org.scijava.display.Display;
import org.scijava.display.event.DisplayCreatedEvent;
import org.scijava.display.event.DisplayUpdatedEvent;
import org.scijava.event.EventHandler;
import org.scijava.plugin.AbstractRichPlugin;

/**
 * The AbstractDisplayViewer provides some basic generic implementations for a
 * DisplayViewer such as storing and providing the display, window and panel for
 * a DisplayViewer.
 * 
 * @author Lee Kamentsky
 * @author Curtis Rueden
 */
public abstract class AbstractDisplayViewer<T> extends AbstractRichPlugin
	implements DisplayViewer<T>
{

	private Display<T> display;
	private DisplayWindow window;
	private DisplayPanel panel;

	@Override
	public void view(final DisplayWindow w, final Display<?> d) {
		if (!canView(d)) {
			throw new IllegalArgumentException("Incompatible display: " + d);
		}
		@SuppressWarnings("unchecked")
		final Display<T> typedDisplay = (Display<T>) d;
		display = typedDisplay;
		window = w;
	}

	@Override
	public Display<T> getDisplay() {
		return display;
	}

	@Override
	public DisplayWindow getWindow() {
		return window;
	}

	@Override
	public void setPanel(final DisplayPanel panel) {
		this.panel = panel;
	}

	@Override
	public DisplayPanel getPanel() {
		return panel;
	}

	// -- Internal AbstractDisplayViewer methods --

	protected void updateTitle() {
		String name = getDisplay().getName();
		if (name == null) name = "";
		getWindow().setTitle(name);
	}

	// -- Event handlers --

	@EventHandler
	protected void onEvent(final DisplayCreatedEvent event) {
		if (event.getObject() != getDisplay()) return;
		updateTitle();
	}

	@EventHandler
	protected void onEvent(final DisplayUpdatedEvent event) {
		if (event.getDisplay() != getDisplay()) return;
		updateTitle();
	}

}
