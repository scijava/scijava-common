/*
 * #%L
 * SciJava Common shared library for SciJava software.
 * %%
 * Copyright (C) 2009 - 2024 SciJava developers.
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

package org.scijava.ui;

import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.FileFilter;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.scijava.app.AppService;
import org.scijava.app.StatusService;
import org.scijava.app.event.StatusEvent;
import org.scijava.display.Display;
import org.scijava.display.DisplayService;
import org.scijava.display.event.DisplayActivatedEvent;
import org.scijava.display.event.DisplayCreatedEvent;
import org.scijava.display.event.DisplayDeletedEvent;
import org.scijava.display.event.DisplayUpdatedEvent;
import org.scijava.event.EventHandler;
import org.scijava.event.EventService;
import org.scijava.log.LogService;
import org.scijava.platform.event.AppQuitEvent;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.plugin.PluginInfo;
import org.scijava.plugin.PluginService;
import org.scijava.service.AbstractService;
import org.scijava.service.Service;
import org.scijava.thread.ThreadService;
import org.scijava.ui.DialogPrompt.MessageType;
import org.scijava.ui.DialogPrompt.OptionType;
import org.scijava.ui.DialogPrompt.Result;
import org.scijava.ui.event.UIShownEvent;
import org.scijava.ui.headless.HeadlessUI;
import org.scijava.ui.viewer.DisplayViewer;

/**
 * Default service for handling SciJava user interfaces.
 * 
 * @author Curtis Rueden
 */
@Plugin(type = Service.class)
public final class DefaultUIService extends AbstractService implements
	UIService
{

	@Parameter
	private LogService log;

	@Parameter
	private EventService eventService;

	@Parameter
	private StatusService statusService;

	@Parameter
	private ThreadService threadService;

	@Parameter
	private AppService appService;

	@Parameter
	private PluginService pluginService;

	@Parameter
	private DisplayService displayService;

	/**
	 * A list of extant display viewers. It's needed in order to find the viewer
	 * associated with a display.
	 */
	private List<DisplayViewer<?>> displayViewers;

	/** List of available user interfaces, ordered by priority. */
	private List<UserInterface> uiList;

	/** Map of available user interfaces, keyed off their names. */
	private Map<String, UserInterface> uiMap;

	/** Whether lazy initialization is complete. */
	private boolean initialized;

	/** Whether the service was disposed */
	private boolean disposed;

	/** The default user interface to use, if one is not explicitly specified. */
	private UserInterface defaultUI;

	/** The last UI used when performing UI operations via the service. */
	private UserInterface activeUI;

	/**
	 * When true, {@link #isHeadless()} will return true regardless of the value
	 * of the {@code java.awt.headless} system property. When false, {@link
	 * #isHeadless()} matches the global JVM headless state defined by {@code
	 * java.awt.headless}.
	 */
	private boolean forceHeadless;

	private boolean activationInvocationPending = false;

	// -- UIService methods --

	@Override
	public void addUI(final UserInterface ui) {
		addUI(null, ui);
	}

	@Override
	public void addUI(final String name, final UserInterface ui) {
		if (!initialized) discoverUIs();
		addUserInterface(name, ui);
	}

	@Override
	public void showUI() {
		if (disposed) return;
		showUI(activeUI());
	}

	@Override
	public void showUI(final String name) {
		final UserInterface ui = getUI(name);
		if (ui == null) {
			throw new IllegalArgumentException("No such user interface: " + name);
		}
		showUI(ui);
	}

	@Override
	public void showUI(final UserInterface ui) {
		log.debug("Launching user interface: " + ui.getClass().getName());
		runOnCorrectThread(ui, () -> {
			ui.show();
			// NB: Also show all the current displays.
			for (final Display<?> display : displayService.getDisplays()) {
				ui.show(display);
			}
		});
		eventService.publish(new UIShownEvent(ui));
	}

	@Override
	public boolean isVisible() {
		return activeUI().isVisible();
	}

	@Override
	public boolean isVisible(final String name) {
		final UserInterface ui = getUI(name);
		return ui != null && ui.isVisible();
	}

	@Override
	public void setHeadless(final boolean headless) {
		forceHeadless = headless;
	}

	@Override
	public boolean isHeadless() {
		return forceHeadless ||
			Boolean.getBoolean("java.awt.headless") ||
			GraphicsEnvironment.isHeadless();
	}

	@Override
	public UserInterface getDefaultUI() {
		if (!initialized) discoverUIs();
		if (isHeadless()) return getUI(HeadlessUI.NAME);
		if (defaultUI != null) return defaultUI;
		return uiList().isEmpty() ? null : uiList().get(0);
	}

	@Override
	public void setDefaultUI(final UserInterface ui) {
		defaultUI = ui;
	}

	@Override
	public boolean isDefaultUI(final String name) {
		return getDefaultUI() == getUI(name);
	}

	@Override
	public UserInterface getUI(final String name) {
		return uiMap().get(name);
	}

	@Override
	public List<UserInterface> getAvailableUIs() {
		return Collections.unmodifiableList(uiList());
	}

	@Override
	public List<UserInterface> getVisibleUIs() {
		final ArrayList<UserInterface> uis = new ArrayList<>();
		for (final UserInterface ui : uiList()) {
			if (ui.isVisible()) uis.add(ui);
		}
		return uis;
	}

	@Override
	public List<PluginInfo<DisplayViewer<?>>> getViewerPlugins() {
		@SuppressWarnings({ "rawtypes", "unchecked" })
		final List<PluginInfo<DisplayViewer<?>>> viewers =
			(List) pluginService.getPluginsOfType(DisplayViewer.class);
		return viewers;
	}

	@Override
	public void show(final Object o) {
		final UserInterface ui = activeUI();
		runOnCorrectThread(ui, () -> ui.show(o));
	}

	@Override
	public void show(final String name, final Object o) {
		final UserInterface ui = activeUI();
		runOnCorrectThread(ui, () -> ui.show(name, o));
	}

	@Override
	public void show(final Display<?> display) {
		final UserInterface ui = activeUI();
		runOnCorrectThread(ui, () -> ui.show(display));
	}

	@Override
	public void addDisplayViewer(final DisplayViewer<?> viewer) {
		displayViewers().add(viewer);
	}

	@Override
	public DisplayViewer<?> getDisplayViewer(final Display<?> display) {
		for (final DisplayViewer<?> displayViewer : displayViewers()) {
			if (displayViewer.getDisplay() == display) return displayViewer;
		}
		return null;
	}

	@Override
	public DialogPrompt.Result showDialog(final String message) {
		return showDialog(message, getTitle());
	}

	@Override
	public Result showDialog(final String message, final MessageType messageType)
	{
		return showDialog(message, getTitle(), messageType);
	}

	@Override
	public Result showDialog(final String message, final MessageType messageType,
		final OptionType optionType)
	{
		return showDialog(message, getTitle(), messageType, optionType);
	}

	@Override
	public DialogPrompt.Result
		showDialog(final String message, final String title)
	{
		return showDialog(message, title,
			DialogPrompt.MessageType.INFORMATION_MESSAGE);
	}

	@Override
	public DialogPrompt.Result showDialog(final String message,
		final String title, final DialogPrompt.MessageType messageType)
	{
		return showDialog(message, title, messageType,
			DialogPrompt.OptionType.DEFAULT_OPTION);
	}

	@Override
	public DialogPrompt.Result showDialog(final String message,
		final String title, final DialogPrompt.MessageType messageType,
		final DialogPrompt.OptionType optionType)
	{
		final UserInterface ui = activeUI();
		DialogPrompt.Result[] result = new DialogPrompt.Result[1];
		runOnCorrectThread(ui, () -> {
			final DialogPrompt dialogPrompt = //
				ui.dialogPrompt(message, title, messageType, optionType);
			result[0] = dialogPrompt == null ? null : dialogPrompt.prompt();
		});
		return result[0];
	}

	@Override
	public File chooseFile(final File file, final String style) {
		final UserInterface ui = activeUI();
		final File[] result = new File[1];
		runOnCorrectThread(ui, () -> {
			result[0] = ui.chooseFile(file, style);
		});
		return result[0];
	}

	@Override
	public File
		chooseFile(final String title, final File file, final String style)
	{
		final UserInterface ui = activeUI();
		final File[] result = new File[1];
		runOnCorrectThread(ui, () -> {
			result[0] = ui.chooseFile(title, file, style);
		});
		return result[0];
	}

	@Override
	public File[] chooseFiles(File parent, File[] files, FileFilter filter, String style) {
		final UserInterface ui = activeUI();
		final File[][] result = new File[1][];
		runOnCorrectThread(ui, () -> {
			result[0] = ui.chooseFiles(parent, files, filter, style);
		});
		return result[0];
	}
	
	@Override
	public List<File> chooseFiles(File parent, List<File> fileList, FileFilter filter, String style) {
		final UserInterface ui = activeUI();
		final List<?>[] result = new List<?>[1];
		runOnCorrectThread(ui, () -> {
			result[0] = ui.chooseFiles(parent, fileList, filter, style);
		});
		return (List<File>) result[0];
	}

	@Override
	public void showContextMenu(final String menuRoot, final Display<?> display,
		final int x, final int y)
	{
		final UserInterface ui = activeUI();
		runOnCorrectThread(ui, () -> ui.showContextMenu(menuRoot, display, x, y));
	}

	@Override
	public String getStatusMessage(final StatusEvent statusEvent) {
		final String appName = appService.getApp().getInfo().getName();
		return statusService.getStatusMessage(appName, statusEvent);
	}

	// -- Disposable methods --

	@Override
	public synchronized void dispose() {
		// dispose active display viewers
		// NB - copy list to avoid ConcurrentModificationExceptions
		final List<DisplayViewer<?>> viewers = new ArrayList<>();
		viewers.addAll(displayViewers());
		for (final DisplayViewer<?> viewer : viewers) {
			viewer.dispose();
		}

		// dispose UIs in reverse priority, "just in case" the order matters
		final List<UserInterface> uis = getAvailableUIs();
		for (int i = uis.size() - 1; i >= 0; i--) {
			uis.get(i).dispose();
		}
		disposed = true;
	}

	// -- Event handlers --

	/**
	 * Called when a display is created. This is the magical place where the
	 * display model is connected with the real UI.
	 */
	@EventHandler
	protected void onEvent(final DisplayCreatedEvent e) {
		final Display<?> display = e.getObject();

		for (final UserInterface ui : getVisibleUIs()) {
			ui.show(display);
		}
	}

	/**
	 * Called when a display is deleted. The display viewer is not removed from
	 * the list of viewers until after this returns.
	 */
	@EventHandler
	protected void onEvent(final DisplayDeletedEvent e) {
		final Display<?> display = e.getObject();
		final DisplayViewer<?> displayViewer = getDisplayViewer(display);
		if (displayViewer != null) {
			displayViewer.onDisplayDeletedEvent(e);
			displayViewers().remove(displayViewer);
		}
	}

	/** Called when a display is updated. */
	@EventHandler
	protected void onEvent(final DisplayUpdatedEvent e) {
		final Display<?> display = e.getDisplay();
		final DisplayViewer<?> displayViewer = getDisplayViewer(display);
		if (displayViewer != null) {
			displayViewer.onDisplayUpdatedEvent(e);
		}
	}

	/**
	 * Called when a display is activated.
	 * <p>
	 * The goal here is to eventually synchronize the window activation state with
	 * the display activation state if the display activation state changed
	 * programmatically. We queue a call on the UI thread to activate the display
	 * viewer of the currently active window.
	 * </p>
	 */
	@EventHandler
	protected void onEvent(final DisplayActivatedEvent e) {
		// CTR FIXME: Verify whether this threading logic is really necessary.
		if (activationInvocationPending) return;
		activationInvocationPending = true;
		threadService.queue(new Runnable() {

			@Override
			public void run() {
				final Display<?> activeDisplay = displayService.getActiveDisplay();
				if (activeDisplay != null) {
					final DisplayViewer<?> displayViewer =
						getDisplayViewer(activeDisplay);
					if (displayViewer != null) displayViewer.onDisplayActivatedEvent(e);
				}
				activationInvocationPending = false;
			}
		});
	}

	@EventHandler
	protected synchronized void onEvent(
		@SuppressWarnings("unused") final AppQuitEvent event)
	{
		if (!initialized) return;
		for (final UserInterface ui : getVisibleUIs()) {
			ui.saveLocation();
		}
	}

	@EventHandler
	protected void onEvent(final StatusEvent event) {
		if (event.isWarning()) {
			// report warning messages to the user in a dialog box
			final String message = event.getStatusMessage();
			if (message != null && !message.isEmpty()) {
				showDialog(message, MessageType.WARNING_MESSAGE);
			}
		}
		else {
			// tell each UI to report status updates in the status bar
			final int val = event.getProgressValue();
			final int max = event.getProgressMaximum();
			final String message = getStatusMessage(event);
			if (max < 0 && message == null) return;
			for (UserInterface ui : getAvailableUIs()) {
				final StatusBar statusBar = ui.getStatusBar();
				if (statusBar != null) {
					if (max >= 0) {
						statusBar.setProgress(val, max);
					}
					if (message != null) {
						statusBar.setStatus(message);
					}
				}
			}
		}
	}

	// -- Helper methods --

	private List<DisplayViewer<?>> displayViewers() {
		if (!initialized) discoverUIs();
		return displayViewers;
	}

	private List<UserInterface> uiList() {
		if (!initialized) discoverUIs();
		return uiList;
	}

	private Map<String, UserInterface> uiMap() {
		if (!initialized) discoverUIs();
		return uiMap;
	}

	/** Discovers available user interfaces. */
	private synchronized void discoverUIs() {
		if (initialized) return;

		displayViewers = new ArrayList<>();
		uiList = new ArrayList<>();
		uiMap = new HashMap<>();

		final List<PluginInfo<UserInterface>> infos =
			pluginService.getPluginsOfType(UserInterface.class);
		for (final PluginInfo<UserInterface> info : infos) {
			// instantiate user interface
			final UserInterface ui = pluginService.createInstance(info);
			if (ui == null) continue;
			log.debug("Discovered user interface: " + ui.getClass().getName());
			addUserInterface(info.getName(), ui);
		}

		// check system property for explicit UI preference
		final String uiProp = System.getProperty(UI_PROPERTY);
		final UserInterface ui = uiMap.get(uiProp);

		if (ui != null) {
			// set the default UI to the one provided by the system property
			setDefaultUI(ui);
		}

		initialized = true;
	}

	private void addUserInterface(final String name, final UserInterface ui) {
		// add to UI list
		uiList.add(ui);

		// add to UI map
		uiMap.put(ui.getClass().getName(), ui);
		if (name != null && !name.isEmpty()) uiMap.put(name, ui);
	}

	private String getTitle() {
		return appService.getApp().getTitle();
	}

	/** Gets the UI to use when performing UI operations via the service. */
	private UserInterface activeUI() {
		// If a particular UI is already active and still visible, use that one.
		if (activeUI != null && activeUI.isVisible()) return activeUI;

		// If a UI is visible, use it.
		final List<UserInterface> visibleUIs = getVisibleUIs();
		if (visibleUIs.size() > 0) return activeUI = visibleUIs.get(0);

		// No UI is visible, so use the default one.
		return activeUI = getDefaultUI();
	}

	private void runOnCorrectThread(final UserInterface ui, final Runnable r) {
		// Dispatch on EDT if necessary
		if (ui.requiresEDT()) {
			try {
				threadService.invoke(r);
			}
			catch (InterruptedException | InvocationTargetException e) {
				throw new RuntimeException(e);
			}
		}
		else {
			r.run();
		}
	}
}
