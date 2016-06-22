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

package org.scijava;

import java.util.HashMap;
import java.util.Map;

import org.scijava.util.StringMaker;

/**
 * Abstract superclass of {@link BasicDetails} implementations.
 * 
 * @author Curtis Rueden
 */
public abstract class AbstractBasicDetails implements BasicDetails {

	/** Unique name of the object. */
	private String name;

	/** Human-readable label for describing the object. */
	private String label;

	/** String describing the object in detail. */
	private String description;

	/** Table of extra key/value pairs. */
	private final Map<String, String> values = new HashMap<>();

	// -- Object methods --

	@Override
	public String toString() {
		final StringMaker sm = new StringMaker();
		sm.append("name", name);
		sm.append("label", label);
		sm.append("description", description);
		for (final String key : values.keySet()) {
			sm.append(key, values.get(key));
		}
		return sm.toString();
	}

	// -- BasicDetails methods --

	@Override
	public String getLabel() {
		return label;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public boolean is(final String key) {
		return values.containsKey(key);
	}

	@Override
	public String get(final String key) {
		return values.get(key);
	}

	@Override
	public void setLabel(final String label) {
		this.label = label;
	}

	@Override
	public void setDescription(final String description) {
		this.description = description;
	}

	@Override
	public void set(final String key, final String value) {
		values.put(key, value);
	}

	// -- Named methods --

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(final String name) {
		this.name = name;
	}

}
