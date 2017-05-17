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

import org.junit.Test;

import java.util.Properties;

import static org.junit.Assert.*;
import static org.scijava.log.LogLevel.*;

/**
 * Tests {@link LogLevelStrategy}
 * @author Matthias Arzt
 */
public class LogLevelStrategyTest {

	@Test
	public void testDefaultLevel() {
		assertEquals(INFO, new LogLevelStrategy().getLevel());
	}

	@Test
	public void testMainSystemProperty() {
		Properties properties = new Properties();
		properties.setProperty(LogService.LOG_LEVEL_PROPERTY, "error");
		int level = new LogLevelStrategy(properties).getLevel();
		assertEquals(ERROR, level);
	}

	@Test
	public void testSetLevel() {
		LogLevelStrategy log = new LogLevelStrategy();
		log.setLevel(LogLevel.ERROR);
		int level = log.getLevel();
		assertEquals(ERROR, level);
	}

	static class Dummy {
		public static int getLevel(LogLevelStrategy log) {
			return log.getLevel();
		}
	}

	@Test
	public void testClassLogLevel() {
		final LogLevelStrategy log = new LogLevelStrategy();
		log.setLevel(Dummy.class.getName(), LogLevel.ERROR);
		int level = Dummy.getLevel(log);
		assertEquals(ERROR, level);
	}

	@Test
	public void testClassLogLevelViaProperties() {
		Properties properties = new Properties();
		properties.setProperty(LogService.LOG_LEVEL_PROPERTY + ":" + Dummy.class.getName(), LogLevel.prefix(LogLevel.ERROR));
		properties.setProperty(LogService.LOG_LEVEL_PROPERTY + ":" + LogLevelStrategyTest.class.getName(), LogLevel.prefix(LogLevel.ERROR));
		final LogLevelStrategy log = new LogLevelStrategy(properties);
		int level = Dummy.getLevel(log);
		assertEquals(ERROR, level);
	}

	@Test
	public void testPackageLogLevel() {
		final LogLevelStrategy log = new LogLevelStrategy();
		log.setLevel("org.scijava.log", LogLevel.TRACE);
		log.setLevel("xyz.foo.bar", LogLevel.ERROR);
		int level = log.getLevel();
		assertEquals(TRACE, level);
	}
}