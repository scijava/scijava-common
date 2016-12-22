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

package org.scijava.console;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.ExecutionException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.scijava.Context;
import org.scijava.Priority;
import org.scijava.console.OutputEvent.Source;
import org.scijava.plugin.Plugin;
import org.scijava.thread.ThreadService;

/**
 * Tests {@link ConsoleService}.
 *
 * @author Curtis Rueden
 */
public class ConsoleServiceTest {

	private ConsoleService consoleService;

	@Before
	public void setUp() {
		consoleService = new Context().service(ConsoleService.class);
	}

	@After
	public void tearDown() {
		consoleService.context().dispose();
	}

	/** Tests {@link ConsoleService#processArgs(String...)}. */
	@Test
	public void testProcessArgs() {
		assertFalse(consoleService.getInstance(FooArgument.class).argsHandled);
		consoleService.processArgs("--foo", "--bar");
		assertTrue(consoleService.getInstance(FooArgument.class).argsHandled);
	}

	/**
	 * Tests that {@link ConsoleService#processArgs(String...)} does not result in
	 * an infinite loop when a buggy {@link ConsoleArgument} forgets to remove its
	 * handled argument from the list.
	 */
	@Test
	public void testInfiniteLoopAvoidance() {
		assertFalse(consoleService.getInstance(BrokenArgument.class).argsHandled);
		consoleService.processArgs("--broken");
		assertTrue(consoleService.getInstance(BrokenArgument.class).argsHandled);
	}

	/**
	 * Tests the {@link OutputListener}-related API:
	 * <ul>
	 * <li>{@link ConsoleService#addOutputListener(OutputListener)}</li>
	 * <li>{@link ConsoleService#removeOutputListener(OutputListener)}</li>
	 * <li>{@link ConsoleService#notifyListeners(OutputEvent)}</li>
	 * </ul>
	 */
	@Test
	public void testOutputListeners() throws InterruptedException,
		ExecutionException
	{
		final ThreadService threadService =
			consoleService.context().service(ThreadService.class);

		final String stdoutBefore = "hoc";
		final String stderrBefore = "us-";
		final String stdoutGlobal = "poc";
		final String stderrGlobal = "us-";
		final String stdoutLocal = "abra";
		final String stderrLocal = "-cad";
		final String stdoutAfter = "ave";
		final String stderrAfter = "rs-";

		final ArrayList<OutputEvent> events = new ArrayList<>();
		final OutputListener outputListener = new OutputTracker(events);

		final Runnable r = new Printer(stdoutLocal, stderrLocal);

		// This output should _not_ be announced!
		System.out.print(stdoutBefore);
		System.err.print(stderrBefore);

		consoleService.addOutputListener(outputListener);

		// This output _should_ be announced!
		System.out.print(stdoutGlobal);
		System.err.print(stderrGlobal);
		threadService.run(r).get();

		consoleService.removeOutputListener(outputListener);

		// This output should _not_ be announced!
		threadService.run(r).get();
		System.out.print(stdoutAfter);
		System.err.print(stderrAfter);

		assertEquals(4, events.size());

		assertOutputEvent(Source.STDOUT, stdoutGlobal, false, events.get(0));
		assertOutputEvent(Source.STDERR, stderrGlobal, false, events.get(1));
		assertOutputEvent(Source.STDOUT, stdoutLocal, true, events.get(2));
		assertOutputEvent(Source.STDERR, stderrLocal, true, events.get(3));
	}

	/** Tests multiple simultaneous {@link Context}s listening for output. */
	@Test
	public void testMultipleContextOutput() throws InterruptedException,
		ExecutionException
	{
		final Context c1 = consoleService.context();
		final Context c2 = new Context();

		final ConsoleService cs1 = consoleService;
		final ConsoleService cs2 = c2.service(ConsoleService.class);

		final ThreadService ts1 = c1.service(ThreadService.class);
		final ThreadService ts2 = c2.service(ThreadService.class);

		final ArrayList<OutputEvent> events1 = new ArrayList<>();
		cs1.addOutputListener(new OutputTracker(events1));
		final ArrayList<OutputEvent> events2 = new ArrayList<>();
		cs2.addOutputListener(new OutputTracker(events2));

		final String globalOut = "and";
		final String globalErr = "-zo";
		final String c1RunOut = "mbi";
		final String c1RunErr = "es-";
		final String c2RunOut = "sha";
		final String c2RunErr = "zam";
		final String c1InvokeOut = "-cl";
		final String c1InvokeErr = "eop";
		final String c2InvokeOut = "atr";
		final String c2InvokeErr = "a";

		System.out.print(globalOut);
		System.err.print(globalErr);

		ts1.run(new Printer(c1RunOut, c1RunErr)).get();
		ts2.run(new Printer(c2RunOut, c2RunErr)).get();

		ts1.run(new EDTPrinter(ts1, c1InvokeOut, c1InvokeErr)).get();
		ts2.run(new EDTPrinter(ts2, c2InvokeOut, c2InvokeErr)).get();

		c2.dispose();

		// NB: all printing is assumed to have completed. If there are test failures
		// at this point it is likely due to a thread not having completed before
		// the assert checks.

		assertEquals(6, events1.size());
		assertOutputEvent(Source.STDOUT, globalOut, false, events1.get(0));
		assertOutputEvent(Source.STDERR, globalErr, false, events1.get(1));
		assertOutputEvent(Source.STDOUT, c1RunOut, true, events1.get(2));
		assertOutputEvent(Source.STDERR, c1RunErr, true, events1.get(3));
		assertOutputEvent(Source.STDOUT, c1InvokeOut, true, events1.get(4));
		assertOutputEvent(Source.STDERR, c1InvokeErr, true, events1.get(5));

		assertEquals(6, events2.size());
		assertOutputEvent(Source.STDOUT, globalOut, false, events2.get(0));
		assertOutputEvent(Source.STDERR, globalErr, false, events2.get(1));
		assertOutputEvent(Source.STDOUT, c2RunOut, true, events2.get(2));
		assertOutputEvent(Source.STDERR, c2RunErr, true, events2.get(3));
		assertOutputEvent(Source.STDOUT, c2InvokeOut, true, events2.get(4));
		assertOutputEvent(Source.STDERR, c2InvokeErr, true, events2.get(5));
	}

	// -- Helper methods --

	private void assertOutputEvent(final Source source, final String output,
		final boolean contextual, final OutputEvent event)
	{
		assertEquals(source, event.getSource());
		assertEquals(output, event.getOutput());
		assertEquals(contextual, event.isContextual());
	}

	// -- Helper classes --

	@Plugin(type = ConsoleArgument.class, priority = Priority.HIGH_PRIORITY)
	public static class FooArgument extends AbstractConsoleArgument {

		public FooArgument() {
			super(1, "--foo");
		}

		private boolean argsHandled;

		@Override
		public void handle(final LinkedList<String> args) {
			assertNotNull(args);
			assertEquals(2, args.size());
			assertEquals("--foo", args.get(0));
			assertEquals("--bar", args.get(1));
			args.clear();
			argsHandled = true;
		}
	}

	@Plugin(type = ConsoleArgument.class)
	public static class BrokenArgument extends AbstractConsoleArgument {

		private boolean argsHandled;

		public BrokenArgument() {
			super(1, "--broken");
		}

		@Override
		public void handle(final LinkedList<String> args) {
			// NB: Does not shorten the list. This is an intentional "bug" which
			// would naively result in an infinite loop. We want to test that
			// the ConsoleService is at least minorly resilient to this problem.
			argsHandled = true;
		}
	}

	private static class OutputTracker implements OutputListener {

		private final Collection<OutputEvent> events;

		private OutputTracker(final Collection<OutputEvent> events) {
			this.events = events;
		}

		@Override
		public void outputOccurred(final OutputEvent event) {
			events.add(event);
		}

	}

	private static class Printer implements Runnable {

		private final String out;
		private final String err;

		private Printer(final String out, final String err) {
			this.out = out;
			this.err = err;
		}

		@Override
		public void run() {
			System.out.print(out);
			System.err.print(err);
		}

	}

	private static class EDTPrinter implements Runnable {

		private final ThreadService ts;
		private final Printer p;

		private EDTPrinter(final ThreadService ts, final String out,
			final String err)
		{
			this.ts = ts;
			p = new Printer(out, err);
		}

		@Override
		public void run() {
			try {
				ts.invoke(p);
			}
			catch (final InvocationTargetException exc) {
				throw new RuntimeException(exc);
			}
			catch (final InterruptedException exc) {
				throw new RuntimeException(exc);
			}
		}

	}

}
