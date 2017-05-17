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

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * LogLevelStrategy provides a detailed control of logger's levels for the
 * AbstractLogService.
 * 
 * @author Matthias Arzt
 */
@IgnoreAsCallingClass
class LogLevelStrategy {

	private final Map<String, Integer> classAndPackageLevels;

	private int currentLevel = System.getenv("DEBUG") == null ? LogLevel.INFO
		: LogLevel.DEBUG;

	public LogLevelStrategy() {
		this(System.getProperties());
	}

	// To enable tests, make properties a parameter.
	LogLevelStrategy(Properties properties) {
		final int level = LogLevel.value(properties.getProperty(
			LogService.LOG_LEVEL_PROPERTY));
		if (level >= 0) setLevel(level);

		classAndPackageLevels = setupMapFromProperties(properties,
			LogService.LOG_LEVEL_PROPERTY + ":");
	}

	public int getLevel() {
		if (classAndPackageLevels.isEmpty()) return currentLevel;
		return getLevelForClass(callingClass(), currentLevel);
	}

	public void setLevel(final int level) {
		currentLevel = level;
	}

	public void setLevel(final String classOrPackageName,
		 final int level)
	{
		classAndPackageLevels.put(classOrPackageName, level);
	}

	private int getLevelForClass(String classOrPackageName, int defaultLevel) {
		// check sor a custom log level for calling class or its parent packages
		while (classOrPackageName != null) {
			final Integer level = classAndPackageLevels.get(classOrPackageName);
			if (level != null) return level;
			classOrPackageName = parentPackage(classOrPackageName);
		}
		return defaultLevel;
	}

	// -- Helper methods --

	private String callingClass() {
		return CallingClassUtils.getCallingClass().getName();
	}

	private String parentPackage(final String classOrPackageName) {
		final int dot = classOrPackageName.lastIndexOf(".");
		if (dot < 0) return null;
		return classOrPackageName.substring(0, dot);
	}

	private HashMap<String, Integer> setupMapFromProperties(Properties properties,
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
