/*
 * #%L
 * SciJava Common shared library for SciJava software.
 * %%
 * Copyright (C) 2009 - 2024 SciJava developers.
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

import org.scijava.module.ModuleItem;
import org.scijava.plugin.Plugin;
import org.scijava.plugin.WrapperPlugin;

/**
 * Interface for input widgets. An input widget is intended to harvest user
 * input for a particular {@link ModuleItem}. They are used by the
 * {@link InputHarvester} preprocessor to collect module input values.
 * <p>
 * Widgets discoverable at runtime must implement this interface and be
 * annotated with @{@link Plugin} with attribute {@link Plugin#type()} =
 * {@link InputWidget}.class. While it possible to create a widget merely by
 * implementing this interface, it is encouraged to instead extend
 * {@link AbstractInputWidget}, for convenience.
 * </p>
 * 
 * @author Curtis Rueden
 * @param <T> The input type of the widget.
 * @param <W> The type of UI component housing the widget.
 * @see Plugin
 * @see WidgetService
 * @see InputHarvester
 * @see InputPanel
 */
public interface InputWidget<T, W> extends WrapperPlugin<WidgetModel>,
	UIComponent<W>
{

	/** Updates the model to reflect the latest widget state. */
	void updateModel();

	/** Gets the current widget value. */
	T getValue();

	/** Refreshes the widget to reflect the latest model value(s). */
	void refreshWidget();

	/**
	 * Returns true iff the widget should be labeled with the parameter label.
	 * Most widgets are labeled this way, though some may not be; e.g.,
	 * {@link MessageWidget}s.
	 * 
	 * @see WidgetModel#getWidgetLabel()
	 */
	default boolean isLabeled() {
		return true;
	}

	/**
	 * Returns true iff the widget should be considered a read-only "message"
	 * rather than a bidirectional input widget. The
	 * {@link InputPanel#isMessageOnly()} method will return true iff this method
	 * returns true for all of its widgets.
	 */
	default boolean isMessage() {
		return false;
	}

	// NB: Javadoc overrides.

	// -- WrapperPlugin methods --

	/**
	 * Initializes the widget to use the given widget model. Once initialized, the
	 * widget's UI pane will be accessible via {@link #getComponent()}.
	 */
	@Override
	void set(WidgetModel model);

	/** Gets the model object backing this widget. */
	@Override
	WidgetModel get();

	// -- Typed methods --

	/** Gets whether this widget would be appropriate for the given model. */
	@Override
	default boolean supports(final WidgetModel model) {
		// check compatibility with the intended input panel
		return model.getPanel().supports(this);
	}

	@Override
	default Class<WidgetModel> getType() {
		return WidgetModel.class;
	}
}
