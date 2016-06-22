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

package org.scijava.event;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.lang.ref.WeakReference;

import org.junit.Test;
import org.scijava.Context;
import org.scijava.service.AbstractService;
import org.scijava.service.event.ServicesLoadedEvent;

/**
 * Verifies that the SciJava event service works as expected.
 * 
 * @author Johannes Schindelin
 */
public class EventServiceTest {
	@Test
	public void testWeakEventHandlers() {
		// verify that the garbage collector collects weak references
		final WeakReference<MyEventHandler> reference =
				new WeakReference<>(new MyEventHandler());
		gc();
		assertNull(reference.get());

		// make a new context with an event service
		final Context context = new Context(EventService.class);
		final EventService eventService = context.getService(EventService.class);

		// register the custom event handler
		MyEventHandler handler = new MyEventHandler();
		eventService.subscribe(handler);

		// verify that the event handler is called even after garbage collecting
		gc();
		assertEquals(0, counter);
		eventService.publish(new MyEvent());
		assertEquals(1, counter);

		// verify that releasing the reference releases the event handler
		handler = null;
		gc();
		eventService.publish(new MyEvent());
		assertEquals(1, counter);
	}

	/**
	 * Tests that when a service has methods labeled with {@code @EventHandler}
	 * annotations, the {@link EventService} will be brought in as a dependency.
	 */
	@Test
	public void testEventHandlerDependencies() throws InterruptedException {
		final Context context = new Context(ServiceNeedingAnEventService.class);
		final EventService eventService = context.getService(EventService.class);
		final ServiceNeedingAnEventService snaeService =
			context.getService(ServiceNeedingAnEventService.class);
		assertNotNull(eventService);
		// NB: ServicesLoadedEvent is published asynchronously.
		synchronized (snaeService) {
			snaeService.wait(500);
		}
		assertTrue(snaeService.isContextCreated());
	}

	private static void gc() {
		System.gc();
		// for some reason, some systems need extra encouragement to collect their garbage
		System.gc();
	}

	private int counter = 0;

	private class MyEvent extends SciJavaEvent {
		public void inc() {
			counter++;
		}
	}

	public static class MyEventHandler {
		@EventHandler
		public void onEvent(final MyEvent e) {
			e.inc();
		}
	}

	public static class ServiceNeedingAnEventService extends AbstractService {

		private boolean contextCreated;

		public boolean isContextCreated() {
			return contextCreated;
		}

		@EventHandler
		public void onEvent(
			@SuppressWarnings("unused") final ServicesLoadedEvent evt)
		{
			contextCreated = true;
			synchronized (this) {
				notifyAll();
			}
		}
	}

}
