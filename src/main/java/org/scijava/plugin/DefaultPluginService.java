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

package org.scijava.plugin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.scijava.InstantiableException;
import org.scijava.event.EventService;
import org.scijava.log.LogService;
import org.scijava.plugin.event.PluginsAddedEvent;
import org.scijava.plugin.event.PluginsRemovedEvent;
import org.scijava.service.AbstractService;
import org.scijava.service.Service;
import org.scijava.util.ListUtils;

/**
 * Default service for keeping track of available plugins.
 * <p>
 * Available plugins are discovered using indexes generated by using
 * scijava-common as annotation processor. Loading of the actual plugin
 * classes can be deferred until a particular plugin is actually needed.
 * </p>
 * <p>
 * Plugins are added or removed via the plugin service are reported via the
 * event service. (No events are published for plugins directly added to or
 * removed from the {@link PluginIndex}.)
 * </p>
 * 
 * @author Curtis Rueden
 * @author Johannes Schindelin
 * @see SciJavaPlugin
 * @see Plugin
 */
@Plugin(type = Service.class)
public class DefaultPluginService extends AbstractService implements
	PluginService
{

	@Parameter
	private LogService log;

	@Parameter
	private EventService eventService;

	/** Index of registered plugins. */
	private PluginIndex pluginIndex;

	// -- PluginService methods --

	@Override
	public PluginIndex getIndex() {
		return pluginIndex;
	}

	@Override
	public void reloadPlugins() {
		// clear all old plugins, and notify interested parties
		final List<PluginInfo<?>> oldPlugins = pluginIndex.getAll();
		pluginIndex.clear();
		if (oldPlugins.size() > 0) {
			eventService.publish(new PluginsRemovedEvent(oldPlugins));
		}

		// re-discover all available plugins, and notify interested parties
		pluginIndex.discover();
		final List<PluginInfo<?>> newPlugins = pluginIndex.getAll();
		if (newPlugins.size() > 0) {
			eventService.publish(new PluginsAddedEvent(newPlugins));
		}

		logExceptions();
	}

	@Override
	public void addPlugin(final PluginInfo<?> plugin) {
		if (pluginIndex.add(plugin)) {
			eventService.publish(new PluginsAddedEvent(plugin));
		}
	}

	@Override
	public <T extends PluginInfo<?>> void
		addPlugins(final Collection<T> plugins)
	{
		if (pluginIndex.addAll(plugins)) {
			eventService.publish(new PluginsAddedEvent(plugins));
		}
	}

	@Override
	public void removePlugin(final PluginInfo<?> plugin) {
		if (pluginIndex.remove(plugin)) {
			eventService.publish(new PluginsRemovedEvent(plugin));
		}
	}

	@Override
	public <T extends PluginInfo<?>> void removePlugins(
		final Collection<T> plugins)
	{
		if (pluginIndex.removeAll(plugins)) {
			eventService.publish(new PluginsRemovedEvent(plugins));
		}
	}

	@Override
	public List<PluginInfo<?>> getPlugins() {
		return pluginIndex.getAll();
	}

	@Override
	public <P extends SciJavaPlugin> PluginInfo<SciJavaPlugin> getPlugin(
		final Class<P> pluginClass)
	{
		return ListUtils.first(getPluginsOfClass(pluginClass));
	}

	@Override
	public <PT extends SciJavaPlugin, P extends PT> PluginInfo<PT>
		getPlugin(final Class<P> pluginClass, final Class<PT> type)
	{
		return ListUtils.first(getPluginsOfClass(pluginClass, type));
	}

	@Override
	public PluginInfo<SciJavaPlugin> getPlugin(final String className) {
		return ListUtils.first(getPluginsOfClass(className));
	}

	@Override
	public <PT extends SciJavaPlugin> List<PluginInfo<PT>> getPluginsOfType(
		final Class<PT> type)
	{
		return pluginIndex.getPlugins(type);
	}

	@Override
	public <P extends SciJavaPlugin> List<PluginInfo<SciJavaPlugin>>
		getPluginsOfClass(final Class<P> pluginClass)
	{
		// NB: We must scan *all* plugins for a match. In theory, the same plugin
		// Class could be associated with multiple PluginInfo entries of differing
		// type anyway. If performance of this method is insufficient, the solution
		// will be to rework the PluginIndex data structure to include an index on
		// plugin class names.
		return getPluginsOfClass(pluginClass, SciJavaPlugin.class);
	}

	@Override
	public <PT extends SciJavaPlugin, P extends PT> List<PluginInfo<PT>>
		getPluginsOfClass(final Class<P> pluginClass, final Class<PT> type)
	{
		final ArrayList<PluginInfo<PT>> result = new ArrayList<>();
		findPluginsOfClass(pluginClass, getPluginsOfType(type), result);
		filterNonmatchingClasses(pluginClass, result);
		return result;
	}

	@Override
	public List<PluginInfo<SciJavaPlugin>> getPluginsOfClass(
		final String className)
	{
		// NB: Since we cannot load the class in question, and cannot know its type
		// hierarch(y/ies) even if we did, we must scan *all* plugins for a match.
		return getPluginsOfClass(className, SciJavaPlugin.class);
	}

	@Override
	public <PT extends SciJavaPlugin> List<PluginInfo<SciJavaPlugin>>
		getPluginsOfClass(final String className, final Class<PT> type)
	{
		final ArrayList<PluginInfo<SciJavaPlugin>> result =
			new ArrayList<>();
		findPluginsOfClass(className, getPluginsOfType(type), result);
		return result;
	}

	@Override
	public <PT extends SciJavaPlugin> List<PT> createInstancesOfType(
		final Class<PT> type)
	{
		final List<PluginInfo<PT>> plugins = getPluginsOfType(type);
		return createInstances(plugins);
	}

	@Override
	public <PT extends SciJavaPlugin> List<PT> createInstances(
		final List<PluginInfo<PT>> infos)
	{
		final ArrayList<PT> list = new ArrayList<>();
		for (final PluginInfo<? extends PT> info : infos) {
			final PT p = createInstance(info);
			if (p != null) list.add(p);
		}
		return list;
	}

	@Override
	public <PT extends SciJavaPlugin> PT
		createInstance(final PluginInfo<PT> info)
	{
		try {
			final PT p = info.createInstance();
			context().inject(p);
			return p;
		}
		catch (final Throwable t) {
			final String errorMessage = //
				"Cannot create plugin: " + info.getClassName();
			if (log.isDebug()) log.debug(errorMessage, t);
			else log.error(errorMessage);
		}
		return null;
	}

	// -- Service methods --

	@Override
	public void initialize() {
		pluginIndex = context().getPluginIndex();

		log.debug("Found " + pluginIndex.size() + " plugins.");
		if (log.isDebug()) {
			for (final PluginInfo<?> info : pluginIndex) {
				log.debug("- " + info);
			}
		}

		logExceptions();

		super.initialize();
	}

	// -- Utility methods --

	/**
	 * Transfers plugins of the given class from the source list to the
	 * destination list. Note that because this method compares class name
	 * strings, it does not need to actually load the class in question.
	 * 
	 * @param className The class name of the desired plugins.
	 * @param srcList The list to scan for matching plugins.
	 * @param destList The list to which matching plugins are added.
	 */
	public static <T extends PluginInfo<?>> void findPluginsOfClass(
		final String className, final List<? extends PluginInfo<?>> srcList,
		final List<T> destList)
	{
		for (final PluginInfo<?> info : srcList) {
			if (info.getClassName().equals(className)) {
				@SuppressWarnings("unchecked")
				final T match = (T) info;
				destList.add(match);
			}
		}
	}

	/**
	 * Gets the plugin type of the given plugin class, as declared by its
	 * {@code @Plugin} annotation (i.e., {@link Plugin#type()}).
	 * 
	 * @param pluginClass The plugin class whose plugin type is needed.
	 * @return The plugin type, or null if no {@link Plugin} annotation exists for
	 *         the given class.
	 */
	public static <PT extends SciJavaPlugin, P extends PT> Class<PT> getPluginType(
		final Class<P> pluginClass)
	{
		final Plugin annotation = pluginClass.getAnnotation(Plugin.class);
		if (annotation == null) return null;
		@SuppressWarnings("unchecked")
		final Class<PT> type = (Class<PT>) annotation.type();
		return type;
	}

	// -- Helper methods --

	/**
	 * Transfers plugins of the given class from the source list to the
	 * destination list. Note that because this method compares class objects, it
	 * <em>must</em> load the classes in question.
	 * 
	 * @param pluginClass The class of the desired plugins.
	 * @param srcList The list to scan for matching plugins.
	 * @param destList The list to which matching plugins are added.
	 */
	private <T extends PluginInfo<?>> void findPluginsOfClass(
		final Class<?> pluginClass, final List<? extends PluginInfo<?>> srcList,
		final List<T> destList)
	{
		final String className = pluginClass.getName();
		for (final PluginInfo<?> info : srcList) {
			try {
				final Class<?> clazz2 = info.getPluginClass();
				if (clazz2 == pluginClass ||
					(info.getClassName().equals(className) && info.loadClass() == pluginClass))
				{
					@SuppressWarnings("unchecked")
					final T match = (T) info;
					destList.add(match);
				}
			}
			catch (InstantiableException exc) {
				log.debug("Ignoring plugin: " + info, exc);
			}
		}
	}

	/**
	 * Filters the given list to include only entries with matching
	 * <em>classes</em> (not just class <em>names</em>).
	 */
	private <PT extends SciJavaPlugin, P extends PT> void
		filterNonmatchingClasses(final Class<P> pluginClass,
			final ArrayList<PluginInfo<PT>> result)
	{
		for (final Iterator<PluginInfo<PT>> iter = result.iterator();
			iter.hasNext(); )
		{
			try {
				if (iter.next().loadClass() != pluginClass) iter.remove();
			}
			catch (InstantiableException exc) {
				log.debug(exc);
				iter.remove();
			}
		}
	}

	/** Logs any exceptions that occurred during the last plugin discovery. */
	private void logExceptions() {
		final Map<String, Throwable> exceptions = pluginIndex.getExceptions();
		final int excCount = exceptions.size();
		if (excCount > 0) {
			log.warn(excCount + " exceptions occurred during plugin discovery.");
			if (log.isDebug()) {
				for (final String name : exceptions.keySet()) {
					final Throwable t = exceptions.get(name);
					log.debug(name, t);
				}
			}
		}
	}

}
