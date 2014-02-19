/*
 * #%L
 * SciJava Common shared library for SciJava software.
 * %%
 * Copyright (C) 2009 - 2014 Board of Regents of the University of
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

package org.scijava;

import org.scijava.app.App;
import org.scijava.app.AppService;
import org.scijava.app.SciJavaApp;
import org.scijava.app.StatusService;
import org.scijava.event.EventHistory;
import org.scijava.event.EventService;
import org.scijava.log.LogService;
import org.scijava.object.ObjectService;
import org.scijava.plugin.AbstractRichPlugin;
import org.scijava.plugin.PluginService;
import org.scijava.service.Service;
import org.scijava.thread.ThreadService;

/**
 * Abstract superclass for {@link Gateway} implementations.
 * 
 * @author Mark Hiner
 * @author Curtis Rueden
 */
public abstract class AbstractGateway extends AbstractRichPlugin implements
	Gateway
{

	private final String appName;

	// -- Constructor --

	public AbstractGateway() {
		this(SciJavaApp.NAME, null);
	}

	public AbstractGateway(final String appName, final Context context) {
		this.appName = appName;
		if (context != null) setContext(context);
	}

	// -- Gateway methods --

	@Override
	public <S extends Service> S get(final Class<S> serviceClass) {
		return context().service(serviceClass);
	}

	@Override
	public Service get(final String serviceClassName) {
		return context().service(serviceClassName);
	}

	// -- Gateway methods - services --

	@Override
	public AppService app() {
		return get(AppService.class);
	}

	@Override
	public EventHistory eventHistory() {
		return get(EventHistory.class);
	}

	@Override
	public EventService event() {
		return get(EventService.class);
	}

	@Override
	public LogService log() {
		return get(LogService.class);
	}

	@Override
	public ObjectService object() {
		return get(ObjectService.class);
	}

	@Override
	public PluginService plugin() {
		return get(PluginService.class);
	}

	@Override
	public StatusService status() {
		return get(StatusService.class);
	}

	@Override
	public ThreadService thread() {
		return get(ThreadService.class);
	}

	// -- Gateway methods - application --

	@Override
	public App getApp() {
		return app().getApp(appName);
	}

	@Override
	public String getTitle() {
		return getApp().getTitle();
	}

	@Override
	public String getVersion() {
		return getApp().getVersion();
	}

	@Override
	public String getInfo(final boolean mem) {
		return getApp().getInfo(mem);
	}

}
