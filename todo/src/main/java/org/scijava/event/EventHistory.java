/*
 * #%L
 * SciJava Common shared library for SciJava software.
 * %%
 * Copyright (C) 2009 - 2017 Board of Regents of the University of
 * Wisconsin-Madison, Broad Institute of MIT and Harvard, Max Planck
 * Institute of Molecular Cell Biology and Genetics, University of
 * Konstanz, and KNIME GmbH.
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


import java.util.Set;

import org.scijava.service.SciJavaService;

/**
 * Interface for service that keeps a history of SciJava events.
 * 
 * @author Curtis Rueden
 */
public interface EventHistory extends SciJavaService {

	/** Activates or deactivates event history tracking. */
	void setActive(boolean active);

	/** Gets whether event history tracking is currently active. */
	boolean isActive();

	/** Clears the recorded event history. */
	void clear();

	/**
	 * Gets the recorded event history as an HTML string.
	 * 
	 * @param filtered Set of event types to filter out from the history.
	 * @param highlighted Set of event types to highlight in the history.
	 * @return An HTML string representing the recorded event history.
	 */
	String toHTML(Set<Class<? extends SciJavaEvent>> filtered,
		Set<Class<? extends SciJavaEvent>> highlighted);

	/**
	 * Adds an event history listener. This mechanism exists (rather than using
	 * the event bus) to avoid event feedback loops when reporting history
	 * changes.
	 */
	void addListener(EventHistoryListener l);

	/**
	 * Removes an event history listener. This mechanism exists (rather than using
	 * the event bus) to avoid event feedback loops when reporting history
	 * changes.
	 */
	void removeListener(EventHistoryListener l);

}
