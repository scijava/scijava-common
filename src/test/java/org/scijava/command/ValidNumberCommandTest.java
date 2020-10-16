/*
 * #%L
 * SciJava Common shared library for SciJava software.
 * %%
 * Copyright (C) 2009 - 2020 SciJava developers.
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

package org.scijava.command;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.scijava.Context;
import org.scijava.ItemIO;
import org.scijava.ValidityProblem;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 * Test commands for verifying that invalid module parameters are dealt with
 * using proper error handling.
 * 
 * @author Curtis Rueden
 */
public class ValidNumberCommandTest {

	private CommandService commandService;

	@Before
	public void setUp() {
		Context ctx = new Context(CommandService.class);
		commandService = ctx.getService(CommandService.class);
	}

	@Test
	public void testValid() {
		final CommandInfo info = commandService.getCommand(ValidCommand.class);
		assertNotNull(info);
		assertTrue(info.isValid());

		final List<ValidityProblem> problems = info.getProblems();
		assertNotNull(problems);
		assertEquals(0, problems.size());
		
		final Number stepSize = info.getInput("x").getStepSize();
		final String format = info.getInput("x").getFormat();
		assertNotNull(stepSize);
		assertEquals(10, stepSize.intValue());
		assertEquals("0.000000", format);
	}


	// -- Helper classes --

	/** A perfectly valid command! */
	@Plugin(type = Command.class)
	public static class ValidCommand implements Command {

		@Parameter(stepSize = "10", format = "0.000000")
		private double x;

		@Parameter(type = ItemIO.OUTPUT)
		private String validOutput;

		@Override
		public void run() {
			validOutput = "ValidCommand: success!";
		}

	}


}
