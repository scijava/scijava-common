/*
 * #%L
 * SciJava Common shared library for SciJava software.
 * %%
 * Copyright (C) 2009 - 2022 SciJava developers.
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

import java.io.EOFException;
import java.io.IOException;

import org.scijava.io.location.Location;

/**
 * Wraps a {@link DataHandle} and acts as a write buffer.
 *
 * @author Gabriel Einsdorf
 */
public class WriteBufferDataHandle extends AbstractHigherOrderHandle<Location> {

	private static final int DEFAULT_BUFFERSIZE = 10_000;
	private long offset = 0;
	private int nextPos = 0;

	private byte[] buffer;
	private final int bufferSize;

	/**
	 * Creates a {@link WriteBufferDataHandle} that wraps the given
	 * {@link DataHandle}, the default size for the buffer is used
	 * ({@value #DEFAULT_BUFFERSIZE} bytes).
	 *
	 * @param handle the handle to wrap
	 */
	public WriteBufferDataHandle(final DataHandle<Location> handle) {
		this(handle, DEFAULT_BUFFERSIZE);
	}

	/**
	 * Creates a {@link WriteBufferDataHandle} that wraps the given
	 * {@link DataHandle}
	 *
	 * @param handle the handle to wrap
	 * @param bufferSize the size of the write buffer in bytes
	 */
	public WriteBufferDataHandle(final DataHandle<Location> handle,
		final int bufferSize)
	{
		super(handle);
		this.bufferSize = bufferSize;
	}

	@Override
	public void write(final int b) throws IOException {
		ensureOpen();
		// if buffer is full flush
		if (nextPos >= buffer.length) {
			flush();
		}
		// buffer the byte
		buffer[nextPos] = (byte) b;
		nextPos++;
		offset++;
	}

	@Override
	public void write(final byte[] b, final int off, final int len)
		throws IOException
	{
		ensureOpen();
		// ensure the range is valid
		if ((off < 0) || (off > b.length) || (len < 0) || ((off +
			len) > b.length) || ((off + len) < 0))
		{
			throw new IndexOutOfBoundsException();
		}
		else if (len == 0) {
			return; // nothing to do
		}

		// skip the buffering and write directly to the handle
		if (len > buffer.length) {
			flush();
			handle().write(b, off, len);
			offset += len;
			return;
		}

		// copy to buffer / flush if necessary
		int start = off;
		final int total = off + len;
		while (start < total) {
			final int numItems = Math.min(buffer.length - nextPos, total - start);
			System.arraycopy(b, start, buffer, nextPos, numItems);
			start += numItems;
			nextPos += numItems;
			if (nextPos >= buffer.length) {
				flush();
			}
		}
	}

	/**
	 * Write the buffer content to the underlying handle
	 */
	private void flush() throws IOException {
		ensureOpen();
		if (nextPos == 0) return;

		handle().write(buffer, 0, nextPos);
		nextPos = 0;
	}

	@Override
	public long length() throws IOException {
		// data written out + data in the buffer
		return handle().length() + nextPos - 1;
	}

	@Override
	public void setLength(final long length) throws IOException {
		ensureOpen();
		handle().setLength(length);
	}

	@Override
	public boolean isReadable() {
		return false;
	}

	/**
	 * @throws IOException if this handle has been closed
	 */
	@Override
	protected void ensureOpen() throws IOException {
		super.ensureOpen();
		if (buffer == null) {
			buffer = new byte[bufferSize];
		}
	}

	@Override
	public long offset() throws IOException {
		return offset;
	}

	@Override
	public void seek(final long pos) throws IOException {
		ensureOpen();
		if (pos >= length()) {
			throw new EOFException();
		}
		flush();
		offset = pos;
		handle().seek(offset);
	}

	@Override
	public long skip(final long n) throws IOException {
		throw new IOException("Operation 'skip' is not supported!");
	}

	@Override
	public byte readByte() throws IOException {
		throw DataHandles.writeOnlyException();
	}

	@Override
	public int read(final byte[] b, final int off, final int len)
		throws IOException
	{
		throw DataHandles.writeOnlyException();
	}

	@Override
	protected void cleanup() throws IOException {
		flush();
		buffer = null;
	}
}
