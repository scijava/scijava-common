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

import java.util.ArrayList;
import java.util.Set;

import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.service.AbstractService;
import org.scijava.service.Service;

/**
 * Default service for keeping a history of SciJava events.
 * 
 * @author Curtis Rueden
 */
@Plugin(type = Service.class)
public class DefaultEventHistory extends AbstractService implements
	EventHistory
{

	@Parameter
	private EventService eventService;

	/** Event details that have been recorded. */
	private ArrayList<EventDetails> history = new ArrayList<>();

	private ArrayList<EventHistoryListener> listeners =
		new ArrayList<>();

	private boolean active;

	// -- EventHistory methods --

	@Override
	public void setActive(final boolean active) {
		this.active = active;
	}

	@Override
	public boolean isActive() {
		return active;
	}

	@Override
	public void clear() {
		history.clear();
	}

	@Override
	public String toHTML(final Set<Class<? extends SciJavaEvent>> filtered,
		final Set<Class<? extends SciJavaEvent>> highlighted)
	{
		final StringBuilder sb = new StringBuilder();
		for (final EventDetails details : history) {
			final Class<? extends SciJavaEvent> eventType = details.getEventType();
			if (filtered != null && filtered.contains(eventType)) {
				// skip filtered event type
				continue;
			}
			final boolean bold =
				highlighted != null && highlighted.contains(eventType);
			sb.append(details.toHTML(bold));
		}
		return sb.toString();
	}

	@Override
	public void addListener(final EventHistoryListener l) {
		synchronized (listeners) {
			listeners.add(l);
		}
		// someone is listening; start recording
		setActive(true);
	}

	@Override
	public void removeListener(final EventHistoryListener l) {
		synchronized (listeners) {
			listeners.remove(l);
		}
		if (listeners.isEmpty()) {
			// if no one is listening, stop recording
			setActive(false);
		}
	}

	// -- Event handlers --

	@EventHandler
	protected void onEvent(final SciJavaEvent event) {
		if (!active) return; // only record events while active
		final EventDetails details = new EventDetails(event);
		history.add(details);
		notifyListeners(details);
	}

	// -- Helper methods --

	private void notifyListeners(final EventDetails details) {
		synchronized (listeners) {
			for (final EventHistoryListener l : listeners) {
				l.eventOccurred(details);
			}
		}
	}

}
