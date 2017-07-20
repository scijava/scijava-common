/*
 * #%L
 * SciJava Common shared library for SciJava software.
 * %%
 * Copyright (C) 2009 - 2017 Board of Regents of the University of
 * Wisconsin-Madison, Broad Institute of MIT and Harvard, Max Planck
 * Institute of Molecular Cell Biology and Genetics, University of
 * Konstanz, and KNIME GmbH.
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

package org.scijava.plugin;


import java.util.Collection;
import java.util.List;

import org.scijava.service.SciJavaService;

/**
 * Interface for service that keeps track of available plugins.
 * <p>
 * The plugin service keeps a master index of all plugins known to the system.
 * At heart, a plugin is a piece of functionality that extends a program's
 * capabilities. Plugins take many forms; see {@link SciJavaPlugin} for details.
 * </p>
 * <p>
 * The default plugin service discovers available plugins on the classpath.
 * </p>
 * 
 * @author Curtis Rueden
 * @see SciJavaPlugin
 */
public interface PluginService extends SciJavaService {

	/** Gets the index of available plugins. */
	PluginIndex getIndex();

	/**
	 * Rediscovers all plugins available on the classpath. Note that this will
	 * clear any individual plugins added programmatically.
	 */
	void reloadPlugins();

	/** Manually registers a plugin with the plugin service. */
	void addPlugin(PluginInfo<?> plugin);

	/** Manually registers plugins with the plugin service. */
	<T extends PluginInfo<?>> void addPlugins(Collection<T> plugins);

	/** Manually unregisters a plugin with the plugin service. */
	void removePlugin(PluginInfo<?> plugin);

	/** Manually unregisters plugins with the plugin service. */
	<T extends PluginInfo<?>> void removePlugins(Collection<T> plugins);

	/** Gets the list of known plugins. */
	List<PluginInfo<?>> getPlugins();

	/**
	 * Gets the first available plugin of the given class, or null if none.
	 * 
	 * @param <P> The <em>class</em> of the plugin to look up.
	 */
	<P extends SciJavaPlugin> PluginInfo<SciJavaPlugin>
		getPlugin(Class<P> pluginClass);

	/**
	 * Gets the first available plugin of the given class, or null if none.
	 * 
	 * @param <PT> The <em>type</em> of the plugin to look up; e.g.,
	 *          {@code Service.class}.
	 * @param <P> The <em>class</em> of the plugin to look up.
	 */
	<PT extends SciJavaPlugin, P extends PT> PluginInfo<PT>
		getPlugin(Class<P> pluginClass, Class<PT> type);

	/**
	 * Gets the first available plugin of the given class name, or null if none.
	 */
	PluginInfo<SciJavaPlugin> getPlugin(String className);

	/**
	 * Gets the list of plugins of the given type (e.g.,
	 * {@link org.scijava.service.Service}).
	 * 
	 * @param <PT> The <em>type</em> of plugins to look up; e.g.,
	 *          {@code Service.class}.
	 */
	<PT extends SciJavaPlugin> List<PluginInfo<PT>>
		getPluginsOfType(Class<PT> type);

	/**
	 * Gets the list of plugins of the given class.
	 * <p>
	 * Most classes will have only a single match, but some special classes (such
	 * as ImageJ's {@code LegacyCommand}) may match many entries.
	 * </p>
	 * <p>
	 * Note that this method will result in {@link PluginInfo}s with matching
	 * class <em>names</em> to load their plugin {@link Class}es so that they can
	 * be compared with the given one.
	 * </p>
	 * <p>
	 * NB: Classes are matched by strict equality, not assignability; subtypes of
	 * the specified class will not match. For this behavior, use
	 * {@link #getPluginsOfType(Class)} on a common parent interface.
	 * </p>
	 * 
	 * @param <P> The <em>class</em> of plugins to look up.
	 * @param pluginClass The class for which to obtain the list of matching
	 *          plugins.
	 */
	<P extends SciJavaPlugin> List<PluginInfo<SciJavaPlugin>>
		getPluginsOfClass(Class<P> pluginClass);

	/**
	 * Gets the list of plugins of the given class.
	 * <p>
	 * Most classes will have only a single match, but some special classes (such
	 * as ImageJ's {@code LegacyCommand}) may match many entries.
	 * </p>
	 * <p>
	 * Note that this method will result in {@link PluginInfo}s with matching
	 * class <em>names</em> and types to load their plugin {@link Class}es so that
	 * they can be compared with the given one.
	 * </p>
	 * <p>
	 * NB: Classes are matched by strict equality, not assignability; subtypes of
	 * the specified class will not match. For this behavior, use
	 * {@link #getPluginsOfType(Class)} on a common parent interface.
	 * </p>
	 * 
	 * @param <PT> The <em>type</em> of plugins to look up; e.g.,
	 *          {@code Service.class}.
	 * @param <P> The <em>class</em> of plugins to look up.
	 * @param pluginClass The class for which to obtain the list of matching
	 *          plugins.
	 * @param type The <em>type</em> of plugins to which the search should be
	 *          limited.
	 */
	<PT extends SciJavaPlugin, P extends PT> List<PluginInfo<PT>>
		getPluginsOfClass(Class<P> pluginClass, Class<PT> type);

	/**
	 * Gets the list of plugins with the given class name.
	 * <p>
	 * Most classes will have only a single match, but some special classes (such
	 * as ImageJ's {@code LegacyCommand}) may match many entries.
	 * </p>
	 * <p>
	 * NB: Classes are matched by strict equality, not assignability; subtypes of
	 * the specified class will not match. For this behavior, use
	 * {@link #getPluginsOfType(Class)} on a common parent interface.
	 * </p>
	 * 
	 * @param className The class name for which to obtain the list of matching
	 *          plugins.
	 */
	List<PluginInfo<SciJavaPlugin>> getPluginsOfClass(String className);

	/**
	 * Gets the list of plugins with the given class name.
	 * <p>
	 * Most classes will have only a single match, but some special classes (such
	 * as ImageJ's {@code LegacyCommand}) may match many entries.
	 * </p>
	 * <p>
	 * NB: Classes are matched by strict equality, not assignability; subtypes of
	 * the specified class will not match. For this behavior, use
	 * {@link #getPluginsOfType(Class)} on a common parent interface.
	 * </p>
	 * 
	 * @param <PT> The <em>type</em> of plugins to look up; e.g.,
	 *          {@code Service.class}.
	 * @param className The class name for which to obtain the list of matching
	 *          plugins.
	 * @param type The <em>type</em> of plugins to which the search should be
	 *          limited.
	 */
	<PT extends SciJavaPlugin> List<PluginInfo<SciJavaPlugin>>
		getPluginsOfClass(final String className, final Class<PT> type);

	/**
	 * Creates one instance each of the available plugins of the given type.
	 * <p>
	 * Note that in the case of commands, this method does <em>not</em> do any
	 * preprocessing on the command instances, so parameters will not be
	 * auto-populated, initializers will not be executed, etc.
	 * </p>
	 * 
	 * @param <PT> The <em>type</em> of plugins to instantiate; e.g.,
	 *          {@code Service.class}.
	 */
	<PT extends SciJavaPlugin> List<PT> createInstancesOfType(Class<PT> type);

	/**
	 * Creates an instance of each of the plugins on the given list.
	 * <p>
	 * If the plugin implements the {@link org.scijava.Contextual} interface, the
	 * appropriate context is injected. Similarly, if the plugin implements the
	 * {@link org.scijava.Prioritized} interface, the appropriate priority is
	 * injected.
	 * </p>
	 * <p>
	 * Note that in the case of commands, this method does <em>not</em> do any
	 * preprocessing on the command instances, so parameters will not be
	 * auto-populated, initializers will not be executed, etc.
	 * </p>
	 * 
	 * @param <PT> The <em>type</em> of plugins to instantiate; e.g.,
	 *          {@code Service.class}.
	 */
	<PT extends SciJavaPlugin> List<PT>
		createInstances(List<PluginInfo<PT>> infos);

	/**
	 * Creates an instance of the given plugin.
	 * <p>
	 * If the plugin implements the {@link org.scijava.Contextual} interface, the
	 * appropriate context is injected. Similarly, if the plugin implements the
	 * {@link org.scijava.Prioritized} interface, the appropriate priority is
	 * injected.
	 * </p>
	 * <p>
	 * Note that in the case of commands, this method does <em>not</em> do any
	 * preprocessing on the command instances, so parameters will not be
	 * auto-populated, initializers will not be executed, etc.
	 * </p>
	 * 
	 * @param <PT> The <em>type</em> of plugin to instantiate; e.g.,
	 *          {@code Service.class}.
	 */
	<PT extends SciJavaPlugin> PT createInstance(PluginInfo<PT> info);

}
