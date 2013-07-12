/*
 * #%L
 * SciJava Common shared library for SciJava software.
 * %%
 * Copyright (C) 2009 - 2013 Board of Regents of the University of
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
 * 
 * The views and conclusions contained in the software and documentation are
 * those of the authors and should not be interpreted as representing official
 * policies, either expressed or implied, of any organization.
 * #L%
 */

package org.scijava.service;

import org.scijava.Context;
import org.scijava.plugin.SortablePlugin;

/**
 * Abstract superclass of {@link Service} implementations.
 * 
 * @author Curtis Rueden
 */
public abstract class AbstractService extends SortablePlugin implements
	Service
{

	/**
	 * A pointer to the service's {@link Context}. Note that the context is not
	 * set in the superclass until {@link #initialize()} is called; this deferral
	 * ensures that event handler methods are not registered until after service
	 * initialization is complete.
	 */
	private Context context;

	// -- Service methods --

	@Override
	public void initialize() {
		// NB: Do nothing by default.
	}

	@Override
	public void registerEventHandlers() {
		// NB: The AbstractContextual superclass automatically takes
		// care of registering event handlers when its context is set.
		super.setContext(context);
	}

	// -- Contextual methods --

	@Override
	public Context getContext() {
		return context;
	}

	@Override
	public void setContext(final Context context) {
		// NB: Do not call super.setContext(Context) yet!
		// It happens later, in registerEventHandlers().
		this.context = context;
	}

	// -- Disposable methods --

	@Override
	public void dispose() {
		// NB: Do nothing by default.
	}

	// -- Object methods --

	@Override
	public String toString() {
		return getClass().getName() + " [priority = " + getPriority() + "]";
	}

}
