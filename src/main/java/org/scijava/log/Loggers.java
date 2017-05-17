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

package org.scijava.log;

import java.util.function.Predicate;

/**
 * @author Matthias Arzt
 */
@IgnoreAsCallingClass
public class Loggers {

	private static final ListenableLogger DUMMY_LOGGER = new ListenableLogger() {

		@Override
		public void addListener(LogListener listener) {
			// ignore
		}

		@Override
		public void removeListener(LogListener listener) {
			// ignore
		}

		@Override
		public void setParentForwardingFilter(Predicate<LogMessage> filter) {
			// ignore
		}

		@Override
		public void alwaysLog(int level, Object msg, Throwable t) {
			// ignore
		}

		@Override
		public String getName() {
			return "";
		}

		@Override
		public LogSource getSource() {
			return LogSource.root();
		}

		@Override
		public int getLevel() {
			return LogLevel.NONE;
		}

		@Override
		public Logger subLogger(String name, int level) {
			return this;
		}

		@Override
		public ListenableLogger listenableLogger(int level) {
			return this;
		}
	};

	private Loggers() {
		// prevent instantiation of utility class
	}

	/** A {@link Logger} that discards all log messages */
	public static ListenableLogger silent() {
		return DUMMY_LOGGER;
	}

	/** Returns a new {@link ListenableLogger}. */
	public static ListenableLogger newRoot(int logLevel) {
		return DefaultListenableLogger.newRoot(logLevel);
	}
}
