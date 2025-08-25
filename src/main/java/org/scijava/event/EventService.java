/*
 * #%L
 * SciJava Common shared library for SciJava software.
 * %%
 * Copyright (C) 2009 - 2025 SciJava developers.
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

import java.util.Collection;
import java.util.List;

import org.scijava.service.SciJavaService;

/**
 * Interface for the event handling service.
 *
 * @author Curtis Rueden
 * @author Grant Harris
 */
public interface EventService extends SciJavaService {

	/**
	 * Publishes the given event immediately, reporting it to all subscribers.
	 * Does not return until all subscribers have handled the event.
	 * <p>
	 * Note that with {@link #publish}, in the case of multiple events published
	 * in a chain to multiple subscribers, the delivery order will resemble that
	 * of a stack. For example:
	 * </p>
	 * <ol>
	 * <li>{@link org.scijava.module.event.ModulesUpdatedEvent} is published with
	 * {@link #publish}.</li>
	 * <li>{@link org.scijava.menu.DefaultMenuService} receives the event and
	 * handles it, publishing {@link org.scijava.menu.event.MenusUpdatedEvent} in
	 * response.</li>
	 * <li>A third party that subscribes to both
	 * {@link org.scijava.module.event.ModulesUpdatedEvent} and
	 * {@link org.scijava.menu.event.MenusUpdatedEvent} will receive the latter
	 * before the former.</li>
	 * </ol>
	 * That said, the behavior of {@link #publish} depends on the thread from
	 * which it is called: if called from a thread identified as a dispatch thread
	 * by {@link org.scijava.thread.ThreadService#isDispatchThread()}, it will
	 * publish immediately; otherwise, it will be queued for publication on a
	 * dispatch thread, and block the calling thread until publication is
	 * complete. This means that a chain of events published with a mixture of
	 * {@link #publish} and {@link #publishLater} may result in event delivery in
	 * an unintuitive order.
	 */
	<E extends SciJavaEvent> void publish(E e);

	/**
	 * Queues the given event for publication, typically on a separate thread
	 * (called the "event dispatch thread"). This method returns immediately,
	 * before subscribers have fully received the event.
	 * <p>
	 * Note that with {@link #publishLater}, in the case of multiple events
	 * published in a chain to multiple subscribers, the delivery order will
	 * resemble that of a queue. For example:
	 * </p>
	 * <ol>
	 * <li>{@link org.scijava.module.event.ModulesUpdatedEvent} is published with
	 * {@link #publishLater}.</li>
	 * <li>{@link org.scijava.menu.DefaultMenuService} receives the event and
	 * handles it, publishing {@link org.scijava.menu.event.MenusUpdatedEvent} in
	 * response.</li>
	 * <li>A third party that subscribes to both
	 * {@link org.scijava.module.event.ModulesUpdatedEvent} and
	 * {@link org.scijava.menu.event.MenusUpdatedEvent} will receive the former
	 * first, since it was already queued by the time the latter was
	 * published.</li>
	 * </ol>
	 */
	<E extends SciJavaEvent> void publishLater(E e);

	/**
	 * Subscribes all of the given object's @{@link EventHandler} annotated
	 * methods.
	 * <p>
	 * This allows a single class to subscribe to multiple types of events by
	 * implementing multiple event handling methods and annotating each one with
	 * the {@link EventHandler} annotation.
	 * </p>
	 * <p>
	 * Note that it is <u>not</u> necessary to store a copy of the event
	 * subscribers (because the event service is expected to hold a weak mapping
	 * between the event handler object and the subscribers) <u>unless</u> the
	 * subscribers need to be unsubscribed explicitly.
	 * </p>
	 * <p>
	 * Most users will want to extend {@link org.scijava.AbstractContextual}, or
	 * call {@link org.scijava.Context#inject(Object)}, instead of subscribing to
	 * the event service explicitly.
	 * </p>
	 *
	 * @param o the event handler object containing the {@link EventHandler}
	 *          annotated methods
	 * @return The list of newly created {@link EventSubscriber}s, weakly
	 *         subscribed to the event service.
	 * @see org.scijava.AbstractContextual
	 * @see org.scijava.Context#inject(Object)
	 */
	List<EventSubscriber<?>> subscribe(Object o);

	/**
	 * Subscribes the given {@link EventSubscriber} to its associated event class.
	 * Its {@link EventSubscriber#onEvent} method will be called whenever an event
	 * of the matching type is published.
	 * <p>
	 * <strong>Important note:</strong> The event service does <em>not</em> keep a
	 * strong reference to the subscriber! If you use this method, you are also
	 * responsible for keeping a reference to the subscriber, or else it is likely
	 * to be garbage collected, and thus no longer respond to events as intended.
	 * One simple way to force a strong reference to exist is to add it to
	 * SciJava's {@link org.scijava.object.ObjectService} via
	 * {@link org.scijava.object.ObjectService#addObject}.
	 * </p>
	 *
	 * @param subscriber the event subscriber to register
	 */
	void subscribe(EventSubscriber<?> subscriber);

	/**
	 * Removes all the given subscribers; they will no longer be notified when
	 * events are published.
	 */
	void unsubscribe(Collection<EventSubscriber<?>> subscribers);

	/**
	 * Gets a list of all subscribers to the given event class (and subclasses
	 * thereof).
	 */
	<E extends SciJavaEvent> List<EventSubscriber<E>> getSubscribers(Class<E> c);

}
