/*
 * #%L
 * SciJava Common shared library for SciJava software.
 * %%
 * Copyright (C) 2009 - 2025 SciJava developers.
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

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;
import java.util.List;

import org.scijava.Disposable;
import org.scijava.display.Display;
import org.scijava.plugin.Plugin;
import org.scijava.plugin.RichPlugin;
import org.scijava.ui.console.ConsolePane;
import org.scijava.ui.viewer.DisplayWindow;
import org.scijava.widget.FileWidget;
import org.scijava.widget.WidgetStyle;

/**
 * An end-user SciJava application user interface.
 * <p>
 * UIs discoverable at runtime must implement this interface and be annotated
 * with @{@link Plugin} with attribute {@link Plugin#type()} =
 * {@link UserInterface}.class. While it possible to create a UI merely by
 * implementing this interface, it is encouraged to instead extend
 * {@link AbstractUserInterface}, for convenience.
 * </p>
 *
 * @author Curtis Rueden
 * @see Plugin
 * @see UIService
 */
public interface UserInterface extends RichPlugin, Disposable {

	/**
	 * Shows the UI.
	 * <p>
	 * Note that the actual UI components are created lazily when this method is
	 * called, rather then upon the UI's initial construction.
	 * </p>
	 */
	void show();

	/** Whether this UI is visible onscreen. */
	boolean isVisible();

	/** Shows the object onscreen using an appropriate UI widget. */
	default void show(final Object o) {
		show(null, o);
	}

	/**
	 * Shows the object onscreen using an appropriate UI widget.
	 *
	 * @param name The name to use when displaying the object.
	 * @param o The object to be displayed.
	 */
	void show(String name, Object o);

	/** Shows the display onscreen using an appropriate UI widget. */
	void show(Display<?> display);

	/**
	 * Gets the desktop, for use with multi-document interfaces (MDI), or null if
	 * not applicable.
	 */
	default Desktop getDesktop() {
		return null;
	}

	/** Gets the main SciJava application frame, or null if not applicable. */
	default ApplicationFrame getApplicationFrame() {
		return null;
	}

	/** Gets the main SciJava toolbar, or null if not applicable. */
	default ToolBar getToolBar() {
		return null;
	}

	/** Gets the main SciJava status bar, or null if not applicable. */
	default StatusBar getStatusBar() {
		return null;
	}

	/** Gets the main SciJava console pane, or null if not applicable. */
	default ConsolePane<?> getConsolePane() {
		return null;
	}

	/**
	 * Gets the system clipboard associated with this UI, or null if not
	 * applicable.
	 */
	default SystemClipboard getSystemClipboard() {
		return null;
	}

	/**
	 * Creates a new display window housing the given display, or null if not
	 * applicable.
	 */
	DisplayWindow createDisplayWindow(Display<?> display);

	/**
	 * Creates a dialog prompter.
	 *
	 * @param message The message in the dialog itself.
	 * @param title The title of the dialog.
	 * @param messageType The type of message. This typically is rendered as an
	 *          icon next to the message. For example,
	 *          {@link DialogPrompt.MessageType#WARNING_MESSAGE} typically appears
	 *          as an exclamation point.
	 * @param optionType The choices available when dismissing the dialog. These
	 *          choices are typically rendered as buttons for the user to click.
	 * @return The newly created DialogPrompt object, or null if not applicable.
	 */
	DialogPrompt dialogPrompt(String message, String title,
		DialogPrompt.MessageType messageType, DialogPrompt.OptionType optionType);

	/**
	 * Prompts the user to choose a file.
	 *
	 * @param file The initial value displayed in the file chooser prompt.
	 * @param style The style of chooser to use:
	 *          <ul>
	 *          <li>{@link FileWidget#OPEN_STYLE}</li>
	 *          <li>{@link FileWidget#SAVE_STYLE}</li>
	 *          <li>{@link FileWidget#DIRECTORY_STYLE}</li>
	 *          </ul>
	 * @return The {@link File} chosen by the user, or null if prompt is not
	 *         available
	 */
	default File chooseFile(final File file, final String style) {
		final String title;
		// style can be a string with multiple comma-separated keywords
		if (style == null) title = "Choose a file";
		else if (WidgetStyle.isStyle(style, FileWidget.DIRECTORY_STYLE)) title = "Choose a directory";
		else if (WidgetStyle.isStyle(style, FileWidget.FILE_AND_DIRECTORY_STYLE)) title = "Choose a file or directory";
		else if (WidgetStyle.isStyle(style, FileWidget.OPEN_STYLE)) title = "Open";
		else if (WidgetStyle.isStyle(style, FileWidget.SAVE_STYLE)) title = "Save";
		else title = "Choose a file";

		return chooseFile(title, file, style);
	}

	/**
	 * Prompts the user to choose a file.
	 *
	 * @param title Title to use in the file chooser dialog.
	 * @param file The initial value displayed in the file chooser prompt.
	 * @param style The style of chooser to use:
	 *          <ul>
	 *          <li>{@link FileWidget#OPEN_STYLE}</li>
	 *          <li>{@link FileWidget#SAVE_STYLE}</li>
	 *          <li>{@link FileWidget#DIRECTORY_STYLE}</li>
	 *          <li>{@link FileWidget#FILE_AND_DIRECTORY_STYLE}</li>
	 *          </ul>
	 * @return The {@link File} chosen by the user, or null if prompt is not
	 *         available
	 */
	default File chooseFile(String title, File file, String style) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Prompts the user to choose a list of files.
	 *
	 * @param parent Parent folder for file selection
	 * @param files The initial value displayed in the file chooser prompt.
	 * @param filter A filter allowing to restrict file choice.
	 * @param style File selection style (files, directories, or both) and optional filters
	 * @return The selected {@link File}s chosen by the user, or null if the
	 *         user cancels the prompt.
	 */
	default File[] chooseFiles(File parent, File[] files, FileFilter filter, String style) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Prompts the user to choose a list of files.
	 *
	 * @param parent Parent folder for file selection
	 * @param fileList The initial value displayed in the file chooser prompt.
	 * @param filter A filter allowing to restrict file choice.
	 * @param style File selection style (files, directories, or both) and optional filters
	 * @return The selected {@link File}s chosen by the user, or null if the
	 *         user cancels the prompt.
	 */
	default List<File> chooseFiles(File parent, List<File> fileList, FileFilter filter, String style) {
		final File[] initialFiles = fileList.toArray(new File[fileList.size()]);
		final File[] chosenFiles = chooseFiles(parent, initialFiles, filter, style);
		return chosenFiles == null ? null : Arrays.asList(chosenFiles);
	}

	/**
	 * Displays a popup context menu for the given display at the specified
	 * position.
	 */
	void showContextMenu(String menuRoot, Display<?> display, int x, int y);

	/** Persists the application frame's current location. */
	void saveLocation();

	/** Restores the application frame's current location. */
	void restoreLocation();

	/** Returns true if this UI requires the EDT. */
	boolean requiresEDT();
}
