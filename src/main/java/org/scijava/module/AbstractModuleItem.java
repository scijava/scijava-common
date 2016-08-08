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

package org.scijava.module;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;

import org.scijava.AbstractBasicDetails;
import org.scijava.ItemIO;
import org.scijava.ItemPersistence;
import org.scijava.ItemVisibility;
import org.scijava.util.ClassUtils;
import org.scijava.util.ConversionUtils;
import org.scijava.util.NumberUtils;
import org.scijava.util.Prefs;
import org.scijava.util.StringMaker;

/**
 * Abstract superclass of {@link ModuleItem} implementations.
 * 
 * @author Curtis Rueden
 */
public abstract class AbstractModuleItem<T> extends AbstractBasicDetails
	implements ModuleItem<T>
{

	private final ModuleInfo info;

	private MethodRef initializerRef;
	private MethodRef callbackRef;

	public AbstractModuleItem(final ModuleInfo info) {
		this.info = info;
	}

	// -- Object methods --

	@Override
	public String toString() {
		final StringMaker sm = new StringMaker();
		sm.append("label", getLabel());
		sm.append("description", getDescription());
		sm.append("visibility", getVisibility(), ItemVisibility.NORMAL);
		sm.append("required", isRequired());
		sm.append("persisted", getPersistence());
		sm.append("persistKey", getPersistKey());
		sm.append("callback", getCallback());
		sm.append("widgetStyle", getWidgetStyle());
		sm.append("default", getDefaultValue());
		sm.append("min", getMinimumValue());
		sm.append("max", getMaximumValue());
		sm.append("softMin", getSoftMinimum());
		sm.append("softMax", getSoftMaximum());
		sm.append("stepSize", getStepSize(), NumberUtils.toNumber("1", getType()));
		sm.append("columnCount", getColumnCount(), 6);
		sm.append("choices", getChoices());
		return getName() + ": " + sm.toString();
	}

	// -- ModuleItem methods --

	@Override
	public ModuleInfo getInfo() {
		return info;
	}

	@Override
	public Type getGenericType() {
		return getType();
	}

	@Override
	public ItemIO getIOType() {
		return ItemIO.INPUT;
	}

	@Override
	public boolean isInput() {
		final ItemIO ioType = getIOType();
		return ioType == ItemIO.INPUT || ioType == ItemIO.BOTH;
	}

	@Override
	public boolean isOutput() {
		final ItemIO ioType = getIOType();
		return ioType == ItemIO.OUTPUT || ioType == ItemIO.BOTH;
	}

	@Override
	public ItemVisibility getVisibility() {
		return ItemVisibility.NORMAL;
	}

	@Override
	public boolean isAutoFill() {
		return true;
	}

	@Override
	public boolean isRequired() {
		return true;
	}

	@Override
	public ItemPersistence getPersistence() {
		return ItemPersistence.DEFAULT;
	}

	@Override
	public String getPersistKey() {
		return null;
	}

	/**
	 * Returns the persisted value of a ModuleItem. Returns null if nothing has
	 * been persisted. It is the API user's responsibility to check the return
	 * value for null.
	 */
	@Override
	@Deprecated
	public T loadValue() {
		// if there is nothing to load from persistence return nothing
		if (getPersistence() == ItemPersistence.NO) return null;

		final String sValue;
		final String persistKey = getPersistKey();
		if (persistKey == null || persistKey.isEmpty()) {
			final Class<?> prefClass = getDelegateClass();
			final String prefKey = getName();
			sValue = Prefs.get(prefClass, prefKey);
		}
		else sValue = Prefs.get(persistKey);

		// if persisted value has never been set before return null
		if (sValue == null) return null;

		return ConversionUtils.convert(sValue, getType());
	}

	@Override
	@Deprecated
	public void saveValue(final T value) {
		if (getPersistence() == ItemPersistence.NO) return;

		final String sValue = value == null ? "" : value.toString();

		// do not persist if object cannot be converted back from a string
		if (!ConversionUtils.canConvert(sValue, getType())) return;

		final String persistKey = getPersistKey();
		if (persistKey == null || persistKey.isEmpty()) {
			final Class<?> prefClass = getDelegateClass();
			final String prefKey = getName();
			Prefs.put(prefClass, prefKey, sValue);
		}
		else Prefs.put(persistKey, sValue);
	}

	@Override
	public String getInitializer() {
		return null;
	}

	@Override
	public void initialize(final Module module) throws MethodCallException {
		final Object delegateObject = module.getDelegateObject();
		if (initializerRef == null) {
			initializerRef =
				new MethodRef(delegateObject.getClass(), getInitializer());
		}
		initializerRef.execute(module.getDelegateObject());
	}

	@Override
	public String getCallback() {
		return null;
	}

	@Override
	public void callback(final Module module) throws MethodCallException {
		final Object delegateObject = module.getDelegateObject();
		if (callbackRef == null) {
			callbackRef = new MethodRef(delegateObject.getClass(), getCallback());
		}
		callbackRef.execute(delegateObject);
	}

	@Override
	public String getWidgetStyle() {
		return null;
	}

	@Override
	public T getDefaultValue() {
		return null;
	}

	@Override
	public T getMinimumValue() {
		return null;
	}

	@Override
	public T getMaximumValue() {
		return null;
	}

	@Override
	public T getSoftMinimum() {
		// NB: Return hard minimum by default.
		return getMinimumValue();
	}

	@Override
	public T getSoftMaximum() {
		// NB: Return hard maximum by default.
		return getMaximumValue();
	}

	@Override
	public Number getStepSize() {
		if (!ClassUtils.isNumber(getType())) return null;
		return NumberUtils.toNumber("1", getType());
	}

	@Override
	public int getColumnCount() {
		return 6;
	}

	@Override
	public List<T> getChoices() {
		final T[] choices = getType().getEnumConstants();
		return choices == null ? null : Arrays.asList(choices);
	}

	@Override
	public T getValue(final Module module) {
		final Object result;
		if (isInput()) result = module.getInput(getName());
		else if (isOutput()) result = module.getOutput(getName());
		else result = null;
		@SuppressWarnings("unchecked")
		final T value = (T) result;
		return value;
	}

	@Override
	public void setValue(final Module module, final T value) {
		if (isInput()) module.setInput(getName(), value);
		if (isOutput()) module.setOutput(getName(), value);
	}

	// -- Internal methods --

	protected Class<?> getDelegateClass() {
		try {
			return info.loadDelegateClass();
		}
		catch (final ClassNotFoundException exc) {
			// TODO: Consider a better error handling mechanism here.
			throw new IllegalStateException(exc);
		}
	}

}
