/*-
 * #%L
 * SciJava Common shared library for SciJava software.
 * %%
 * Copyright (C) 2009 - 2026 SciJava developers.
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

package org.scijava.task;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.scijava.Context;
import org.scijava.log.LogLevel;
import org.scijava.log.LogMessage;
import org.scijava.log.Logger;

/**
 * Tests {@link TaskService}.
 * 
 * @author Curtis Rueden
 */
public class TaskServiceTest {

	private TaskService taskService;

	@Before
	public void setUp() {
		final Context ctx = new Context(TaskService.class);
		taskService = ctx.service(TaskService.class);
	}

	@After
	public void tearDown() {
		taskService.context().dispose();
	}

	@Test
	public void testTask() throws InterruptedException, ExecutionException {
		final int[] result = new int[1];
		final Task task = taskService.createTask("hello");
		task.run(() -> {
			task.setStatusMessage("Hello");
			task.setProgressMaximum(10);
			task.setProgressValue(5);
			result[0] = 100;
		});
		task.waitFor();
		assertEquals(100, result[0]);
	}

	@Test
	public void testLogger() {
		final Task task = taskService.createTask("chatty");
		final Logger logger = task.log();
		assertSame(logger, task.log());
		assertEquals("chatty", logger.getSource().name());

		final List<LogMessage> messages = new ArrayList<>();
		logger.addLogListener(messages::add);
		logger.debug("detail");
		logger.warn("careful");
		assertEquals(2, messages.size());
		assertEquals("detail", messages.get(0).text());
		assertEquals(LogLevel.DEBUG, messages.get(0).level());
		assertEquals(LogLevel.WARN, messages.get(1).level());
	}

	@Test
	public void testDefaultLoggerDiscards() {
		final Task task = new Task() {
			@Override public void run(final Runnable r) {}
			@Override public void waitFor() {}
			@Override public boolean isDone() { return false; }
			@Override public String getStatusMessage() { return null; }
			@Override public long getProgressValue() { return 0; }
			@Override public long getProgressMaximum() { return 0; }
			@Override public void setStatusMessage(final String status) {}
			@Override public void setProgressValue(final long step) {}
			@Override public void setProgressMaximum(final long max) {}
			@Override public boolean isCanceled() { return false; }
			@Override public void cancel(final String reason) {}
			@Override public String getCancelReason() { return null; }
			@Override public String getName() { return null; }
			@Override public void setName(final String name) {}
		};
		final Logger logger = task.log();
		final List<LogMessage> messages = new ArrayList<>();
		logger.addLogListener(messages::add);
		logger.error("ignored");
		assertTrue(messages.isEmpty());
	}
}
