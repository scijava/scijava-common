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

package org.scijava.tool;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.scijava.app.StatusService;
import org.scijava.display.event.DisplayEvent;
import org.scijava.display.event.input.KyPressedEvent;
import org.scijava.display.event.input.KyReleasedEvent;
import org.scijava.display.event.input.MsClickedEvent;
import org.scijava.display.event.input.MsDraggedEvent;
import org.scijava.display.event.input.MsMovedEvent;
import org.scijava.display.event.input.MsPressedEvent;
import org.scijava.display.event.input.MsReleasedEvent;
import org.scijava.display.event.input.MsWheelEvent;
import org.scijava.event.EventHandler;
import org.scijava.event.EventService;
import org.scijava.plugin.AbstractSingletonService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.service.Service;
import org.scijava.tool.event.ToolActivatedEvent;
import org.scijava.tool.event.ToolDeactivatedEvent;

/**
 * Default service for keeping track of available tools, including which tool is
 * active, and delegating UI events to the active tool.
 * 
 * @author Grant Harris
 * @author Curtis Rueden
 * @see Tool
 * @see Tool
 */
@Plugin(type = Service.class)
public class DefaultToolService extends AbstractSingletonService<Tool>
	implements ToolService
{

	@Parameter
	private EventService eventService;

	@Parameter
	private StatusService statusService;

	private Map<String, Tool> alwaysActiveTools;
	private List<Tool> alwaysActiveToolList;

	private Map<String, Tool> tools;
	private List<Tool> toolList;

	private Tool activeTool;

	// -- ToolService methods --

	@Override
	public Tool getTool(final String name) {
		final Tool alwaysActiveTool = alwaysActiveTools().get(name);
		if (alwaysActiveTool != null) return alwaysActiveTool;
		return tools().get(name);
	}

	@Override
	public List<Tool> getTools() {
		return toolList();
	}

	@Override
	public List<Tool> getAlwaysActiveTools() {
		return alwaysActiveToolList();
	}

	@Override
	public Tool getActiveTool() {
		return activeTool();
	}

	@Override
	public void setActiveTool(final Tool activeTool) {
		if (activeTool() == activeTool) return; // nothing to do
		assert this.activeTool != null;
		if (activeTool == null) {
			throw new IllegalArgumentException("Active tool cannot be null");
		}

		// deactivate old tool
		this.activeTool.deactivate();
		eventService.publish(new ToolDeactivatedEvent(this.activeTool));

		// activate new tool
		this.activeTool = activeTool;
		activeTool.activate();
		eventService.publish(new ToolActivatedEvent(activeTool));
	}

	@Override
	public void reportRectangle(final double x, final double y, final double w,
		final double h)
	{
		final DecimalFormat f = new DecimalFormat("0.##");
		final String fx = f.format(x);
		final String fy = f.format(y);
		final String fw = f.format(w);
		final String fh = f.format(h);
		statusService.showStatus("x=" + fx + ", y=" + fy + ", w=" + fw + ", h=" +
			fh);
	}

	@Override
	public void reportLine(final double x1, final double y1, final double x2,
		final double y2)
	{
		// compute line angle
		final double dx = x2 - x1;
		final double dy = y1 - y2;
		final double angle = 180.0 / Math.PI * Math.atan2(dy, dx);

		// compute line length
		final double w = Math.abs(x2 - x1);
		final double h = Math.abs(y2 - y1);
		final double length = Math.sqrt(w * w + h * h);

		final DecimalFormat f = new DecimalFormat("0.##");
		final String fx = f.format(x2);
		final String fy = f.format(y2);
		final String fa = f.format(angle);
		final String fl = f.format(length);
		statusService.showStatus("x=" + fx + ", y=" + fy + ", angle=" + fa +
			", length=" + fl);
	}

	@Override
	public void reportPoint(final double x, final double y) {
		final DecimalFormat f = new DecimalFormat("0.##");
		final String fx = f.format(x);
		final String fy = f.format(y);
		statusService.showStatus("x=" + fx + ", y=" + fy);
	}

	// -- Event handlers --

	@EventHandler
	protected void onEvent(final KyPressedEvent event) {
		if (event.isConsumed()) return;
		final Tool aTool = getActiveTool();
		if (eventOk(event, aTool)) aTool.onKeyDown(event);
		for (final Tool tool : getAlwaysActiveTools()) {
			if (event.isConsumed()) break;
			if (eventOk(event, tool)) tool.onKeyDown(event);
		}
	}

	@EventHandler
	protected void onEvent(final KyReleasedEvent event) {
		if (event.isConsumed()) return;
		final Tool aTool = getActiveTool();
		if (eventOk(event, aTool)) aTool.onKeyUp(event);
		for (final Tool tool : getAlwaysActiveTools()) {
			if (event.isConsumed()) break;
			if (eventOk(event, tool)) tool.onKeyUp(event);
		}
	}

	@EventHandler
	protected void onEvent(final MsPressedEvent event) {
		if (event.isConsumed()) return;
		final Tool aTool = getActiveTool();
		if (eventOk(event, aTool)) aTool.onMouseDown(event);
		for (final Tool tool : getAlwaysActiveTools()) {
			if (event.isConsumed()) break;
			if (eventOk(event, tool)) tool.onMouseDown(event);
		}
	}

	@EventHandler
	protected void onEvent(final MsReleasedEvent event) {
		if (event.isConsumed()) return;
		final Tool aTool = getActiveTool();
		if (eventOk(event, aTool)) aTool.onMouseUp(event);
		for (final Tool tool : getAlwaysActiveTools()) {
			if (event.isConsumed()) break;
			if (eventOk(event, tool)) tool.onMouseUp(event);
		}
	}

	@EventHandler
	protected void onEvent(final MsClickedEvent event) {
		if (event.isConsumed()) return;
		final Tool aTool = getActiveTool();
		if (eventOk(event, aTool)) aTool.onMouseClick(event);
		for (final Tool tool : getAlwaysActiveTools()) {
			if (event.isConsumed()) break;
			if (eventOk(event, tool)) tool.onMouseClick(event);
		}
	}

	@EventHandler
	protected void onEvent(final MsMovedEvent event) {
		if (event.isConsumed()) return;
		final Tool aTool = getActiveTool();
		if (eventOk(event, aTool)) aTool.onMouseMove(event);
		for (final Tool tool : getAlwaysActiveTools()) {
			if (event.isConsumed()) break;
			if (eventOk(event, tool)) tool.onMouseMove(event);
		}
	}

	@EventHandler
	protected void onEvent(final MsDraggedEvent event) {
		if (event.isConsumed()) return;
		final Tool aTool = getActiveTool();
		if (eventOk(event, aTool)) aTool.onMouseDrag(event);
		for (final Tool tool : getAlwaysActiveTools()) {
			if (event.isConsumed()) break;
			if (eventOk(event, tool)) tool.onMouseDrag(event);
		}
	}

	@EventHandler
	protected void onEvent(final MsWheelEvent event) {
		if (event.isConsumed()) return;
		final Tool aTool = getActiveTool();
		if (eventOk(event, aTool)) aTool.onMouseWheel(event);
		for (final Tool tool : getAlwaysActiveTools()) {
			if (event.isConsumed()) break;
			if (eventOk(event, tool)) tool.onMouseWheel(event);
		}
	}

	// -- Helper methods - lazy initialization --

	/** Gets {@link #alwaysActiveTools}, initializing if needed. */
	private Map<String, Tool> alwaysActiveTools() {
		if (alwaysActiveTools == null) initAlwaysActiveTools();
		return alwaysActiveTools;
	}

	/** Gets {@link #alwaysActiveToolList}, initializing if needed. */
	private List<Tool> alwaysActiveToolList() {
		if (alwaysActiveToolList == null) initAlwaysActiveToolList();
		return alwaysActiveToolList;
	}

	/** Gets {@link #tools}, initializing if needed. */
	private Map<String, Tool> tools() {
		if (tools == null) initTools();
		return tools;
	}

	/** Gets {@link #toolList}, initializing if needed. */
	private List<Tool> toolList() {
		if (toolList == null) initToolList();
		return toolList;
	}

	/** Gets {@link #activeTool}, initializing if needed. */
	private Tool activeTool() {
		if (activeTool == null) initActiveTool();
		return activeTool;
	}

	/** Initializes {@link #alwaysActiveTools}. */
	private synchronized void initAlwaysActiveTools() {
		if (alwaysActiveTools != null) return; // already initialized

		final HashMap<String, Tool> map = new HashMap<>();
		for (final Tool tool : alwaysActiveToolList()) {
			map.put(tool.getInfo().getName(), tool);
		}

		alwaysActiveTools = map;
	}

	/** Initializes {@link #alwaysActiveToolList}. */
	private synchronized void initAlwaysActiveToolList() {
		if (alwaysActiveToolList != null) return; // already initialized

		final ArrayList<Tool> list = new ArrayList<>();
		for (final Tool tool : getInstances()) {
			if (!tool.isAlwaysActive()) continue;
			list.add(tool);
		}

		alwaysActiveToolList = list;
	}

	/** Initializes {@link #tools}. */
	private synchronized void initTools() {
		if (tools != null) return; // already initialized

		final HashMap<String, Tool> map = new HashMap<>();
		for (final Tool tool : toolList()) {
			map.put(tool.getInfo().getName(), tool);
		}

		tools = map;
	}

	/** Initializes {@link #toolList}. */
	private synchronized void initToolList() {
		if (toolList != null) return; // already initialized

		final ArrayList<Tool> list = new ArrayList<>();
		for (final Tool tool : getInstances()) {
			if (tool.isAlwaysActive()) continue;
			list.add(tool);
		}

		toolList = list;
	}

	/** Initializes {@link #activeTool}. */
	private synchronized void initActiveTool() {
		if (activeTool != null) return; // already initialized

		final Tool rectangle = getTool("Rectangle");
		final Tool active = rectangle == null ? new DummyTool() : rectangle;

		activeTool = active;
	}

	// -- Helper methods - other --

	/** Checks that an event is OK to be dispatched to a particular tool. */
	private boolean eventOk(final DisplayEvent event, final Tool tool) {
		if (event.getDisplay() != null) return true;
		// NB: An event with a null display came from the main app frame.
		// We only pass these events on to tools flagged with activeInAppFrame.
		return tool.isActiveInAppFrame();
	}

}
