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

package org.scijava.app;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.scijava.event.EventHandler;
import org.scijava.log.LogService;
import org.scijava.platform.event.AppAboutEvent;
import org.scijava.platform.event.AppPreferencesEvent;
import org.scijava.platform.event.AppQuitEvent;
import org.scijava.plugin.AbstractSingletonService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.plugin.PluginService;
import org.scijava.service.Service;

/**
 * Default service for application-level functionality.
 * 
 * @author Curtis Rueden
 */
@Plugin(type = Service.class)
public class DefaultAppService extends AbstractSingletonService<App> implements AppService {

	@Parameter
	private LogService log;

	@Parameter
	private PluginService pluginService;

	/** Read-only table of SciJava applications. */
	private Map<String, App> apps;

	// -- AppService methods --

	@Override
	public App getApp(final String name) {
		return apps().get(name);
	}

	@Override
	public Map<String, App> getApps() {
		return apps();
	}

	// -- Event handlers --

	@EventHandler(key = "org.scijava.app.AppService#about")
	protected void onEvent(@SuppressWarnings("unused") final AppAboutEvent event)
	{
		getApp().about();
	}

	@EventHandler(key = "org.scijava.app.AppService#prefs")
	protected void onEvent(
		@SuppressWarnings("unused") final AppPreferencesEvent event)
	{
		getApp().prefs();
	}

	@EventHandler(key = "org.scijava.app.AppService#quit")
	protected void onEvent(@SuppressWarnings("unused") final AppQuitEvent event) {
		getApp().quit();
	}

	// -- Helper methods - lazy initialization --

	/** Gets {@link #apps}, initializing if necessary. */
	private Map<String, App> apps() {
		if (apps == null) initApps();
		return apps;
	}

	/** Initializes {@link #apps}. */
	private synchronized void initApps() {
		if (apps != null) return; // already initialized
		final HashMap<String, App> map = new HashMap<>();

		for (final App app : getInstances()) {
			final String name = app.getInfo().getName();
			if (!map.containsKey(name)) {
				// no (higher-priority) app with the same name exists
				map.put(name, app);
			}
		}
		apps = Collections.unmodifiableMap(map);
	}

}
