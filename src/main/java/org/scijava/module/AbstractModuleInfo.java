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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.scijava.AbstractUIDetails;
import org.scijava.util.ConversionUtils;

/**
 * Abstract superclass of {@link ModuleInfo} implementation.
 * <p>
 * By default, {@link ModuleItem}s are stored in {@link HashMap}s and
 * {@link ArrayList}s, internally.
 * </p>
 * 
 * @author Curtis Rueden
 */
public abstract class AbstractModuleInfo extends AbstractUIDetails implements
	ModuleInfo
{

	/** Table of inputs, keyed on name. */
	private HashMap<String, ModuleItem<?>> inputMap;

	/** Table of outputs, keyed on name. */
	private HashMap<String, ModuleItem<?>> outputMap;

	/** Ordered list of input items. */
	private ArrayList<ModuleItem<?>> inputList;

	/** Ordered list of output items. */
	private ArrayList<ModuleItem<?>> outputList;

	/** Whether lazy initialization is complete. */
	private boolean initialized;

	// -- ModuleInfo methods --

	@Override
	public ModuleItem<?> getInput(final String name) {
		return inputMap().get(name);
	}

	@Override
	public <T> ModuleItem<T> getInput(final String name, final Class<T> type) {
		return castItem(getInput(name), type);
	}

	@Override
	public ModuleItem<?> getOutput(final String name) {
		return outputMap().get(name);
	}

	@Override
	public <T> ModuleItem<T> getOutput(final String name, final Class<T> type) {
		return castItem(getOutput(name), type);
	}

	@Override
	public Iterable<ModuleItem<?>> inputs() {
		return Collections.unmodifiableList(inputList());
	}

	@Override
	public Iterable<ModuleItem<?>> outputs() {
		return Collections.unmodifiableList(outputList());
	}

	// -- Internal methods --

	/**
	 * Parses input and output parameters. Intended to be overridden by concrete
	 * subclasses.
	 */
	protected void parseParameters() {
		// NB: Do nothing by default.
	}

	/** Clears input and output parameters. */
	protected void clearParameters() {
		inputMap.clear();
		outputMap.clear();
		inputList.clear();
		outputList.clear();
	}

	/**
	 * Adds an input. Intended to be called from overridden
	 * {@link #parseParameters()} methods.
	 */
	protected void registerInput(final ModuleItem<?> input) {
		inputMap.put(input.getName(), input);
		inputList.add(input);
	}

	/**
	 * Adds an output. Intended to be called from overridden
	 * {@link #parseParameters()} methods.
	 */
	protected void registerOutput(final ModuleItem<?> output) {
		outputMap.put(output.getName(), output);
		outputList.add(output);
	}

	/** Gets {@link #inputMap}, initializing if needed. */
	protected Map<String, ModuleItem<?>> inputMap() {
		if (!initialized) initParameters();
		return inputMap;
	}

	/** Gets {@link #inputList}, initializing if needed. */
	protected Map<String, ModuleItem<?>> outputMap() {
		if (!initialized) initParameters();
		return outputMap;
	}

	/** Gets {@link #outputMap}, initializing if needed. */
	protected List<ModuleItem<?>> inputList() {
		if (!initialized) initParameters();
		return inputList;
	}

	/** Gets {@link #outputList}, initializing if needed. */
	protected List<ModuleItem<?>> outputList() {
		if (!initialized) initParameters();
		return outputList;
	}

	// -- Helper methods --

	private <T> ModuleItem<T> castItem(final ModuleItem<?> item,
		final Class<T> type)
	{
		final Class<?> itemType = item.getType();
		// if (!type.isAssignableFrom(itemType)) {
		final Class<?> saneItemType = ConversionUtils.getNonprimitiveType(itemType);
		if (!ConversionUtils.canCast(type, saneItemType)) {
			throw new IllegalArgumentException("Type " + type.getName() +
				" is incompatible with item of type " + itemType.getName());
		}
		@SuppressWarnings("unchecked")
		final ModuleItem<T> typedItem = (ModuleItem<T>) item;
		return typedItem;
	}

	// -- Helper methods - lazy initialization --

	/** Initializes data structures and parses parameters. */
	private synchronized void initParameters() {
		if (initialized) return; // already initialized

		inputMap = new HashMap<>();
		outputMap = new HashMap<>();
		inputList = new ArrayList<>();
		outputList = new ArrayList<>();

		parseParameters();

		initialized = true;
	}

}
