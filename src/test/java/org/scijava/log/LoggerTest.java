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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.scijava.log.LogLevel.DEBUG;
import static org.scijava.log.LogLevel.ERROR;
import static org.scijava.log.LogLevel.INFO;
import static org.scijava.log.LogLevel.WARN;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

/**
 * Tests {@link Logger}.
 * 
 * @author Curtis
 */
public class LoggerTest {

	@Test
	public void testLogListeners() {
		final Logger log = new StderrLogService().channel("test");
		final List<Object[]> results = new ArrayList<>();
		final LogListener l = new LogListener() {

			@Override
			public void messageLogged(final int level, final Object msg,
				final Throwable t)
			{
				results.add(new Object[] { level, msg, t });
			}
		};
		log.addLogListener(l);

		// test that listeners receive events up to the current level (INFO)
		log.debug("donkey", new IllegalArgumentException()); // ignored
		log.error("egrit", new IllegalAccessException());
		log.info("i");
		log.trace("turkey", new RuntimeException()); // ignored
		log.warn("wyvern", new IllegalStateException());
		assertEquals(3, results.size());
		assertLogged(ERROR, "egrit", IllegalAccessException.class, results.get(0));
		assertLogged(INFO, "i", null, results.get(1));
		assertLogged(WARN, "wyvern", IllegalStateException.class, results.get(2));

		// test that changing the level works
		log.setLevel(DEBUG);
		results.clear();
		log.debug("devilish");
		log.trace("terrifying");
		assertEquals(1, results.size());
		assertLogged(DEBUG, "devilish", null, results.get(0));

		// test that listeners can be removed
		log.removeLogListener(l);
		results.clear();
		log.error("grout");
		assertTrue(results.isEmpty());
	}

	// -- Helper methods --

	private void assertLogged(final int level, final String msg,
		final Class<? extends Throwable> t, final Object[] result)
	{
		assertEquals(level, result[0]);
		assertEquals(msg, result[1]);
		if (t == null) assertNull(result[2]);
		else {
			assertNotNull(result[2]);
			assertEquals(t, result[2].getClass());
		}
	}
}
