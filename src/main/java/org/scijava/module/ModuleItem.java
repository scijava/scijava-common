/*
 * #%L
 * SciJava Common shared library for SciJava software.
 * %%
 * Copyright (C) 2009 - 2020 SciJava developers.
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

package org.scijava.module;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.List;

import org.scijava.BasicDetails;
import org.scijava.ItemIO;
import org.scijava.ItemVisibility;

/**
 * A ModuleItem represents metadata about one input or output of a module.
 * 
 * @author Aivar Grislis
 * @author Curtis Rueden
 */
public interface ModuleItem<T> extends BasicDetails {

	/** Gets the {@link ModuleInfo} to which this item belongs. */
	ModuleInfo getInfo();

	/** Gets the type of the item. */
	Class<T> getType();

	/**
	 * Gets the type of the item, including Java generic parameters.
	 * <p>
	 * For many modules, this may be the same {@link Class} object returned by
	 * {@link #getType()}, but some module inputs or outputs may be backed by a
	 * generic type such as {@code List<String>} or {@code Iterable<Integer>}.
	 * </p>
	 * 
	 * @see Field#getGenericType()
	 */
	Type getGenericType();

	/** Gets the input/output type of the item. */
	ItemIO getIOType();

	/** Gets whether the item is a module input. */
	boolean isInput();

	/** Gets whether the item is a module output. */
	boolean isOutput();

	/** Gets the visibility of the item. */
	ItemVisibility getVisibility();

	/** Gets whether the item value is allowed to be auto-filled. */
	boolean isAutoFill();

	/** Gets whether the item value must be specified (i.e., no default). */
	boolean isRequired();

	/** Gets whether to remember the most recent value of the parameter. */
	boolean isPersisted();

	/** Gets the key to use for saving the value persistently. */
	String getPersistKey();

	/**
	 * Loads the item's persisted value. This recalls the value last stored using
	 * {@link #saveValue(Object)}.
	 * <p>
	 * Note that this is different than obtaining a module instance's current
	 * value for the input; see {@link #getValue(Module)} for that.
	 * </p>
	 *
	 * @deprecated
	 * @see ModuleService#load(ModuleItem)
	 */
	@Deprecated
	T loadValue();

	/**
	 * Saves the given value to persistent storage. This allows later restoration
	 * of the value via {@link #loadValue()}, even from a different JVM.
	 *
	 * @deprecated
	 * @see ModuleService#save(ModuleItem, Object)
	 */
	@Deprecated
	void saveValue(T value);

	/** Gets the function that is called to initialize the item's value. */
	String getInitializer();

	/**
	 * Invokes this item's initializer function, if any, on the given module.
	 * 
	 * @see #getInitializer()
	 */
	void initialize(Module module) throws MethodCallException;

	/** Gets the function that is called to validate the item's value. */
	String getValidater();

	/**
	 * Invokes this item's validation function, if any, on the given module.
	 * 
	 * @see #getValidater()
	 */
	void validate(Module module) throws MethodCallException;

	/**
	 * Gets the function that is called whenever this item changes.
	 * <p>
	 * This mechanism enables interdependent items of various types. For example,
	 * two int parameters "width" and "height" could update each other when
	 * another boolean "Preserve aspect ratio" flag is set.
	 * </p>
	 */
	String getCallback();

	/**
	 * Invokes this item's callback function, if any, on the given module.
	 * 
	 * @see #getCallback()
	 */
	void callback(Module module) throws MethodCallException;

	/**
	 * Gets the preferred widget style to use when rendering the item in a user
	 * interface.
	 */
	String getWidgetStyle();
	
	/**
	 * Gets the name of the group the widget belongs to so it can be displayed within
	 * the group.
	 */
	String getWidgetGroup();
	
	/** Returns the state of the widget group, expanded and visible or not expanded and hidden. */
	boolean isExpanded();

	/** Gets the default value. */
	T getDefaultValue();

	/** Gets the minimum allowed value (if applicable). */
	T getMinimumValue();

	/** Gets the maximum allowed value (if applicable). */
	T getMaximumValue();

	/**
	 * Gets the "soft" minimum value (if applicable).
	 * <p>
	 * The soft minimum is a hint for use in bounded scenarios, such as rendering
	 * in a user interface with a slider or scroll bar widget; the parameter value
	 * will not actually be clamped to the soft bounds, but they may be used in
	 * certain circumstances where appropriate.
	 * </p>
	 */
	T getSoftMinimum();

	/**
	 * Gets the "soft" maximum value (if applicable).
	 * <p>
	 * The soft maximum is a hint for use in bounded scenarios, such as rendering
	 * in a user interface with a slider or scroll bar widget; the parameter value
	 * will not actually be clamped to the soft bounds, but they may be used in
	 * certain circumstances where appropriate.
	 * </p>
	 */
	T getSoftMaximum();

	/**
	 * Gets the preferred step size to use when rendering the item in a user
	 * interface (if applicable).
	 */
	Number getStepSize();

	/**
	 * Gets the preferred width of the input field in characters (if applicable).
	 */
	int getColumnCount();

	/** Gets the list of possible values. */
	List<T> getChoices();

	/** Gets the item's current value with respect to the given module. */
	T getValue(Module module);

	/** Sets the item's current value with respect to the given module. */
	void setValue(Module module, T value);

}
