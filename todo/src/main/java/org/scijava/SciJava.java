/*
 * #%L
 * SciJava Common shared library for SciJava software.
 * %%
 * Copyright (C) 2009 - 2017 Board of Regents of the University of
 * Wisconsin-Madison, Broad Institute of MIT and Harvard, Max Planck
 * Institute of Molecular Cell Biology and Genetics, University of
 * Konstanz, and KNIME GmbH.
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

import java.util.Collection;

import org.scijava.app.SciJavaApp;
import org.scijava.plugin.Plugin;
import org.scijava.service.Service;

/**
 * Main entry point into SciJava. This class enables working with SciJava
 * services in a simple way, while retaining extensibility (i.e., access to
 * third-party services).
 * 
 * @author Curtis Rueden
 */
@Plugin(type = Gateway.class)
public class SciJava extends AbstractGateway {

	// -- Constructors --

	/** Creates a new SciJava application context with all available services. */
	public SciJava() {
		this(new Context());
	}

	/**
	 * Creates a new Scijava application context.
	 * 
	 * @param empty If true, the context will be empty; otherwise, it will be
	 *          initialized with all available services.
	 */
	public SciJava(final boolean empty) {
		this(new Context(empty));
	}

	/**
	 * Creates a new SciJava application context with the specified services (and
	 * any required service dependencies).
	 * <p>
	 * <b>Developer's note:</b> This constructor's argument is raw (i.e.,
	 * {@code Class...} instead of {@code Class<? extends Service>...}) because
	 * otherwise, downstream invocations (e.g.,
	 * {@code new SciJava(LogService.class)}) yield the potentially confusing
	 * warning:
	 * </p>
	 * <blockquote>Type safety: A generic array of
	 * {@code Class<? extends Service>} is created for a varargs
	 * parameter</blockquote>
	 * <p>
	 * To avoid this, we have opted to use raw types and suppress the relevant
	 * warning here instead.
	 * </p>
	 * 
	 * @param serviceClasses A list of types that implement the {@link Service}
	 *          interface (e.g., {@code LogService.class}).
	 * @throws ClassCastException If any of the given arguments do not implement
	 *           the {@link Service} interface.
	 */
	@SuppressWarnings({ "rawtypes" })
	public SciJava(final Class... serviceClasses) {
		this(new Context(serviceClasses));
	}

	/**
	 * Creates a new SciJava application context with the specified services (and
	 * any required service dependencies).
	 * 
	 * @param serviceClasses A collection of types that implement the
	 *          {@link Service} interface (e.g., {@code LogService.class}).
	 */
	public SciJava(final Collection<Class<? extends Service>> serviceClasses) {
		this(new Context(serviceClasses));
	}

	/**
	 * Creates a new SciJava application context which wraps the given existing
	 * SciJava context.
	 * 
	 * @see Context
	 */
	public SciJava(final Context context) {
		super(SciJavaApp.NAME, context);
	}

	// -- Gateway methods --

	@Override
	public String getShortName() {
		return "sj";
	}

}
