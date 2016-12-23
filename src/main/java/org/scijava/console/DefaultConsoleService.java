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

package org.scijava.console;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.scijava.Context;
import org.scijava.console.OutputEvent.Source;
import org.scijava.log.LogService;
import org.scijava.plugin.AbstractHandlerService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.service.Service;
import org.scijava.thread.ThreadService;
import org.scijava.thread.ThreadService.ThreadContext;

/**
 * Default service for managing interaction with the console.
 *
 * @author Curtis Rueden
 */
@Plugin(type = Service.class)
public class DefaultConsoleService extends
	AbstractHandlerService<LinkedList<String>, ConsoleArgument> implements
	ConsoleService
{

	@Parameter
	private ThreadService threadService;

	@Parameter
	private LogService log;

	private MultiPrintStream sysout, syserr;
	private OutputStreamReporter out, err;

	/** List of listeners for {@code stdout} and {@code stderr} output. */
	private ArrayList<OutputListener> listeners;

	private OutputListener[] cachedListeners;

	// -- ConsoleService methods --

	@Override
	public void processArgs(final String... args) {
		log.debug("Received command line arguments:");
		final LinkedList<String> argList = new LinkedList<>();
		for (final String arg : args) {
			log.debug("\t" + arg);
			argList.add(arg);
		}

		final List<String> previousArgs = new ArrayList<>();

		while (!argList.isEmpty()) {
			final ConsoleArgument handler = getHandler(argList);
			if (handler == null) {
				// ignore invalid command line argument
				final String arg = argList.removeFirst();
				log.warn("Ignoring invalid argument: " + arg);
				continue;
			}

			// keep a copy of the argument list prior to handling
			previousArgs.clear();
			previousArgs.addAll(argList);

			// process the argument
			handler.handle(argList);

			// verify that the handler did something to the list;
			// this guards against bugs which would cause infinite loops
			if (sameElements(previousArgs, argList)) {
				// skip improperly handled argument
				final String arg = argList.removeFirst();
				log.warn("Plugin '" + handler.getClass().getName() +
					"' failed to handle argument: " + arg);
			}
		}
	}

	@Override
	public void addOutputListener(final OutputListener l) {
		if (listeners == null) initListeners();
		synchronized (listeners) {
			listeners.add(l);
			cacheListeners();
		}
	}

	@Override
	public void removeOutputListener(final OutputListener l) {
		if (listeners == null) initListeners();
		synchronized (listeners) {
			listeners.remove(l);
			cacheListeners();
		}
	}

	@Override
	public void notifyListeners(final OutputEvent event) {
		if (listeners == null) initListeners();
		final OutputListener[] toNotify = cachedListeners;
		for (final OutputListener l : toNotify)
			l.outputOccurred(event);
	}

	// -- Disposable methods --

	@Override
	public void dispose() {
		if (out != null) sysout.getParent().removeOutputStream(out);
		if (err != null) syserr.getParent().removeOutputStream(err);
	}

	// -- Helper methods - lazy initialization --

	/** Initializes {@link #listeners} and related data structures. */
	private synchronized void initListeners() {
		if (listeners != null) return; // already initialized

		sysout = multiPrintStream(System.out);
		if (System.out != sysout) System.setOut(sysout);
		out = new OutputStreamReporter(Source.STDOUT);
		sysout.getParent().addOutputStream(out);

		syserr = multiPrintStream(System.err);
		if (System.err != syserr) System.setErr(syserr);
		err = new OutputStreamReporter(Source.STDERR);
		syserr.getParent().addOutputStream(err);

		listeners = new ArrayList<>();
		cachedListeners = listeners.toArray(new OutputListener[0]);
	}

	// -- Helper methods --

	private void cacheListeners() {
		cachedListeners = listeners.toArray(new OutputListener[listeners.size()]);
	}

	private MultiPrintStream multiPrintStream(final PrintStream ps) {
		if (ps instanceof MultiPrintStream) return (MultiPrintStream) ps;
		return new MultiPrintStream(ps);
	}

	/**
	 * Gets whether two lists have exactly the same elements in them.
	 * <p>
	 * We cannot use {@link List#equals(Object)} because want to check for
	 * identical references, not per-element object equality.
	 * </p>
	 */
	private boolean sameElements(final List<String> l1,
		final List<String> l2)
	{
		if (l1.size() != l2.size()) return false;
		for (int i = 0; i < l1.size(); i++) {
			if (l1.get(i) != l2.get(i)) return false;
		}
		return true;
	}

	// -- Helper classes --

	/**
	 * An output stream that publishes its output to the {@link OutputListener}s
	 * of its associated {@link ConsoleService}.
	 */
	private class OutputStreamReporter extends OutputStream {

		/** Source of the output stream; i.e., {@code stdout} or {@code stderr}. */
		private final Source source;

		public OutputStreamReporter(final Source source) {
			this.source = source;
		}

		// -- OutputStream methods --

		@Override
		public void write(final int b) {
			final ThreadContext relevance = getRelevance();
			if (relevance == ThreadContext.OTHER) return; // different context
			publish(relevance, "" + b);
		}

		@Override
		public void write(final byte[] buf, final int off, final int len) {
			final ThreadContext relevance = getRelevance();
			if (relevance == ThreadContext.OTHER) return; // different context
			publish(relevance, new String(buf, off, len));
		}

		// -- Helper methods --

		private ThreadContext getRelevance() {
			return threadService.getThreadContext(Thread.currentThread());
		}

		private void publish(final ThreadContext relevance, final String output) {
			final Context context = getContext();
			final boolean contextual = relevance == ThreadContext.SAME;
			final OutputEvent event =
				new OutputEvent(context, source, output, contextual);
			notifyListeners(event);
		}
	}

}
