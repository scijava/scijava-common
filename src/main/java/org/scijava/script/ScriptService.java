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
import java.io.IOException;
import java.io.Reader;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

import javax.script.ScriptEngineFactory;
import javax.script.ScriptException;

import org.scijava.MenuPath;
import org.scijava.module.process.PostprocessorPlugin;
import org.scijava.module.process.PreprocessorPlugin;
import org.scijava.plugin.Plugin;
import org.scijava.plugin.SingletonService;
import org.scijava.service.SciJavaService;

/**
 * Interface for service that works with scripting languages. This service
 * discovers available scripting languages, and provides convenience methods to
 * interact with them.
 * 
 * @author Johannes Schindelin
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

	// -- Scripting languages --

	/** Gets the index of available scripting languages. */
	ScriptLanguageIndex getIndex();

	/**
	 * Gets the available scripting languages.
	 * <p>
	 * This method does the same thing as {@link #getInstances()}.
	 * </p>
	 */
	List<ScriptLanguage> getLanguages();

	/** Gets the scripting language that handles the given file extension. */
	ScriptLanguage getLanguageByExtension(String extension);

	/** Gets the scripting language with the given name. */
	ScriptLanguage getLanguageByName(String name);

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
	Future<ScriptModule> run(String path, String script, boolean process,
		Object... inputs) throws IOException, ScriptException;

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
	Future<ScriptModule> run(String path, String script, boolean process,
		Map<String, Object> inputMap) throws IOException, ScriptException;

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
	Future<ScriptModule> run(String path, Reader reader, boolean process,
		Object... inputs) throws IOException, ScriptException;

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
	Future<ScriptModule> run(String path, Reader reader, boolean process,
		Map<String, Object> inputMap) throws IOException, ScriptException;

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
	boolean canHandleFile(File file);

	/** TODO */
	boolean canHandleFile(String fileName);

	/** TODO */
	void addAlias(Class<?> type);

	/** TODO */
	void addAlias(String alias, Class<?> type);

	/** TODO */
	Class<?> lookupClass(String typeName) throws ScriptException;

}
