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

package org.scijava.console;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiFunction;

import org.scijava.Context;
import org.scijava.console.OutputEvent.Source;
import org.scijava.log.LogLevel;
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

	private OutputStreamReporter out, err;

	/** List of listeners for {@code stdout} and {@code stderr} output. */
	private List<OutputListener> listeners;

	// -- ConsoleService methods --

	@Override
	public void initialize() {
		PrintStream logErr = logStream(Source.STDERR);
		PrintStream logOut = logStream(Source.STDOUT);
		log.setPrintStreams(level -> (level <= LogLevel.WARN) ? logErr : logOut);
	}

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
		listeners.add(l);
	}

	@Override
	public void removeOutputListener(final OutputListener l) {
		if (listeners == null) initListeners();
		listeners.remove(l);
	}

	@Override
	public void notifyListeners(final OutputEvent event) {
		if (listeners == null) initListeners();
		for (final OutputListener l : listeners)
			l.outputOccurred(event);
	}

	PrintStream logStream(Source source) {
		OutputStream a = getBypassingStream(source);
		OutputStream b = new OutputStreamReporter((relevance, text) -> {
			final Context context = getContext();
			final boolean contextual = true;
			final boolean containsLog = true;
			return new OutputEvent(context, source, text, contextual, containsLog);
		});
		return new PrintStream(new MultiOutputStream(a, b));
	}

	private OutputStream getBypassingStream(Source source) {
		switch (source) {
			case STDOUT:
				return ListenableSystemStreams.out().bypass();
			case STDERR:
				return ListenableSystemStreams.err().bypass();
		}
		throw new AssertionError();
	}

	// -- Disposable methods --

	@Override
	public void dispose() {
		if(out != null) ListenableSystemStreams.out().removeOutputStream(out);
		if(err != null) ListenableSystemStreams.err().removeOutputStream(err);
	}

	// -- Helper methods - lazy initialization --

	/** Initializes {@link #listeners} and related data structures. */
	private synchronized void initListeners() {
		if (listeners != null) return; // already initialized

		out = setupDefaultReporter(Source.STDOUT);
		err = setupDefaultReporter(Source.STDERR);
		ListenableSystemStreams.out().addOutputStream(out);
		ListenableSystemStreams.err().addOutputStream(err);

		listeners = new CopyOnWriteArrayList<>();
	}

	private OutputStreamReporter setupDefaultReporter(Source source) {
		return new OutputStreamReporter((relevance, output) -> {
			final Context context = getContext();
			final boolean contextual = relevance == ThreadContext.SAME;
			final boolean containsLog = false;
			return new OutputEvent(context, source, output, contextual, containsLog);
		});
	}

	// -- Helper methods --

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

		private final BiFunction<ThreadContext, String, OutputEvent> eventFactory;

		public OutputStreamReporter(
			BiFunction<ThreadContext, String, OutputEvent> eventFactory)
		{
			this.eventFactory = eventFactory;
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
			final OutputEvent event = eventFactory.apply(relevance, output);
			notifyListeners(event);
		}
	}
}
