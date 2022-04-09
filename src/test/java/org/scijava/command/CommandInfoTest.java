/*
 * #%L
 * SciJava Common shared library for SciJava software.
 * %%
 * Copyright (C) 2009 - 2022 SciJava developers.
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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Iterator;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.scijava.Context;
import org.scijava.command.CommandInfoTest.CommandWithEnumParam.Choice;
import org.scijava.log.LogService;
import org.scijava.module.ModuleItem;
import org.scijava.plugin.Parameter;

/**
 * Tests {@link CommandInfo}.
 * 
 * @author Curtis Rueden
 */
public class CommandInfoTest {

	private Context ctx;
	private CommandService commandService;

	@Before
	public void setUp() {
		ctx = new Context(CommandService.class);
		commandService = ctx.getService(CommandService.class);
	}

	@After
	public void tearDown() {
		ctx.dispose();
	}

	@Test
	public void testEnumParam() {
		final CommandInfo info = commandService.getCommand(
			CommandWithEnumParam.class);
		assertNotNull(info);

		final Iterator<ModuleItem<?>> iter = info.inputs().iterator();
		assertTrue(iter.hasNext());
		ModuleItem<?> freeform = iter.next();
		assertTrue(iter.hasNext());
		ModuleItem<?> constrained = iter.next();
		assertTrue(iter.hasNext());
		ModuleItem<?> choice = iter.next();
		assertFalse(iter.hasNext());

		assertSame(String.class, freeform.getType());
		assertNull(freeform.getChoices());

		assertSame(String.class, constrained.getType());
		assertEquals(Arrays.asList("foo", "bar", "fubar"), //
			constrained.getChoices());

		assertSame(Choice.class, choice.getType());
		assertEquals(Arrays.asList(Choice.YES, Choice.NO, Choice.MAYBE_SO), //
			choice.getChoices());
	}

	@Test
	public void testDuplicateServiceParameters() {
		CommandInfo commandInfo = new CommandInfo(ExtendedServiceCommand.class);
		assertTrue(commandInfo.isValid());
	}

	// -- Helper classes --

	/** A command with an enum parameter. */
	@org.scijava.plugin.Plugin(type = Command.class)
	public static class CommandWithEnumParam implements Command {

		public enum Choice {
				YES, NO, MAYBE_SO
		}

		@Parameter
		private String freeform;

		@Parameter(choices = {"foo", "bar", "fubar"})
		private String constrained;

		@Parameter
		private Choice choice;

		@Override
		public void run() {
			// NB: No implementation needed.
		}
	}

	private static class ServiceCommand implements Command {

		@Parameter
		private LogService logService;

		@Override
		public void run() {
			// do nothing
		}
	}

	private static class ExtendedServiceCommand extends ServiceCommand {

		@Parameter
		private LogService logService;

		@Override
		public void run() {
			// do nothing
		}
	}
}
