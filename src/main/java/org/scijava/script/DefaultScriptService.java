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
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

import javax.script.ScriptException;

import org.scijava.Context;
import org.scijava.Gateway;
import org.scijava.InstantiableException;
import org.scijava.MenuPath;
import org.scijava.Priority;
import org.scijava.app.AppService;
import org.scijava.command.CommandService;
import org.scijava.log.LogService;
import org.scijava.module.Module;
import org.scijava.module.ModuleService;
import org.scijava.object.LazyObjects;
import org.scijava.parse.ParseService;
import org.scijava.plugin.AbstractSingletonService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.plugin.PluginService;
import org.scijava.plugin.SciJavaPlugin;
import org.scijava.service.Service;
import org.scijava.util.ClassUtils;
import org.scijava.util.ColorRGB;
import org.scijava.util.ColorRGBA;

/**
 * Default service for working with scripts.
 * 
 * @author Johannes Schindelin
 * @author Curtis Rueden
 */
@Plugin(type = Service.class, priority = Priority.HIGH_PRIORITY)
public class DefaultScriptService extends
	AbstractSingletonService<ScriptLanguage> implements ScriptService
{

	@Parameter
	private PluginService pluginService;

	@Parameter
	private ModuleService moduleService;

	@Parameter
	private CommandService commandService;

	@Parameter
	private AppService appService;

	@Parameter
	private ParseService parser;

	@Parameter
	private LogService log;

	/** Index of registered scripting languages. */
	private ScriptLanguageIndex scriptLanguageIndex;

	/** List of directories to scan for scripts. */
	private ArrayList<File> scriptDirs;

	/** Menu prefix to use for each script directory, if any. */
	private HashMap<File, MenuPath> menuPrefixes;

	/** Index of available scripts, by script path. */
	private HashMap<String, ScriptInfo> scripts;

	/** Table of short type names to associated {@link Class}. */
	private HashMap<String, Class<?>> aliasMap;

	// -- ScriptService methods - scripting languages --

	@Override
	public ScriptLanguageIndex getIndex() {
		return scriptLanguageIndex();
	}

	// -- ScriptService methods - scripts --

	@Override
	public List<File> getScriptDirectories() {
		return Collections.unmodifiableList(scriptDirs());
	}

	@Override
	public MenuPath getMenuPrefix(final File scriptDirectory) {
		return menuPrefixes().get(scriptDirectory);
	}

	@Override
	public void addScriptDirectory(final File scriptDirectory) {
		scriptDirs().add(scriptDirectory);
	}

	@Override
	public void addScriptDirectory(final File scriptDirectory,
		final MenuPath menuPrefix)
	{
		scriptDirs().add(scriptDirectory);
		menuPrefixes().put(scriptDirectory, menuPrefix);
	}

	@Override
	public void removeScriptDirectory(final File scriptDirectory) {
		scriptDirs().remove(scriptDirectory);
	}

	@Override
	public Collection<ScriptInfo> getScripts() {
		return Collections.unmodifiableCollection(scripts().values());
	}

	@Override
	public ScriptInfo getScript(final File scriptFile) {
		return getOrCreate(scriptFile);
	}

	@Override
	public Future<ScriptModule> run(final File file, final boolean process,
		final Object... inputs)
	{
		return run(getOrCreate(file), process, inputs);
	}

	@Override
	public Future<ScriptModule> run(final File file, final boolean process,
		final Map<String, Object> inputMap)
	{
		return run(getOrCreate(file), process, inputMap);
	}

	@Override
	public Future<ScriptModule> run(final ScriptInfo info, final boolean process,
		final Object... inputs)
	{
		return cast(moduleService.run(info, process, inputs));
	}

	@Override
	public Future<ScriptModule> run(final ScriptInfo info, final boolean process,
		final Map<String, Object> inputMap)
	{
		return cast(moduleService.run(info, process, inputMap));
	}

	@Override
	public void addAlias(final String alias, final Class<?> type) {
		aliasMap().put(alias, type);
	}

	@Override
	public synchronized Class<?> lookupClass(final String alias)
		throws ScriptException
	{
		final String componentAlias = stripArrayNotation(alias);
		final Class<?> type = aliasMap().get(componentAlias);
		if (type != null) {
			final int arrayDim = (alias.length() - componentAlias.length()) / 2;
			return makeArrayType(type, arrayDim);
		}

		try {
			final Class<?> c = ClassUtils.loadClass(alias, false);
			aliasMap().put(alias, c);
			return c;
		}
		catch (final IllegalArgumentException exc) {
			final ScriptException se = new ScriptException("Unknown type: " + alias);
			se.initCause(exc);
			throw se;
		}
	}

	// -- Service methods --

	@Override
	public void initialize() {
		super.initialize();

		// add scripts to the module index... only when needed!
		moduleService.getIndex().addLater(new LazyObjects<ScriptInfo>() {

			@Override
			public Collection<ScriptInfo> get() {
				return scripts().values();
			}

		});
	}

	// -- Helper methods - lazy initialization --

	/** Gets {@link #scriptLanguageIndex}, initializing if needed. */
	private ScriptLanguageIndex scriptLanguageIndex() {
		if (scriptLanguageIndex == null) initScriptLanguageIndex();
		return scriptLanguageIndex;
	}

	/** Gets {@link #scriptDirs}, initializing if needed. */
	private List<File> scriptDirs() {
		if (scriptDirs == null) initScriptDirs();
		return scriptDirs;
	}

	/** Gets {@link #menuPrefixes}, initializing if needed. */
	private HashMap<File, MenuPath> menuPrefixes() {
		if (menuPrefixes == null) initMenuPrefixes();
		return menuPrefixes;
	}

	/** Gets {@link #scripts}, initializing if needed. */
	private HashMap<String, ScriptInfo> scripts() {
		if (scripts == null) initScripts();
		return scripts;
	}

	/** Gets {@link #aliasMap}, initializing if needed. */
	private HashMap<String, Class<?>> aliasMap() {
		if (aliasMap == null) initAliasMap();
		return aliasMap;
	}

	/** Initializes {@link #scriptLanguageIndex}. */
	private synchronized void initScriptLanguageIndex() {
		if (scriptLanguageIndex != null) return; // already initialized

		final ScriptLanguageIndex index = new ScriptLanguageIndex(log);

		// add ScriptLanguage plugins
		for (final ScriptLanguage language : getInstances()) {
			index.add(language, true);
		}

		scriptLanguageIndex = index;
	}

	/** Initializes {@link #scriptDirs}. */
	private synchronized void initScriptDirs() {
		if (scriptDirs != null) return;

		final ArrayList<File> dirs = new ArrayList<>();

		// append default script directories
		final File baseDir = appService.getApp().getBaseDirectory();
		dirs.add(new File(baseDir, SCRIPTS_RESOURCE_DIR));

		// append additional script directories from system property
		final String scriptsPath = System.getProperty(SCRIPTS_PATH_PROPERTY);
		if (scriptsPath != null) {
			for (final String dir : scriptsPath.split(File.pathSeparator)) {
				dirs.add(new File(dir));
			}
		}

		scriptDirs = dirs;
	}

	/** Initializes {@link #menuPrefixes}. */
	private synchronized void initMenuPrefixes() {
		if (menuPrefixes != null) return;
		menuPrefixes = new HashMap<>();
	}

	/** Initializes {@link #scripts}. */
	private synchronized void initScripts() {
		if (scripts != null) return; // already initialized

		final HashMap<String, ScriptInfo> map = new HashMap<>();

		final ArrayList<ScriptInfo> scriptList = new ArrayList<>();
		new ScriptFinder(context()).findScripts(scriptList);

		for (final ScriptInfo info : scriptList) {
			map.put(info.getPath(), info);
		}

		scripts = map;
	}

	/** Initializes {@link #aliasMap}. */
	private synchronized void initAliasMap() {
		if (aliasMap != null) return; // already initialized

		final HashMap<String, Class<?>> map = new HashMap<>();

		// primitives
		addAliases(map, boolean.class, byte.class, char.class, double.class,
			float.class, int.class, long.class, short.class);

		// primitive wrappers
		addAliases(map, Boolean.class, Byte.class, Character.class, Double.class,
			Float.class, Integer.class, Long.class, Short.class);

		// built-in types
		addAliases(map, Context.class, BigDecimal.class, BigInteger.class,
			ColorRGB.class, ColorRGBA.class, Date.class, File.class, String.class);

		// service types
		addAliases(map, pluginClasses(Service.class));

		// gateway types
		addAliases(map, pluginClasses(Gateway.class));

		aliasMap = map;
	}

	// -- Helper methods - run --

	/**
	 * Gets a {@link ScriptInfo} for the given file, creating a new one if none
	 * are registered with the service.
	 */
	private ScriptInfo getOrCreate(final File file) {
		final ScriptInfo info = scripts().get(file);
		if (info != null) return info;
		return new ScriptInfo(getContext(), file);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Future<ScriptModule> cast(final Future<Module> future) {
		return (Future) future;
	}

	// -- Helper methods - aliases --

	private void addAliases(final HashMap<String, Class<?>> map,
		final Class<?>... types)
	{
		for (final Class<?> type : types) {
			addAlias(map, type);
		}
	}

	private void
		addAlias(final HashMap<String, Class<?>> map, final Class<?> type)
	{
		if (type == null) return;
		map.put(type.getSimpleName(), type);
		// NB: Recursively add supertypes.
		addAlias(map, type.getSuperclass());
		addAliases(map, type.getInterfaces());
	}

	private Class<?>[] pluginClasses(final Class<? extends SciJavaPlugin> type) {
		return pluginService.getPluginsOfType(type).stream().map(info -> {
			try {
				return info.loadClass();
			}
			catch (final InstantiableException exc) {
				log.warn("Invalid class: " + info.getClassName(), exc);
				return null;
			}
		}).toArray(Class<?>[]::new);
	}

	private String stripArrayNotation(final String alias) {
		if (!alias.endsWith("[]")) return alias;
		return stripArrayNotation(alias.substring(0, alias.length() - 2));
	}

	private Class<?> makeArrayType(final Class<?> type, final int arrayDim) {
		if (arrayDim <= 0) return type;
		return makeArrayType(ClassUtils.getArrayClass(type), arrayDim - 1);
	}

}
