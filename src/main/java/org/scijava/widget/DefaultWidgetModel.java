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

package org.scijava.widget;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.WeakHashMap;

import org.scijava.AbstractContextual;
import org.scijava.Context;
import org.scijava.ItemVisibility;
import org.scijava.convert.ConvertService;
import org.scijava.log.LogService;
import org.scijava.module.MethodCallException;
import org.scijava.module.Module;
import org.scijava.module.ModuleItem;
import org.scijava.module.ModuleService;
import org.scijava.plugin.Parameter;
import org.scijava.thread.ThreadService;
import org.scijava.util.ClassUtils;
import org.scijava.util.ConversionUtils;
import org.scijava.util.NumberUtils;

/**
 * The backing data model for a particular {@link InputWidget}.
 * 
 * @author Curtis Rueden
 */
public class DefaultWidgetModel extends AbstractContextual implements WidgetModel {

	private final InputPanel<?, ?> inputPanel;
	private final Module module;
	private final ModuleItem<?> item;
	private final List<?> objectPool;
	private final Map<Object, Object> convertedObjects;

	@Parameter
	private ThreadService threadService;

	@Parameter
	private ConvertService convertService;

	@Parameter
	private ModuleService moduleService;

	@Parameter(required = false)
	private LogService log;

	private boolean initialized;

	public DefaultWidgetModel(final Context context, final InputPanel<?, ?> inputPanel,
		final Module module, final ModuleItem<?> item, final List<?> objectPool)
	{
		setContext(context);
		this.inputPanel = inputPanel;
		this.module = module;
		this.item = item;
		this.objectPool = objectPool;
		convertedObjects = new WeakHashMap<>();

		if (item.getValue(module) == null) {
			// assign the item's default value as the current value
			setValue(moduleService.getDefaultValue(item));
		}
	}

	@Override
	public InputPanel<?, ?> getPanel() {
		return inputPanel;
	}

	@Override
	public Module getModule() {
		return module;
	}

	@Override
	public ModuleItem<?> getItem() {
		return item;
	}

	@Override
	public List<?> getObjectPool() {
		return objectPool;
	}

	@Override
	public String getWidgetLabel() {
		// Do this dynamically. Don't cache this result.
		// Some controls change their labels at runtime.
		final String label = item.getLabel();
		if (label != null && !label.isEmpty()) return label;

		final String name = item.getName();
		return name.substring(0, 1).toUpperCase() + name.substring(1);
	}

	@Override
	public boolean isStyle(final String style) {
		final String widgetStyle = getItem().getWidgetStyle();
		if (widgetStyle == null) return style == null;
		for (final String s : widgetStyle.split(",")) {
			if (s.equals(style)) return true;
		}
		return false;
	}

	@Override
	public Object getValue() {
		final Object value = item.getValue(module);

		if (isMultipleChoice()) return ensureValidChoice(value);
		if (getObjectPool().size() > 0) return ensureValidObject(value);
		return value;
	}

	@Override
	public void setValue(final Object value) {
		final String name = item.getName();
		if (Objects.equals(item.getValue(module), value)) return; // no change

		// Check if a converted value is present
		Object convertedInput = convertedObjects.get(value);
		if (convertedInput != null &&
			Objects.equals(item.getValue(module), convertedInput))
		{
			return; // no change
		}

		// Pass the value through the convertService
		convertedInput = convertService.convert(value, item.getType());

		// If we get a different (converted) value back, cache it weakly.
		if (convertedInput != value) {
			convertedObjects.put(value, convertedInput);
		}

		module.setInput(name, convertedInput);

		if (initialized) {
			threadService.run(new Runnable() {

				@Override
				public void run() {
					callback();
					inputPanel.refresh(); // must be on AWT thread?
					module.preview();
				}
			});
		}
	}

	@Override
	public void callback() {
		try {
			item.callback(module);
		}
		catch (final MethodCallException exc) {
			if (log != null) log.error(exc);
		}
	}

	@Override
	public Number getMin() {
		final Number min = toNumber(item.getMinimumValue());
		if (min != null) return min;
		return NumberUtils.getMinimumNumber(item.getType());
	}

	@Override
	public Number getMax() {
		final Number max = toNumber(item.getMaximumValue());
		if (max != null) return max;
		return NumberUtils.getMaximumNumber(item.getType());
	}

	@Override
	public Number getSoftMin() {
		final Number softMin = toNumber(item.getSoftMinimum());
		if (softMin != null) return softMin;
		return getMin();
	}

	@Override
	public Number getSoftMax() {
		final Number softMax = toNumber(item.getSoftMaximum());
		if (softMax != null) return softMax;
		return getMax();
	}

	@Override
	public Number getStepSize() {
		final Number stepSize = toNumber(item.getStepSize());
		if (stepSize != null) return stepSize;
		return NumberUtils.toNumber("1", item.getType());
	}

	@Override
	public String[] getChoices() {
		final List<?> choicesList = item.getChoices();
		final String[] choices = new String[choicesList.size()];
		for (int i = 0; i < choices.length; i++) {
			choices[i] = choicesList.get(i).toString();
		}
		return choices;
	}

	@Override
	public String getText() {
		final Object value = getValue();
		if (value == null) return "";
		final String text = value.toString();
		if (text.equals("\0")) return ""; // render null character as empty
		return text;
	}

	@Override
	public boolean isMessage() {
		return getItem().getVisibility() == ItemVisibility.MESSAGE;
	}

	@Override
	public boolean isText() {
		return ClassUtils.isText(getItem().getType());
	}

	@Override
	public boolean isCharacter() {
		return ClassUtils.isCharacter(getItem().getType());
	}

	@Override
	public boolean isNumber() {
		return ClassUtils.isNumber(getItem().getType());
	}

	@Override
	public boolean isBoolean() {
		return ClassUtils.isBoolean(getItem().getType());
	}

	@Override
	public boolean isMultipleChoice() {
		final List<?> choices = item.getChoices();
		return choices != null && !choices.isEmpty();
	}

	@Override
	public boolean isType(final Class<?> type) {
		return type.isAssignableFrom(getItem().getType());
	}

	@Override
	public void setInitialized(final boolean initialized) {
		this.initialized = initialized;
	}

	@Override
	public boolean isInitialized() {
		return initialized;
	}

	// -- Helper methods --

	/**
	 * For multiple choice widgets, ensures the value is a valid choice.
	 * 
	 * @see #getChoices()
	 * @see ChoiceWidget
	 */
	private Object ensureValidChoice(final Object value) {
		return ensureValid(value, Arrays.asList(getChoices()));
	}

	/**
	 * For object widgets, ensures the value is a valid object.
	 * 
	 * @see #getObjectPool()
	 * @see ObjectWidget
	 */
	private Object ensureValidObject(final Object value) {
		return ensureValid(value, getObjectPool());
	}

	/** Ensures the value is on the given list. */
	private Object ensureValid(final Object value, final List<?> list) {
		for (final Object o : list) {
			if (o.equals(value)) return value; // value is valid
			// check if value was converted and cached
			final Object convertedValue = convertedObjects.get(o);
			if (convertedValue != null && value.equals(convertedValue)) {
				return convertedValue;
			}
		}

		// value is not valid; override with the first item on the list instead
		final Object validValue = list.get(0);
		// CTR TODO: Mutating the model in a getter is dirty. Find a better way?
		setValue(validValue);
		return validValue;
	}

	/** Converts the given object to a number matching the input type. */
	private Number toNumber(final Object value) {
		final Class<?> type = item.getType();
		final Class<?> saneType = ConversionUtils.getNonprimitiveType(type);
		return NumberUtils.toNumber(value, saneType);
	}

}
