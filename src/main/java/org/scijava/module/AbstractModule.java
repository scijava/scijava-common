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

package org.scijava.module;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.scijava.Initializable;

/**
 * Abstract superclass of {@link Module} implementations.
 * <p>
 * By default, input and output values are stored in {@link HashMap}s.
 * </p>
 * 
 * @author Curtis Rueden
 */
public abstract class AbstractModule implements Module {

	private final HashMap<String, Object> inputs;
	private final HashMap<String, Object> outputs;

	private final HashSet<String> resolvedInputs;
	private final HashSet<String> resolvedOutputs;

	private MethodRef initializerRef;

	public AbstractModule() {
		inputs = new HashMap<>();
		outputs = new HashMap<>();
		resolvedInputs = new HashSet<>();
		resolvedOutputs = new HashSet<>();
	}

	// -- Module methods --

	@Override
	public void preview() {
		// do nothing by default
	}

	@Override
	public void cancel() {
		// do nothing by default
	}

	@Override
	public void initialize() throws MethodCallException {
		// execute global module initializer
		final Object delegateObject = getDelegateObject();
		if (delegateObject instanceof Initializable) {
			((Initializable) delegateObject).initialize();
		}
		else {
			if (initializerRef == null) {
				final String initializer = getInfo().getInitializer();
				initializerRef = new MethodRef(delegateObject.getClass(), initializer);
			}
			initializerRef.execute(delegateObject);
		}

		// execute individual module item initializers
		for (final ModuleItem<?> item : getInfo().inputs()) {
			item.initialize(this);
		}
	}

	@Override
	public Object getDelegateObject() {
		return this;
	}

	@Override
	public Object getInput(final String name) {
		return inputs.get(name);
	}

	@Override
	public Object getOutput(final String name) {
		return outputs.get(name);
	}

	@Override
	public Map<String, Object> getInputs() {
		return createMap(getInfo().inputs(), false);
	}

	@Override
	public Map<String, Object> getOutputs() {
		return createMap(getInfo().outputs(), true);
	}

	@Override
	public void setInput(final String name, final Object value) {
		inputs.put(name, value);
	}

	@Override
	public void setOutput(final String name, final Object value) {
		outputs.put(name, value);
	}

	@Override
	public void setInputs(final Map<String, Object> inputs) {
		for (final String name : inputs.keySet()) {
			setInput(name, inputs.get(name));
		}
	}

	@Override
	public void setOutputs(final Map<String, Object> outputs) {
		for (final String name : outputs.keySet()) {
			setOutput(name, outputs.get(name));
		}
	}

	@Override
	public boolean isInputResolved(final String name) {
		return resolvedInputs.contains(name);
	}

	@Override
	public boolean isOutputResolved(final String name) {
		return resolvedOutputs.contains(name);
	}

	@Override
	public void resolveInput(final String name) {
		final ModuleItem<?> item = getInputItem(name);
		if (item != null) {
			try {
				item.validate(this);
			}
			catch (final MethodCallException exc) {
				// NB: Hacky, but avoids changing the API signature.
				throw new RuntimeException(exc);
			}
		}
		resolvedInputs.add(name);
	}

	@Override
	public void resolveOutput(final String name) {
		resolvedOutputs.add(name);
	}

	@Override
	public void unresolveInput(final String name) {
		resolvedInputs.remove(name);
	}

	@Override
	public void unresolveOutput(final String name) {
		resolvedOutputs.remove(name);
	}

	// -- Helper methods --

	private Map<String, Object> createMap(final Iterable<ModuleItem<?>> items,
		final boolean outputMap)
	{
		final Map<String, Object> map = new HashMap<>();
		for (final ModuleItem<?> item : items) {
			final String name = item.getName();
			final Object value = outputMap ? getOutput(name) : getInput(name);
			map.put(name, value);
		}
		return map;
	}

	private ModuleItem<?> getInputItem(final String name) {
		for (final ModuleItem<?> item : getInfo().inputs()) {
			if (item.getName().equals(name)) return item;
		}
		return null;
	}
}
