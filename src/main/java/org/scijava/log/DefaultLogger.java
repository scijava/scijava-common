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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Default implementation for {@link Logger}.
 *
 * @author Curtis Rueden
 */
public class DefaultLogger implements Logger {

	private final Map<String, Integer> classAndPackageLevels = new HashMap<>();

	private int currentLevel = levelFromEnvironment();

	private String name;

	/** List of listeners for logging events. */
	private ArrayList<LogListener> listeners;

	private LogListener[] cachedListeners;

	// -- Constructor --

	public DefaultLogger() {
		// check SciJava log level system properties for initial logging levels

		// global log level property
		final String logProp = System.getProperty(LogService.LOG_LEVEL_PROPERTY);
		final int level = LogLevel.value(logProp);
		if (level >= 0) setLevel(level);

		if (getLevel() == 0) {
			// use the default, which is INFO unless the DEBUG env. variable is set
			setLevel(levelFromEnvironment());
		}

		// populate custom class- and package-specific log level properties
		final String logLevelPrefix = LogService.LOG_LEVEL_PROPERTY + ":";
		final Properties props = System.getProperties();
		for (final Object propKey : props.keySet()) {
			if (!(propKey instanceof String)) continue;
			final String propName = (String) propKey;
			if (!propName.startsWith(logLevelPrefix)) continue;
			final String classOrPackageName = propName.substring(logLevelPrefix
				.length());
			setLevel(classOrPackageName, LogLevel.value(props.getProperty(propName)));
		}
	}

	// -- Logger methods --

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

	@Override
	public void alwaysLog(final int level, final Object msg, final Throwable t) {
		notifyListeners(level, msg, t);
	}

	@Override
	public void addLogListener(final LogListener l) {
		if (listeners == null) initListeners();
		synchronized (listeners) {
			listeners.add(l);
			cacheListeners();
		}
	}

	@Override
	public void removeLogListener(final LogListener l) {
		if (listeners == null) initListeners();
		synchronized (listeners) {
			listeners.remove(l);
			cacheListeners();
		}
	}

	@Override
	public void notifyListeners(final int level, final Object msg,
		final Throwable t)
	{
		if (listeners == null) initListeners();
		final LogListener[] toNotify = cachedListeners;
		for (final LogListener l : toNotify)
			l.messageLogged(level, msg, t);
	}

	// -- Named methods --

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(final String name) {
		this.name = name;
	}

	// -- Helper methods - lazy initialization --

	/** Initializes {@link #listeners} and related data structures. */
	private synchronized void initListeners() {
		if (listeners != null) return; // already initialized

		listeners = new ArrayList<>();
		cachedListeners = listeners.toArray(new LogListener[0]);
	}

	// -- Helper methods --

	private void cacheListeners() {
		cachedListeners = listeners.toArray(new LogListener[listeners.size()]);
	}

	private String callingClass() {
		final String thisClass = DefaultLogger.class.getName();
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
		return System.getenv("DEBUG") == null ? LogLevel.INFO : LogLevel.DEBUG;
	}
}
