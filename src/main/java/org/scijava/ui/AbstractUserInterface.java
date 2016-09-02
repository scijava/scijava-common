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

package org.scijava.ui;

import java.util.List;

import org.scijava.display.Display;
import org.scijava.display.DisplayService;
import org.scijava.log.LogService;
import org.scijava.plugin.AbstractRichPlugin;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.PluginInfo;
import org.scijava.plugin.PluginService;
import org.scijava.prefs.PrefService;
import org.scijava.thread.ThreadService;
import org.scijava.ui.viewer.DisplayViewer;
import org.scijava.ui.viewer.DisplayWindow;

/**
 * Abstract superclass for {@link UserInterface} implementations.
 * 
 * @author Curtis Rueden
 */
public abstract class AbstractUserInterface extends AbstractRichPlugin
	implements UserInterface
{

	private static final String LAST_X = "lastXLocation";
	private static final String LAST_Y = "lastYLocation";

	@Parameter
	private DisplayService displayService;

	@Parameter
	private LogService log;

	@Parameter
	private PluginService pluginService;

	@Parameter
	private ThreadService threadService;

	@Parameter
	private UIService uiService;

	@Parameter
	private PrefService prefService;

	/** Whether the UI is currently being displayed. */
	private boolean visible = false;

	// -- UserInterface methods --

	@Override
	public void show() {
		createUI();
		visible = true;
	}

	@Override
	public boolean isVisible() {
		return visible;
	}

	@Override
	public void show(final String name, final Object o) {
		final Display<?> display;
		if (o instanceof Display) {
			display = (Display<?>) o;
		}
		else {
			display = displayService.createDisplay(name, o);
		}
		if (!isVisible()) {
			// NB: If this UI is invisible, the display will not be automatically
			// shown. So in that case, we show it explicitly here.
			show(display);
		}
	}

	@Override
	public void show(final Display<?> display) {
		if (uiService.getDisplayViewer(display) != null) {
			// display is already being shown
			return;
		}

		final List<PluginInfo<DisplayViewer<?>>> viewers =
			uiService.getViewerPlugins();

		DisplayViewer<?> displayViewer = null;
		for (final PluginInfo<DisplayViewer<?>> info : viewers) {
			// check that viewer can actually handle the given display
			final DisplayViewer<?> viewer = pluginService.createInstance(info);
			if (viewer == null) continue;
			if (!viewer.canView(display)) continue;
			if (!viewer.isCompatible(this)) continue;
			displayViewer = viewer;
			break; // found a suitable viewer; we are done
		}
		if (displayViewer == null) {
			log.warn("For UI '" + getClass().getName() +
				"': no suitable viewer for display: " + display);
			return;
		}

		final DisplayViewer<?> finalViewer = displayViewer;
		threadService.queue(new Runnable() {
			@Override
			public void run() {
				final DisplayWindow displayWindow = createDisplayWindow(display);
				finalViewer.view(displayWindow, display);
				displayWindow.setTitle(display.getName());
				uiService.addDisplayViewer(finalViewer);
				displayWindow.showDisplay(true);
				display.update();
			}
		});
	}

	@Override
	public void saveLocation() {
		final ApplicationFrame appFrame = getApplicationFrame();
		if (appFrame != null) {
			prefService.put(getClass(), LAST_X, appFrame.getLocationX());
			prefService.put(getClass(), LAST_Y, appFrame.getLocationY());
		}
	}

	@Override
	public void restoreLocation() {
		final ApplicationFrame appFrame = getApplicationFrame();
		if (appFrame != null) {
			final int lastX = prefService.getInt(getClass(), LAST_X, 0);
			final int lastY = prefService.getInt(getClass(), LAST_Y, 0);
			appFrame.setLocation(lastX, lastY);
		}
	}

	// -- Internal methods --

	/**
	 * Subclasses override to control UI creation. They must also call
	 * super.createUI() after creating the {@link ApplicationFrame} but before
	 * showing it (assuming the UI has an {@link ApplicationFrame}).
	 */
	protected void createUI() {
		restoreLocation();
	}
}
