package org.scijava.ui.headlessUI;

import static org.scijava.Priority.LAST_PRIORITY;

import java.io.File;

import org.scijava.Context;
import org.scijava.display.Display;
import org.scijava.plugin.PluginInfo;
import org.scijava.ui.ApplicationFrame;
import org.scijava.ui.Desktop;
import org.scijava.ui.DialogPrompt;
import org.scijava.ui.StatusBar;
import org.scijava.ui.SystemClipboard;
import org.scijava.ui.ToolBar;
import org.scijava.ui.UserInterface;
import org.scijava.ui.console.ConsolePane;
import org.scijava.ui.viewer.DisplayWindow;

/**
 * A "null object" UI implementation that can be returned when a UIService is running headless
 *
 * @author Richard Domander (Royal Veterinary College, London)
 */
public class HeadlessUI implements UserInterface {
	private static HeadlessUI instance;

	private HeadlessUI() {}

	public static HeadlessUI getInstance() {
		if (instance == null) {
			instance = new HeadlessUI();
		}

		return instance;
	}

	@Override
	public void show() {}

	@Override
	public boolean isVisible() { return false; }

	@Override
	public void show(final Object o) {}

	@Override
	public void show(final String name, final Object o) {}

	@Override
	public void show(final Display<?> display) {}

	@Override
	public Desktop getDesktop() { return null; }

	@Override
	public ApplicationFrame getApplicationFrame() { return null; }

	@Override
	public ToolBar getToolBar() { return null; }

	@Override
	public StatusBar getStatusBar() { return null; }

	@Override
	public ConsolePane<?> getConsolePane() { return null; }

	@Override
	public SystemClipboard getSystemClipboard() { return null; }

	@Override
	public DisplayWindow createDisplayWindow(final Display<?> display) { return null; }

	@Override
	public DialogPrompt dialogPrompt(final String message, final String title,
						 final DialogPrompt.MessageType messageType,
						 final DialogPrompt.OptionType optionType) {
		return null;
	}

	@Override
	public File chooseFile(final File file, final String style) { return null; }

	@Override
	public File chooseFile(final String title, final File file, final String style) { return null; }

	@Override
	public void showContextMenu(final String menuRoot, final Display<?> display, final int x, final int y) {}

	@Override
	public void saveLocation() {}

	@Override
	public void restoreLocation() {}

	@Override
	public boolean requiresEDT() { return false; }

	/** Returns null since this is a contextless null object */
	@Override
	public Context context() { return null; }

	/** Returns null since this is a contextless null object */
	@Override
	public Context getContext() { return null; }

	@Override
	public void setContext(final Context context) {}

	@Override
	public PluginInfo<?> getInfo() { return null; }

	@Override
	public void setInfo(final PluginInfo<?> info) {}

	@Override
	public void dispose() {}

	@Override
	public double getPriority() { return LAST_PRIORITY; }

	@Override
	public void setPriority(final double priority) {}
}
