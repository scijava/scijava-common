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

package org.scijava.common.platform;

import java.util.Collections;
import java.util.List;

import org.scijava.common.Priority;
import org.scijava.common.app.App;
import org.scijava.common.app.AppService;
import org.scijava.common.command.Command;
import org.scijava.common.platform.event.AppAboutEvent;
import org.scijava.common.platform.event.AppPreferencesEvent;
import org.scijava.common.platform.event.AppQuitEvent;
import org.scijava.common.plugin.Parameter;
import org.scijava.common.plugin.Plugin;
import org.scijava.common.service.AbstractService;
import org.scijava.common.service.Service;

/** @deprecated Use {@link AppService} and {@link App} instead. */
@Deprecated
@Plugin(type = Service.class, priority = Priority.LOW)
public class DefaultAppEventService extends AbstractService implements
	AppEventService
{

	@Parameter
	private AppService appService;

	// -- AppService methods --

	@Override
	public void about() {
		appService.getApp().about();
	}

	@Override
	public void prefs() {
		appService.getApp().prefs();
	}

	@Override
	public void quit() {
		appService.getApp().quit();
	}

	@Override
	public List<Class<? extends Command>> getCommands() {
		return Collections.emptyList();
	}

	@Deprecated
	protected void onEvent(@SuppressWarnings("unused") final AppAboutEvent event)
	{
		about();
	}

	@Deprecated
	protected void onEvent(
		@SuppressWarnings("unused") final AppPreferencesEvent event)
	{
		prefs();
	}

	@Deprecated
	protected void onEvent(@SuppressWarnings("unused") final AppQuitEvent event) {
		quit();
	}

}
