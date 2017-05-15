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
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CopyOnWriteArrayList;

import org.scijava.service.AbstractService;

/**
 * Base class for {@link LogService} implementations.
 *
 * @author Johannes Schindelin
 * @author Curtis Rueden
 */
@IgnoreAsCallingClass
public abstract class AbstractLogService extends AbstractService implements
	LogService
{

	private int currentLevel = levelFromEnvironment();

	private final Map<String, Integer> classAndPackageLevels;

	private final List<LogListener> listeners = new CopyOnWriteArrayList<>();

	// -- constructor --

	public AbstractLogService() {
		this(System.getProperties());
	}

	public AbstractLogService(final Properties properties) {
		// provide this constructor to enable unit tests
		final int level = LogLevel.value(properties.getProperty(
			LogService.LOG_LEVEL_PROPERTY));
		if (level >= 0) currentLevel = level;
		classAndPackageLevels = setupMapFromProperties(properties,
			LogService.LOG_LEVEL_PROPERTY + ":");
	}

	// -- AbstractLogService methods --

	protected void notifyListeners(LogMessage message) {
		for (LogListener listener : listeners)
			listener.messageLogged(message);
	}

	// -- Logger methods --

	@Override
	public int getLevel() {
		if (classAndPackageLevels.isEmpty()) return currentLevel;
		return getLevelForClass(CallingClassUtils.getCallingClass().getName(),
			currentLevel);
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
		notifyListeners(new LogMessage(level, msg, t));
	}

	@Override
	public void addListener(final LogListener listener) {
		listeners.add(listener);
	}

	@Override
	public void removeListener(final LogListener listener) {
		listeners.remove(listener);
	}

	// -- Deprecated --

	/** @deprecated Use {@link LogLevel#prefix(int)} instead. */
	@Deprecated
	protected String getPrefix(final int level) {
		return "[" + LogLevel.prefix(level) + "]";
	}

	// -- Helper methods --

	private int getLevelForClass(String classOrPackageName, int defaultLevel) {
		// check for a custom log level for calling class or its parent packages
		while (classOrPackageName != null) {
			final Integer level = classAndPackageLevels.get(classOrPackageName);
			if (level != null) return level;
			classOrPackageName = parentPackage(classOrPackageName);
		}
		return defaultLevel;
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

	private Map<String, Integer> setupMapFromProperties(Properties properties,
		String prefix)
	{
		final HashMap<String, Integer> map = new HashMap<>();
		for (final String propName : properties.stringPropertyNames())
			if (propName.startsWith(prefix)) {
				final String key = propName.substring(prefix.length());
				map.put(key, LogLevel.value(properties.getProperty(propName)));
			}
		return map;
	}
}
