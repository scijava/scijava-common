/*
 * #%L
 * SciJava Common shared library for SciJava software.
 * %%
 * Copyright (C) 2009 - 2021 SciJava developers.
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
import java.io.InputStream;

import org.scijava.io.location.Location;

/**
 * {@link InputStream} backed by a {@link DataHandle}.
 * 
 * @author Curtis Rueden
 * @author Melissa Linkert
 */
public class DataHandleInputStream<L extends Location> extends InputStream {

	// -- Fields --

	private final DataHandle<L> handle;

	private long mark = -1;

	// -- Constructors --

	/** Creates an input stream around the given {@link DataHandle}. */
	public DataHandleInputStream(final DataHandle<L> handle) {
		this.handle = handle;
	}

	// -- DataHandleInputStream methods --

	public DataHandle<L> getDataHandle() {
		return handle;
	}

	// -- InputStream methods --

	@Override
	public int read() throws IOException {
		return handle.read();
	}

	@Override
	public int read(final byte[] array, final int offset, final int n)
		throws IOException
	{
		return handle.read(array, offset, n);
	}

	@Override
	public long skip(final long n) throws IOException {
		return handle.skip(n);
	}

	@Override
	public int available() throws IOException {
		long remain = handle.length() - handle.offset();
		if (remain > Integer.MAX_VALUE) remain = Integer.MAX_VALUE;
		return (int) remain;
	}

	@Override
	public synchronized void mark(final int readLimit) {
		try {
			mark = handle.offset();
		}
		catch (final IOException exc) {
			throw new IllegalStateException(exc);
		}
	}

	@Override
	public synchronized void reset() throws IOException {
		if (mark < 0) throw new IOException("No mark set");
		handle.seek(mark);
	}

	@Override
	public boolean markSupported() {
		return true;
	}

	// -- Closeable methods --

	@Override
	public void close() throws IOException {
		handle.close();
		mark = -1;
	}

}
