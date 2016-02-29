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

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

import org.scijava.Identifiable;
import org.scijava.Prioritized;
import org.scijava.input.Accelerator;
import org.scijava.module.process.ModulePostprocessor;
import org.scijava.module.process.ModulePreprocessor;
import org.scijava.module.process.PostprocessorPlugin;
import org.scijava.module.process.PreprocessorPlugin;
import org.scijava.prefs.PrefService;
import org.scijava.service.SciJavaService;

/**
 * Interface for service that tracks and executes available modules.
 * <p>
 * The module service keeps a master index of all modules known to the system.
 * At heart, a module is a {@link Runnable} piece of code, but with explicit
 * typed input and output parameters.
 * </p>
 * <p>
 * The module service has no innate ability to discover modules, and must be
 * explicitly told about them via the {@link #addModule} and {@link #addModules}
 * methods.
 * </p>
 * <p>
 * A <em>module</em> is distinct from a <em>plugin</em> in that plugins extend
 * a program's functionality in some way, taking many forms, whereas modules
 * are always runnable code with typed inputs and outputs. There is a
 * particular type of plugin called a {@link org.scijava.command.Command} which
 * is also a module, but many plugins (e.g., {@link org.scijava.tool.Tool}s and
 * {@link org.scijava.display.Display}s) are not modules.
 * </p>
 * 
 * @author Curtis Rueden
 * @see Module
 * @see org.scijava.plugin.PluginService
 */
public interface ModuleService extends SciJavaService {

	/** Gets the index of available modules. */
	ModuleIndex getIndex();

	/** Manually registers a module with the module service. */
	void addModule(ModuleInfo module);

	/** Manually unregisters a module with the module service. */
	void removeModule(ModuleInfo module);

	/** Manually registers a list of modules with the module service. */
	void addModules(Collection<? extends ModuleInfo> modules);

	/** Manually unregisters a list of modules with the module service. */
	void removeModules(Collection<? extends ModuleInfo> modules);

	/** Gets the list of available modules. */
	List<ModuleInfo> getModules();

	/**
	 * Gets the module with the given identifier string.
	 * 
	 * @param id The identifier string corresponding to the desired module.
	 * @return The {@link Identifiable} module with the given identifier.
	 */
	ModuleInfo getModuleById(String id);

	/**
	 * Gets the module for a given keyboard shortcut.
	 * 
	 * @param acc the accelerator for which to search.
	 * @return the module info for the corresponding module, or null.
	 */
	ModuleInfo getModuleForAccelerator(Accelerator acc);

	/**
	 * Creates an instance of the given module.
	 * <p>
	 * If the module implements the {@link org.scijava.Contextual} interface, the
	 * appropriate context is injected. Similarly, if the module implements the
	 * {@link Prioritized} interface, the appropriate priority is injected.
	 * </p>
	 * <p>
	 * Note that in the case of commands, this method does <em>not</em> do any
	 * preprocessing on the command instances, so parameters will not be
	 * auto-populated, initializers will not be executed, etc.
	 * </p>
	 */
	Module createModule(ModuleInfo info);

	/**
	 * Executes the given module.
	 * 
	 * @param info The module to instantiate and run.
	 * @param process If true, executes the module with pre- and postprocessing
	 *          steps from all available {@link PreprocessorPlugin}s and
	 *          {@link PostprocessorPlugin}s in the plugin index; if false,
	 *          executes the module with no pre- or postprocessing.
	 * @param inputs List of input parameter names and values. The expected order
	 *          is in pairs: an input name followed by its value, for each desired
	 *          input to populate. Leaving some inputs unpopulated is allowed.
	 *          Passing the name of an input that is not valid for the module, or
	 *          passing a value of a type incompatible with the associated input
	 *          parameter, will issue an error and ignore that name/value pair.
	 * @return {@link Future} of the module instance being executed. Calling
	 *         {@link Future#get()} will block until execution is complete.
	 */
	Future<Module> run(ModuleInfo info, boolean process, Object... inputs);

	/**
	 * Executes the given module.
	 * 
	 * @param info The module to instantiate and run.
	 * @param process If true, executes the module with pre- and postprocessing
	 *          steps from all available {@link PreprocessorPlugin}s and
	 *          {@link PostprocessorPlugin}s in the plugin index; if false,
	 *          executes the module with no pre- or postprocessing.
	 * @param inputMap Table of input parameter values, with keys matching the
	 *          {@link ModuleInfo}'s input parameter names. Passing a value of a
	 *          type incompatible with the associated input parameter will issue
	 *          an error and ignore that value.
	 * @return {@link Future} of the module instance being executed. Calling
	 *         {@link Future#get()} will block until execution is complete.
	 */
	Future<Module> run(ModuleInfo info, boolean process,
		Map<String, Object> inputMap);

	/**
	 * Executes the given module.
	 * 
	 * @param info The module to instantiate and run.
	 * @param pre List of preprocessing steps to perform.
	 * @param post List of postprocessing steps to perform.
	 * @param inputs List of input parameter names and values. The expected order
	 *          is in pairs: an input name followed by its value, for each desired
	 *          input to populate. Leaving some inputs unpopulated is allowed.
	 *          Passing the name of an input that is not valid for the module, or
	 *          passing a value of a type incompatible with the associated input
	 *          parameter, will issue an error and ignore that name/value pair.
	 * @return {@link Future} of the module instance being executed. Calling
	 *         {@link Future#get()} will block until execution is complete.
	 */
	Future<Module> run(ModuleInfo info, List<? extends ModulePreprocessor> pre,
		List<? extends ModulePostprocessor> post, Object... inputs);

	/**
	 * Executes the given module.
	 * 
	 * @param info The module to instantiate and run.
	 * @param pre List of preprocessing steps to perform.
	 * @param post List of postprocessing steps to perform.
	 * @param inputMap Table of input parameter values, with keys matching the
	 *          {@link ModuleInfo}'s input parameter names. Passing a value of a
	 *          type incompatible with the associated input parameter will issue
	 *          an error and ignore that value.
	 * @return {@link Future} of the module instance being executed. Calling
	 *         {@link Future#get()} will block until execution is complete.
	 */
	Future<Module> run(ModuleInfo info, List<? extends ModulePreprocessor> pre,
		List<? extends ModulePostprocessor> post, Map<String, Object> inputMap);

	/**
	 * Executes the given module.
	 * 
	 * @param module The module to run.
	 * @param process If true, executes the module with pre- and postprocessing
	 *          steps from all available {@link PreprocessorPlugin}s and
	 *          {@link PostprocessorPlugin}s in the plugin index; if false,
	 *          executes the module with no pre- or postprocessing.
	 * @param inputs List of input parameter names and values. The expected order
	 *          is in pairs: an input name followed by its value, for each desired
	 *          input to populate. Leaving some inputs unpopulated is allowed.
	 *          Passing the name of an input that is not valid for the module, or
	 *          passing a value of a type incompatible with the associated input
	 *          parameter, will issue an error and ignore that name/value pair.
	 * @return {@link Future} of the module instance being executed. Calling
	 *         {@link Future#get()} will block until execution is complete.
	 */
	<M extends Module> Future<M> run(M module, boolean process, Object... inputs);

	/**
	 * Executes the given module.
	 * 
	 * @param module The module to run.
	 * @param process If true, executes the module with pre- and postprocessing
	 *          steps from all available {@link PreprocessorPlugin}s and
	 *          {@link PostprocessorPlugin}s in the plugin index; if false,
	 *          executes the module with no pre- or postprocessing.
	 * @param inputMap Table of input parameter values, with keys matching the
	 *          {@link ModuleInfo}'s input parameter names. Passing a value of a
	 *          type incompatible with the associated input parameter will issue
	 *          an error and ignore that value.
	 * @return {@link Future} of the module instance being executed. Calling
	 *         {@link Future#get()} will block until execution is complete.
	 */
	<M extends Module> Future<M> run(M module, boolean process,
		Map<String, Object> inputMap);

	/**
	 * Executes the given module.
	 * 
	 * @param module The module to run.
	 * @param pre List of preprocessing steps to perform.
	 * @param post List of postprocessing steps to perform.
	 * @param inputs List of input parameter names and values. The expected order
	 *          is in pairs: an input name followed by its value, for each desired
	 *          input to populate. Leaving some inputs unpopulated is allowed.
	 *          Passing the name of an input that is not valid for the module, or
	 *          passing a value of a type incompatible with the associated input
	 *          parameter, will issue an error and ignore that name/value pair.
	 * @return {@link Future} of the module instance being executed. Calling
	 *         {@link Future#get()} will block until execution is complete.
	 */
	<M extends Module> Future<M> run(M module,
		List<? extends ModulePreprocessor> pre,
		List<? extends ModulePostprocessor> post, Object... inputs);

	/**
	 * Executes the given module.
	 * 
	 * @param module The module to run.
	 * @param pre List of preprocessing steps to perform.
	 * @param post List of postprocessing steps to perform.
	 * @param inputMap Table of input parameter values, with keys matching the
	 *          module's {@link ModuleInfo}'s input parameter names. Passing a
	 *          value of a type incompatible with the associated input parameter
	 *          will issue an error and ignore that value.
	 * @return {@link Future} of the module instance being executed. Calling
	 *         {@link Future#get()} will block until execution is complete.
	 */
	<M extends Module> Future<M> run(M module,
		List<? extends ModulePreprocessor> pre,
		List<? extends ModulePostprocessor> post, Map<String, Object> inputMap);

	/** Blocks until the given module is finished executing. */
	<M extends Module> M waitFor(Future<M> future);

	/**
	 * Checks the given module for a solitary unresolved fillable input of the
	 * given type, returning the relevant {@link ModuleItem} if found, or null if
	 * not exactly one unresolved fillable input of that type.
	 */
	<T> ModuleItem<T> getSingleInput(Module module, Class<T> type);

	/**
	 * Checks the given module for a solitary unresolved output of the given type,
	 * returning the relevant {@link ModuleItem} if found, or null if not exactly
	 * one unresolved output of that type.
	 */
	<T> ModuleItem<T> getSingleOutput(Module module, Class<T> type);

	/**
	 * As {@link #getSingleInput(Module, Class)} but will match with a set of
	 * potential classes, at the cost of generic parameter safety.
	 */
	ModuleItem<?> getSingleInput(Module module, Collection<Class<?>> types);

	/**
	 * As {@link #getSingleOutput(Module, Class)} but will match with a set of
	 * potential classes, at the cost of generic parameter safety.
	 */
	ModuleItem<?> getSingleOutput(Module module, Collection<Class<?>> types);

	/**
	 * Registers the given value for the given {@link ModuleItem} using the
	 * {@link PrefService}.
	 */
	<T> void save(ModuleItem<T> item, T value);

	/**
	 * Returns the value, if any, stored in the {@link PrefService} for the given
	 * {@link ModuleItem}.
	 */
	<T> T load(ModuleItem<T> item);

	/** Gets the default value of the given {@link ModuleItem}. */
	<T> T getDefaultValue(final ModuleItem<T> item);

}
