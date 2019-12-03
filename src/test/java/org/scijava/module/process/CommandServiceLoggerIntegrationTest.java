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

package org.scijava.module.process;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.ExecutionException;

import org.junit.Test;
import org.scijava.Context;
import org.scijava.command.Command;
import org.scijava.command.CommandInfo;
import org.scijava.command.CommandService;
import org.scijava.log.DefaultLogger;
import org.scijava.log.LogLevel;
import org.scijava.log.LogService;
import org.scijava.log.LogSource;
import org.scijava.log.Logger;
import org.scijava.log.TestLogListener;
import org.scijava.plugin.Parameter;

/**
 * Tests logger injection with {@link CommandService}
 *
 * @author Matthias Arzt
 */
public class CommandServiceLoggerIntegrationTest {

	private final Context context = new Context(CommandService.class);
	private final CommandService commandService = context.service(CommandService.class);
	private final LogService service = context.service(LogService.class);
	private static final String MESSAGE_TEXT = "foobar";

	/** Test logging, when no logger is explicitly given to {@link CommandService#run} */
	@Test
	public void testInjection() throws InterruptedException, ExecutionException {
		// setup
		final TestLogListener listener = new TestLogListener();
		service.addLogListener(listener);
		// process
		commandService.run(CommandWithLogger.class, true).get();
		// test
		assertTrue(listener.hasLogged(m -> MESSAGE_TEXT.equals(m.text())));
	}

	/** Tests redirection of a command's log output. */
	@Test
	public void testCustomLogger() throws ExecutionException, InterruptedException {
		// setup
		final TestLogListener listener = new TestLogListener();
		final LogSource customSource = LogSource.newRoot();
		final DefaultLogger customLogger = new DefaultLogger(listener, customSource, LogLevel.TRACE);
		// process
		commandService.run(CommandWithLogger.class, true, "log", customLogger)
			.get();
		// test
		assertTrue(listener.hasLogged(m -> m.source().equals(customSource)));
	}

	public static class CommandWithLogger implements Command {

		@Parameter
		public Logger log;

		@Override
		public void run() {
			log.info(MESSAGE_TEXT);
		}
	}
}
