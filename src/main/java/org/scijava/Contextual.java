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

import org.scijava.plugin.Parameter;
import org.scijava.service.Service;

/**
 * An object that belongs to a SciJava application context.
 * 
 * @author Lee Kamentsky
 * @author Curtis Rueden
 */
public interface Contextual {

	/**
	 * Gets the application context to which the object belongs.
	 * 
	 * @see #getContext()
	 * @throws NullContextException if the context has not yet been set via
	 *           {@link #setContext(Context)}.
	 */
	Context context();

	/**
	 * Gets the application context to which the object belongs, or null if
	 * {@link #setContext(Context)} has not yet been called on this object.
	 * 
	 * @see #context()
	 */
	Context getContext();

	/**
	 * Sets the application context to which the object belongs.
	 * <p>
	 * Typically this method simply delegates to {@link Context#inject(Object)},
	 * and should be called only once to populate the context. Most contextual
	 * objects do not support later alteration of the context, and will throw
	 * {@link IllegalStateException} if this method is invoked again.
	 * </p>
	 * 
	 * @see Context#inject(Object)
	 * @throws IllegalStateException If the object already has a context.
	 * @throws IllegalArgumentException If the object has a required
	 *           {@link Service} parameter (see {@link Parameter#required()})
	 *           which is not available from the context.
	 */
	default void setContext(final Context context) {
		context.inject(this);
	}


}
