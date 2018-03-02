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

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Default implementation of {@link Logger}.
 *
 * @author Matthias Arzt
 * @author Curtis Rueden
 */
@IgnoreAsCallingClass
public class DefaultLogger implements Logger, LogListener {

	private final LogListener destination;

	private final LogSource source;

	private final int level;

	private final List<LogListener> listeners = new CopyOnWriteArrayList<>();

	public DefaultLogger(final LogListener destination,
		final LogSource source, final int level)
	{
		this.destination = destination;
		this.source = source;
		this.level = level;
	}

	// -- Logger methods --

	@Override
	public LogSource getSource() {
		return source;
	}

	@Override
	public int getLevel() {
		return level;
	}

	@Override
	public void alwaysLog(final int level, final Object msg, final Throwable t) {
		messageLogged(new LogMessage(source, level, msg, t));
	}

	@Override
	public Logger subLogger(final String name, final int level) {
		LogSource source = getSource().subSource(name);
		int actualLevel = source.hasLogLevel() ? source.logLevel() : level;
		return new DefaultLogger(this, source, actualLevel);
	}

	@Override
	public void addLogListener(final LogListener listener) {
		listeners.add(listener);
	}

	@Override
	public void removeLogListener(final LogListener listener) {
		listeners.remove(listener);
	}

	@Override
	public void notifyListeners(final LogMessage message) {
		for (LogListener listener : listeners)
			listener.messageLogged(message);
	}

	// -- LogListener methods --

	@Override
	public void messageLogged(final LogMessage message) {
		notifyListeners(message);
		destination.messageLogged(message);
	}
}
