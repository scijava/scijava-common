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

package org.scijava.command;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.ExecutionException;

import org.junit.Test;
import org.scijava.Cancelable;
import org.scijava.Context;
import org.scijava.ItemIO;
import org.scijava.Priority;
import org.scijava.log.LogService;
import org.scijava.module.Module;
import org.scijava.module.ModuleItem;
import org.scijava.module.process.AbstractPreprocessorPlugin;
import org.scijava.module.process.PreprocessorPlugin;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.service.Service;

/** Regression tests for {@link CommandModule}. */
public class CommandModuleTest {

	@Test
	public void testCancelable() throws InterruptedException, ExecutionException {
		final Context context = new Context(CommandService.class);
		final CommandService commandService = context.service(CommandService.class);
		final CommandModule ice = commandService.run(IceCommand.class, true).get();
		final Cancelable c = (Cancelable) ice.getDelegateObject();
		assertTrue(ice.isCanceled());
		assertTrue(c.isCanceled());
		assertEquals("Stop! Collaborate and listen!", ice.getCancelReason());
		assertEquals("Stop! Collaborate and listen!", c.getCancelReason());

		final CommandModule crow = commandService.run(CrowCommand.class, true).get();
		assertFalse(crow.isCanceled());
	}

	@Test
	public void testNotCancelable() throws InterruptedException,
		ExecutionException
	{
		final Context context = new Context(CommandService.class);
		final CommandService commandService = context.service(CommandService.class);
		final CommandModule fire = commandService.run(FireCommand.class, true).get();
		assertFalse(fire.getDelegateObject() instanceof Cancelable);
		assertTrue(fire.isCanceled());
		assertEquals("NO SINGING!", fire.getCancelReason());
	}

	@Test
	public void testDefaultValues() {
		final Context context = new Context(CommandService.class);
		final CommandService commandService = context.service(CommandService.class);
		final CommandInfo info = //
			commandService.getCommand(CommandWithDefaultValues.class);

		assertEquals(5, info.getInput("weekdays").getDefaultValue());

		final long defaultTime = (Long) info.getInput("time").getDefaultValue();
		final long timeDiff = System.currentTimeMillis() - defaultTime;
		assertTrue(timeDiff >= 0 && timeDiff < 50); // 50 ms should be enough ;-)

		final String defaultName = (String) info.getInput("name").getDefaultValue();
		assertEquals("John Jacob Jingleheimer Schmidt", defaultName);

		assertEquals(null, info.getInput("thing").getDefaultValue());
	}

	@Test
	public void testValidation() throws InterruptedException, ExecutionException {
		final Context context = new Context(CommandService.class);
		final CommandService commandService = context.service(CommandService.class);

		final CommandModule module = //
			commandService.run(CommandWithValidation.class, true).get();
		assertNotNull(module.getInput("stuff"));
		assertEquals("success", module.getOutput("result"));
	}

	@Test
	public void testCommandInjection() throws InterruptedException,
		ExecutionException
	{
		final Context context = new Context(CommandService.class);
		final CommandService commandService = context.service(CommandService.class);
		final LogService logService = context.service(LogService.class);

		final CommandModule module = //
			commandService.run(CommandWithService.class, false).get();
		assertSame(logService, module.getInput("log"));
		assertTrue((boolean) module.getOutput("success"));
	}

	// -- Helper classes --

	/** A command which implements {@link Cancelable}. */
	@Plugin(type = Command.class, initializer = "init")
	public static class IceCommand extends ContextCommand {

		public void init() {
			cancel("Stop! Collaborate and listen!");
		}

		@Override
		public void run() {
			throw new IllegalStateException("Unexpected");
		}
	}

	/** A command which does not implement {@link Cancelable}. */
	@Plugin(type = Command.class)
	public static class FireCommand implements Command {

		@Override
		public void run() {
			throw new IllegalStateException("Unexpected");
		}
	}

	/** A {@link Cancelable} command which is not auto-canceled. */
	@Plugin(type = Command.class)
	public static class CrowCommand extends ContextCommand {

		@Override
		public void run() {
			// everything's good
		}
	}

	@Plugin(type = PreprocessorPlugin.class)
	public static class CommandCanceler extends AbstractPreprocessorPlugin {

		@Override
		public void process(final Module module) {
			final Object command = module.getDelegateObject();
			if (command instanceof IceCommand || command instanceof FireCommand) {
				// NB: A Monty Python quote which is also a Game of Thrones reference.
				// That's right -- we did that.
				cancel("NO SINGING!");
			}
		}
	}

	/** A command which assigns default values to its parameters. */
	@Plugin(type = Command.class)
	public static class CommandWithDefaultValues extends ContextCommand {

		@Parameter
		private int weekdays = 5;

		@Parameter
		private long time = System.currentTimeMillis();

		@Parameter
		private String name = "John Jacob Jingleheimer Schmidt";

		@Parameter
		private Object thing;

		@Override
		public void run() {
			weekdays = 0;
			time = 0;
		}
	}

	/** A command which validates an input. */
	@Plugin(type = Command.class)
	public static class CommandWithValidation extends ContextCommand {

		@Parameter
		private LogService log;

		@Parameter(validater = "validateStuff")
		private Stuff stuff;

		@Parameter(type = ItemIO.OUTPUT)
		private String result = "default";

		@SuppressWarnings("unused")
		private void validateStuff() {
			final StringBuilder sb = new StringBuilder();
			if (log == null) sb.append("[null-log] ");
			if (stuff == null) sb.append("[null-stuff] ");
			result = sb.length() == 0 ? "success" : sb.toString();
		}

		@Override
		public void run() {
			if (!result.equals("success")) result += " failure";
		}
	}

	/**
	 * Preprocessor to inject {@link Stuff} instances very early. But (in theory)
	 * not as early as {@link Service} and {@link Context} parameters get
	 * populated.
	 */
	@Plugin(type = PreprocessorPlugin.class,
		priority = Priority.VERY_HIGH_PRIORITY)
	public static class StuffPreprocessor extends AbstractPreprocessorPlugin {

		@Override
		public void process(final Module module) {
			for (final ModuleItem<?> input : module.getInfo().inputs()) {
				if (Stuff.class.isAssignableFrom(input.getType())) {
					module.setInput(input.getName(), new Stuff());
					module.resolveInput(input.getName());
				}
			}
		}

	}

	/** Placeholder class, for type safety. */
	public static class Stuff {}

	/** A command which has a {@link Service} parameter. */
	@Plugin(type = Command.class)
	public static class CommandWithService implements Command {

		@Parameter
		private LogService log;

		@Parameter(type = ItemIO.OUTPUT)
		private boolean success;

		@Override
		public void run() {
			success = log != null;
		}
	}

}
