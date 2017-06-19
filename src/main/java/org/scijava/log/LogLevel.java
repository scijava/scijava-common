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

/**
 * Constants for specifying a logger's level of verbosity.
 * 
 * @author Curtis Rueden
 */
public final class LogLevel {

	private LogLevel() {
		// prevent instantiation of utility class
	}

	public static final int NONE = 0;
	public static final int ERROR = 1;
	public static final int WARN = 2;
	public static final int INFO = 3;
	public static final int DEBUG = 4;
	public static final int TRACE = 5;

	public static String prefix(final int level) {
		switch (level) {
			case ERROR:
				return "ERROR";
			case WARN:
				return "WARNING";
			case INFO:
				return "INFO";
			case DEBUG:
				return "DEBUG";
			case TRACE:
				return "TRACE";
			default:
				return "LEVEL" + level;
		}
	}

	/**
	 * Extracts the log level value from a string.
	 * 
	 * @return The log level, or -1 if the level cannot be parsed.
	 */
	public static int value(final String s) {
		if (s == null) return -1;

		// check whether it's a string label (e.g., "debug")
		final String log = s.trim().toLowerCase();
		if (log.startsWith("n")) return LogLevel.NONE;
		if (log.startsWith("e")) return LogLevel.ERROR;
		if (log.startsWith("w")) return LogLevel.WARN;
		if (log.startsWith("i")) return LogLevel.INFO;
		if (log.startsWith("d")) return LogLevel.DEBUG;
		if (log.startsWith("t")) return LogLevel.TRACE;

		// check whether it's a numerical value (e.g., 5)
		try {
			return Integer.parseInt(log);
		}
		catch (final NumberFormatException exc) {
			// nope!
		}
		return -1;
	}

}
