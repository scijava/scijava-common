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

package org.scijava.io.handle;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.scijava.io.ByteArrayByteBank;
import org.scijava.io.ByteBank;
import org.scijava.io.location.Location;

/**
 * A {@link BufferedStreamHandle} backed by a {@link ByteBank}.
 * 
 * @author Gabriel Einsdorf
 */
public class DefaultBufferedStreamHandle<L extends Location> extends
	AbstractStreamHandle<L> implements BufferedStreamHandle<L>
{

	private static final int CHUNK_SIZE = 8192;

	private final StreamHandle<L> handle;
	private ByteBank buffer;

	private InputStream inStreamProxy;
	private OutputStream outStreamProxy;

	private boolean closed;

	/**
	 * Wraps around StreamHandle in a buffer
	 *
	 * @param handle
	 */
	public DefaultBufferedStreamHandle(final StreamHandle<L> handle) {
		this.handle = handle;
	}

	@Override
	public byte readByte() throws IOException {

		// reached end of the buffer
		if (offset() == handle.length()) {
			return -1;
		}

		// check if we need to refill the buffer
		if (offset() > maxBuf() || maxBuf() == -1) {
			// buffer more bytes
			final int filled = fill();
			if (filled <= 0) {
				// no more bytes in input handle
				return -1;
			}
		}

		byte b = getBufferIfOpen().getByte(offset());
		advance(1);
		return b;
	}

	@Override
	public void seek(final long pos) throws IOException {
		final long off = offset();
		if (off == pos) return;
		if (pos > off) {
			// ensure target is buffered
			while (pos > maxBuf()) {
				fill();
			}
		}
		// values in the range (pos < off) are already buffered
		setOffset(pos);
	}

	private long maxBuf() throws IOException {
		return getBufferIfOpen().size() - 1;
	}

	@Override
	public long skip(final long n) throws IOException {
		seek(offset() + n);
		return handle().skip(n);
	}

	@Override
	public int read(final byte[] b, final int off, final int len)
		throws IOException
	{

		while (maxBuf() < offset() + len) {
			final int filled = fill();
			if (filled <= 0) {
				// no more bytes available
				break;
			}
		}

		// read all available bytes
		int available = (int) available(len);
		getBufferIfOpen().getBytes(offset(), b, off, available);
		setOffset(offset() + available);
		return available;
	}

	/**
	 * Fills the buffer with XX more bytes
	 *
	 * @throws IOException
	 */
	private int fill() throws IOException {
		final byte[] buf = new byte[CHUNK_SIZE];
		final int read = handle().read(buf);
		if (read <= 0) {
			return -1;
		}
		getBufferIfOpen().appendBytes(buf, read);
		return read;
	}

	@Override
	public InputStream in() {
		if (inStreamProxy == null) {
			inStreamProxy = new DataHandleInputStream<>(this);
		}
		return inStreamProxy;
	}

	@Override
	public OutputStream out() {
		if (outStreamProxy == null) {
			outStreamProxy = new DataHandleOutputStream<>(this);
		}
		return outStreamProxy;
	}

	@Override
	public long length() throws IOException {
		return handle().length();
	}

	private StreamHandle<L> handle() throws IOException {
		if (closed) {
			throw new IOException("Handle is closed!");
		}
		return handle;
	}

	@Override
	public void setLength(final long length) throws IOException {
		handle().setLength(length);
	}

	@Override
	public boolean isReadable() {
		return !closed && handle.isReadable();
	}

	@Override
	public boolean isWritable() {
		return !closed && handle.isWritable();
	}

	@Override
	public boolean exists() throws IOException {
		return handle.exists();
	}

	@Override
	public Class<L> getType() {
		return handle.getType();
	}

	@Override
	public void resetStream() throws IOException {
		getBufferIfOpen();
		if (handle() instanceof ResettableStreamHandle) {
			((ResettableStreamHandle<L>) handle()).resetStream();
		}
		else {
			throw new IOException("Handle can not be reset!");
		}
	}

	@Override
	public void close() throws IOException {
		if (!closed) {
			closed = true;
			handle().close();
			getBufferIfOpen().clear();
			buffer = null;
		}
	}

	/**
	 * @return the buffer used in this handle
	 * @throws IOException if this handle has been closed
	 */
	private ByteBank getBufferIfOpen() throws IOException {
		if (closed) {
			throw new IOException("Handle is closed");
		}
		if (buffer == null) {
			buffer = new ByteArrayByteBank();
		}
		return buffer;
	}
}
