/*
 * #%L
 * SciJava Common shared library for SciJava software.
 * %%
 * Copyright (C) 2009 - 2015 Board of Regents of the University of
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

package org.scijava.plugin;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.scijava.log.LogService;
import org.scijava.object.LazyObjects;
import org.scijava.object.ObjectService;

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

	// TODO: Listen for PluginsAddedEvent and PluginsRemovedEvent
	// and update the map of singletons accordingly.

	private Map<Class<? extends PT>, PT> instanceMap;

	// -- SingletonService methods --

	@Override
	public List<PT> getInstances() {
		final List<PT> plugins = objectService.getObjects(getPluginType());
		return Collections.unmodifiableList(plugins);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <P extends PT> P getInstance(final Class<P> pluginClass) {
		if (instanceMap == null) initInstances();
		return (P) instanceMap.get(pluginClass);
	}

	// -- Service methods --

	@Override
	public void initialize() {
		// add singleton instances to the object index... IN THE FUTURE!
		objectService.getIndex().addLater(new LazyObjects<PT>() {

			@Override
			public List<PT> get() {
				return createInstances();
			}

			@Override
			public Class<?> getType() {
				return getPluginType();
			}

		});
	}

	// -- Internal methods --

	/**
	 * Allows subclasses to exclude instances.
	 * 
	 * @param list the initial list of instances
	 * @return the filtered list of instances
	 */
	protected List<PT> filterInstances(final List<PT> list) {
		return list;
	}

	// -- Helper methods --

	private synchronized void initInstances() {
		if (instanceMap != null) return;

		final HashMap<Class<? extends PT>, PT> map =
			new HashMap<Class<? extends PT>, PT>();

		final List<PT> list = getInstances();
		for (final PT plugin : list) {
			@SuppressWarnings("unchecked")
			final Class<? extends PT> ptClass =
				(Class<? extends PT>) plugin.getClass();
			map.put(ptClass, plugin);
		}

		instanceMap = map;
	}

	private List<PT> createInstances() {
		final List<PT> instances =
			filterInstances(getPluginService().createInstancesOfType(getPluginType()));

		log.info("Found " + instances.size() + " " +
			getPluginType().getSimpleName() + " plugins.");

		return instances;
	}

}
