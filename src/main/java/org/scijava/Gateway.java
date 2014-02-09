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

package org.scijava;

import org.scijava.app.App;
import org.scijava.app.AppService;
import org.scijava.app.StatusService;
import org.scijava.event.EventHistory;
import org.scijava.event.EventService;
import org.scijava.log.LogService;
import org.scijava.object.ObjectService;
import org.scijava.plugin.PluginService;
import org.scijava.service.Service;
import org.scijava.thread.ThreadService;

/**
 * Interface for convenience classes that wrap a {@link Context} to provide
 * one-line access to a suite of {@link Service}s.
 * <p>
 * The {@link #get} methods provide consistent {@link Service} instantiation,
 * while throwing {@link NoSuchServiceException} if the requested
 * {@link Service} is not found.
 * </p>
 * <h3>Sample implementation</h3>
 * <p>
 * Let's say we have a {@code Kraken} service and a {@code Cow} service. Using
 * the {@code Context} directly, the code would look like:
 * </p>
 * <pre>
 * Context context = new Context();
 * context.getService(Cow.class).feedToKraken();
 * context.getService(Kraken.class).burp();</pre>
 * <p>
 * To perform these actions, you have to know <em>a priori</em> to ask for a
 * {@code Cow} and a {@code Kraken}; i.e., your IDE's code completion will not
 * give you a hint. Further, if either service is unavailable, a
 * {@link NullPointerException} is thrown.
 * </p>
 * <p>
 * But if we create a {@code Gateway} class called {@code Animals} with the
 * following signatures:
 * </p>
 * <pre>
 * public Cow cow() { return get(Cow.class); }
 * public Kraken kraken() { return get(Kraken.class); }</pre>
 * <p>
 * We can now access our services through the new {@code Animals} gateway:
 * </p>
 * <pre>
 * Animals animals = new Animals();
 * animals.cow().feedToKraken();
 * animals.kraken().burp();</pre>
 * <p>
 * This provides succinct yet explicit access to the {@code Cow} and
 * {@code Kraken} services; it is a simple two-layer access to functionality,
 * which an IDE can auto-complete. And if one of the services is not available,
 * a {@link NoSuchServiceException} is thrown, which facilitates appropriate
 * (but optional) handling of missing services.
 * </p>
 * 
 * @see Context
 * @author Mark Hiner
 * @author Curtis Rueden
 */
public interface Gateway extends Contextual {

	/**
	 * Returns an implementation of the requested {@link Service}, if it exists in
	 * the underlying {@link Context}.
	 * 
	 * @param serviceClass the requested {@link Service}
	 * @return The singleton instance of the given class
	 * @throws NullContextException if the application context is not set.
	 * @throws NoSuchServiceException if there is no service of the given class.
	 */
	<S extends Service> S get(Class<S> serviceClass);

	/**
	 * Returns an implementation of the {@link Service} with the given class name,
	 * if it exists in the underlying {@link Context}.
	 * 
	 * @param serviceClassName name of the requested {@link Service}
	 * @return The singleton instance of the requested {@link Service}
	 * @throws NullContextException if the application context is not set.
	 * @throws NoSuchServiceException if there is no service matching
	 *           {@code serviceClassName}.
	 */
	Service get(final String serviceClassName);

	// -- Gateway methods - services --

	AppService app();

	EventHistory eventHistory();

	EventService event();

	LogService log();

	ObjectService object();

	PluginService plugin();

	StatusService status();

	ThreadService thread();

	// -- Gateway methods - application --

	/** @see org.scijava.app.AppService */
	App getApp();

	/** @see org.scijava.app.App#getTitle() */
	String getTitle();

	/** @see org.scijava.app.App#getVersion() */
	String getVersion();

	/** @see org.scijava.app.App#getInfo(boolean) */
	String getInfo(boolean mem);

}
