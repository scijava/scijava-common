/*-
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

package org.scijava.ui.headless;

import java.io.File;

import org.scijava.Priority;
import org.scijava.display.Display;
import org.scijava.log.LogService;
import org.scijava.plugin.AbstractRichPlugin;
import org.scijava.plugin.Plugin;
import org.scijava.ui.DialogPrompt;
import org.scijava.ui.UserInterface;
import org.scijava.ui.viewer.DisplayWindow;
import org.scijava.util.ListUtils;

/**
 * A no-op user interface used when the application is running headless.
 * <p>
 * Most operations do nothing. Attempting to show an object via one of the
 * {@link #show} methods logs the object via the {@link LogService}.
 * </p>
 *
 * @author Richard Domander (Royal Veterinary College, London)
 * @author Curtis Rueden
 */
@Plugin(type = UserInterface.class, name = HeadlessUI.NAME,
	priority = Priority.VERY_LOW)
public class HeadlessUI extends AbstractRichPlugin implements UserInterface {

	public static final String NAME = "headless";

	private boolean visible;

	@Override
	public void show() {
		visible = true;
	}

	@Override
	public boolean isVisible() {
		return visible;
	}

	@Override
	public void show(final String name, final Object o) {
		// NB: Rather than creating a Display, let's just log it.
		log().info(name + " = " + o);
	}

	@Override
	public void show(final Display<?> display) {
		// NB: Rather than looking for a DisplayViewer, let's just log it.
		log().info(display.getName() + " = " + ListUtils.string(display, false));
	}

	@Override
	public DisplayWindow createDisplayWindow(final Display<?> display) {
		return null;
	}

	@Override
	public DialogPrompt dialogPrompt(final String message, final String title,
		final DialogPrompt.MessageType messageType,
		final DialogPrompt.OptionType optionType)
	{
		return null;
	}

	@Override
	public File chooseFile(final String title, final File file,
		final String style)
	{
		return null;
	}

	@Override
	public void showContextMenu(final String menuRoot, final Display<?> display,
		final int x, final int y)
	{}

	@Override
	public void saveLocation() {}

	@Override
	public void restoreLocation() {}

	@Override
	public boolean requiresEDT() {
		return false;
	}

	@Override
	public void dispose() {}
}
