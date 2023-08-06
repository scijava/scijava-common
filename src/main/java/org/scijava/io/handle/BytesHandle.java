/*
 * #%L
 * SciJava Common shared library for SciJava software.
 * %%
 * Copyright (C) 2009 - 2023 SciJava developers.
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

import org.scijava.io.ByteBank;
import org.scijava.io.location.BytesLocation;
import org.scijava.plugin.Plugin;

/**
 * {@link DataHandle} for a {@link BytesLocation}.
 *
 * @author Curtis Rueden
 * @author Melissa Linkert
 * @author Gabriel Einsdorf
 */
@Plugin(type = DataHandle.class)
public class BytesHandle extends AbstractDataHandle<BytesLocation> {

	private long offset = 0;

	// -- Constructors --

	public BytesHandle() { }

	public BytesHandle(final BytesLocation location) {
		set(location);
	}

	// -- DataHandle methods --

	@Override
	public boolean isReadable() {
		return true;
	}

	@Override
	public boolean isWritable() {
		return !bytes().isReadOnly();
	}

	@Override
	public boolean exists() {
		return true;
	}

	@Override
	public long offset() {
		return offset;
	}

	@Override
	public long length() {
		return bytes().size();
	}

	@Override
	public void setLength(final long length) throws IOException {
		// check if new length is legal
		bytes().basicRangeCheck(0, length);
		// TODO update the maxLength?
	}

	@Override
	public int read(final byte[] b, final int off, int len) throws IOException {
		if(len == 0) return 0;
		if (offset + len > length()) {
			len = (int) (length() - offset);
		}
		if(len == 0) { // EOF
			return -1;
		}
		bytes().getBytes(offset, b, off, len);
		offset += len;
		return len;
	}

	@Override
	public void seek(final long pos) throws IOException {
		if (pos > length()) setLength(pos);
		offset = pos;
	}

	// -- DataInput methods --

	@Override
	public byte readByte() throws IOException {
		ensureReadable(1);
		try {
			// we need to convert the bytes into the range 0-255
			return bytes().getByte(offset++);
		}
		catch (final Exception e) {
			throw eofException(e);
		}
	}

	@Override
	public void readFully(final byte[] b, final int off, final int len)
		throws IOException
	{
		ensureReadable(len);
		try {
			bytes().getBytes(offset, b, off, len);
			offset += len;
		}
		catch (final Exception e) {
			throw eofException(e);
		}
	}

	// -- DataOutput methods --

	@Override
	public void write(final byte[] b, final int off, final int len)
		throws IOException
	{
		ensureWritable(len);
		bytes().setBytes(offset, b, off, len);
		offset += len;
	}

	@Override
	public void write(final int b) throws IOException {
		ensureWritable(1);
		bytes().setByte(offset, (byte) b);
		offset++;
	}

	// -- Closeable methods --

	@Override
	public void close() {
		// NB: No action needed.
	}

	// -- Typed methods --

	@Override
	public Class<BytesLocation> getType() {
		return BytesLocation.class;
	}

	// -- Helper methods --

	private ByteBank bytes() {
		return get().getByteBank();
	}

	private EOFException eofException(final Throwable cause) {
		final EOFException eof = new EOFException();
		eof.initCause(cause);
		return eof;
	}

}
