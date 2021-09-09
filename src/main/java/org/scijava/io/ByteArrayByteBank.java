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

package org.scijava.io;

import org.scijava.util.ByteArray;

/**
 * {@link ByteBank} implementation backed by a {@link ByteArray}. Self-growing
 * up to a maximum capacity of {@link Integer#MAX_VALUE}.
 *
 * @author Gabriel Einsdorf
 */
public class ByteArrayByteBank implements ByteBank {

	private final ByteArray buffer;
	private long size;

	/**
	 * Creates a {@link ByteArrayByteBank}.
	 */
	public ByteArrayByteBank() {
		this(new ByteArray());
	}

	/**
	 * Creates a {@link ByteArrayByteBank} with the specified initial capacity.
	 *
	 * @param initialCapacity the initial capacity of this {@link ByteBank}
	 */
	public ByteArrayByteBank(final int initialCapacity) {
		this(emptyByteArrayOfCapacity(initialCapacity));
	}

	/**
	 * Creates a {@link ByteArrayByteBank} that wraps the provided byte array.
	 *
	 * @param bytes the bytes to wrap
	 */
	public ByteArrayByteBank(final byte[] bytes) {
		this(new ByteArray(bytes));
	}

	/**
	 * Creates a {@link ByteArrayByteBank} that wraps the specified
	 * {@link ByteArray}.
	 *
	 * @param bytes the {@link ByteArray} to wrap
	 */
	public ByteArrayByteBank(final ByteArray bytes) {
		buffer = bytes;
		size = bytes.size();
	}

	@Override
	public long getMaxBufferSize() {
		return Integer.MAX_VALUE;
	}

	@Override
	public void setBytes(final long startpos, final byte[] bytes,
		final int offset, final int length)
	{
		// ensure we have space
		checkWritePos(startpos, startpos + length);
		final int neededCapacity = (int) (size + length);
		buffer.ensureCapacity(neededCapacity);

		// copy the data
		System.arraycopy(bytes, offset, buffer.getArray(), (int) startpos, length);
		buffer.setSize(neededCapacity);
		updateSize(startpos + length);
	}

	@Override
	public void setByte(final long pos, final byte b) {
		checkWritePos(pos, pos);
		buffer.ensureCapacity((int) pos);
		// NB: update the size of the underlying buffer before appending to it
		if (pos == buffer.size()) {
			buffer.setSize((int) (pos + 1));
		}
		buffer.setValue((int) pos, b);
		updateSize(pos + 1);
	}

	@Override
	public void clear() {
		buffer.clear();
		size = 0;
	}

	@Override
	public byte getByte(final long pos) {
		checkReadPos(pos, pos);
		// the buffer might contain bytes with negative value
		// we need to flip the sign to positive to satisfy the method contract
		return buffer.getValue((int) pos);
	}

	@Override
	public int getBytes(final long startPos, final byte[] b, final int offset,
		final int length)
	{
		checkReadPos(startPos, startPos + length);
		// ensure we don't try to read data which is not in the buffer
		final int readLength = (int) Math.min(size() - startPos, length);
		System.arraycopy(buffer.getArray(), (int) startPos, b, offset, readLength);
		return readLength;
	}

	@Override
	public long size() {
		return size;
	}

	// -- Helper methods --

	private void updateSize(final long newSize) {
		size = newSize > size ? newSize : size;
	}

	private static ByteArray emptyByteArrayOfCapacity(final int capacity) {
		final ByteArray byteArray = new ByteArray(new byte[capacity]);
		byteArray.setSize(0);
		return byteArray;
	}
}
