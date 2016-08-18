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
import org.scijava.app.StatusService;
import org.scijava.command.CommandService;
import org.scijava.console.ConsoleService;
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
import org.scijava.plugin.Plugin;
import org.scijava.plugin.PluginService;
import org.scijava.plugin.RichPlugin;
import org.scijava.script.ScriptService;
import org.scijava.service.Service;
import org.scijava.text.TextService;
import org.scijava.thread.ThreadService;
import org.scijava.tool.IconService;
import org.scijava.tool.ToolService;
import org.scijava.ui.UIService;
import org.scijava.widget.WidgetService;

/**
 * Interface for convenience classes that wrap a {@link Context} to provide
 * one-line access to a suite of {@link Service}s.
 * <p>
 * The {@link #get} methods provide consistent {@link Service} instantiation,
 * while throwing {@link NoSuchServiceException} if the requested
 * {@link Service} is not found.
 * </p>
 * <h3>Sample implementation</h3>
 * <p>
 * Let's say we have a {@code Kraken} service and a {@code Cow} service. Using
 * the {@code Context} directly, the code would look like:
 * </p>
 * <pre>
 * Context context = new Context();
 * context.getService(Cow.class).feedToKraken();
 * context.getService(Kraken.class).burp();</pre>
 * <p>
 * To perform these actions, you have to know <em>a priori</em> to ask for a
 * {@code Cow} and a {@code Kraken}; i.e., your IDE's code completion will not
 * give you a hint. Further, if either service is unavailable, a
 * {@link NullPointerException} is thrown.
 * </p>
 * <p>
 * But if we create a {@code Gateway} class called {@code Animals} with the
 * following signatures:
 * </p>
 * <pre>
 * public Cow cow() { return get(Cow.class); }
 * public Kraken kraken() { return get(Kraken.class); }</pre>
 * <p>
 * We can now access our services through the new {@code Animals} gateway:
 * </p>
 * <pre>
 * Animals animals = new Animals();
 * animals.cow().feedToKraken();
 * animals.kraken().burp();</pre>
 * <p>
 * This provides succinct yet explicit access to the {@code Cow} and
 * {@code Kraken} services; it is a simple two-layer access to functionality,
 * which an IDE can auto-complete. And if one of the services is not available,
 * a {@link NoSuchServiceException} is thrown, which facilitates appropriate
 * (but optional) handling of missing services.
 * </p>
 * <p>
 * Gateways discoverable at runtime must implement this interface and be
 * annotated with @{@link Gateway} with attribute {@link Plugin#type()} =
 * {@link Gateway}.class. While it possible to create a gateway merely by
 * implementing this interface, it is encouraged to instead extend
 * {@link AbstractGateway}, for convenience.
 * </p>
 * 
 * @see Context
 * @author Mark Hiner
 * @author Curtis Rueden
 */
public interface Gateway extends RichPlugin {

	/**
	 * Perform launch operations associated with this gateway.
	 * <p>
	 * Typical operations might include:
	 * </p>
	 * <ul>
	 * <li>Handle the given command line arguments using the
	 * {@link ConsoleService}.</li>
	 * <li>Execute registered main classes of the {@link MainService}.</li>
	 * <li>Display the default user interface using the {@link UIService}.</li>
	 * <li>In some circumstances (e.g., when running headless), dispose the
	 * context after launch operations are complete.</li>
	 * </ul>
	 * 
	 * @param args The arguments to pass to the application.
	 */
	void launch(String... args);

	/**
	 * Gets a very succinct name for use referring to this gateway, e.g. as a
	 * variable name for scripting.
	 */
	String getShortName();

	/**
	 * Returns an implementation of the requested {@link Service}, if it exists in
	 * the underlying {@link Context}.
	 * 
	 * @param serviceClass the requested {@link Service}
	 * @return The singleton instance of the given class
	 * @throws NullContextException if the application context is not set.
	 * @throws NoSuchServiceException if there is no service of the given class.
	 */
	<S extends Service> S get(Class<S> serviceClass);

	/**
	 * Returns an implementation of the {@link Service} with the given class name,
	 * if it exists in the underlying {@link Context}.
	 * 
	 * @param serviceClassName name of the requested {@link Service}
	 * @return The singleton instance of the requested {@link Service}
	 * @throws NullContextException if the application context is not set.
	 * @throws NoSuchServiceException if there is no service matching
	 *           {@code serviceClassName}.
	 */
	Service get(final String serviceClassName);

	// -- Gateway methods - services --

	/**
	 * Gets this application context's {@link AppEventService}.
	 *
	 * @return The {@link AppEventService} of this application context.
	 */
	AppEventService appEvent();

	/**
	 * Gets this application context's {@link AppService}.
	 *
	 * @return The {@link AppService} of this application context.
	 */
	AppService app();

	/**
	 * Gets this application context's {@link CommandService}.
	 *
	 * @return The {@link CommandService} of this application context.
	 */
	CommandService command();

	/**
	 * Gets this application context's {@link ConsoleService}.
	 *
	 * @return The {@link ConsoleService} of this application context.
	 */
	ConsoleService console();

	/**
	 * Gets this application context's {@link DisplayService}.
	 *
	 * @return The {@link DisplayService} of this application context.
	 */
	DisplayService display();

	/**
	 * Gets this application context's {@link EventHistory}.
	 *
	 * @return The {@link EventHistory} of this application context.
	 */
	EventHistory eventHistory();

	/**
	 * Gets this application context's {@link EventService}.
	 *
	 * @return The {@link EventService} of this application context.
	 */
	EventService event();

	/**
	 * Gets this application context's {@link IconService}.
	 *
	 * @return The {@link IconService} of this application context.
	 */
	IconService icon();

	/**
	 * Gets this application context's {@link InputService}.
	 *
	 * @return The {@link InputService} of this application context.
	 */
	InputService input();

	/**
	 * Gets this application context's {@link IOService}.
	 *
	 * @return The {@link IOService} of this application context.
	 */
	IOService io();

	/**
	 * Gets this application context's {@link LogService}.
	 *
	 * @return The {@link LogService} of this application context.
	 */
	@Override
	LogService log();

	/**
	 * Gets this application context's {@link MainService}.
	 *
	 * @return The {@link MainService} of this application context.
	 */
	MainService main();

	/**
	 * Gets this application context's {@link MenuService}.
	 *
	 * @return The {@link MenuService} of this application context.
	 */
	MenuService menu();

	/**
	 * Gets this application context's {@link ModuleService}.
	 *
	 * @return The {@link ModuleService} of this application context.
	 */
	ModuleService module();

	/**
	 * Gets this application context's {@link ObjectService}.
	 *
	 * @return The {@link ObjectService} of this application context.
	 */
	ObjectService object();

	/**
	 * Gets this application context's {@link OptionsService}.
	 *
	 * @return The {@link OptionsService} of this application context.
	 */
	OptionsService options();

	/**
	 * Gets this application context's {@link PlatformService}.
	 *
	 * @return The {@link PlatformService} of this application context.
	 */
	PlatformService platform();

	/**
	 * Gets this application context's {@link PluginService}.
	 *
	 * @return The {@link PluginService} of this application context.
	 */
	PluginService plugin();

	/**
	 * Gets this application context's {@link RecentFileService}.
	 *
	 * @return The {@link RecentFileService} of this application context.
	 */
	RecentFileService recentFile();

	/**
	 * Gets this application context's {@link ScriptService}.
	 *
	 * @return The {@link ScriptService} of this application context.
	 */
	ScriptService script();

	/**
	 * Gets this application context's {@link StatusService}.
	 *
	 * @return The {@link StatusService} of this application context.
	 */
	StatusService status();

	/**
	 * Gets this application context's {@link TextService}.
	 *
	 * @return The {@link TextService} of this application context.
	 */
	TextService text();

	/**
	 * Gets this application context's {@link ThreadService}.
	 *
	 * @return The {@link ThreadService} of this application context.
	 */
	ThreadService thread();

	/**
	 * Gets this application context's {@link ToolService}.
	 *
	 * @return The {@link ToolService} of this application context.
	 */
	ToolService tool();

	/**
	 * Gets this application context's {@link UIService}.
	 *
	 * @return The {@link UIService} of this application context.
	 */
	UIService ui();

	/**
	 * Gets this application context's {@link WidgetService}.
	 *
	 * @return The {@link WidgetService} of this application context.
	 */
	WidgetService widget();

	// -- Gateway methods - application --

	/** @see org.scijava.app.AppService */
	App getApp();

	/** @see org.scijava.app.App#getTitle() */
	String getTitle();

	/** @see org.scijava.app.App#getInfo(boolean) */
	String getInfo(boolean mem);

}
