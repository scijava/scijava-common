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

package org.scijava.io.handle;

import java.io.IOException;
import java.io.OutputStream;

import org.scijava.io.location.Location;

/**
 * {@link OutputStream} backed by a {@link DataHandle}.
 * 
 * @author Curtis Rueden
 * @author Melissa Linkert
 */
public class DataHandleOutputStream<L extends Location> extends OutputStream {

	// -- Fields --

	private final DataHandle<L> handle;

	// -- Constructor --

	/** Creates an output stream around the given {@link DataHandle}. */
	public DataHandleOutputStream(final DataHandle<L> handle) {
		this.handle = handle;
	}

	// -- OutputStream methods --

	@Override
	public void write(final int i) throws IOException {
		handle.write(i);
	}

	@Override
	public void write(final byte[] b) throws IOException {
		handle.write(b);
	}

	@Override
	public void write(final byte[] b, final int off, final int len)
		throws IOException
	{
		handle.write(b, off, len);
	}

	// -- Closeable methods --

	@Override
	public void close() throws IOException {
		handle.close();
	}

	// -- Flushable methods --

	@Override
	public void flush() throws IOException {
		// NB: No action needed.
	}

}
