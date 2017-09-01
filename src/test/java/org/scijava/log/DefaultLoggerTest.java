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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests {@link DefaultLogger}
 *
 * @author Matthias Arzt
 */
public class DefaultLoggerTest {

	private Logger logger;
	private TestLogListener listener;

	@Before
	public void setup() {
		logger = new DefaultLogger(message -> {}, "", LogLevel.INFO);
		listener = new TestLogListener();
		logger.addListener(listener);
	}

	@Test
	public void test() {
		listener.clear();

		logger.error("Hello World!");

		assertTrue(listener.hasLogged(m -> m.text().equals("Hello World!")));
		assertTrue(listener.hasLogged(m -> m.level() == LogLevel.ERROR));
	}

	@Test
	public void testSubLogger() {
		listener.clear();
		Logger sub = logger.subLogger("sub");

		sub.error("Hello World!");

		assertTrue(listener.hasLogged(m -> m.text().equals("Hello World!")));
	}

	@Test
	public void testLogForwarding() {
		listener.clear();
		Logger sub = logger.subLogger("xyz");
		TestLogListener subListener = new TestLogListener();
		sub.addListener(subListener);

		sub.error("Hello World!");
		logger.error("Goodbye!");

		assertTrue(subListener.hasLogged(m -> m.text().equals("Hello World!")));
		assertFalse(subListener.hasLogged(m -> m.text().equals("Goodbye!")));
		assertTrue(listener.hasLogged(m -> m.text().equals("Hello World!")));
		assertTrue(listener.hasLogged(m -> m.text().equals("Goodbye!")));
	}
}
