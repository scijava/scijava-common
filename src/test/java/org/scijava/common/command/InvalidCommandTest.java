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

package org.scijava.common.command;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.scijava.common.Context;
import org.scijava.common.ItemIO;
import org.scijava.common.ValidityProblem;
import org.scijava.common.command.Command;
import org.scijava.common.command.CommandInfo;
import org.scijava.common.command.CommandService;
import org.scijava.common.plugin.Parameter;
import org.scijava.common.plugin.Plugin;

/**
 * Test commands for verifying that invalid module parameters are dealt with
 * using proper error handling.
 * 
 * @author Curtis Rueden
 */
public class InvalidCommandTest {

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
		assertNotNull(stepSize);
		assertEquals(10, stepSize.intValue());
	}

	@Test
	public void testInvalid() {
		final CommandInfo info = commandService.getCommand(InvalidCommand.class);
		assertNotNull(info);
		assertFalse(info.isValid());

		final List<ValidityProblem> problems = info.getProblems();
		assertNotNull(problems);
		assertEquals(3, problems.size());

		final String p0 = problems.get(0).getMessage();
		assertEquals("Delegate class is abstract", p0);

		final String p1 = problems.get(1).getMessage();
		assertEquals("Invalid duplicate parameter: private int "
			+ "org.scijava.command.InvalidCommandTest$InvalidCommand.x", p1);

		final String p2 = problems.get(2).getMessage();
		assertEquals("Invalid final parameter: private final float "
			+ "org.scijava.command.InvalidCommandTest$InvalidCommand.y", p2);
	}

	// -- Helper classes --

	/** A perfectly valid command! */
	@Plugin(type = Command.class)
	public static class ValidCommand implements Command {

		@Parameter(stepSize = "10")
		private double x;

		@Parameter(type = ItemIO.OUTPUT)
		private String validOutput;

		@Override
		public void run() {
			validOutput = "ValidCommand: success!";
		}

	}

	/** A very much invalid command, for multiple reasons, explained below. */
	@Plugin(type = Command.class)
	public static abstract class InvalidCommand extends ValidCommand {

		/**
		 * This parameter is invalid because it shadows a private parameter of a
		 * superclass. Such parameters violate the principle of parameter names as
		 * unique keys.
		 */
		@Parameter
		private int x;

		/**
		 * This parameter is invalid because it is declared {@code final} without
		 * being {@link org.scijava.common.ItemVisibility#MESSAGE} visibility. Java does
		 * not allow such parameter values to be set via reflection.
		 */
		@Parameter
		private final float y = 0;

		@Parameter(type = ItemIO.OUTPUT)
		private String invalidOutput;

		@Override
		public void run() {
			invalidOutput = "InvalidCommand: FAILURE";
		}

	}

}
