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

package org.scijava.command;

import java.util.Map;

import org.scijava.Cancelable;
import org.scijava.Context;
import org.scijava.Contextual;
import org.scijava.InstantiableException;
import org.scijava.NullContextException;
import org.scijava.module.AbstractModule;
import org.scijava.module.Module;
import org.scijava.module.ModuleException;
import org.scijava.module.ModuleInfo;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.PluginInfo;
import org.scijava.util.ClassUtils;

/**
 * Module adapter class for working with a {@link Command} instance.
 * <p>
 * The {@link Module} interface describes an encapsulated piece of functionality
 * with inputs and outputs.
 * </p>
 * <p>
 * Conceptually, a command is such a piece of functionality. However, the
 * {@link Command} interface is intended to be as simple to implement as
 * possible; the only required method is {@link Command#run()}, which is
 * inherited from {@link Runnable}. The command's inputs and outputs are
 * indicated using @{@link Parameter} annotations.
 * </p>
 * <p>
 * To bridge the gap, this class adapts a {@link Command} instance into an
 * object that fully implements the {@link Module} interface.
 * </p>
 * 
 * @author Curtis Rueden
 * @author Johannes Schindelin
 * @author Grant Harris
 * @see CommandInfo - An adapter class that bridges the gap between SciJava
 *      commands and the rich {@link ModuleInfo} interface.
 * @see Module - The interface which provides many methods for interrogating an
 *      encapsulated piece of functionality with inputs and outputs.
 */
public class CommandModule extends AbstractModule implements Cancelable,
	Contextual
{

	/** The metadata describing the command. */
	private final CommandInfo info;

	/** The command instance handled by this module. */
	@Parameter
	private final Command command;

	@Parameter
	private Context context;

	/**
	 * Reason for cancelation, or null if not canceled. Note that this field is
	 * only relevant if the delegate {@link Command} is not itself
	 * {@link Cancelable}.
	 */
	private String cancelReason;

	/** Creates a command module for the given {@link PluginInfo}. */
	public CommandModule(final CommandInfo info) throws ModuleException {
		super();
		this.info = info;
		command = instantiateCommand();
		assignPresets();
	}

	/**
	 * Creates a command module for the given {@link CommandInfo}, around the
	 * specified {@link Command} instance.
	 */
	public CommandModule(final CommandInfo info, final Command command) {
		super();
		this.info = info;
		this.command = command;
		assignPresets();
	}

	// -- CommandModule methods --

	/** Gets the command instance handled by this module. */
	public Command getCommand() {
		return command;
	}

	// -- Module methods --

	/**
	 * Computes a preview of the command's results. For this method to do
	 * anything, the command must implement the {@link Previewable} interface.
	 */
	@Override
	public void preview() {
		if (!(command instanceof Previewable)) return; // cannot preview
		final Previewable previewPlugin = (Previewable) command;
		previewPlugin.preview();
	}

	/**
	 * Cancels the command, undoing the effects of any {@link #preview()} calls.
	 * For this method to do anything, the command must implement the
	 * {@link Previewable} interface.
	 */
	@Override
	public void cancel() {
		if (!(command instanceof Previewable)) return; // nothing to cancel
		final Previewable previewPlugin = (Previewable) command;
		previewPlugin.cancel();
	}

	@Override
	public CommandInfo getInfo() {
		return info;
	}

	@Override
	public Object getDelegateObject() {
		return command;
	}

	@Override
	public Object getInput(final String name) {
		final CommandModuleItem<?> item = info.getInput(name);
		return item == null ? null : ClassUtils.getValue(item.getField(), command);
	}

	@Override
	public Object getOutput(final String name) {
		final CommandModuleItem<?> item = info.getOutput(name);
		return item == null ? null : ClassUtils.getValue(item.getField(), command);
	}

	@Override
	public void setInput(final String name, final Object value) {
		final CommandModuleItem<?> item = info.getInput(name);
		if (item == null) {
			throw new IllegalArgumentException("No such input: " + name);
		}
		ClassUtils.setValue(item.getField(), command, value);
	}

	@Override
	public void setOutput(final String name, final Object value) {
		final CommandModuleItem<?> item = info.getOutput(name);
		if (item == null) {
			throw new IllegalArgumentException("No such output: " + name);
		}
		ClassUtils.setValue(item.getField(), command, value);
	}

	// -- Object methods --

	@Override
	public String toString() {
		return command.getClass().getName();
	}

	// -- Runnable methods --

	@Override
	public void run() {
		command.run();
	}

	// -- Cancelable methods --

	@Override
	public boolean isCanceled() {
		if (command instanceof Cancelable) {
			return ((Cancelable) command).isCanceled();
		}
		return cancelReason != null;
	}

	@Override
	public void cancel(final String reason) {
		if (command instanceof Cancelable) {
			((Cancelable) command).cancel(reason);
			return;
		}
		cancelReason = reason == null ? "" : reason;
	}

	@Override
	public String getCancelReason() {
		if (command instanceof Cancelable) {
			return ((Cancelable) command).getCancelReason();
		}
		return cancelReason;
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

	// -- Helper methods --

	private Command instantiateCommand() throws ModuleException {
		try {
			return info.createInstance();
		}
		catch (final InstantiableException exc) {
			throw new ModuleException(exc);
		}
	}

	private void assignPresets() {
		final Map<String, Object> presets = info.getPresets();
		for (final String name : presets.keySet()) {
			final Object value = presets.get(name);
			setInput(name, value);
			resolveInput(name);
		}
	}

}
