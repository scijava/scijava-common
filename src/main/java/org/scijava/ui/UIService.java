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
import java.util.List;

import org.scijava.app.StatusService;
import org.scijava.app.event.StatusEvent;
import org.scijava.display.Display;
import org.scijava.plugin.PluginInfo;
import org.scijava.service.SciJavaService;
import org.scijava.ui.viewer.DisplayViewer;
import org.scijava.widget.FileWidget;

/**
 * Interface for service that handles user interfaces.
 * 
 * @author Curtis Rueden
 */
public interface UIService extends SciJavaService {

	// CTR TODO: Extend SingletonService<UserInterface>.

	/** System property to set for overriding the default UI. */
	String UI_PROPERTY = "scijava.ui";

	/**
	 * Adds the given UI to those managed by the service.
	 * <p>
	 * Note that a UI added explicitly via this method will never be considered
	 * the default UI unless {@link #setDefaultUI(UserInterface)} is also called.
	 * </p>
	 * 
	 * @param ui The UI to add.
	 */
	void addUI(UserInterface ui);

	/**
	 * Adds the given UI to those managed by the service.
	 * <p>
	 * Note that a UI added explicitly via this method will never be considered
	 * the default UI unless {@link #setDefaultUI(UserInterface)} is also called.
	 * </p>
	 * 
	 * @param name The nickname for the UI.
	 * @param ui The UI to add.
	 */
	void addUI(String name, UserInterface ui);

	/**
	 * Displays the UI for the default user interface.
	 * 
	 * @see #getDefaultUI()
	 * @see #setDefaultUI(UserInterface)
	 */
	void showUI();

	/** Displays the UI with the given name (or class name). */
	void showUI(String name);

	/** Displays the given UI. */
	void showUI(UserInterface ui);

	/**
	 * Gets whether the default UI is visible.
	 * 
	 * @see #getDefaultUI()
	 * @see #setDefaultUI(UserInterface)
	 */
	boolean isVisible();

	/** Gets whether the UI with the given name or class name is visible. */
	boolean isVisible(String name);

	/**
	 * Sets whether the application should run in headless mode (no UI).
	 * <p>
	 * Note that if the system itself is headless&mdash;which can be detected via
	 * the {@code java.awt.headless} system property or by calling
	 * {@link java.awt.GraphicsEnvironment#isHeadless()}&mdash;then calling
	 * {@code setHeadless(false)} will have no effect; the system will still be
	 * headless, and {@link #isHeadless()} will still return true.
	 * </p>
	 * <p>
	 * But if the system itself is <em>not</em> headless, calling
	 * {@code setHeadless(true)} will force {@link #isHeadless()} to return true,
	 * instructing the application to behave in a headless manner insofar as it
	 * can.
	 * </p>
	 */
	void setHeadless(boolean isHeadless);

	/**
	 * Gets whether the UI is running in headless mode (no UI).
	 * <p>
	 * More precisely: returns true when {@code java.awt.headless} system
	 * property is set, and/or {@link java.awt.GraphicsEnvironment#isHeadless()}
	 * returns true, and/or {@link #setHeadless(boolean)} was called with {@code
	 * true} to force headless behavior in an otherwise headful environment.
	 * </p>
	 */
	boolean isHeadless();

	/**
	 * Gets the default user interface.
	 * 
	 * @see #showUI()
	 * @see #isVisible()
	 */
	UserInterface getDefaultUI();

	/**
	 * Sets the default user interface.
	 * 
	 * @see #showUI()
	 */
	void setDefaultUI(UserInterface ui);

	/**
	 * Gets whether the UI with the given name (or class name) is the default one.
	 */
	boolean isDefaultUI(String name);

	/** Gets the UI with the given name (or class name). */
	UserInterface getUI(String name);

	/** Gets the user interfaces available to the service. */
	List<UserInterface> getAvailableUIs();

	/** Gets the user interfaces that are currently visible. */
	List<UserInterface> getVisibleUIs();

	/** Gets the list of known viewer plugins. */
	List<PluginInfo<DisplayViewer<?>>> getViewerPlugins();

	/** Registers the given viewer with the service. */
	void addDisplayViewer(DisplayViewer<?> viewer);

	/** Gets the UI widget being used to visualize the given {@link Display}. */
	DisplayViewer<?> getDisplayViewer(Display<?> display);

	/**
	 * Creates a {@link Display} for the given object, and shows it using an
	 * appropriate UI widget of the default user interface.
	 */
	void show(Object o);

	/**
	 * Creates a {@link Display} for the given object, and shows it using an
	 * appropriate UI widget of the default user interface.
	 * 
	 * @param name The name to use when displaying the object.
	 * @param o The object to be displayed.
	 */
	void show(String name, Object o);

	/**
	 * Creates and shows the given {@link Display} using an appropriate UI widget
	 * of the default user interface.
	 */
	void show(Display<?> display);

	/**
	 * Displays a dialog prompt.
	 * <p>
	 * The prompt is displayed in the default user interface.
	 * </p>
	 * 
	 * @param message The message in the dialog itself.
	 * @return The choice selected by the user when dismissing the dialog.
	 */
	DialogPrompt.Result showDialog(String message);

	/**
	 * Displays a dialog prompt.
	 * <p>
	 * The prompt is displayed in the default user interface.
	 * </p>
	 * 
	 * @param message The message in the dialog itself.
	 * @param messageType The type of message. This typically is rendered as an
	 *          icon next to the message. For example,
	 *          {@link DialogPrompt.MessageType#WARNING_MESSAGE} typically appears
	 *          as an exclamation point.
	 * @return The choice selected by the user when dismissing the dialog.
	 */
	DialogPrompt.Result showDialog(String message,
		DialogPrompt.MessageType messageType);

	/**
	 * Displays a dialog prompt.
	 * <p>
	 * The prompt is displayed in the default user interface.
	 * </p>
	 * 
	 * @param message The message in the dialog itself.
	 * @param messageType The type of message. This typically is rendered as an
	 *          icon next to the message. For example,
	 *          {@link DialogPrompt.MessageType#WARNING_MESSAGE} typically appears
	 *          as an exclamation point.
	 * @param optionType The choices available when dismissing the dialog. These
	 *          choices are typically rendered as buttons for the user to click.
	 * @return The choice selected by the user when dismissing the dialog.
	 */
	DialogPrompt.Result showDialog(String message,
		DialogPrompt.MessageType messageType, DialogPrompt.OptionType optionType);

	/**
	 * Displays a dialog prompt.
	 * <p>
	 * The prompt is displayed in the default user interface.
	 * </p>
	 * 
	 * @param message The message in the dialog itself.
	 * @param title The title of the dialog.
	 * @return The choice selected by the user when dismissing the dialog.
	 */
	DialogPrompt.Result showDialog(String message, String title);

	/**
	 * Displays a dialog prompt.
	 * <p>
	 * The prompt is displayed in the default user interface.
	 * </p>
	 * 
	 * @param message The message in the dialog itself.
	 * @param title The title of the dialog.
	 * @param messageType The type of message. This typically is rendered as an
	 *          icon next to the message. For example,
	 *          {@link DialogPrompt.MessageType#WARNING_MESSAGE} typically appears
	 *          as an exclamation point.
	 * @return The choice selected by the user when dismissing the dialog.
	 */
	DialogPrompt.Result showDialog(String message, String title,
		DialogPrompt.MessageType messageType);

	/**
	 * Displays a dialog prompt.
	 * <p>
	 * The prompt is displayed in the default user interface.
	 * </p>
	 * 
	 * @param message The message in the dialog itself.
	 * @param title The title of the dialog.
	 * @param messageType The type of message. This typically is rendered as an
	 *          icon next to the message. For example,
	 *          {@link DialogPrompt.MessageType#WARNING_MESSAGE} typically appears
	 *          as an exclamation point.
	 * @param optionType The choices available when dismissing the dialog. These
	 *          choices are typically rendered as buttons for the user to click.
	 * @return The choice selected by the user when dismissing the dialog.
	 */
	DialogPrompt.Result showDialog(String message, String title,
		DialogPrompt.MessageType messageType, DialogPrompt.OptionType optionType);

	/**
	 * Prompts the user to choose a file.
	 * <p>
	 * The prompt is displayed in the default user interface.
	 * </p>
	 * 
	 * @param file The initial value displayed in the file chooser prompt.
	 * @param style The style of chooser to use:
	 *          <ul>
	 *          <li>{@link FileWidget#OPEN_STYLE}</li>
	 *          <li>{@link FileWidget#SAVE_STYLE}</li>
	 *          <li>{@link FileWidget#DIRECTORY_STYLE}</li>
	 *          </ul>
	 */
	File chooseFile(File file, String style);

	/**
	 * Prompts the user to choose a file.
	 * <p>
	 * The prompt is displayed in the default user interface.
	 * </p>
	 * 
	 * @param title Title to use in the file chooser dialog.
	 * @param file The initial value displayed in the file chooser prompt.
	 * @param style The style of chooser to use:
	 *          <ul>
	 *          <li>{@link FileWidget#OPEN_STYLE}</li>
	 *          <li>{@link FileWidget#SAVE_STYLE}</li>
	 *          <li>{@link FileWidget#DIRECTORY_STYLE}</li>
	 *          </ul>
	 */
	File chooseFile(String title, File file, String style);

	/**
	 * Prompts the user to select one or multiple files.
	 * <p>
	 * The prompt is displayed in the default user interface.
	 * </p>
	 * 
	 * @param files The initial value displayed in the file chooser prompt.
	 * @param filter A filter allowing to restrict the choice of files
	 */
	File[] chooseFiles(File parent, File[] files, FileFilter filter, String style);

	/**
	 * Prompts the user to select one or multiple files.
	 * <p>
	 * The prompt is displayed in the default user interface.
	 * </p>
	 * 
	 * @param fileList The initial value displayed in the file chooser prompt.
	 * @param filter A filter allowing to restrict the choice of files
	 */
	List<File> chooseFiles(File parent, List<File> fileList, FileFilter filter, String style);

	/**
	 * Displays a popup context menu for the given display at the specified
	 * position.
	 * <p>
	 * The context menu is displayed in the default user interface.
	 * </p>
	 */
	void showContextMenu(String menuRoot, Display<?> display, int x, int y);

	/**
	 * Gets the status message associated with the given event.
	 * 
	 * @see StatusService#getStatusMessage(String, StatusEvent)
	 */
	String getStatusMessage(StatusEvent statusEvent);
}
