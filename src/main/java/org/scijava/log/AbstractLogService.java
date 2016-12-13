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

package org.scijava.log;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.scijava.service.AbstractService;

/**
 * Base class for {@link LogService} implementations.
 *
 * @author Johannes Schindelin
 */
public abstract class AbstractLogService extends AbstractService implements
	LogService
{

	private int currentLevel = levelFromEnvironment();

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
		final int level = LogLevel.value(logProp);
		if (level >= 0) setLevel(level);

		if (getLevel() == 0)
			setLevel(levelFromEnvironment());

		// populate custom class- and package-specific log level properties
		final String logLevelPrefix = LOG_LEVEL_PROPERTY + ":";
		final Properties props = System.getProperties();
		for (final Object propKey : props.keySet()) {
			if (!(propKey instanceof String)) continue;
			final String propName = (String) propKey;
			if (!propName.startsWith(logLevelPrefix)) continue;
			final String classOrPackageName =
				propName.substring(logLevelPrefix.length());
			setLevel(classOrPackageName, LogLevel.value(props.getProperty(propName)));
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
		final String prefix = LogLevel.prefix(level);
		log((prefix == null ? "" : prefix + " ") + msg);
	}

	// -- LogService methods --

	@Override
	public void debug(final Object msg) {
		log(LogLevel.DEBUG, msg, null);
	}

	@Override
	public void debug(final Throwable t) {
		log(LogLevel.DEBUG, null, t);
	}

	@Override
	public void debug(final Object msg, final Throwable t) {
		log(LogLevel.DEBUG, msg, t);
	}

	@Override
	public void error(final Object msg) {
		log(LogLevel.ERROR, msg, null);
	}

	@Override
	public void error(final Throwable t) {
		log(LogLevel.ERROR, null, t);
	}

	@Override
	public void error(final Object msg, final Throwable t) {
		log(LogLevel.ERROR, msg, t);
	}

	@Override
	public void info(final Object msg) {
		log(LogLevel.INFO, msg, null);
	}

	@Override
	public void info(final Throwable t) {
		log(LogLevel.INFO, null, t);
	}

	@Override
	public void info(final Object msg, final Throwable t) {
		log(LogLevel.INFO, msg, t);
	}

	@Override
	public void trace(final Object msg) {
		log(LogLevel.TRACE, msg, null);
	}

	@Override
	public void trace(final Throwable t) {
		log(LogLevel.TRACE, null, t);
	}

	@Override
	public void trace(final Object msg, final Throwable t) {
		log(LogLevel.TRACE, msg, t);
	}

	@Override
	public void warn(final Object msg) {
		log(LogLevel.WARN, msg, null);
	}

	@Override
	public void warn(final Throwable t) {
		log(LogLevel.WARN, null, t);
	}

	@Override
	public void warn(final Object msg, final Throwable t) {
		log(LogLevel.WARN, msg, t);
	}

	@Override
	public boolean isDebug() {
		return getLevel() >= LogLevel.DEBUG;
	}

	@Override
	public boolean isError() {
		return getLevel() >= LogLevel.ERROR;
	}

	@Override
	public boolean isInfo() {
		return getLevel() >= LogLevel.INFO;
	}

	@Override
	public boolean isTrace() {
		return getLevel() >= LogLevel.TRACE;
	}

	@Override
	public boolean isWarn() {
		return getLevel() >= LogLevel.WARN;
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

	// -- Deprecated --

	/** @deprecated Use {@link LogLevel#prefix(int)} instead. */
	@Deprecated
	protected String getPrefix(final int level) {
		return "[" + LogLevel.prefix(level) + "]";
	}

	// -- Helper methods --

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

	private int levelFromEnvironment() {
		// use the default, which is INFO unless the DEBUG env. variable is set
		return System.getenv("DEBUG") == null ? LogLevel.INFO : LogLevel.DEBUG;
	}

}
