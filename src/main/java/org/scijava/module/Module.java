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

import java.util.Map;

import org.scijava.display.DisplayPostprocessor;
import org.scijava.module.process.ModulePostprocessor;
import org.scijava.module.process.ModulePreprocessor;
import org.scijava.widget.InputHarvester;

/**
 * A module is an encapsulated piece of functionality with inputs and outputs.
 * <p>
 * There are several types of modules, including plugins and scripts, as well as
 * workflows, which are directed acyclic graphs consisting of modules whose
 * inputs and outputs are connected.
 * </p>
 * <p>
 * The {@code Module} interface represents a specific instance of a module,
 * while the corresponding {@link ModuleInfo} represents metadata about that
 * module, particularly its input and output names and types.
 * </p>
 * 
 * @author Aivar Grislis
 * @author Curtis Rueden
 */
public interface Module extends Runnable {

	/**
	 * Computes a preview of the module's execution results, if available. A
	 * preview is a quick approximation of the results that would be obtained by
	 * actually executing the module with {@link #run()}. Not all modules support
	 * previews.
	 * 
	 * @see ModuleInfo#canPreview()
	 */
	void preview();

	/**
	 * Performs necessary cleanup in response to cancellation of the module
	 * execution. This is useful in conjunction with {@link #preview()} to undo
	 * any changes made as a result of the preview.
	 * 
	 * @see ModuleInfo#canCancel()
	 */
	void cancel();

	/**
	 * Initializes the module.
	 * <p>
	 * First, the module's global initializer method (if any) is called, followed
	 * by each individual {@link ModuleItem} initializer method (i.e.,
	 * {@link ModuleItem#initialize(Module)}).
	 * </p>
	 * 
	 * @see ModuleInfo#getInitializer()
	 * @see ModuleItem#initialize(Module)
	 */
	void initialize() throws MethodCallException;

	/** Gets metadata about this module. */
	ModuleInfo getInfo();

	/**
	 * Gets the object containing the module's actual implementation. By
	 * definition, this is an object whose fully qualified class name is given by
	 * {@code getInfo().getDelegateClassName()}. This object must possess all
	 * callback methods specified by {@link ModuleItem#getCallback()}.
	 * <p>
	 * The nature of this method is implementation-specific; e.g., a
	 * {@code CommandModule} will return its associated {@code Command}. For
	 * modules that are not plugins, the result may be something else. If you are
	 * implementing this interface directly, a good rule of thumb is to return
	 * {@code this}.
	 * </p>
	 */
	Object getDelegateObject();

	/** Gets the value of the input with the given name. */
	Object getInput(String name);

	/** Gets the value of the output with the given name. */
	Object getOutput(String name);

	/** Gets a table of input values. */
	Map<String, Object> getInputs();

	/** Gets a table of output values. */
	Map<String, Object> getOutputs();

	/** Sets the value of the input with the given name. */
	void setInput(String name, Object value);

	/** Sets the value of the output with the given name. */
	void setOutput(String name, Object value);

	/** Sets input values according to the given table. */
	void setInputs(Map<String, Object> inputs);

	/** Sets output values according to the given table. */
	void setOutputs(Map<String, Object> outputs);

	/**
	 * Gets the resolution status of the input with the given name.
	 * 
	 * @see #resolveInput(String)
	 */
	boolean isInputResolved(String name);

	/**
	 * Gets the resolution status of the output with the given name.
	 * 
	 * @see #resolveOutput(String)
	 */
	boolean isOutputResolved(String name);

	/**
	 * Marks the input with the given name as resolved. A "resolved" input is
	 * known to have a final, valid value for use with the module.
	 * <p>
	 * {@link ModulePreprocessor}s in the module execution chain that populate
	 * input values (e.g. {@link InputHarvester} plugins) will typically skip over
	 * inputs which have already been resolved.
	 * </p>
	 */
	void resolveInput(String name);

	/**
	 * Marks the output with the given name as resolved. A "resolved" output has
	 * been handled by the framework somehow, typically displayed to the user.
	 * <p>
	 * {@link ModulePostprocessor}s in the module execution chain that handle
	 * output values (e.g. the {@link DisplayPostprocessor}) will typically skip
	 * over outputs which have already been resolved.
	 * </p>
	 */
	void resolveOutput(String name);

	/**
	 * Marks the input with the given name as unresolved.
	 * 
	 * @see #resolveInput(String)
	 */
	void unresolveInput(String name);

	/**
	 * Marks the output with the given name as unresolved.
	 * 
	 * @see #resolveOutput(String)
	 */
	void unresolveOutput(String name);

	// -- Deprecated --

	/** @deprecated Use {@link #isInputResolved(String)} instead. */
	@Deprecated
	default boolean isResolved(final String name) {
		return isInputResolved(name);
	}

	/**
	 * @deprecated Use {@link #resolveInput(String)} and
	 *             {@link #unresolveInput(String)} instead.
	 */
	@Deprecated
	default void setResolved(final String name, final boolean resolved) {
		if (resolved) resolveInput(name);
		else unresolveInput(name);
	}

}
