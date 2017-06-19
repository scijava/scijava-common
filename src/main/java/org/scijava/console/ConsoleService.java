/*
 * #%L
 * SciJava Common shared library for SciJava software.
 * %%
 * Copyright (C) 2009 - 2017 Board of Regents of the University of
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

package org.scijava.console;

import java.io.PrintStream;
import java.util.LinkedList;

import org.scijava.plugin.HandlerService;
import org.scijava.service.SciJavaService;

/**
 * Interface for service that manages interaction with the console.
 * <p>
 * In particular, this is the service that defines how command line arguments
 * are handled. It also provides an extension mechanism for {@code stdout} and
 * {@code stderr} logging.
 * </p>
 *
 * @author Curtis Rueden
 */
public interface ConsoleService extends
	HandlerService<LinkedList<String>, ConsoleArgument>, SciJavaService
{

	/** Handles arguments from an external source such as the command line. */
	void processArgs(String... args);

	/** Adds a listener for output sent to {@code stdout} or {@code stderr}. */
	void addOutputListener(OutputListener l);

	/** Removes a listener for output sent to {@code stdout} or {@code stderr}. */
	void removeOutputListener(OutputListener l);

	/** Notifies listeners of output sent to {@code stdout} or {@code stderr}. */
	void notifyListeners(OutputEvent event);

	// -- PTService methods --

	@Override
	default Class<ConsoleArgument> getPluginType() {
		return ConsoleArgument.class;
	}

	// -- Typed methods --

	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	default Class<LinkedList<String>> getType() {
		return (Class) LinkedList.class;
	}
}
