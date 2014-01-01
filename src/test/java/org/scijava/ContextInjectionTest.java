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
 * 
 * The views and conclusions contained in the software and documentation are
 * those of the authors and should not be interpreted as representing official
 * policies, either expressed or implied, of any organization.
 * #L%
 */

package org.scijava;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.scijava.event.EventHandler;
import org.scijava.event.EventService;
import org.scijava.event.SciJavaEvent;
import org.scijava.plugin.Parameter;
import org.scijava.service.AbstractService;
import org.scijava.service.Service;

/**
 * Tests {@link Context} and {@link Service} injection via the
 * {@link Context#inject(Object)} and
 * {@link AbstractContextual#setContext(Context)} methods.
 * 
 * @author Curtis Rueden
 */
public class ContextInjectionTest {

	/**
	 * Tests that the {@link Context} and {@link Service} parameters are properly
	 * injected when calling {@link Contextual#setContext} on an
	 * {@link AbstractContextual}-based class.
	 */
	@Test
	public void testAbstractContextualSetContext() {
		final Context context = new Context(FooService.class);

		final NeedsFooContextual needsFoo = new NeedsFooContextual();
		assertNull(needsFoo.fooService);
		needsFoo.setContext(context);
		assertSame(context, needsFoo.getContext());
		assertSame(context.getService(FooService.class), needsFoo.fooService);
	}

	/**
	 * Tests that the {@link Context} and {@link Service} parameters are properly
	 * injected when calling {@link Context#inject(Object)} on an
	 * {@link AbstractContextual}-based class.
	 */
	@Test
	public void testAbstractContextualContextInject() {
		final Context context = new Context(FooService.class);

		final NeedsFooContextual needsFoo = new NeedsFooContextual();
		assertNull(needsFoo.fooService);
		context.inject(needsFoo);
		assertSame(context, needsFoo.getContext());
		assertSame(context.getService(FooService.class), needsFoo.fooService);
	}

	/**
	 * Tests that {@link Service} parameters are properly injected when calling
	 * {@link Context#inject(Object)} on a class that does <em>not</em> implement
	 * the {@link Contextual} interface.
	 */
	@Test
	public void testNonContextualServiceParameters() {
		final Context context = new Context(FooService.class);

		final NeedsFooPlain needsFoo = new NeedsFooPlain();
		assertNull(needsFoo.fooService);
		context.inject(needsFoo);
		assertSame(context.getService(FooService.class), needsFoo.fooService);
	}

	/**
	 * Tests that {@link Context} parameters are properly injected when calling
	 * {@link Context#inject(Object)} on a class that does <em>not</em> implement
	 * the {@link Contextual} interface.
	 * <p>
	 * Also verifies that calling {@link Context#inject(Object)} more than once
	 * fails with an {@link IllegalStateException} as advertised.
	 * </p>
	 */
	@Test
	public void testNonContextualContextParameters() {
		final Context context = new Context(true);

		final NeedsContext needsContext = new NeedsContext();
		assertNull(needsContext.context);
		context.inject(needsContext);
		assertSame(context, needsContext.context);

		// test that a second injection attempt fails
		try {
			context.inject(needsContext);
			fail("Expected IllegalStateException");
		}
		catch (final IllegalStateException exc) {
			final String expectedMessage =
				"Context already injected: " + needsContext.getClass().getName() +
					"#context";
			assertEquals(expectedMessage, exc.getMessage());
		}
	}

	/**
	 * Tests that event subscription works properly for objects which extend
	 * {@link AbstractContextual}.
	 */
	@Test
	public void testAbstractContextualEventSubscription() {
		final Context context = new Context(EventService.class);
		final EventService eventService = context.getService(EventService.class);

		final HasEventsContextual hasEvents = new HasEventsContextual();
		assertFalse(hasEvents.eventReceived);

		eventService.publish(new SciJavaEvent() {/**/});
		assertFalse(hasEvents.eventReceived);

		hasEvents.setContext(context);
		eventService.publish(new SciJavaEvent() {/**/});
		assertTrue(hasEvents.eventReceived);
	}

	/**
	 * Tests that event subscription works properly for objects which do not
	 * implement {@link Contextual}, when injected using
	 * {@link Context#inject(Object)}.
	 */
	@Test
	public void testNonContextualEventSubscription() {
		final Context context = new Context(EventService.class);
		final EventService eventService = context.getService(EventService.class);

		final HasEventsPlain hasEvents = new HasEventsPlain();
		assertFalse(hasEvents.eventReceived);

		eventService.publish(new SciJavaEvent() {/**/});
		assertFalse(hasEvents.eventReceived);

		context.inject(hasEvents);
		eventService.publish(new SciJavaEvent() {/**/});
		assertTrue(hasEvents.eventReceived);
	}

	// -- Helper classes --

	/** A simple service with no dependencies. */
	public static class FooService extends AbstractService {
		// NB: No implementation needed.
	}

	/**
	 * An object that needs a {@link FooService} and extends
	 * {@link AbstractContextual}.
	 */
	public static class NeedsFooContextual extends AbstractContextual {

		@Parameter
		private FooService fooService;

	}

	/**
	 * An object that needs a {@link FooService} but does not implement
	 * {@link Contextual}.
	 */
	public static class NeedsFooPlain {

		@Parameter
		private FooService fooService;

	}

	/**
	 * An object that needs a {@link Context} but does not implement
	 * {@link Contextual}.
	 */
	public static class NeedsContext {

		@Parameter
		private Context context;

	}

	/**
	 * An object that subscribes to {@link SciJavaEvent}s and extends
	 * {@link AbstractContextual}.
	 */
	public static class HasEventsContextual extends AbstractContextual {

		private boolean eventReceived;

		@EventHandler
		private void onEvent(@SuppressWarnings("unused") SciJavaEvent e) {
			eventReceived = true;
		}

	}

	/**
	 * An object that subscribes to {@link SciJavaEvent}s but does not implement
	 * {@link Contextual}.
	 */
	public static class HasEventsPlain {

		private boolean eventReceived;

		@EventHandler
		private void onEvent(@SuppressWarnings("unused") SciJavaEvent e) {
			eventReceived = true;
		}

	}

}
