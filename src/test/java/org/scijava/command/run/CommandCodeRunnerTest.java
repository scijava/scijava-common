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

package org.scijava.command.run;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.scijava.Context;
import org.scijava.ItemIO;
import org.scijava.command.Command;
import org.scijava.command.CommandService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 * Tests {@link CommandCodeRunner}.
 * 
 * @author Curtis Rueden
 */
public class CommandCodeRunnerTest {

	private Context context;
	private CommandCodeRunner runner;

	@Before
	public void setUp() {
		context = new Context(CommandService.class);
		runner = new CommandCodeRunner();
		context.inject(runner);
	}

	@After
	public void tearDown() {
		context.dispose();
	}

	@Test
	public void testRunList() throws InvocationTargetException {
		final StringBuilder buffer = new StringBuilder();

		runner.run(OpenSesame.class, "buffer", buffer);
		assertEquals("Alakazam", buffer.toString());

		runner.run(OpenSesame.class, "buffer", buffer, "magicWord", "Shazam");
		assertEquals("AlakazamShazam", buffer.toString());

		runner.run("Open Sesame", "buffer", buffer, "magicWord", "Marzipan");
		assertEquals("AlakazamShazamMarzipan", buffer.toString());
	}

	@Test
	public void testRunMap() throws InvocationTargetException {
		final StringBuilder buffer = new StringBuilder();

		final Map<String, Object> inputMap = new HashMap<>();
		inputMap.put("buffer", buffer);

		runner.run(OpenSesame.class, inputMap);
		assertEquals("Alakazam", buffer.toString());

		inputMap.put("magicWord", "Shazam");
		runner.run(OpenSesame.class, inputMap);
		assertEquals("AlakazamShazam", buffer.toString());

		inputMap.put("magicWord", "Marzipan");
		runner.run("Open Sesame", inputMap);
		assertEquals("AlakazamShazamMarzipan", buffer.toString());
	}

	@Test
	public void testSupports() {
		assertTrue(runner.supports(OpenSesame.class));
		assertTrue(runner.supports(OpenSesame.class.getName()));
		assertTrue(runner.supports("Open Sesame"));

		assertFalse(runner.supports(CommandCodeRunnerTest.class));
		assertFalse(runner.supports("Not an actual command"));
		assertFalse(runner.supports(0));
	}

	// -- Helper methods --

	@Plugin(type = Command.class, label = "Open Sesame")
	public static class OpenSesame implements Command {

		@Parameter(type = ItemIO.BOTH)
		private StringBuilder buffer;

		@Parameter(required = false, persist = false)
		private String magicWord;

		@Override
		public void run() {
			buffer.append(magicWord == null ? "Alakazam" : magicWord);
		}

	}

}
