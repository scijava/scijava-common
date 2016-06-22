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

package org.scijava.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;

/**
 * A helper class to help with optimizing the performance of a list of
 * operations.
 * <p>
 * For example, when trying to figure out which
 * {@link org.scijava.service.Service} would be the best candidate to speed up
 * {@link org.scijava.Context} initialization, this helper comes in real handy:
 * it accumulates a list of operations with their duration and prints out a
 * sorted list when asked to.
 * </p>
 * <p>
 * Use this class as following:
 * </p>
 * <code>
 * private static Timing timing = new Timing();<br />
 * ...<br />
 * private void oneOperation() {<br />
 * &nbsp;final long t1 = System.nanoTime();<br />
 * &nbsp;...<br />
 * &nbsp;timing.add(System.nanoTime() - t1, "Operation #1");<br />
 * }<br />
 * ...<br />
 * private void atEnd() {<br />
 * &nbsp;...<br />
 * &nbsp;timing.report("Operations");<br />
 * }
 * </code>
 * 
 * @author Johannes Schindelin
 */
public class Timing {
	private long total = 0, start = System.nanoTime(), tick = start;
	private List<Entry<Long, String>> list = new ArrayList<>();

	public void reset() {
		tick = System.nanoTime();
	}

	public void addTiming(final Object message) {
		addTiming(System.nanoTime() - tick, message == null ? getCaller() : message);
		tick = System.nanoTime();
	}

	public void addTiming(final long duration, final Object message) {
		final long now = System.nanoTime();
		total += duration;
		list.add(new Entry<Long, String>() {

			@Override
			public Long getKey() {
				return Long.valueOf(duration);
			}

			@Override
			public String getValue() {
				return message.toString() + ": " + ((now - start - duration) / 1e6) + " - " + ((now - start) / 1e6);
			}

			@Override
			public String setValue(String value) {
				throw new UnsupportedOperationException();
			}

		});
	}

	public void report(final String description) {
		System.err.println(description == null ? getCaller() : description);
		Collections.sort(list, new Comparator<Entry<Long, String>>() {

			@Override
			public int compare(Entry<Long, String> o1,
					Entry<Long, String> o2) {
				return Double.compare(o1.getKey(), o2.getKey());
			}
		});
		for (final Entry<?,?> e: list) {
			System.err.printf("% 5.3f ms %s\n", ((Long)e.getKey()) / 1e6, e.getValue());
		}
		System.err.println("Total time: " + total + " = " + (total / 1e9) + " sec");

	}

	private static String getCaller() {
		final StackTraceElement[] trace =
			Thread.currentThread().getStackTrace();
		int i = 1;
		while (i + 1 < trace.length &&
				Timing.class.getName().equals(trace[i].getClassName())) {
			i++;
		}
		return i >= trace.length ? "?" : trace[i].getClassName() + "."
				+ trace[i].getMethodName() + "(" + trace[i].getFileName() + ":"
				+ trace[i].getLineNumber() + ")";
	}

	public static Timing start(boolean condition) {
		return condition ? new Timing() : null;
	}

	public static void tick(final Timing timing) {
		if (timing != null) timing.addTiming(null);
	}

	public static void tick(final Timing timing, final Object message) {
		if (timing != null) timing.addTiming(message);
	}

	public static void stop(final Timing timing) {
		if (timing == null) return;
		timing.addTiming(null);
		timing.report(null);
	}
}
