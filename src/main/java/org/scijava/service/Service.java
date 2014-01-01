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
 * 
 * The views and conclusions contained in the software and documentation are
 * those of the authors and should not be interpreted as representing official
 * policies, either expressed or implied, of any organization.
 * #L%
 */

package org.scijava.service;

import org.scijava.Disposable;
import org.scijava.plugin.Plugin;
import org.scijava.plugin.RichPlugin;

/**
 * A SciJava service, for a particular area of functionality.
 * <p>
 * Services discoverable at runtime must implement this interface and be
 * annotated with @{@link Plugin} with attribute {@link Plugin#type()} =
 * {@link Service}.class. While it possible to create a service merely by
 * implementing this interface, it is encouraged to instead extend
 * {@link AbstractService}, for convenience.
 * </p>
 * 
 * @author Curtis Rueden
 * @see Plugin
 */
public interface Service extends RichPlugin, Disposable {

	/**
	 * Performs any needed initialization when the service is first loaded.
	 * <p>
	 * NB: This method is not intended to be called directly. It is called by
	 * the service framework itself (specifically by the {@link ServiceHelper})
	 * when initializing the service. It should not be called a second time.
	 * </p>
	 */
	void initialize();

	/**
	 * Registers the service's event handler methods.
	 * <p>
	 * NB: This method is not intended to be called directly. It is called by
	 * the service framework itself (specifically by the {@link ServiceHelper})
	 * when initializing the service. It should not be called a second time.
	 * </p>
	 */
	void registerEventHandlers();

}
