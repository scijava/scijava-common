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

import org.scijava.AbstractContextual;

/**
 * Base class for all SciJava events.
 * 
 * @author Curtis Rueden
 */
public abstract class SciJavaEvent extends AbstractContextual {

	/** Whether the event has been handled already. */
	private boolean consumed;

	/** The thread which published this event. */
	private Thread callingThread;

	/** The stack trace of the calling thread when the event was published. */
	private StackTraceElement[] stackTrace;

	// -- SciJavaEvent methods --

	public boolean isConsumed() {
		return consumed;
	}

	public void setConsumed(final boolean consumed) {
		this.consumed = consumed;
	}

	public void consume() {
		setConsumed(true);
	}

	/** Gets the thread that published the event. */
	public Thread getCallingThread() {
		return callingThread;
	}

	/** Sets the thread that published the event. */
	public void setCallingThread(final Thread callingThread) {
		this.callingThread = callingThread;
		stackTrace = callingThread.getStackTrace();
	}

	/**
	 * Gets the stack trace of the calling thread when the event was published.
	 * This method is useful for debugging what triggered an event.
	 */
	public StackTraceElement[] getStackTrace() {
		return stackTrace;
	}

	// Object methods --

	@Override
	public String toString() {
		return "\n\tcontext = " + getContext() + "\n\tconsumed = " + consumed;
	}

}
