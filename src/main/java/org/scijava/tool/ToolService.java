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

import java.util.List;

import org.scijava.plugin.SingletonService;
import org.scijava.service.SciJavaService;
import org.scijava.util.RealCoords;

/**
 * Interface for service that tracks available tools.
 * 
 * @author Grant Harris
 * @author Curtis Rueden
 */
public interface ToolService extends SingletonService<Tool>, SciJavaService {

	double SEPARATOR_DISTANCE = 10;

	Tool getTool(String name);

	/**
	 * Get a tool given its class.
	 * 
	 * @param <T> the tool's type
	 * @param toolClass the class of the tool to fetch
	 * @return the tool, or null if no such tool
	 */
	default <T extends Tool> T getTool(final Class<T> toolClass) {
		for (final Tool tool : getAlwaysActiveTools()) {
			if (toolClass.isInstance(tool)) return toolClass.cast(tool);
		}
		for (final Tool tool : getTools()) {
			if (toolClass.isInstance(tool)) return toolClass.cast(tool);
		}
		return null;
	}

	List<Tool> getTools();

	List<Tool> getAlwaysActiveTools();

	Tool getActiveTool();

	void setActiveTool(Tool activeTool);

	/**
	 * Returns true if the two specified tools should have a separator between
	 * them on the tool bar.
	 */
	default boolean isSeparatorNeeded(final Tool tool1, final Tool tool2) {
		if (tool1 == null || tool2 == null) return false;
		final double priority1 = tool1.getInfo().getPriority();
		final double priority2 = tool2.getInfo().getPriority();
		return Math.abs(priority1 - priority2) >= SEPARATOR_DISTANCE;
	}

	/** Publishes rectangle dimensions in the status bar. */
	void reportRectangle(final double x, final double y, final double w,
		final double h);

	/** Publishes rectangle dimensions in the status bar. */
	default void reportRectangle(final RealCoords p1, final RealCoords p2) {
		final double x = Math.min(p1.x, p2.x);
		final double y = Math.min(p1.y, p2.y);
		final double w = Math.abs(p2.x - p1.x);
		final double h = Math.abs(p2.y - p1.y);
		reportRectangle(x, y, w, h);
	}

	/** Publishes line length and angle in the status bar. */
	void reportLine(final double x1, final double y1, final double x2,
		final double y2);

	/** Publishes line length and angle in the status bar. */
	default void reportLine(final RealCoords p1, final RealCoords p2) {
		reportLine(p1.x, p1.y, p2.x, p2.y);
	}

	/** Publishes point coordinates to the status bar. */
	void reportPoint(final double x, final double y);

	/** Publishes point coordinates to the status bar. */
	default void reportPoint(final RealCoords p) {
		reportPoint(p.x, p.y);
	}

	// -- PTService methods --

	@Override
	default Class<Tool> getPluginType() {
		return Tool.class;
	}
}
