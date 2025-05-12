/*
 * #%L
 * SciJava Common shared library for SciJava software.
 * %%
 * Copyright (C) 2009 - 2025 SciJava developers.
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
 * @author Curtis Rueden
 * @author Matthias Arzt
 */
@IgnoreAsCallingClass
public abstract class AbstractLogService extends AbstractService implements
	LogService
{

	private int currentLevel = levelFromEnvironment();

	private final Map<String, Integer> classAndPackageLevels;

	private final Logger rootLogger;

	// -- constructor --

	public AbstractLogService() {
		this(System.getProperties());
	}

	public AbstractLogService(final Properties properties) {
		rootLogger = new RootLogger();
		// provide this constructor to enable unit tests
		final int level = LogLevel.value(properties.getProperty(
			LogService.LOG_LEVEL_PROPERTY));
		if (level >= 0) currentLevel = level;
		classAndPackageLevels = setupMapFromProperties(properties,
			LogService.LOG_LEVEL_PROPERTY + ":");
		initLogSourceLevels(properties);
	}

	// -- AbstractLogService methods --

	@Override
	public void setLevel(final int level) {
		currentLevel = level;
	}

	@Override
	public void setLevel(final String classOrPackageName, final int level) {
		classAndPackageLevels.put(classOrPackageName, level);
	}

	@Override
	public void setLevelForLogger(final String source, final int level) {
		rootLogger.getSource().subSource(source).setLogLevel(level);
	}

	abstract protected void messageLogged(LogMessage message);

	// -- Logger methods --

	@Override
	public void alwaysLog(final int level, final Object msg, final Throwable t) {
		rootLogger.alwaysLog(level, msg, t);
	}

	@Override
	public LogSource getSource() {
		return rootLogger.getSource();
	}

	@Override
	public int getLevel() {
		if (classAndPackageLevels.isEmpty()) return currentLevel;
		return getLevelForClass(CallingClassUtils.getCallingClassName(),
			currentLevel);
	}

	@Override
	public Logger subLogger(String name, int level) {
		return rootLogger.subLogger(name, level);
	}

	@Override
	public void addLogListener(final LogListener listener) {
		rootLogger.addLogListener(listener);
	}

	@Override
	public void removeLogListener(final LogListener listener) {
		rootLogger.removeLogListener(listener);
	}

	@Override
	public void notifyListeners(final LogMessage event) {
		rootLogger.notifyListeners(event);
	}

	// -- Deprecated --

	/** @deprecated Use {@link LogLevel#prefix(int)} instead. */
	@Deprecated
	protected String getPrefix(final int level) {
		return "[" + LogLevel.prefix(level) + "]";
	}

	// -- Helper methods --

	private void initLogSourceLevels(Properties properties) {
		Map<String, Integer> nameLevels = setupMapFromProperties(properties,
			LOG_LEVEL_BY_SOURCE_PROPERTY + ":");
		nameLevels.forEach(this::setLevelForLogger);
	}

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

	// -- Helper classes --

	@IgnoreAsCallingClass
	private class RootLogger extends DefaultLogger {

		public RootLogger() {
			super(AbstractLogService.this::messageLogged, LogSource.newRoot(),
				LogLevel.NONE);
		}

		@Override
		public int getLevel() {
			return AbstractLogService.this.getLevel();
		}
	}
}
