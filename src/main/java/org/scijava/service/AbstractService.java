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

package org.scijava.service;

import org.scijava.Context;
import org.scijava.plugin.AbstractRichPlugin;

/**
 * Abstract superclass of {@link Service} implementations.
 * 
 * @author Curtis Rueden
 */
public abstract class AbstractService extends AbstractRichPlugin implements
	Service
{

	/**
	 * A pointer to the service's {@link Context}. Note that for two reasons, the
	 * context is not set in the superclass:
	 * <ol>
	 * <li>As services are initialized, their dependencies are recursively created
	 * and initialized too, which is something that normal context injection does
	 * not handle. I.e., the {@link Context#inject(Object)} method assumes the
	 * context and its associated services have all been initialized already.</li>
	 * <li>Event handler methods must not be registered until after service
	 * initialization is complete (i.e., during {@link #registerEventHandlers()},
	 * after {@link #initialize()}).</li>
	 * </ol>
	 */
	private Context context;

	// -- Contextual methods --

	@Override
	public Context getContext() {
		return context;
	}

	@Override
	public void setContext(final Context context) {
		// NB: Do not call super.setContext(Context)!
		// The ServiceHelper populates service parameters.
		// We do this because we need to recursively create and initialize
		// service dependencies, rather than merely injecting existing ones.
		this.context = context;
	}

	// -- Object methods --

	@Override
	public String toString() {
		return getClass().getName() + " [priority = " + getPriority() + "]";
	}

}
