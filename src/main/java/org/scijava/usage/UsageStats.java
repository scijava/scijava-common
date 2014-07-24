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

package org.scijava.usage;

import org.scijava.AbstractBasicDetails;
import org.scijava.BasicDetails;
import org.scijava.Identifiable;
import org.scijava.Locatable;
import org.scijava.Versioned;

/**
 * Data structure storing usage statistics for a particular identifier.
 * 
 * @author Curtis Rueden
 */
public class UsageStats extends AbstractBasicDetails implements Identifiable,
	Locatable, Versioned
{

	/** The object's unique identifier. */
	private String id;

	/** This object's location URL. */
	private String url;

	/** The object's version. */
	private String version;

	/** Number of times the object was used. */
	private long count;

	/**
	 * Creates usage statistics for the given object. Note that while several
	 * pieces of information are initially extracted from the object, no reference
	 * is retained to the object itself.
	 */
	public UsageStats(final Object o) {
		if (o instanceof BasicDetails) {
			final BasicDetails basicDetails = (BasicDetails) o;
			setName(basicDetails.getName());
			setLabel(basicDetails.getLabel());
			setDescription(basicDetails.getDescription());
		}
		id = o instanceof Identifiable ? ((Identifiable) o).getIdentifier() : null;
		url = o instanceof Locatable ? ((Locatable) o).getLocation() : null;
		version = o instanceof Versioned ? ((Versioned) o).getVersion() : null;
	}

	/** Gets the number of times the object has been used. */
	public long getCount() {
		return count;
	}

	/** Increment the object's usage count. */
	public void increment() {
		count++;
	}

	// -- Identifiable methods --

	@Override
	public String getIdentifier() {
		return id;
	}

	// -- Locatable methods --

	@Override
	public String getLocation() {
		return url;
	}

	// -- Versioned methods --

	@Override
	public String getVersion() {
		return version;
	}

}
