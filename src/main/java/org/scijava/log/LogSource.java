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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringJoiner;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * Identifies where a {@link LogMessage} came from.
 *
 * @author Matthias Arzt
 */
public class LogSource {

	public static final String SEPARATOR = ":";

	private final LogSource parent;

	private final List<String> path;

	private final ConcurrentMap<String, LogSource> children =
		new ConcurrentSkipListMap<>();

	private String formatted = null;

	private Integer logLevel;

	private LogSource(LogSource parent, String name) {
		this.parent = parent;
		List<String> parentPath = parent.path();
		List<String> list = new ArrayList<>(parentPath.size() + 1);
		list.addAll(parentPath);
		list.add(name);
		this.path = Collections.unmodifiableList(list);
	}

	private LogSource() {
		this.parent = null;
		this.path = Collections.emptyList();
	}

	/** Returns the root log source. This LogSource represents the empty list. */
	public static LogSource newRoot() {
		return new LogSource();
	}

	/**
	 * Returns a log source with the given path.
	 *
	 * @param subPath Relative path to the source, divided by
	 *          {@link LogSource#SEPARATOR}.
	 */
	public LogSource subSource(final String subPath) {
		LogSource result = this;
		for (final String name : subPath.split(SEPARATOR))
			result = result.child(name);
		return result;
	}

	/** Returns the list of strings which is represented by this LogSource. */
	public List<String> path() {
		return path;
	}

	/** Returns the last entry in the list of strings. */
	public String name() {
		if (path.isEmpty()) return "";
		return path.get(path.size() - 1);
	}

	@Override
	public String toString() {
		if (formatted != null) return formatted;
		StringJoiner joiner = new StringJoiner(SEPARATOR);
		path.forEach(s -> joiner.add(s));
		formatted = joiner.toString();
		return formatted;
	}

	public boolean isRoot() {
		return parent == null;
	}

	/** Gets the parent of this source, or null if the source is a root. */
	public LogSource parent() {
		return parent;
	}

	public void setLogLevel(int logLevel) {
		this.logLevel = logLevel;
	}

	public boolean hasLogLevel() {
		return logLevel != null;
	}

	public int logLevel() {
		if (!hasLogLevel()) throw new IllegalStateException();
		return logLevel;
	}

	// -- Helper methods --

	private LogSource child(final String name) {
		if (name.isEmpty()) return this;
		LogSource child = children.get(name);
		if (child != null) return child;
		child = new LogSource(this, name);
		children.putIfAbsent(name, child);
		return children.get(name);
	}
}
