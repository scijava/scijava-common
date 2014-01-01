/*
 * #%L
 * SciJava Common shared library for SciJava software.
 * %%
 * Copyright (C) 2009 - 2014 Board of Regents of the University of
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
 * 
 * The views and conclusions contained in the software and documentation are
 * those of the authors and should not be interpreted as representing official
 * policies, either expressed or implied, of any organization.
 * #L%
 */

package org.scijava.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;

/**
 * This class takes an {@link InputStream} and either accumulates the read bytes
 * in a {@link String} or outputs to a {@link PrintStream}.
 * <p>
 * Its intended use is to catch the output and error streams of {@link Process}
 * instances.
 * </p>
 * 
 * @author Johannes Schindelin
 */
public class ReadInto extends Thread {

	protected BufferedReader reader;
	protected PrintStream out;
	protected StringBuilder buffer = new StringBuilder();
	protected boolean done, closeOnEOF;

	/**
	 * Construct a ReadInto thread and start it right away.
	 * 
	 * @param in the stream to read
	 * @param out the stream to print to; if it is null, the {@link #toString()}
	 *          method will have the output instead
	 */
	public ReadInto(final InputStream in, final PrintStream out) {
		this(in, out, false);
	}

	/**
	 * Construct a ReadInto thread and start it right away.
	 * 
	 * @param in the stream to read
	 * @param out the stream to print to; if it is null, the {@link #toString()}
	 *          method will have the output instead
	 */
	public ReadInto(final InputStream in, final PrintStream out, final boolean closeOnEOF) {
		reader = new BufferedReader(new InputStreamReader(in));
		this.out = out;
		this.closeOnEOF = closeOnEOF;
		if (out == null && closeOnEOF) throw new IllegalArgumentException("Cannot close null output");
		start();
	}

	/**
	 * The main method.
	 * <p>
	 * It runs until interrupted, or until the {@link InputStream} ends, whichever
	 * comes first.
	 * </p>
	 */
	@Override
	public void run() {
		try {
			for (;;) {
				final String line = reader.readLine();
				if (line == null) break;
				if (out != null) out.println(line);
				else buffer.append(line).append("\n");
				if (done) break;
				Thread.sleep(0);
			}
		}
		catch (final InterruptedException e) { /* just stop */}
		catch (final IOException e) { /* just stop */}
		if (closeOnEOF) {
			out.close();
		}
		try {
			reader.close();
		}
		catch (final IOException e) { /* just stop */ }
	}

	@Override
	public void interrupt() {
		try {
			done();
		} catch (IOException e) { /* just stop */ }
		super.interrupt();
	}

	public void done() throws IOException {
		if (done) return;
		done = true;
		reader.close();
	}

	/**
	 * Return the output as a {@link String} unless a {@link PrintStream} was
	 * specified in the constructor.
	 */
	@Override
	public String toString() {
		if (out != null) return "ReadInto " + out;
		return buffer.toString();
	}
}
