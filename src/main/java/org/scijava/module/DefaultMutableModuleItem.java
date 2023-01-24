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

package org.scijava.module;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.scijava.ItemIO;
import org.scijava.ItemVisibility;

/**
 * Default {@link MutableModuleItem} implementation, for use with custom
 * {@link MutableModule} implementations.
 * 
 * @author Curtis Rueden
 */
public class DefaultMutableModuleItem<T> extends AbstractModuleItem<T>
	implements MutableModuleItem<T>
{

	private final Class<T> type;
	private final Type genericType;
	private ItemIO ioType;
	private ItemVisibility visibility;
	private boolean autoFill;
	private boolean required;
	private boolean persisted;
	private String persistKey;
	private String initializer;
	private String validater;
	private String callback;
	private String widgetStyle;
	private T defaultValue;
	private T minimumValue;
	private T maximumValue;
	private T softMinimum;
	private T softMaximum;
	private Number stepSize;
	private int columnCount;
	private final List<T> choices = new ArrayList<>();
	private String name;
	private String label;
	private String description;

	public DefaultMutableModuleItem(final Module module, final String name,
		final Class<T> type)
	{
		this(module.getInfo(), name, type);
	}

	public DefaultMutableModuleItem(final ModuleInfo info, final String name,
		final Class<T> type)
	{
		super(info);
		this.name = name;
		this.type = type;
		genericType = type;
		ioType = super.getIOType();
		visibility = super.getVisibility();
		autoFill = super.isAutoFill();
		required = super.isRequired();
		persisted = super.isPersisted();
		persistKey = super.getPersistKey();
		initializer = super.getInitializer();
		validater = super.getValidater();
		callback = super.getCallback();
		widgetStyle = super.getWidgetStyle();
		minimumValue = super.getMinimumValue();
		maximumValue = super.getMaximumValue();
		stepSize = super.getStepSize();
		columnCount = super.getColumnCount();
		final List<T> superChoices = super.getChoices();
		if (superChoices != null) choices.addAll(superChoices);
		label = super.getLabel();
		description = super.getDescription();
	}

	/** Creates a new module item with the same values as the given item. */
	public DefaultMutableModuleItem(final ModuleInfo info,
		final ModuleItem<T> item)
	{
		super(info);
		name = item.getName();
		type = item.getType();
		genericType = item.getGenericType();
		ioType = item.getIOType();
		visibility = item.getVisibility();
		autoFill = item.isAutoFill();
		required = item.isRequired();
		persisted = item.isPersisted();
		persistKey = item.getPersistKey();
		initializer = item.getInitializer();
		validater = item.getValidater();
		callback = item.getCallback();
		widgetStyle = item.getWidgetStyle();
		minimumValue = item.getMinimumValue();
		maximumValue = item.getMaximumValue();
		softMinimum = item.getSoftMinimum();
		softMaximum = item.getSoftMaximum();
		stepSize = item.getStepSize();
		columnCount = item.getColumnCount();
		final List<T> itemChoices = item.getChoices();
		if (itemChoices != null) choices.addAll(itemChoices);
		label = item.getLabel();
		description = item.getDescription();
	}

	// -- MutableModuleItem methods --

	@Override
	public void setIOType(final ItemIO ioType) {
		this.ioType = ioType;
	}

	@Override
	public void setVisibility(final ItemVisibility visibility) {
		this.visibility = visibility;
	}

	@Override
	public void setAutoFill(final boolean autoFill) {
		this.autoFill = autoFill;
	}

	@Override
	public void setRequired(final boolean required) {
		this.required = required;
	}

	@Override
	public void setPersisted(final boolean persisted) {
		this.persisted = persisted;
	}

	@Override
	public void setPersistKey(final String persistKey) {
		this.persistKey = persistKey;
	}

	@Override
	public void setInitializer(final String initializer) {
		this.initializer = initializer;
	}

	@Override
	public void setValidater(final String validater) {
		this.validater = validater;
	}

	@Override
	public void setCallback(final String callback) {
		this.callback = callback;
	}

	@Override
	public void setWidgetStyle(final String widgetStyle) {
		this.widgetStyle = widgetStyle;
	}

	@Override
	public void setDefaultValue(final T defaultValue) {
		this.defaultValue = defaultValue;
	}

	@Override
	public void setMinimumValue(final T minimumValue) {
		this.minimumValue = minimumValue;
	}

	@Override
	public void setMaximumValue(final T maximumValue) {
		this.maximumValue = maximumValue;
	}

	@Override
	public void setSoftMinimum(final T softMinimum) {
		this.softMinimum = softMinimum;
	}

	@Override
	public void setSoftMaximum(final T softMaximum) {
		this.softMaximum = softMaximum;
	}

	@Override
	public void setStepSize(final Number stepSize) {
		this.stepSize = stepSize;
	}

	@Override
	public void setColumnCount(final int columnCount) {
		this.columnCount = columnCount;
	}

	@Override
	public void setChoices(final List<? extends T> choices) {
		this.choices.clear();
		this.choices.addAll(choices);
	}

	// -- ModuleItem methods --

	@Override
	public Class<T> getType() {
		return type;
	}

	@Override
	public Type getGenericType() {
		return genericType;
	}

	@Override
	public ItemIO getIOType() {
		return ioType;
	}

	@Override
	public ItemVisibility getVisibility() {
		return visibility;
	}

	@Override
	public boolean isAutoFill() {
		return autoFill;
	}

	@Override
	public boolean isRequired() {
		return required;
	}

	@Override
	public boolean isPersisted() {
		return persisted;
	}

	@Override
	public String getPersistKey() {
		return persistKey;
	}

	@Override
	public String getInitializer() {
		return initializer;
	}

	@Override
	public String getValidater() {
		return validater;
	}

	@Override
	public String getCallback() {
		return callback;
	}

	@Override
	public String getWidgetStyle() {
		return widgetStyle;
	}

	@Override
	public T getDefaultValue() {
		return defaultValue;
	}

	@Override
	public T getMinimumValue() {
		return minimumValue;
	}

	@Override
	public T getMaximumValue() {
		return maximumValue;
	}

	@Override
	public T getSoftMinimum() {
		return softMinimum;
	}

	@Override
	public T getSoftMaximum() {
		return softMaximum;
	}

	@Override
	public Number getStepSize() {
		return stepSize;
	}

	@Override
	public int getColumnCount() {
		return columnCount;
	}

	@Override
	public List<T> getChoices() {
		return Collections.unmodifiableList(choices);
	}

	// -- BasicDetails methods --

	@Override
	public String getLabel() {
		return label;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public void setLabel(final String label) {
		this.label = label;
	}

	@Override
	public void setDescription(final String description) {
		this.description = description;
	}

	// -- Named methods --

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(final String name) {
		this.name = name;
	}

}
