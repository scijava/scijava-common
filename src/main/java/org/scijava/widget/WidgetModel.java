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

package org.scijava.widget;

import java.util.List;

import org.scijava.Contextual;
import org.scijava.ItemVisibility;
import org.scijava.module.Module;
import org.scijava.module.ModuleItem;

/**
 * The backing data model for a particular {@link InputWidget}.
 * 
 * @author Curtis Rueden
 */
public interface WidgetModel extends Contextual {

	/** Gets the input panel intended to house the widget. */
	InputPanel<?, ?> getPanel();

	/** Gets the module's associated module instance. */
	Module getModule();

	/** Gets the module input's associated item descriptor. */
	ModuleItem<?> getItem();

	/**
	 * Gets the available objects for use with the widget. For example,
	 * {@link ObjectWidget}s typically display a dropdown combo box providing
	 * multiple choice selection between these objects.
	 * <p>
	 * Note that this list does not represent a constraint in allowed widget
	 * values, but rather provides a list of possibilities in cases where the
	 * realm of values is not defined by the type in some other way.
	 * </p>
	 * 
	 * @see ObjectWidget
	 */
	List<?> getObjectPool();

	/**
	 * Gets the text to use when labeling this widget. The linked item's label
	 * will be given if available (i.e., {@link ModuleItem#getLabel()}).
	 * Otherwise, a capitalized version of the item's name is given (i.e.,
	 * {@link ModuleItem#getName()}).
	 */
	String getWidgetLabel();

	/**
	 * Gets whether the widget is the given style. A widget may have multiple
	 * styles separated by commas, so this method is more correct than using
	 * {@code style.equals(getItem().getWidgetStyle())}.
	 */
	boolean isStyle(String style);

	/**
	 * Gets the current value of the module input.
	 * <p>
	 * In the case of inputs with a limited set of choices (i.e.,
	 * {@link ChoiceWidget}s and {@link ObjectWidget}s), this method ensures the
	 * value is in the set; if not, it returns the first item of the set.
	 * </p>
	 */
	Object getValue();

	/** Sets the current value of the module input. */
	void setValue(Object value);

	/** Executes the callback associated with this widget's associated input. */
	void callback();

	/**
	 * Gets the minimum value for the module input.
	 * 
	 * @return The minimum value, or null if the type is unbounded.
	 */
	Number getMin();

	/**
	 * Gets the maximum value for the module input.
	 * 
	 * @return The maximum value, or null if the type is unbounded.
	 */
	Number getMax();

	/**
	 * Gets the "soft" minimum value for the module input.
	 * 
	 * @return The "soft" minimum value, or {@link #getMin()} if none.
	 * @see ModuleItem#getSoftMinimum()
	 */
	Number getSoftMin();

	/**
	 * Gets the "soft" maximum value for the module input.
	 * 
	 * @return The "soft" maximum value, or {@link #getMax()} if none.
	 * @see ModuleItem#getSoftMaximum()
	 */
	Number getSoftMax();

	/**
	 * Gets the step size between values for the module input.
	 * 
	 * @return The step size, or 1 by default.
	 */
	Number getStepSize();

	/**
	 * Gets the multiple choice list for the module input.
	 * 
	 * @return The available choices, or an empty list if not multiple choice.
	 * @see ChoiceWidget
	 */
	String[] getChoices();

	/**
	 * Gets the input's value rendered as a string.
	 * 
	 * @return String representation of the input value, or the empty string if
	 *         the value is null or the null character ('\0').
	 */
	String getText();

	/**
	 * Gets whether the input is a message.
	 * 
	 * @see ItemVisibility#MESSAGE
	 */
	boolean isMessage();

	/**
	 * Gets whether the input is a text type (i.e., {@link String},
	 * {@link Character} or {@code char}.
	 */
	boolean isText();

	/**
	 * Gets whether the input is a character type (i.e., {@link Character} or
	 * {@code char}).
	 */
	boolean isCharacter();

	/**
	 * Gets whether the input is a number type (e.g., {@code int}, {@code float}
	 * or any {@link Number} implementation.
	 */
	boolean isNumber();

	/**
	 * Gets whether the input is a boolean type (i.e., {@link Boolean} or
	 * {@code boolean}).
	 */
	boolean isBoolean();

	/** Gets whether the input provides a restricted set of choices. */
	boolean isMultipleChoice();

	/** Gets whether the input is compatible with the given type. */
	boolean isType(Class<?> type);

	/**
	 * Toggles the widget's initialization state. An initialized widget can be
	 * assumed to be an active part of a container {@link InputPanel}.
	 */
	void setInitialized(boolean initialized);

	/**
	 * Gets the widget's initialization state. An initialized widget can be
	 * assumed to be an active part of a container {@link InputPanel}.
	 */
	boolean isInitialized();

}
