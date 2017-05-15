/*
 * #%L
 * SciJava Common shared library for SciJava software.
 * %%
 * Copyright (C) 2009 - 2017 Board of Regents of the University of
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

package org.scijava.log;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;

/**
 * LogMessage represents a log message.
 *
 * @author Matthias Arzt
 */
public class LogMessage {

	private final int level;
	private final String message;
	private final Throwable throwable;
	private final Date time;

	private Collection<Object> attachments;

	public LogMessage(int level, Object message,
		Throwable throwable)
	{
		this.attachments = null;
		this.level = level;
		this.message = message == null ? null : message.toString();
		this.throwable = throwable;
		this.time = new Date();
	}

	public LogMessage(int level, Object msg) {
		this(level, msg, null);
	}

	/**
	 * Log level of the message.
	 * 
	 * @see LogLevel
	 */
	public int level() {
		return level;
	}

	/** The content of this log message. */
	public String text() {
		return message;
	}

	/** Exception associated with the log message. */
	public Throwable throwable() {
		return throwable;
	}

	/** Time of the creation of the log message. */
	public Date time() {
		return time;
	}

	/**
	 * Collection of objects that have been attached to this message with
	 * {@link #attach(Object)}.
	 */
	public Collection<Object> attachments() {
		return Collections.unmodifiableCollection(attachments);
	}

	/**
	 * Attach object to this log message. This can be used to attach additional
	 * information to the log message.
	 */
	public void attach(Object value) {
		if (attachments == null) attachments = new LinkedList<>();
		attachments.add(value);
	}

	// -- Object methods --

	@Override
	public String toString() {
		return "LogMessage {" + level + ", " + message + ", " + throwable + "}";
	}

}
