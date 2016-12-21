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

package org.scijava;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.After;
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

	private Context context;

	@After
	public void tearDown() {
		context.dispose();
	}

	/** Tests {@link Context#isInjectable(Class)}. */
	public void testInjectable() {
		context = new Context(true);
		assertTrue(context.isInjectable(Context.class));
		assertTrue(context.isInjectable(FooContext.class));
		assertTrue(context.isInjectable(Service.class));
		assertTrue(context.isInjectable(FooService.class));
		assertFalse(context.isInjectable(String.class));
		assertFalse(context.isInjectable(Integer.class));
		assertFalse(context.isInjectable(int.class));
		assertFalse(context.isInjectable(void.class));
	}

	/**
	 * Tests that the {@link Context} and {@link Service} parameters are properly
	 * injected when calling {@link Contextual#setContext} on an
	 * {@link AbstractContextual}-based class.
	 */
	@Test
	public void testAbstractContextualSetContext() {
		context = new Context(FooService.class);

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
		context = new Context(FooService.class);

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
		context = new Context(FooService.class);

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
		context = new Context(true);

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
	 * Tests that subclasses of {@link Context} are injected properly in the
	 * relevant circumstances.
	 */
	@Test
	public void testContextSubclassInjection() {
		context = new Context(true);
		final FooContext foo = new FooContext();
		final BarContext bar = new BarContext();

		final ContextSubclassParameters cspPlain = new ContextSubclassParameters();
		context.inject(cspPlain);
		assertSame(context, cspPlain.c);
		assertNull(cspPlain.foo);
		assertNull(cspPlain.bar);

		final ContextSubclassParameters cspFoo = new ContextSubclassParameters();
		foo.inject(cspFoo);
		assertNull(cspFoo.o);
		assertSame(foo, cspFoo.c);
		assertSame(foo, cspFoo.foo);
		assertNull(cspFoo.bar);

		final ContextSubclassParameters cspBar = new ContextSubclassParameters();
		bar.inject(cspBar);
		assertNull(cspBar.o);
		assertSame(bar, cspBar.c);
		assertNull(cspBar.foo);
		assertSame(bar, cspBar.bar);
	}

	/**
	 * Tests that event subscription works properly for objects which extend
	 * {@link AbstractContextual}.
	 */
	@Test
	public void testAbstractContextualEventSubscription() {
		context = new Context(EventService.class);
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
		context = new Context(EventService.class);
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

	/** An object that only wants matching {@link Context} types injected. */
	public static class ContextSubclassParameters {

		@Parameter
		private Object o;

		@Parameter
		private Context c;

		@Parameter
		private FooContext foo;

		@Parameter
		private BarContext bar;

	}

	/** A simple {@link Context} subclass. */
	public static class FooContext extends Context {

		public FooContext() {
			super(true);
		}

	}

	/** Another simple {@link Context} subclass. */
	public static class BarContext extends Context {

		public BarContext() {
			super(true);
		}

	}

	/**
	 * An object that subscribes to {@link SciJavaEvent}s and extends
	 * {@link AbstractContextual}.
	 */
	public static class HasEventsContextual extends AbstractContextual {

		private boolean eventReceived;

		@EventHandler
		private void onEvent(@SuppressWarnings("unused") final SciJavaEvent e) {
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
		private void onEvent(@SuppressWarnings("unused") final SciJavaEvent e) {
			eventReceived = true;
		}

	}

}
