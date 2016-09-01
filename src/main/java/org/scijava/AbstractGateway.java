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

package org.scijava;

import org.scijava.app.App;
import org.scijava.app.AppService;
import org.scijava.app.SciJavaApp;
import org.scijava.app.StatusService;
import org.scijava.command.CommandService;
import org.scijava.console.ConsoleService;
import org.scijava.convert.ConvertService;
import org.scijava.display.DisplayService;
import org.scijava.event.EventHistory;
import org.scijava.event.EventService;
import org.scijava.input.InputService;
import org.scijava.io.IOService;
import org.scijava.io.RecentFileService;
import org.scijava.log.LogService;
import org.scijava.main.MainService;
import org.scijava.menu.MenuService;
import org.scijava.module.ModuleService;
import org.scijava.object.ObjectService;
import org.scijava.options.OptionsService;
import org.scijava.platform.AppEventService;
import org.scijava.platform.PlatformService;
import org.scijava.plugin.AbstractRichPlugin;
import org.scijava.plugin.PluginService;
import org.scijava.prefs.PrefService;
import org.scijava.script.ScriptService;
import org.scijava.service.Service;
import org.scijava.text.TextService;
import org.scijava.thread.ThreadService;
import org.scijava.tool.IconService;
import org.scijava.tool.ToolService;
import org.scijava.ui.UIService;
import org.scijava.widget.WidgetService;

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
	public void launch(final String... args) {
		// parse command line arguments
		console().processArgs(args);

		// launch main methods
		final int mainCount = main().execMains();

		// display the user interface (NB: does not block)
		if (mainCount == 0 && !ui().isHeadless()) ui().showUI();

		if (ui().isHeadless()) {
			// now that CLI processing/execution is done, we can shut down
			getContext().dispose();
		}
	}

	@Override
	public String getShortName() {
		return getClass().getSimpleName().toLowerCase();
	}

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
	public AppEventService appEvent() {
		return get(AppEventService.class);
	}

	@Override
	public AppService app() {
		return get(AppService.class);
	}

	@Override
	public CommandService command() {
		return get(CommandService.class);
	}

	@Override
	public ConsoleService console() {
		return get(ConsoleService.class);
	}

	public ConvertService convert() {
		return get(ConvertService.class);
	}

	@Override
	public DisplayService display() {
		return get(DisplayService.class);
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
	public IconService icon() {
		return get(IconService.class);
	}

	@Override
	public InputService input() {
		return get(InputService.class);
	}

	@Override
	public IOService io() {
		return get(IOService.class);
	}

	@Override
	public LogService log() {
		return get(LogService.class);
	}

	@Override
	public MainService main() {
		return get(MainService.class);
	}

	@Override
	public MenuService menu() {
		return get(MenuService.class);
	}

	@Override
	public ModuleService module() {
		return get(ModuleService.class);
	}

	@Override
	public ObjectService object() {
		return get(ObjectService.class);
	}

	@Override
	public OptionsService options() {
		return get(OptionsService.class);
	}

	@Override
	public PlatformService platform() {
		return get(PlatformService.class);
	}

	@Override
	public PluginService plugin() {
		return get(PluginService.class);
	}

	public PrefService prefs() {
		return get(PrefService.class);
	}

	@Override
	public RecentFileService recentFile() {
		return get(RecentFileService.class);
	}

	@Override
	public ScriptService script() {
		return get(ScriptService.class);
	}

	@Override
	public StatusService status() {
		return get(StatusService.class);
	}

	@Override
	public TextService text() {
		return get(TextService.class);
	}

	@Override
	public ThreadService thread() {
		return get(ThreadService.class);
	}

	@Override
	public ToolService tool() {
		return get(ToolService.class);
	}

	@Override
	public UIService ui() {
		return get(UIService.class);
	}

	@Override
	public WidgetService widget() {
		return get(WidgetService.class);
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
	public String getInfo(final boolean mem) {
		return getApp().getInfo(mem);
	}

	// -- Versioned methods --

	@Override
	public String getVersion() {
		return getApp().getVersion();
	}

}
