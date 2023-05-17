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

package org.scijava.event;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;

import org.scijava.event.bushe.ThreadSafeEventService;
import org.scijava.log.LogService;
import org.scijava.service.Service;
import org.scijava.thread.ThreadService;

/**
 * An {@code org.scijava.event.bushe.EventService} implementation for SciJava.
 * <p>
 * It is called "DefaultEventBus" rather than "DefaultEventService" to avoid a
 * name clash with {@link DefaultEventService}, which is not an
 * {@code org.scijava.event.bushe.EventService} but rather a SciJava
 * {@link Service} implementation.
 * </p>
 * 
 * @author Curtis Rueden
 */
public class DefaultEventBus extends ThreadSafeEventService {

	private final ThreadService threadService;
	private final LogService log;

	public DefaultEventBus(final ThreadService threadService,
		final LogService log)
	{
		super(200L, null, null, null);
		this.threadService = threadService;
		this.log = log;
	}

	// -- DefaultEventBus methods --

	public void publishNow(final Object event) {
		if (event == null) {
			throw new IllegalArgumentException("Cannot publish null event.");
		}
		publishNow(event, null, null, getSubscribers(event.getClass()),
			getVetoSubscribers(event.getClass()), null);
	}

	public void publishNow(final Type genericType, final Object event) {
		if (genericType == null) {
			throw new IllegalArgumentException("genericType must not be null.");
		}
		if (event == null) {
			throw new IllegalArgumentException("Cannot publish null event.");
		}
		publishNow(event, null, null, getSubscribers(genericType), null, null);
	}

	public void publishNow(final String topicName, final Object eventObj) {
		publishNow(null, topicName, eventObj, getSubscribers(topicName),
			getVetoEventListeners(topicName), null);
	}

	public void publishLater(final Object event) {
		if (event == null) {
			throw new IllegalArgumentException("Cannot publish null event.");
		}
		publishLater(event, null, null, getSubscribers(event.getClass()),
			getVetoSubscribers(event.getClass()), null);
	}

	public void publishLater(final Type genericType, final Object event) {
		if (genericType == null) {
			throw new IllegalArgumentException("genericType must not be null.");
		}
		if (event == null) {
			throw new IllegalArgumentException("Cannot publish null event.");
		}
		publishLater(event, null, null, getSubscribers(genericType), null, null);
	}

	public void publishLater(final String topicName, final Object eventObj) {
		publishLater(null, topicName, eventObj, getSubscribers(topicName),
			getVetoEventListeners(topicName), null);
	}

	// -- org.scijava.event.bushe.EventService methods --

	@Override
	public void publish(final Object event) {
		publishNow(event);
	}

	@Override
	public void publish(final Type genericType, final Object event) {
		publishNow(genericType, event);
	}

	@Override
	public void publish(final String topicName, final Object eventObj) {
		publishNow(topicName, eventObj);
	}

	// -- Internal methods --

	@Override
	protected void publish(final Object event, final String topic,
		final Object eventObj,
		@SuppressWarnings("rawtypes") final List subscribers,
		@SuppressWarnings("rawtypes") final List vetoSubscribers,
		final StackTraceElement[] callingStack)
	{
		publishNow(event, topic, eventObj, subscribers, vetoSubscribers,
			callingStack);
	}

	// -- Helper methods --

	private void publishNow(final Object event, final String topic,
		final Object eventObj,
		@SuppressWarnings("rawtypes") final List subscribers,
		@SuppressWarnings("rawtypes") final List vetoSubscribers,
		final StackTraceElement[] callingStack)
	{
		if (subscribers == null || subscribers.isEmpty()) return;
		try {
			threadService.invoke(new Runnable() {

				@Override
				public void run() {
					log.debug("publish(" + event + "," + topic + "," + eventObj +
						"), called from non-EDT Thread:" + Arrays.toString(callingStack));
					DefaultEventBus.super.publish(event, topic, eventObj, subscribers,
						vetoSubscribers, callingStack);
				}
			});
		}
		catch (final InterruptedException exc) {
			log.error(exc);
		}
		catch (final InvocationTargetException exc) {
			log.error(exc);
		}
	}

	private void publishLater(final Object event, final String topic,
		final Object eventObj,
		@SuppressWarnings("rawtypes") final List subscribers,
		@SuppressWarnings("rawtypes") final List vetoSubscribers,
		final StackTraceElement[] callingStack)
	{
		if (subscribers == null || subscribers.isEmpty()) return;
		threadService.run(new Runnable() {

			@Override
			public void run() {
				log.debug("publish(" + event + "," + topic + "," + eventObj +
					"), called from non-EDT Thread:" + Arrays.toString(callingStack));
				DefaultEventBus.super.publish(event, topic, eventObj, subscribers,
					vetoSubscribers, callingStack);
			}
		});
	}

}
