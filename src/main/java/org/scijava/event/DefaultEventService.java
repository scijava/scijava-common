/*
 * #%L
 * SciJava Common shared library for SciJava software.
 * %%
 * Copyright (C) 2009 - 2013 Board of Regents of the University of
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

package org.scijava.event;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.WeakHashMap;

import org.bushe.swing.event.annotation.AbstractProxySubscriber;
import org.bushe.swing.event.annotation.BaseProxySubscriber;
import org.bushe.swing.event.annotation.ReferenceStrength;
import org.scijava.Priority;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.service.AbstractService;
import org.scijava.service.Service;
import org.scijava.thread.ThreadService;

/**
 * Default service for publishing and subscribing to SciJava events.
 * 
 * @author Curtis Rueden
 * @author Grant Harris
 */
@Plugin(type = Service.class, priority = DefaultEventService.PRIORITY)
public class DefaultEventService extends AbstractService implements
	EventService
{

	/**
	 * The default event service's priority.
	 * <p>
	 * It is paramount that the event service be created and initialized before
	 * other services. Any service created before the event service may not have
	 * its event handling methods registered correctly.
	 * </p>
	 * <p>
	 * Alternative event service implementations that wish to prioritize
	 * themselves above this one can still ensure preferential usage via
	 * {@code priority = DefaultEventService.PRIORITY + 1} or similar.
	 * </p>
	 */
	public static final double PRIORITY = 10 * Priority.VERY_HIGH_PRIORITY;

	@Parameter
	private LogService log;

	@Parameter
	private ThreadService threadService;

	private DefaultEventBus eventBus;

	// -- EventService methods --

	@Override
	public <E extends SciJavaEvent> void publish(final E e) {
		e.setContext(getContext());
		eventBus.publishNow(e);
	}

	@Override
	public <E extends SciJavaEvent> void publishLater(final E e) {
		e.setContext(getContext());
		eventBus.publishLater(e);
	}

	@Override
	public List<EventSubscriber<?>> subscribe(final Object o) {
		final List<EventSubscriber<?>> subscribers =
			new ArrayList<EventSubscriber<?>>();
		subscribeRecursively(subscribers, o.getClass(), o);
		return subscribers;
	}

	@Override
	public void unsubscribe(final Collection<EventSubscriber<?>> subscribers) {
		for (final EventSubscriber<?> subscriber : subscribers) {
			unsubscribe(subscriber);
		}
	}

	@Override
	public <E extends SciJavaEvent> List<EventSubscriber<E>> getSubscribers(
		final Class<E> c)
	{
		// HACK - It appears that EventBus API is incorrect, in that
		// EventBus#getSubscribers(Class<T>) returns a List<T> when it should
		// actually be a List<EventSubscriber<T>>. This method works around the
		// problem with casts.
		@SuppressWarnings("rawtypes")
		final List list = eventBus.getSubscribers(c);
		@SuppressWarnings("unchecked")
		final List<EventSubscriber<E>> typedList = list;
		return typedList;
	}

	// -- Service methods --

	@Override
	public void initialize() {
		eventBus = new DefaultEventBus(threadService, log);
		super.initialize();
	}

	// -- Disposable methods --

	@Override
	public void dispose() {
		eventBus.clearAllSubscribers();
	}

	// -- Helper methods --

	/**
	 * Recursively scans for @{@link EventHandler} annotated methods, and
	 * subscribes them to the event service.
	 */
	private void subscribeRecursively(
		final List<EventSubscriber<?>> subscribers, final Class<?> type,
		final Object o)
	{
		if (type == null || type == Object.class) return;
		for (final Method m : type.getDeclaredMethods()) {
			final EventHandler ann = m.getAnnotation(EventHandler.class);
			if (ann == null) continue; // not an event handler method

			final Class<? extends SciJavaEvent> eventClass = getEventClass(m);
			if (eventClass == null) {
				log.warn("Invalid EventHandler method: " + m);
				continue;
			}

			subscribers.add(subscribe(eventClass, o, m));
		}
		subscribeRecursively(subscribers, type.getSuperclass(), o);
	}

	private <E extends SciJavaEvent> void subscribe(final Class<E> c,
		final EventSubscriber<E> subscriber)
	{
		eventBus.subscribe(c, subscriber);
	}

	private <E extends SciJavaEvent> void unsubscribe(
		final EventSubscriber<E> subscriber)
	{
		unsubscribe(subscriber.getEventClass(), subscriber);
	}

	private <E extends SciJavaEvent> void unsubscribe(final Class<E> c,
		final EventSubscriber<E> subscriber)
	{
		eventBus.unsubscribe(c, subscriber);
	}

	private <E extends SciJavaEvent> EventSubscriber<E> subscribe(
		final Class<E> c, final Object o, final Method m)
	{
		final ProxySubscriber<E> subscriber = new ProxySubscriber<E>(c, o, m);
		subscribe(c, subscriber);
		return subscriber;
	}

	/** Gets the event class parameter of the given method. */
	private Class<? extends SciJavaEvent> getEventClass(final Method m) {
		final Class<?>[] c = m.getParameterTypes();
		if (c == null || c.length != 1) return null; // wrong number of args
		if (!SciJavaEvent.class.isAssignableFrom(c[0])) return null; // wrong class

		@SuppressWarnings("unchecked")
		final Class<? extends SciJavaEvent> eventClass =
			(Class<? extends SciJavaEvent>) c[0];
		return eventClass;
	}

	// -- Event handlers garbage collection preventer --

	private WeakHashMap<Object, List<ProxySubscriber<?>>> keepEm =
			new WeakHashMap<Object, List<ProxySubscriber<?>>>();

	/**
	 * Prevents {@link ProxySubscriber} instances from being garbage collected
	 * prematurely.
	 * <p>
	 * We instantiate a {@link ProxySubscriber} for each method with an
	 * {@link EventHandler} annotation. These instances are then passed to the
	 * EventBus. The way the instances are created ensures that the event handlers
	 * will be held only as weak references. But they are weak references to the
	 * {@link ProxySubscriber} instances rather than the object containing the
	 * {@link EventHandler}-annotated methods. Therefore, we have to make sure
	 * that there is a non-GC'able reference to each {@link ProxySubscriber} as
	 * long as there is a reference to the containing event handler object.
	 * </p>
	 * 
	 * @param o the object containing {@link EventHandler}-annotated methods
	 * @param subscriber a {@link ProxySubscriber} for a particular {@link EventHandler}
	 */
	private synchronized void keepIt(final Object o, final ProxySubscriber<?> subscriber) {
		List<ProxySubscriber<?>> list = keepEm.get(o);
		if (list == null) {
			list = new ArrayList<ProxySubscriber<?>>();
			keepEm.put(o, list);
		}
		list.add(subscriber);
	}

	// -- Helper classes --

	/**
	 * Helper class used by {@link #subscribe(Object)}.
	 * <p>
	 * Recapitulates some logic from {@link BaseProxySubscriber}, because that
	 * class implements {@link org.bushe.swing.event.EventSubscriber} as a raw
	 * type, which is incompatible with this class implementing SciJava's
	 * {@link EventSubscriber} as a typed interface; it becomes impossible to
	 * implement both {@code onEvent(Object)} and {@code onEvent(E)}.
	 * </p>
	 */
	private class ProxySubscriber<E extends SciJavaEvent> extends
		AbstractProxySubscriber implements EventSubscriber<E>
	{

		private final Class<E> c;

		public ProxySubscriber(final Class<E> c, final Object o, final Method m) {
			super(o, m, ReferenceStrength.WEAK, eventBus, false);
			keepIt(o, this);
			this.c = c;

			// allow calling of non-public methods
			m.setAccessible(true);
		}

		/**
		 * Handles the event publication by pushing it to the real subscriber's
		 * subscription method.
		 * 
		 * @param event The event to publish.
		 */
		@Override
		public void onEvent(final E event) {
			try {
				final Object obj = getProxiedSubscriber();
				if (obj == null) return; // has been garbage collected
				getSubscriptionMethod().invoke(obj, event);
			}
			catch (final IllegalAccessException exc) {
				log.error("Exception during event handling:\n\t[Event] " +
					event.getClass().getName() + ":" + event + "\n\t[Subscriber] " +
					getProxiedSubscriber() + "\n\t[Method] " + getSubscriptionMethod(),
					exc);
			}
			catch (final InvocationTargetException exc) {
				log.error("Exception during event handling:\n\t[Event] " +
					event.getClass().getName() + event + "\n\t[Subscriber] " +
					getProxiedSubscriber() + "\n\t[Method] " + getSubscriptionMethod(),
					exc.getCause());
			}
		}

		@Override
		public Class<E> getEventClass() {
			return c;
		}

	}

}
