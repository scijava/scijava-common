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
import java.util.function.Consumer;

/**
 * ListenableSystemStream allows listing to System.out and System.err.
 *
 * @author Matthais Arzt
 */
class ListenableSystemStreams {

	private ListenableSystemStreams() {
		// prevent from being initialized
	}

	public static ListenableStream out() {
		return LazyHolder.OUT;
	}

	public static ListenableStream err() {
		return LazyHolder.ERR;
	}

	private static class LazyHolder { // using idiom for lazy-loaded singleton

		private static final ListenableStream OUT = new ListenableStream(System.out,
			System::setOut);
		private static final ListenableStream ERR = new ListenableStream(System.err,
			System::setErr);
	}

	public static class ListenableStream {

		private final PrintStream out;
		private PrintStream in;
		private final MultiOutputStream multi;

		ListenableStream(PrintStream out, Consumer<PrintStream> streamSetter) {
			this.out = out;
			this.multi = new MultiOutputStream(out);
			this.in = new PrintStream(multi);
			streamSetter.accept(in);
		}

		public void addOutputStream(OutputStream os) {
			multi.addOutputStream(os);
		}

		public void removeOutputStream(OutputStream os) {
			multi.removeOutputStream(os);
		}

		public PrintStream bypass() {
			return out;
		}

		public PrintStream stream() {
			return in;
		}
	}
}
