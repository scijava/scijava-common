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

package org.scijava.script;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

import javax.script.ScriptException;

import org.scijava.MenuPath;
import org.scijava.module.process.PostprocessorPlugin;
import org.scijava.module.process.PreprocessorPlugin;
import org.scijava.plugin.SingletonService;
import org.scijava.service.SciJavaService;

/**
 * Interface for service that works with scripts. This service discovers
 * available scripts, and provides convenience methods to interact with them.
 * 
 * @author Johannes Schindelin
 * @author Curtis Rueden
 */
public interface ScriptService extends SingletonService<ScriptLanguage>,
	SciJavaService
{

	/**
	 * System property for overriding the list of directories to scan for scripts.
	 * 
	 * @see #getScriptDirectories()
	 */
	String SCRIPTS_PATH_PROPERTY = "scijava.scripts.path";

	/**
	 * Base directory for discovering scripts, including within classpath
	 * resources as well as beneath the application base directory.
	 */
	String SCRIPTS_RESOURCE_DIR = "scripts";

	// -- Scripting languages --

	/** Gets the index of available scripting languages. */
	ScriptLanguageIndex getIndex();

	/**
	 * Gets the available scripting languages.
	 * <p>
	 * This method does the same thing as {@link #getInstances()}.
	 * </p>
	 */
	default List<ScriptLanguage> getLanguages() {
		return new ArrayList<>(getIndex());
	}

	/** Gets the scripting language that handles the given file extension. */
	default ScriptLanguage getLanguageByExtension(final String extension) {
		return getIndex().getByExtension(extension);
	}

	/** Gets the scripting language with the given name. */
	default ScriptLanguage getLanguageByName(final String name) {
		return getIndex().getByName(name);
	}

	// -- Scripts --

	/** Gets the base directories to scan for scripts. */
	List<File> getScriptDirectories();

	/**
	 * Gets the menu path prefix for the given script directory, or null if none.
	 */
	MenuPath getMenuPrefix(File scriptDirectory);

	/** Adds a base directory to scan for scripts. */
	void addScriptDirectory(File scriptDirectory);

	/**
	 * Adds a base directory to scan for scripts, placing discovered scripts
	 * beneath the given menu path prefix.
	 */
	void addScriptDirectory(File scriptDirectory, final MenuPath menuPrefix);

	/** Removes a base directory to scan for scripts. */
	void removeScriptDirectory(File scriptDirectory);

	/** Gets all available scripts. */
	Collection<ScriptInfo> getScripts();

	/**
	 * Gets the cached {@link ScriptInfo} metadata for the script at the given
	 * file, creating it if it does not already exist.
	 */
	ScriptInfo getScript(File scriptFile);

	/**
	 * Executes the script in the given file.
	 * 
	 * @param file File containing the script to execute.
	 * @param process If true, executes the script with pre- and postprocessing
	 *          steps from all available {@link PreprocessorPlugin}s and
	 *          {@link PostprocessorPlugin}s in the plugin index; if false,
	 *          executes the script with no pre- or postprocessing.
	 * @param inputs List of input parameter names and values. The expected order
	 *          is in pairs: an input name followed by its value, for each desired
	 *          input to populate. Leaving some inputs unpopulated is allowed.
	 *          Passing the name of an input that is not valid for the plugin, or
	 *          passing a value of a type incompatible with the associated input
	 *          parameter, will issue an error and ignore that name/value pair.
	 * @return {@link Future} of the module instance being executed. Calling
	 *         {@link Future#get()} will block until execution is complete.
	 */
	Future<ScriptModule> run(File file, boolean process, Object... inputs)
		throws FileNotFoundException, ScriptException;

	/**
	 * Executes the script in the given file.
	 * 
	 * @param file File containing the script to execute.
	 * @param process If true, executes the script with pre- and postprocessing
	 *          steps from all available {@link PreprocessorPlugin}s and
	 *          {@link PostprocessorPlugin}s in the plugin index; if false,
	 *          executes the script with no pre- or postprocessing.
	 * @param inputMap Table of input parameter values, with keys matching the
	 *          plugin's input parameter names. Passing a value of a type
	 *          incompatible with the associated input parameter will issue an
	 *          error and ignore that value.
	 * @return {@link Future} of the module instance being executed. Calling
	 *         {@link Future#get()} will block until execution is complete.
	 */
	Future<ScriptModule> run(File file, boolean process,
		Map<String, Object> inputMap) throws FileNotFoundException, ScriptException;

	/**
	 * Executes the given script.
	 * 
	 * @param path Pseudo-path to the script. This is important mostly for the
	 *          path's file extension, which provides an important hint as to the
	 *          language of the script.
	 * @param script The script itself to execute.
	 * @param process If true, executes the script with pre- and postprocessing
	 *          steps from all available {@link PreprocessorPlugin}s and
	 *          {@link PostprocessorPlugin}s in the plugin index; if false,
	 *          executes the script with no pre- or postprocessing.
	 * @param inputs List of input parameter names and values. The expected order
	 *          is in pairs: an input name followed by its value, for each desired
	 *          input to populate. Leaving some inputs unpopulated is allowed.
	 *          Passing the name of an input that is not valid for the plugin, or
	 *          passing a value of a type incompatible with the associated input
	 *          parameter, will issue an error and ignore that name/value pair.
	 * @return {@link Future} of the module instance being executed. Calling
	 *         {@link Future#get()} will block until execution is complete.
	 */
	default Future<ScriptModule> run(final String path, final String script,
		final boolean process, final Object... inputs)
	{
		return run(path, new StringReader(script), process, inputs);
	}

	/**
	 * Executes the given script.
	 * 
	 * @param path Pseudo-path to the script. This is important mostly for the
	 *          path's file extension, which provides an important hint as to the
	 *          language of the script.
	 * @param script The script itself to execute.
	 * @param process If true, executes the script with pre- and postprocessing
	 *          steps from all available {@link PreprocessorPlugin}s and
	 *          {@link PostprocessorPlugin}s in the plugin index; if false,
	 *          executes the script with no pre- or postprocessing.
	 * @param inputMap Table of input parameter values, with keys matching the
	 *          plugin's input parameter names. Passing a value of a type
	 *          incompatible with the associated input parameter will issue an
	 *          error and ignore that value.
	 * @return {@link Future} of the module instance being executed. Calling
	 *         {@link Future#get()} will block until execution is complete.
	 */
	default Future<ScriptModule> run(final String path, final String script,
		final boolean process, final Map<String, Object> inputMap)
	{
		return run(path, new StringReader(script), process, inputMap);
	}

	/**
	 * Executes the given script.
	 * 
	 * @param path Pseudo-path to the script. This is important mostly for the
	 *          path's file extension, which provides an important hint as to the
	 *          language of the script.
	 * @param reader A stream providing the script contents.
	 * @param process If true, executes the script with pre- and postprocessing
	 *          steps from all available {@link PreprocessorPlugin}s and
	 *          {@link PostprocessorPlugin}s in the plugin index; if false,
	 *          executes the script with no pre- or postprocessing.
	 * @param inputs List of input parameter names and values. The expected order
	 *          is in pairs: an input name followed by its value, for each desired
	 *          input to populate. Leaving some inputs unpopulated is allowed.
	 *          Passing the name of an input that is not valid for the plugin, or
	 *          passing a value of a type incompatible with the associated input
	 *          parameter, will issue an error and ignore that name/value pair.
	 * @return {@link Future} of the module instance being executed. Calling
	 *         {@link Future#get()} will block until execution is complete.
	 */
	default Future<ScriptModule> run(final String path, final Reader reader,
		final boolean process, final Object... inputs)
	{
		return run(new ScriptInfo(getContext(), path, reader), process, inputs);
	}

	/**
	 * Executes the given script.
	 * 
	 * @param path Pseudo-path to the script. This is important mostly for the
	 *          path's file extension, which provides an important hint as to the
	 *          language of the script.
	 * @param reader A stream providing the script contents.
	 * @param process If true, executes the script with pre- and postprocessing
	 *          steps from all available {@link PreprocessorPlugin}s and
	 *          {@link PostprocessorPlugin}s in the plugin index; if false,
	 *          executes the script with no pre- or postprocessing.
	 * @param inputMap Table of input parameter values, with keys matching the
	 *          plugin's input parameter names. Passing a value of a type
	 *          incompatible with the associated input parameter will issue an
	 *          error and ignore that value.
	 * @return {@link Future} of the module instance being executed. Calling
	 *         {@link Future#get()} will block until execution is complete.
	 */
	default Future<ScriptModule> run(final String path, final Reader reader,
		final boolean process, final Map<String, Object> inputMap)
	{
		return run(new ScriptInfo(getContext(), path, reader), process, inputMap);
	}

	/**
	 * Executes the given script.
	 * 
	 * @param info The script to instantiate and run.
	 * @param process If true, executes the script with pre- and postprocessing
	 *          steps from all available {@link PreprocessorPlugin}s and
	 *          {@link PostprocessorPlugin}s in the plugin index; if false,
	 *          executes the script with no pre- or postprocessing.
	 * @param inputs List of input parameter names and values. The expected order
	 *          is in pairs: an input name followed by its value, for each desired
	 *          input to populate. Leaving some inputs unpopulated is allowed.
	 *          Passing the name of an input that is not valid for the plugin, or
	 *          passing a value of a type incompatible with the associated input
	 *          parameter, will issue an error and ignore that name/value pair.
	 * @return {@link Future} of the module instance being executed. Calling
	 *         {@link Future#get()} will block until execution is complete.
	 */
	Future<ScriptModule> run(ScriptInfo info, boolean process, Object... inputs);

	/**
	 * Executes the given script.
	 * 
	 * @param info The script to instantiate and run.
	 * @param process If true, executes the script with pre- and postprocessing
	 *          steps from all available {@link PreprocessorPlugin}s and
	 *          {@link PostprocessorPlugin}s in the plugin index; if false,
	 *          executes the script with no pre- or postprocessing.
	 * @param inputMap Table of input parameter values, with keys matching the
	 *          plugin's input parameter names. Passing a value of a type
	 *          incompatible with the associated input parameter will issue an
	 *          error and ignore that value.
	 * @return {@link Future} of the module instance being executed. Calling
	 *         {@link Future#get()} will block until execution is complete.
	 */
	Future<ScriptModule> run(ScriptInfo info, boolean process,
		Map<String, Object> inputMap);

	/** TODO */
	default boolean canHandleFile(final File file) {
		return getIndex().canHandleFile(file);
	}

	/** TODO */
	default boolean canHandleFile(final String fileName) {
		return getIndex().canHandleFile(fileName);
	}

	/** TODO */
	default void addAlias(final Class<?> type) {
		addAlias(type.getSimpleName(), type);
	}

	/** TODO */
	void addAlias(String alias, Class<?> type);

	/** TODO */
	Class<?> lookupClass(String typeName) throws ScriptException;

	// -- PTService methods --

	@Override
	default Class<ScriptLanguage> getPluginType() {
		return ScriptLanguage.class;
	}
}
