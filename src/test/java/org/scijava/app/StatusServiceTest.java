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

package org.scijava.app;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.scijava.AbstractContextual;
import org.scijava.Context;
import org.scijava.app.event.StatusEvent;
import org.scijava.event.EventHandler;
import org.scijava.plugin.Parameter;

/**
 * Tests {@link StatusService}.
 * 
 * @author Lee Kamentsky
 */
public class StatusServiceTest {

	private Context context;
	private StatusListener statusListener;
	private BlockingQueue<StatusEvent> queue;
	private StatusService ss;

	private class StatusListener extends AbstractContextual {

		@Parameter
		private StatusService statusService;

		@EventHandler
		private void eventHandler(final StatusEvent e) {
			try {
				queue.put(new StatusEvent(e.getProgressValue(), e.getProgressMaximum(),
					e.getStatusMessage(), e.isWarning()));
			}
			catch (final InterruptedException e1) {
				e1.printStackTrace();
				fail();
			}
		}
	}

	@Before
	public void setUp() {
		context = new Context();
		queue = new ArrayBlockingQueue<>(10);
		statusListener = new StatusListener();
		statusListener.setContext(context);
		ss = statusListener.statusService;
	}

	@After
	public void tearDown() {
		context.dispose();
	}

	@Test
	public void testShowProgress() throws InterruptedException {
		ss.showProgress(15, 45);
		final StatusEvent event = queue.poll(10, TimeUnit.SECONDS);
		assertEquals(event.getProgressValue(), 15);
		assertEquals(event.getProgressMaximum(), 45);
		assertFalse(event.isWarning());
	}

	@Test
	public void testShowStatusString() throws InterruptedException {
		final String text = "Hello, world";
		ss.showStatus(text);
		final StatusEvent event = queue.poll(10, TimeUnit.SECONDS);
		assertEquals(event.getStatusMessage(), text);
		assertFalse(event.isWarning());
	}

	@Test
	public void testShowStatusIntIntString() throws InterruptedException {
		final String text = "Working...";
		ss.showStatus(25, 55, text);
		final StatusEvent event = queue.poll(10, TimeUnit.SECONDS);
		assertEquals(event.getProgressValue(), 25);
		assertEquals(event.getProgressMaximum(), 55);
		assertEquals(event.getStatusMessage(), text);
		assertFalse(event.isWarning());
	}

	@Test
	public void testWarn() throws InterruptedException {
		final String text = "Totally hosed";
		ss.warn(text);
		final StatusEvent event = queue.poll(10, TimeUnit.SECONDS);
		assertEquals(event.getStatusMessage(), text);
		assertTrue(event.isWarning());
	}

	@Test
	public void testShowStatusIntIntStringBoolean() throws InterruptedException {
		final String text = "Working and hosed...";
		ss.showStatus(33, 44, text, true);
		final StatusEvent event = queue.poll(10, TimeUnit.SECONDS);
		assertEquals(event.getStatusMessage(), text);
		assertEquals(event.getProgressValue(), 33);
		assertEquals(event.getProgressMaximum(), 44);
		assertTrue(event.isWarning());
	}

	@Test
	public void testClearStatus() throws InterruptedException {
		ss.clearStatus();
		final StatusEvent event = queue.poll(10, TimeUnit.SECONDS);
		assertEquals(event.getStatusMessage(), "");
		assertFalse(event.isWarning());
	}

}
