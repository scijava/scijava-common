/*
 * #%L
 * SciJava Common shared library for SciJava software.
 * %%
 * Copyright (C) 2009 - 2023 SciJava developers.
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

import java.lang.Thread.UncaughtExceptionHandler;

/**
 * A handler for otherwise uncaught exceptions.
 * <p>
 * Installing this handler will automatically log the exceptions that would
 * otherwise be missed; e.g., when things go awry on the AWT Event Dispatch
 * Thread.
 * </p>
 * 
 * @author Johannes Schindelin
 */
public class DefaultUncaughtExceptionHandler implements
	UncaughtExceptionHandler
{

	private final LogService log;

	public DefaultUncaughtExceptionHandler(final LogService log) {
		this.log = log;
	}

	@Override
	public void
		uncaughtException(final Thread thread, final Throwable throwable)
	{
		log.error("Uncaught exception in thread " + thread, throwable);
	}

	public void handle(final Exception exception) {
		log.error("Uncaught exception on the Event Dispatch Thread", exception);
	}

	public static void install(final LogService log) {
		final UncaughtExceptionHandler handler =
			new DefaultUncaughtExceptionHandler(log);
		Thread.setDefaultUncaughtExceptionHandler(handler);
	}

}
