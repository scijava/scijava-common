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

package org.scijava.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;

/**
 * Useful methods for debugging programs.
 * 
 * @author Curtis Rueden
 */
public final class DebugUtils {

	private static final String NL = System.getProperty("line.separator");

	private DebugUtils() {
		// prevent instantiation of utility class
	}

	/** Extracts the given exception's corresponding stack trace to a string. */
	public static String getStackTrace(final Throwable t) {
		try {
			final ByteArrayOutputStream out = new ByteArrayOutputStream();
			t.printStackTrace(new PrintStream(out, false, "UTF-8"));
			return new String(out.toByteArray(), "UTF-8");
		}
		catch (final IOException exc) {
			return null;
		}
	}

	/**
	 * Provides a complete stack dump of all threads.
	 * <p>
	 * The output is similar to a subset of that given when Ctrl+\ (or Ctrl+Pause
	 * on Windows) is pressed from the console.
	 */
	public static String getStackDump() {
		final StringBuilder sb = new StringBuilder();

		final Map<Thread, StackTraceElement[]> stackTraces =
			Thread.getAllStackTraces();

		// sort list of threads by name
		final ArrayList<Thread> threads =
			new ArrayList<>(stackTraces.keySet());
		Collections.sort(threads, new Comparator<Thread>() {

			@Override
			public int compare(final Thread t1, final Thread t2) {
				return t1.getName().compareTo(t2.getName());
			}
		});

		for (final Thread t : threads) {
			dumpThread(t, stackTraces.get(t), sb);
		}

		return sb.toString();
	}

	/**
	 * This method uses reflection to scan the values of the given class's static
	 * fields, returning the first matching field's name.
	 */
	public static String getFieldName(final Class<?> c, final int value) {
		final Field[] fields = c.getDeclaredFields();
		for (int i = 0; i < fields.length; i++) {
			if (!Modifier.isStatic(fields[i].getModifiers())) continue;
			fields[i].setAccessible(true);
			try {
				if (fields[i].getInt(null) == value) return fields[i].getName();
			}
			catch (final IllegalAccessException exc) {
				// no action needed
			}
			catch (final IllegalArgumentException exc) {
				// no action needed
			}
		}
		return "" + value;
	}

	/**
	 * Get the class whose main method launched the application. The heuristic
	 * will fail if the main thread has terminated before this method is called.
	 */
	public static String getMainClassName() {
		final Map<Thread, StackTraceElement[]> traceMap =
			Thread.getAllStackTraces();
		for (final Thread thread : traceMap.keySet()) {
			if (!"main".equals(thread.getName())) continue;
			final StackTraceElement[] trace = traceMap.get(thread);
			if (trace == null || trace.length == 0) continue;
			final StackTraceElement element = trace[trace.length - 1];
			return element.getClassName();
		}
		return null;
	}

	// -- Helper methods --

	private static void dumpThread(final Thread t,
		final StackTraceElement[] trace, final StringBuilder sb)
	{
		threadInfo(t, sb);
		for (final StackTraceElement element : trace) {
			sb.append("\tat ");
			sb.append(element);
			sb.append(NL);
		}
		sb.append(NL);
	}

	private static void threadInfo(final Thread t, final StringBuilder sb) {
		sb.append("\"");
		sb.append(t.getName());
		sb.append("\"");
		if (!t.isAlive()) sb.append(" DEAD");
		if (t.isInterrupted()) sb.append(" INTERRUPTED");
		if (t.isDaemon()) sb.append(" daemon");
		sb.append(" prio=");
		sb.append(t.getPriority());
		sb.append(" id=");
		sb.append(t.getId());
		sb.append(" group=");
		sb.append(t.getThreadGroup().getName());
		sb.append(NL);
		sb.append("   java.lang.Thread.State: ");
		sb.append(t.getState());
		sb.append(NL);
	}

}
