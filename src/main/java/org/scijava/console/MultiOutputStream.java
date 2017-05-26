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

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * A {@code MultiOutputStream} is a collection of constituent
 * {@link OutputStream} objects, to which all output is forwarded.
 * <p>
 * Thanks to Ian F. Darwin for <a href=
 * "http://www.java2s.com/Code/Java/File-Input-Output/TeePrintStreamteesallPrintStreamoperationsintoafileratherliketheUNIXtee1command.htm"
 * >his implementation of a similar concept</a>.
 * </p>
 *
 * @author Curtis Rueden
 */
public class MultiOutputStream extends OutputStream {

	private final List<OutputStream> streams;

	/**
	 * Forwards output to a list of output streams.
	 *
	 * @param os Output streams which will receive this stream's output.
	 */
	public MultiOutputStream(final OutputStream... os) {
		streams = new CopyOnWriteArrayList<>(os);
	}

	// -- MultiOutputStream methods --

	/** Adds an output stream to those receiving this stream's output. */
	public void addOutputStream(final OutputStream os) {
		streams.add(os);
	}

	/** Removes an output stream from those receiving this stream's output. */
	public void removeOutputStream(final OutputStream os) {
		streams.remove(os);
	}

	// -- OutputStream methods --

	@Override
	public void write(final int b) throws IOException {
		for (final OutputStream stream : streams)
			stream.write(b);
	}

	@Override
	public void write(final byte[] buf, final int off, final int len)
		throws IOException
	{
		for (final OutputStream stream : streams)
			stream.write(buf, off, len);
	}

	// -- Closeable methods --

	@Override
	public void close() throws IOException {
		for (final OutputStream stream : streams)
			stream.close();
	}

	// -- Flushable methods --

	@Override
	public void flush() throws IOException {
		for (final OutputStream stream : streams)
			stream.flush();
	}

}
