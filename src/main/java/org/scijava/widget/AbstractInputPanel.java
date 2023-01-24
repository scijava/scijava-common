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

package org.scijava.widget;

import java.util.HashMap;
import java.util.Map;

/**
 * Abstract superclass of UI-specific {@link InputPanel} implementations.
 * 
 * @author Curtis Rueden
 * @param <P> The type of UI component housing the input panel itself.
 * @param <W> The type of UI component housing each input widget.
 */
public abstract class AbstractInputPanel<P, W> implements InputPanel<P, W> {

	/** Table of widgets. */
	protected Map<String, InputWidget<?, W>> widgets =
		new HashMap<>();

	// -- InputPanel methods --

	@Override
	public void addWidget(final InputWidget<?, W> widget) {
		widgets.put(widget.get().getItem().getName(), widget);
	}
	
	@Override
	public InputWidget<?, W> getWidget(final String name) {
		return widgets.get(name);
	}

	@Override
	public Object getValue(final String name) {
		return widgets.get(name).getValue();
	}

	@Override
	public int getWidgetCount() {
		return widgets.size();
	}

	@Override
	public boolean hasWidgets() {
		return widgets.size() > 0;
	}

	@Override
	public boolean isMessageOnly() {
		int messageCount = 0;
		for (final InputWidget<?, W> widget : widgets.values()) {
			if (widget.isMessage()) messageCount++;
		}
		return messageCount == getWidgetCount();
	}

	@Override
	public void refresh() {
		for (final InputWidget<?, W> w : widgets.values()) {
			w.refreshWidget();
		}
	}

}
