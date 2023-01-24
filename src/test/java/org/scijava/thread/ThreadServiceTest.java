/*
 * #%L
 * SciJava Common shared library for SciJava software.
 * %%
 * Copyright (C) 2009 - 2023 SciJava developers.
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

package org.scijava.thread;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.scijava.Context;

/**
 * Tests the {@link ThreadService}.
 * 
 * @author Johannes Schindelin
 */
public class ThreadServiceTest {

	private Context context;
	private ThreadService threadService;

	@Before
	public void setUp() {
		context = new Context(ThreadService.class);
		threadService = context.getService(ThreadService.class);
	}

	@After
	public void tearDown() {
		context.dispose();
	}

	/** Tests {@link ThreadService#run(Callable)}. */
	@Test
	public void testRunCallable() throws InterruptedException, ExecutionException
	{
		final Thread result = threadService.run(new Callable<Thread>() {

			@Override
			public Thread call() {
				return Thread.currentThread();
			}
		}).get();
		assertNotSame(Thread.currentThread(), result);
	}

	/** Tests {@link ThreadService#run(Runnable)}. */
	@Test
	public void testRunRunnable() throws InterruptedException, ExecutionException
	{
		final Thread[] results = new Thread[1];
		threadService.run(new Runnable() {

			@Override
			public void run() {
				results[0] = Thread.currentThread();
			}
		}).get();
		assertNotSame(Thread.currentThread(), results[0]);
	}

	/**
	 * Tests {@link ThreadService#invoke(Runnable)} and
	 * {@link ThreadService#isDispatchThread()}.
	 */
	@Test
	public void testInvoke() throws InterruptedException,
		InvocationTargetException
	{
		final boolean[] results = new boolean[1];
		threadService.invoke(new Runnable() {

			@Override
			public void run() {
				results[0] = threadService.isDispatchThread();
			}
		});
		assertTrue(results[0]);
		assertFalse(threadService.isDispatchThread());
	}

	/**
	 * Tests {@link ThreadService#queue(Runnable)} and
	 * {@link ThreadService#isDispatchThread()}.
	 */
	@Test
	public void testQueue() throws InterruptedException {
		final Object sync = new Object();
		final boolean[] results = new boolean[1];
		synchronized (sync) {
			threadService.queue(new Runnable() {

				@Override
				public void run() {
					results[0] = threadService.isDispatchThread();
					synchronized (sync) {
						sync.notifyAll();
					}
				}
			});
			sync.wait();
		}
		assertTrue(results[0]);
		assertFalse(threadService.isDispatchThread());
	}

	/**
	 * Tests {@link ThreadService#getParent(Thread)} when called after
	 * {@link ThreadService#invoke(Runnable)}.
	 */
	@Test
	public void testGetParentInvoke() throws Exception {
		final AskForParentR ask = new AskForParentR(threadService);
		threadService.invoke(ask);
		assertSame(Thread.currentThread(), ask.parent);
	}

	/**
	 * Tests {@link ThreadService#getParent(Thread)} when called after
	 * {@link ThreadService#run(Callable)}.
	 */
	@Test
	public void testGetParentRunCallable() throws Exception {
		final AskForParentC ask = new AskForParentC(threadService);
		final Thread parent = threadService.run(ask).get();
		assertSame(Thread.currentThread(), parent);
	}

	/**
	 * Tests {@link ThreadService#getParent(Thread)} when called after
	 * {@link ThreadService#run(Runnable)}.
	 */
	@Test
	public void testGetParentRunRunnable() throws Exception {
		final AskForParentR ask = new AskForParentR(threadService);
		threadService.run(ask).get();
		assertSame(Thread.currentThread(), ask.parent);
	}

	private static class AskForParentR implements Runnable {

		private final ThreadService threadService;

		private Thread parent;

		public AskForParentR(final ThreadService threadService) {
			this.threadService = threadService;
		}

		@Override
		public void run() {
			parent = threadService.getParent(null);
		}
	}

	private static class AskForParentC implements Callable<Thread> {

		private final ThreadService threadService;

		public AskForParentC(final ThreadService threadService) {
			this.threadService = threadService;
		}

		@Override
		public Thread call() {
			return threadService.getParent(null);
		}
	}

}
