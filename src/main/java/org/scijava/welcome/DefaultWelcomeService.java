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

package org.scijava.welcome;

import java.io.File;
import java.io.IOException;

import org.scijava.app.AppService;
import org.scijava.command.CommandService;
import org.scijava.display.Display;
import org.scijava.display.DisplayService;
import org.scijava.event.EventHandler;
import org.scijava.event.EventService;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.service.AbstractService;
import org.scijava.service.Service;
import org.scijava.text.TextService;
import org.scijava.ui.event.UIShownEvent;
import org.scijava.util.Prefs;
import org.scijava.welcome.event.WelcomeEvent;

/**
 * Default service for displaying the welcome greeting.
 * 
 * @author Curtis Rueden
 * @author Mark Hiner
 */
@Plugin(type = Service.class)
public class DefaultWelcomeService extends AbstractService implements
	WelcomeService
{

	private String WELCOME_FILE = "WELCOME.md";

	@Parameter
	private LogService log;

	@Parameter
	private AppService appService;

	@Parameter
	DisplayService displayService;

	@Parameter
	private CommandService commandService;

	@Parameter
	private TextService textService;

	@Parameter
	private EventService eventService;

	// -- ReadmeService methods --

	@Override
	public void displayWelcome() {
		final File baseDir = appService.getApp().getBaseDirectory();
		final File welcomeFile = new File(baseDir, WELCOME_FILE);
		try {
			if (welcomeFile.exists()) {
				final String welcomeText = textService.asHTML(welcomeFile);
				Display<?> display = displayService.createDisplay(welcomeText);
				if (display != null) display.display(welcomeText);
			}
		}
		catch (final IOException e) {
			throw new IllegalStateException(e.getMessage());
		}
	}

	@Override
	public boolean isFirstRun() {
		final String firstRun = Prefs.get(getClass(), firstRunPrefKey());
		return firstRun == null || Boolean.parseBoolean(firstRun);
	}

	@Override
	public void setFirstRun(final boolean firstRun) {
		Prefs.put(getClass(), firstRunPrefKey(), firstRun);
	}

	// -- Event handlers --

	/** Displays the welcome text when a UI is shown for the first time. */
	@EventHandler
	protected void onEvent(@SuppressWarnings("unused") final UIShownEvent evt) {
		if (!isFirstRun()) return;
		eventService.publish(new WelcomeEvent());
		setFirstRun(false);
		displayWelcome();
	}

	// -- Helper methods --

	/** Gets the preference key for ImageJ's first run. */
	private String firstRunPrefKey() {
		return "firstRun-" + appService.getApp().getVersion();
	}

}
