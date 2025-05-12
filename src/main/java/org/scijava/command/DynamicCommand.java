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

package org.scijava.command;

import java.lang.reflect.Field;

import org.scijava.Cancelable;
import org.scijava.Context;
import org.scijava.Contextual;
import org.scijava.NullContextException;
import org.scijava.module.DefaultMutableModule;
import org.scijava.module.ModuleService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.PluginService;
import org.scijava.util.ClassUtils;

/**
 * A command with a variable number of inputs and outputs. This class provides
 * greater configurability, but also greater complexity, than implementing the
 * {@link Command} interface and using only @{@link Parameter} annotations on
 * instance fields.
 * 
 * @author Curtis Rueden
 */
public abstract class DynamicCommand extends DefaultMutableModule implements
	Cancelable, Command, Contextual
{

	@Parameter
	private Context context;

	@Parameter
	private CommandService commandService;

	@Parameter
	protected PluginService pluginService;

	@Parameter
	protected ModuleService moduleService;

	private DynamicCommandInfo info;

	/** Reason for cancelation, or null if not canceled. */
	private String cancelReason;

	// -- Module methods --

	@Override
	public DynamicCommandInfo getInfo() {
		if (info == null) {
			// NB: Create dynamic metadata lazily.
			CommandInfo commandInfo = commandService.getCommand(getClass());
			if (commandInfo == null) commandInfo = new CommandInfo(getClass());
			info = new DynamicCommandInfo(commandInfo, getClass());
		}
		return info;
	}

	@Override
	public Object getInput(final String name) {
		final Field field = getInfo().getInputField(name);
		if (field == null) return super.getInput(name);
		return ClassUtils.getValue(field, this);
	}

	@Override
	public Object getOutput(final String name) {
		final Field field = getInfo().getOutputField(name);
		if (field == null) return super.getOutput(name);
		return ClassUtils.getValue(field, this);
	}

	@Override
	public void setInput(final String name, final Object value) {
		final Field field = getInfo().getInputField(name);
		if (field == null) super.setInput(name, value);
		else ClassUtils.setValue(field, this, value);
	}

	@Override
	public void setOutput(final String name, final Object value) {
		final Field field = getInfo().getOutputField(name);
		if (field == null) super.setOutput(name, value);
		else ClassUtils.setValue(field, this, value);
	}

	// -- Contextual methods --

	@Override
	public Context context() {
		if (context == null) throw new NullContextException();
		return context;
	}

	@Override
	public Context getContext() {
		return context;
	}

	@Override
	public void setContext(final Context context) {
		context.inject(this);
	}

	// -- Cancelable methods --

	@Override
	public boolean isCanceled() {
		return cancelReason != null;
	}

	@Override
	public void cancel(final String reason) {
		cancelReason = reason == null ? "" : reason;
	}

	@Override
	public String getCancelReason() {
		return cancelReason;
	}

	// HACK: For OptionsPlugin.
	public void uncancel() {
		cancelReason = null;
	}

	// -- Internal methods --

	/**
	 * Persists current input values. Use e.g. for {@link InteractiveCommand}s
	 * that want to persist values as they change, since interactive commands do
	 * not complete the module execution lifecycle normally.
	 */
	protected void saveInputs() {
		moduleService.saveInputs(this);
	}
}
