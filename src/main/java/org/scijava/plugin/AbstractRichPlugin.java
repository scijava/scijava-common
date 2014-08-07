/*
 * #%L
 * SciJava Common shared library for SciJava software.
 * %%
 * Copyright (C) 2009 - 2014 Board of Regents of the University of
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

package org.scijava.plugin;

import java.net.URL;

import org.scijava.AbstractContextual;
import org.scijava.BasicDetails;
import org.scijava.Prioritized;
import org.scijava.Priority;
import org.scijava.util.ClassUtils;
import org.scijava.util.Manifest;
import org.scijava.util.MiscUtils;

/**
 * Abstract base class for {@link RichPlugin} implementations.
 * 
 * @author Curtis Rueden
 */
public abstract class AbstractRichPlugin extends AbstractContextual implements
	RichPlugin
{

	/** The priority of the plugin. */
	private double priority = Priority.NORMAL_PRIORITY;

	/** The metadata associated with the plugin. */
	private PluginInfo<?> info;

	// -- Object methods --

	@Override
	public String toString() {
		final PluginInfo<?> pi = getInfo();
		return pi == null ? super.toString() : pi.getTitle();
	}

	// -- Prioritized methods --

	@Override
	public double getPriority() {
		return priority;
	}

	@Override
	public void setPriority(final double priority) {
		this.priority = priority;
	}

	// -- HasPluginInfo methods --

	@Override
	public PluginInfo<?> getInfo() {
		return info;
	}

	@Override
	public void setInfo(final PluginInfo<?> info) {
		this.info = info;
	}

	// -- Identifiable methods --

	@Override
	public String getIdentifier() {
		return "plugin:" + getClass().getName();
	}

	// -- Locatable methods --

	@Override
	public String getLocation() {
		final URL location = ClassUtils.getLocation(getClass());
		return location == null ? null : location.toExternalForm();
	}

	// -- Versioned methods --

	@Override
	public String getVersion() {
		final Manifest m = Manifest.getManifest(getClass());
		return m == null ? null : m.getImplementationVersion();
	}

	// -- BasicDetails methods --

	@Override
	public String getName() {
		return getInfo().getName();
	}

	@Override
	public String getLabel() {
		return getInfo().getLabel();
	}

	@Override
	public String getDescription() {
		return getInfo().getDescription();
	}

	@Override
	public boolean is(String key) {
		return getInfo().is(key);
	}

	@Override
	public String get(String key) {
		return getInfo().get(key);
	}

	@Override
	public void setName(String name) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setLabel(String label) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setDescription(String description) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void set(String key, String value) {
		throw new UnsupportedOperationException();
	}

	// -- Comparable methods --

	@Override
	public int compareTo(final Prioritized that) {
		if (that == null) return 1;

		// compare priorities
		final int priorityCompare = Priority.compare(this, that);
		if (priorityCompare != 0) return priorityCompare;

		// compare classes
		final int classCompare = ClassUtils.compare(getClass(), that.getClass());
		if (classCompare != 0) return classCompare;

		if (!(that instanceof BasicDetails)) return 1;
		final BasicDetails basicDetails = (BasicDetails) that;

		// compare names
		final String thisName = getName();
		final String thatName = basicDetails.getName();
		return MiscUtils.compare(thisName, thatName);
	}

}
