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

package org.scijava.plugin;

import org.scijava.BasicDetails;
import org.scijava.Contextual;
import org.scijava.Identifiable;
import org.scijava.Locatable;
import org.scijava.Prioritized;
import org.scijava.Versioned;
import org.scijava.util.MiscUtils;

/**
 * Base interface for {@link Contextual}, {@link Prioritized} plugins that
 * retain access to their associated {@link PluginInfo} metadata via the
 * {@link HasPluginInfo} interface. This interface is intended as a convenient
 * extension point for new types of plugins.
 * 
 * @author Curtis Rueden
 */
public interface RichPlugin extends SciJavaPlugin, Contextual, Prioritized,
	HasPluginInfo, Identifiable, Locatable, Versioned, BasicDetails
{

	// -- Identifiable methods --

	@Override
	default String getIdentifier() {
		return "plugin:" + getClass().getName();
	}

	// -- BasicDetails methods --

	@Override
	default String getLabel() {
		return getInfo().getLabel();
	}

	@Override
	default String getDescription() {
		return getInfo().getDescription();
	}

	@Override
	default boolean is(final String key) {
		return getInfo().is(key);
	}

	@Override
	default String get(final String key) {
		return getInfo().get(key);
	}

	@Override
	default void setLabel(final String label) {
		throw new UnsupportedOperationException();
	}

	@Override
	default void setDescription(final String description) {
		throw new UnsupportedOperationException();
	}

	@Override
	default void set(final String key, final String value) {
		throw new UnsupportedOperationException();
	}

	// -- Named methods --

	@Override
	default String getName() {
		return getInfo().getName();
	}

	@Override
	default void setName(final String name) {
		throw new UnsupportedOperationException();
	}

	// -- Comparable methods --

	@Override
	default int compareTo(final Prioritized that) {
		final int compare = Prioritized.super.compareTo(that);
		if (compare != 0) return compare;

		if (!(that instanceof BasicDetails)) return 1;
		final BasicDetails basicDetails = (BasicDetails) that;

		// compare names
		final String thisName = getName();
		final String thatName = basicDetails.getName();
		return MiscUtils.compare(thisName, thatName);
	}

}
