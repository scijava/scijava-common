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

package org.scijava.widget;

import org.scijava.plugin.AbstractWrapperPlugin;

/**
 * Base class for input widgets.
 * 
 * @author Curtis Rueden
 * @param <T> The input type of the widget.
 * @param <W> The type of UI component housing the widget.
 */
public abstract class AbstractInputWidget<T, W> extends
	AbstractWrapperPlugin<WidgetModel> implements InputWidget<T, W>
{

	private WidgetModel widgetModel;

	// -- InputWidget methods --

	@Override
	public void updateModel() {
		widgetModel.setValue(getValue());
	}

	// -- WrapperPlugin methods --

	@Override
	public void set(final WidgetModel model) {
		if (widgetModel != null) {
			throw new IllegalStateException("Widget already initialized");
		}
		widgetModel = model;
	}

	@Override
	public WidgetModel get() {
		return widgetModel;
	}

	// -- Typed methods --

	@Override
	public boolean supports(final WidgetModel data) {
		return InputWidget.super.supports(data);
	}
}
