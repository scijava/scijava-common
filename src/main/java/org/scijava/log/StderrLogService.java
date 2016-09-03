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

package org.scijava.log;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;
import org.scijava.service.Service;

/**
 * Implementation of {@link LogService} using the standard error stream.
 * <p>
 * Actually, this service is somewhat misnamed now, since it prints {@code WARN}
 * and {@code ERROR} messages to stderr, but messages at lesser severities to
 * stdout.
 * </p>
 * 
 * @author Johannes Schindelin
 * @author Curtis Rueden
 */
@Plugin(type = Service.class, priority = Priority.LOW_PRIORITY)
public class StderrLogService extends AbstractLogService {

	@Override
	protected void log(final int level, final Object msg) {
		final String prefix = getPrefix(level);
		final String message = (prefix == null ? "" : prefix + " ") + msg;
		// NB: Emit severe messages to stderr, and less severe ones to stdout.
		if (level <= WARN) System.err.println(message);
		else System.out.println(message);
	}

	/**
	 * Prints a message to stderr.
	 * 
	 * @param message the message
	 */
	@Override
	protected void log(final String message) {
		System.err.println(message);
	}

	/**
	 * Prints an exception to stderr.
	 * 
	 * @param t the exception
	 */
	@Override
	protected void log(final Throwable t) {
		t.printStackTrace();
	}

}
