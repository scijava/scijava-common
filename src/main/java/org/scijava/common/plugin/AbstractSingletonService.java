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

package org.scijava.common.plugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.scijava.common.event.EventHandler;
import org.scijava.common.log.LogService;
import org.scijava.common.object.ObjectService;
import org.scijava.common.plugin.event.PluginsAddedEvent;
import org.scijava.common.plugin.event.PluginsRemovedEvent;

/**
 * Abstract base class for {@link SingletonService}s.
 * 
 * @author Curtis Rueden
 * @param <PT> Plugin type of the {@link SingletonPlugin}s being managed.
 */
public abstract class AbstractSingletonService<PT extends SingletonPlugin>
	extends AbstractPTService<PT> implements SingletonService<PT>
{

	@Parameter
	private LogService log;

	@Parameter
	private ObjectService objectService;

	/** List of singleton plugin instances. */
	private List<PT> instances;

	private Map<Class<? extends PT>, PT> instanceMap;

	// -- SingletonService methods --

	@Override
	public ObjectService objectService() {
		return objectService;
	}

	@Override
	public List<PT> getInstances() {
		if (instances == null) initInstances();
		return Collections.unmodifiableList(instances);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <P extends PT> P getInstance(final Class<P> pluginClass) {
		if (instanceMap == null) initInstances();
		return (P) instanceMap.get(pluginClass);
	}

//-- Event handlers --

	@EventHandler
	protected void onEvent(final PluginsRemovedEvent event) {
		if (instanceMap == null) return;
		for (final PluginInfo<?> info : event.getItems()) {
			final PT obj = instanceMap.remove(info.getPluginClass());
			if (obj != null) { // we actually removed a plugin
				instances.remove(obj);
				objectService.removeObject(obj);
			}
		}
	}

	@EventHandler
	protected void onEvent(final PluginsAddedEvent event) {
		if (instanceMap == null) return;
		// collect singleton plugins
		final List<PluginInfo<PT>> singletons = new ArrayList<>();
		for (final PluginInfo<?> pluginInfo : event.getItems()) {
			if (getPluginType().isAssignableFrom(pluginInfo.getPluginType())) {
				@SuppressWarnings("unchecked")
				final PT plugin = pluginService().createInstance(
					(PluginInfo<PT>) pluginInfo);
				@SuppressWarnings("unchecked")
				final Class<? extends PT> pluginClass = (Class<? extends PT>) plugin
					.getClass();
				instanceMap.put(pluginClass, plugin);
				instances.add(plugin);
			}
		}

		for (final PluginInfo<PT> pluginInfo : singletons) {
			final PT plugin = pluginService().createInstance(pluginInfo);
			@SuppressWarnings("unchecked")
			final Class<? extends PT> pluginClass = (Class<? extends PT>) plugin
				.getClass();
			instanceMap.put(pluginClass, plugin);
			instances.add(plugin);
		}

	}

	// -- Helper methods --

	private synchronized void initInstances() {
		if (instances != null) return;

		@SuppressWarnings("unchecked")
		final List<PT> list = (List<PT>) filterInstances(pluginService()
			.createInstancesOfType(getPluginType()));

		final Map<Class<? extends PT>, PT> map = new HashMap<>();

		for (final PT plugin : list) {
			@SuppressWarnings("unchecked")
			final Class<? extends PT> ptClass = //
				(Class<? extends PT>) plugin.getClass();
			map.put(ptClass, plugin);
		}

		log.debug("Found " + list.size() + " " + getPluginType().getSimpleName() +
			" plugins.");

		instanceMap = map;
		instances = list;
	}

}
