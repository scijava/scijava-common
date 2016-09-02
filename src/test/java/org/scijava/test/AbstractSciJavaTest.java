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

package org.scijava.test;

import org.junit.After;
import org.junit.Before;
import org.scijava.Context;
import org.scijava.plugin.Parameter;
import org.scijava.service.Service;

/**
 * Base class for unit testing of SciJava components.
 * <p>
 * Many SciJava-based unit tests need to have a {@link Context} with relevant
 * services. Following the
 * <a href="https://en.wikipedia.org/wiki/Don%27t_repeat_yourself">DRY
 * principle</a>, we should implement it only once. Here.
 * </p>
 * 
 * @author Johannes Schindelin
 * @author Curtis Rueden
 */
public abstract class AbstractSciJavaTest {

	@Parameter
	protected Context context;

	/** Subclasses can override to create a differently configured context. */
	protected Context createContext() {
		return new Context(serviceClasses());
	}

	/** Subclasses must override to define the services the context will have. */
	protected abstract Class<? extends Service>[] serviceClasses();

	/** Sets up a SciJava context and injects needed services. */
	@Before
	public void setUp() {
		createContext().inject(this);
	}

	/**
	 * Disposes of the {@link Context} that was initialized in {@link #setUp()}.
	 */
	@After
	public synchronized void cleanUp() {
		if (context != null) {
			context.dispose();
			context = null;
		}
	}

}
