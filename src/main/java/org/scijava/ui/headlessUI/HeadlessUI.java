/*-
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

package org.scijava.ui.headlessUI;

import java.io.File;

import org.scijava.Priority;
import org.scijava.display.Display;
import org.scijava.plugin.AbstractRichPlugin;
import org.scijava.plugin.Plugin;
import org.scijava.ui.DialogPrompt;
import org.scijava.ui.UserInterface;
import org.scijava.ui.viewer.DisplayWindow;

/**
 * A "null object" UI implementation that can be returned when a UIService is
 * running headless
 *
 * @author Richard Domander (Royal Veterinary College, London)
 * @author Curtis Rueden
 */
@Plugin(type = UserInterface.class, name = HeadlessUI.NAME,
	priority = Priority.VERY_LOW_PRIORITY)
public class HeadlessUI extends AbstractRichPlugin implements UserInterface {

	public static final String NAME = "headless";

	@Override
	public void show() {}

	@Override
	public boolean isVisible() {
		return false;
	}

	@Override
	public void show(final String name, final Object o) {}

	@Override
	public void show(final Display<?> display) {}

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
