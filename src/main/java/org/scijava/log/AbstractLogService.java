/*
 * #%L
 * SciJava Common shared library for SciJava software.
 * %%
 * Copyright (C) 2009 - 2013 Board of Regents of the University of
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

package org.scijava.log;

import org.scijava.service.AbstractService;

/**
 * Base implementation of an abstract {@link LogService}.
 * 
 * @author Johannes Schindelin
 */
public abstract class AbstractLogService extends AbstractService implements LogService {
	private int currentLevel = WARN;

	// -- abstract methods --

	/**
	 * Displays a message.
	 * 
	 * @param msg the message to display.
	 */
	protected abstract void log(final String msg);

	/**
	 * Displays an exception.
	 * 
	 * @param t the exception to display.
	 */
	protected abstract void log(final Throwable t);

	// -- constructor --

	public AbstractLogService() {
		// check SciJava log level system property for initial logging level
		final String logProp = System.getProperty(LOG_LEVEL_PROPERTY);
		if (logProp != null) {
			// check whether it's a string label (e.g., "debug")
			final String log = logProp.trim().toLowerCase();
			if (log.startsWith("n")) setLevel(NONE);
			else if (log.startsWith("e")) setLevel(ERROR);
			else if (log.startsWith("w")) setLevel(WARN);
			else if (log.startsWith("i")) setLevel(INFO);
			else if (log.startsWith("d")) setLevel(DEBUG);
			else if (log.startsWith("t")) setLevel(TRACE);
			else {
				// check whether it's a numerical value (e.g., 5)
				try {
					setLevel(Integer.parseInt(log));
				}
				catch (final NumberFormatException exc) {
					// nope!
				}
			}
		}

		if (getLevel() == 0) {
			// use the default, which is INFO unless the DEBUG env. variable is set
			setLevel(System.getenv("DEBUG") == null ? INFO : DEBUG);
		}
	}

	// -- helper methods --

	protected void log(final int level, final Object msg, final Throwable t) {
		if (level > currentLevel) return;

		if (msg != null || t == null) {
			log(level, msg);
		}
		if (t != null) log(t);
	}

	protected void log(final int level, final Object msg) {
		final String prefix = getPrefix(level);
		log((prefix == null ? "" : prefix + " ") + msg);
	}

	protected String getPrefix(int level) {
		switch (level) {
		case ERROR:
			return "[ERROR]";
		case WARN:
			return "[WARNING]";
		case INFO:
			return "[INFO]";
		case DEBUG:
			return "[DEBUG]";
		case TRACE:
			return "[TRACE]";
		default:
			return null;
		}
	}

	// -- LogService methods --

	@Override
	public void debug(Object msg) {
		log(DEBUG, msg, null);
	}

	@Override
	public void debug(Throwable t) {
		log(DEBUG, null, t);
	}

	@Override
	public void debug(Object msg, Throwable t) {
		log(DEBUG, msg, t);
	}

	@Override
	public void error(Object msg) {
		log(ERROR, msg, null);
	}

	@Override
	public void error(Throwable t) {
		log(ERROR, null, t);
	}

	@Override
	public void error(Object msg, Throwable t) {
		log(ERROR, msg, t);
	}

	@Override
	public void info(Object msg) {
		log(INFO, msg, null);
	}

	@Override
	public void info(Throwable t) {
		log(INFO, null, t);
	}

	@Override
	public void info(Object msg, Throwable t) {
		log(INFO, msg, t);
	}

	@Override
	public void trace(Object msg) {
		log(TRACE, msg, null);
	}

	@Override
	public void trace(Throwable t) {
		log(TRACE, null, t);
	}

	@Override
	public void trace(Object msg, Throwable t) {
		log(TRACE, msg, t);
	}

	@Override
	public void warn(Object msg) {
		log(WARN, msg, null);
	}

	@Override
	public void warn(Throwable t) {
		log(WARN, null, t);
	}

	@Override
	public void warn(Object msg, Throwable t) {
		log(WARN, msg, t);
	}

	@Override
	public boolean isDebug() {
		return currentLevel >= DEBUG;
	}

	@Override
	public boolean isError() {
		return currentLevel >= ERROR;
	}

	@Override
	public boolean isInfo() {
		return currentLevel >= INFO;
	}

	@Override
	public boolean isTrace() {
		return currentLevel >= TRACE;
	}

	@Override
	public boolean isWarn() {
		return currentLevel >= WARN;
	}

	@Override
	public int getLevel() {
		return currentLevel;
	}

	@Override
	public void setLevel(int level) {
		currentLevel = level;
	}
}
