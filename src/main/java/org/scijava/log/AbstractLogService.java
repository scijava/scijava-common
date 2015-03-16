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

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.scijava.service.AbstractService;

/**
 * Base class for {@link LogService} implementationst.
 *
 * @author Johannes Schindelin
 */
public abstract class AbstractLogService extends AbstractService implements
	LogService
{

	private int currentLevel = System.getenv("DEBUG") == null ? INFO : DEBUG;

	private final Map<String, Integer> classAndPackageLevels =
		new HashMap<>();

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
		// check SciJava log level system properties for initial logging levels

		// global log level property
		final String logProp = System.getProperty(LOG_LEVEL_PROPERTY);
		final int level = level(logProp);
		if (level >= 0) setLevel(level);

		if (getLevel() == 0) {
			// use the default, which is WARN unless the DEBUG env. variable is set
			setLevel(System.getenv("DEBUG") == null ? INFO : DEBUG);
		}

		// populate custom class- and package-specific log level properties
		final String logLevelPrefix = LOG_LEVEL_PROPERTY + ":";
		final Properties props = System.getProperties();
		for (final Object propKey : props.keySet()) {
			if (!(propKey instanceof String)) continue;
			final String propName = (String) propKey;
			if (!propName.startsWith(logLevelPrefix)) continue;
			final String classOrPackageName =
				propName.substring(logLevelPrefix.length());
			setLevel(classOrPackageName, level(props.getProperty(propName)));
		}

	}

	// -- helper methods --

	protected void log(final int level, final Object msg, final Throwable t) {
		if (level > getLevel()) return;

		if (msg != null || t == null) {
			log(level, msg);
		}
		if (t != null) log(t);
	}

	protected void log(final int level, final Object msg) {
		final String prefix = getPrefix(level);
		log((prefix == null ? "" : prefix + " ") + msg);
	}

	protected String getPrefix(final int level) {
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
	public void debug(final Object msg) {
		log(DEBUG, msg, null);
	}

	@Override
	public void debug(final Throwable t) {
		log(DEBUG, null, t);
	}

	@Override
	public void debug(final Object msg, final Throwable t) {
		log(DEBUG, msg, t);
	}

	@Override
	public void error(final Object msg) {
		log(ERROR, msg, null);
	}

	@Override
	public void error(final Throwable t) {
		log(ERROR, null, t);
	}

	@Override
	public void error(final Object msg, final Throwable t) {
		log(ERROR, msg, t);
	}

	@Override
	public void info(final Object msg) {
		log(INFO, msg, null);
	}

	@Override
	public void info(final Throwable t) {
		log(INFO, null, t);
	}

	@Override
	public void info(final Object msg, final Throwable t) {
		log(INFO, msg, t);
	}

	@Override
	public void trace(final Object msg) {
		log(TRACE, msg, null);
	}

	@Override
	public void trace(final Throwable t) {
		log(TRACE, null, t);
	}

	@Override
	public void trace(final Object msg, final Throwable t) {
		log(TRACE, msg, t);
	}

	@Override
	public void warn(final Object msg) {
		log(WARN, msg, null);
	}

	@Override
	public void warn(final Throwable t) {
		log(WARN, null, t);
	}

	@Override
	public void warn(final Object msg, final Throwable t) {
		log(WARN, msg, t);
	}

	@Override
	public boolean isDebug() {
		return getLevel() >= DEBUG;
	}

	@Override
	public boolean isError() {
		return getLevel() >= ERROR;
	}

	@Override
	public boolean isInfo() {
		return getLevel() >= INFO;
	}

	@Override
	public boolean isTrace() {
		return getLevel() >= TRACE;
	}

	@Override
	public boolean isWarn() {
		return getLevel() >= WARN;
	}

	@Override
	public int getLevel() {
		if (!classAndPackageLevels.isEmpty()) {
			// check for a custom log level for calling class or its parent packages
			String classOrPackageName = callingClass();
			while (classOrPackageName != null) {
				final Integer level = classAndPackageLevels.get(classOrPackageName);
				if (level != null) return level;
				classOrPackageName = parentPackage(classOrPackageName);
			}
		}
		// no custom log level; return the global log level
		return currentLevel;
	}

	@Override
	public void setLevel(final int level) {
		currentLevel = level;
	}

	@Override
	public void setLevel(final String classOrPackageName, final int level) {
		classAndPackageLevels.put(classOrPackageName, level);
	}

	// -- Helper methods --

	/** Extracts the log level value from a string. */
	private int level(final String logProp) {
		if (logProp == null) return -1;

		// check whether it's a string label (e.g., "debug")
		final String log = logProp.trim().toLowerCase();
		if (log.startsWith("n")) return NONE;
		if (log.startsWith("e")) return ERROR;
		if (log.startsWith("w")) return WARN;
		if (log.startsWith("i")) return INFO;
		if (log.startsWith("d")) return DEBUG;
		if (log.startsWith("t")) return TRACE;

		// check whether it's a numerical value (e.g., 5)
		try {
			return Integer.parseInt(log);
		}
		catch (final NumberFormatException exc) {
			// nope!
		}
		return -1;
	}

	private String callingClass() {
		final String thisClass = AbstractLogService.class.getName();
		for (final StackTraceElement element : new Exception().getStackTrace()) {
			final String className = element.getClassName();
			// NB: Skip stack trace elements from other methods of this class.
			if (!thisClass.equals(className)) return className;
		}
		return null;
	}

	private String parentPackage(final String classOrPackageName) {
		final int dot = classOrPackageName.lastIndexOf(".");
		if (dot < 0) return null;
		return classOrPackageName.substring(0, dot);
	}

}
