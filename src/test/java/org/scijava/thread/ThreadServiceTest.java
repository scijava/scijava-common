/*
 * #%L
 * SciJava Common shared library for SciJava software.
 * %%
 * Copyright (C) 2009 - 2014 Board of Regents of the University of
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

package org.scijava.thread;

import static org.junit.Assert.assertSame;

import org.junit.Test;
import org.scijava.Context;

/**
 * Tests the {@link ThreadService}.
 * 
 * @author Johannes Schindelin
 */
public class ThreadServiceTest {

	/**
	 * Tests {@link ThreadService#getParent(Thread)} when called after
	 * {@link ThreadService#invoke(Runnable)}.
	 */
	@Test
	public void testGetParent() throws Exception {
		final Context context = new Context(ThreadService.class);
		final ThreadService threadService = context.getService(ThreadService.class);
		final AskForParent ask = new AskForParent(threadService);
		threadService.invoke(ask);
		assertSame(Thread.currentThread(), ask.parent);
		context.dispose();
	}

	private static class AskForParent implements Runnable {
		private final ThreadService threadService;

		private Thread parent;

		public AskForParent(final ThreadService threadService) {
			this.threadService = threadService;
		}

		@Override
		public void run() {
			parent = threadService.getParent(null);
		}
	}

}
