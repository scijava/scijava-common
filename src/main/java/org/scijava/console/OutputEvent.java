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

import org.scijava.Context;
import org.scijava.event.EventService;
import org.scijava.event.SciJavaEvent;

/**
 * An event indicating output occurred on {@code stdout} or {@code stderr}.
 * <p>
 * NB: This event is published <em>on the calling thread</em> by
 * {@link ConsoleService#notifyListeners(OutputEvent)}, <em>not</em> on a
 * dedicated event dispatch thread by the {@link EventService}. This is done to
 * avoid the overhead of the event service's synchronized pub/sub
 * implementation, as well as to avoid potential infinite output loops caused by
 * debugging output surrounding event publication.
 * </p>
 *
 * @author Curtis Rueden
 */
public class OutputEvent extends SciJavaEvent {

	/** Possible output sources. */
	public enum Source {
		STDOUT, STDERR
	}

	/** The source of the output. */
	private final Source source;

	/** The output string. */
	private final String output;

	/**
	 * Whether the output was produced within this specific SciJava
	 * {@link Context}.
	 */
	private final boolean contextual;

	/** Whether the output is a printed log message. */
	private final boolean containsLog;

	/**
	 * Creates a new output event.
	 * 
	 * @param source The source of the output.
	 * @param output The output string.
	 * @param contextual Whether the output was produced within this specific
	 *          SciJava {@link Context}.
	 * @param containsLog Whether the output is contains a log message.
	 */
	public OutputEvent(final Context context, final Source source,
		final String output, final boolean contextual, boolean containsLog)
	{
		this.source = source;
		this.output = output;
		this.contextual = contextual;
		this.containsLog = containsLog;
		setContext(context);
		setCallingThread(Thread.currentThread());
	}

	/** Gets the source of the output. */
	public Source getSource() {
		return source;
	}

	/** Gets the output string. */
	public String getOutput() {
		return output;
	}

	/**
	 * Returns true if the output was produced outside of a specific SciJava
	 * {@link Context}.
	 */
	public boolean isContextual() {
		return contextual;
	}

	/** Returns true of the source of the output is {@code stdout}. */
	public boolean isStdout() {
		return source == Source.STDOUT;
	}

	/** Returns true of the source of the output is {@code stderr}. */
	public boolean isStderr() {
		return source == Source.STDERR;
	}

	/**
	 * Returns true if the source of the output is a streams returned
	 * {@link org.scijava.console.ConsoleService#logStream(org.scijava.console.OutputEvent.Source)}
	 */
	public boolean containsLog() {
		return containsLog;
	}

	// -- Object methods --

	@Override
	public String toString() {
		return super.toString() + "\n\tsource = " + source + "\n\toutput = " +
			output + "\n\tcontextual = " + contextual;
	}

}
