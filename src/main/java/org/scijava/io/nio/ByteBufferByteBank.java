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

package org.scijava.io.nio;

import java.nio.ByteBuffer;
import java.util.function.Function;

import org.scijava.io.ByteBank;

/**
 * A {@link ByteBank} backed by a {@link ByteBuffer}. Self-growing up to a
 * maximal capacity of {@link Integer#MAX_VALUE}
 *
 * @author Gabriel Einsdorf
 */
public class ByteBufferByteBank implements ByteBank {

	private static final int DEFAULT_CAPACITY = 10_000;

	private ByteBuffer buffer;

	private int maxBufferedPos = -1;

	private Function<Integer, ByteBuffer> provider;

	public ByteBufferByteBank() {
		provider = ByteBuffer::allocate;
		buffer = provider.apply(DEFAULT_CAPACITY);
	}

	public ByteBufferByteBank(final Function<Integer, ByteBuffer> provider) {
		this.provider = provider;
		buffer = provider.apply(DEFAULT_CAPACITY);
	}

	public ByteBufferByteBank(final Function<Integer, ByteBuffer> provider,
		final int initialCapacity)
	{
		this.provider = provider;
		buffer = provider.apply(initialCapacity);
	}

	public ByteBufferByteBank(final int initialCapacity) {
		provider = ByteBuffer::allocate;
		buffer = provider.apply(initialCapacity);
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
		final int neededCapacity = Math.max(maxBufferedPos, 0) + length;
		ensureCapacity(neededCapacity);

		// copy the data
		buffer.position((int) startpos);
		buffer.put(bytes, offset, length);

		// update the maxpos
		updateMaxPos(startpos + length - 1);
	}

	@Override
	public void setByte(final long pos, final byte b) {
		checkWritePos(pos, pos);
		if (pos == buffer.capacity()) {
			ensureCapacity((int) pos + 1);
		}
		buffer.put((int) pos, b);
		updateMaxPos(pos);
	}

	private void updateMaxPos(final long pos) {
		maxBufferedPos = (int) (pos > maxBufferedPos ? pos : maxBufferedPos);
	}

	@Override
	public void clear() {
		buffer.clear();
		maxBufferedPos = 0;
	}

	@Override
	public byte getByte(final long pos) {
		checkReadPos(pos, pos);
		// the buffer might contain bytes with negative value
		// we need to flip the sign to positive to satisfy the contract of this
		// method
		return buffer.get((int) pos);
	}

	@Override
	public int getBytes(final long startPos, final byte[] b, final int offset,
		final int length)
	{
		checkReadPos(startPos, startPos + length);
		// ensure we don't try to read data which is not in the buffer
		final int readLength = (int) Math.min(getMaxPos() - startPos + 1, length);
		buffer.position((int) startPos);
		buffer.get(b, offset, readLength);

		return readLength;
	}

	@Override
	public long getMaxPos() {
		return maxBufferedPos;
	}

	@Override
	public boolean isReadOnly() {
		// NB: Some ByteBuffers are read-only. But there is no API to check it.
		// Therefore, we make a "best effort" guess based on known read-only types.
		// Since these read-only types are not public, we compare class names rather
		// than checking for type equality or using instanceof.
		final String className = buffer.getClass().getName();
		return className.equals("java.nio.HeapByteBufferR") ||
			className.equals("java.nio.DirectByteBufferR");
	}

	private void ensureCapacity(final int minCapacity) {
		final int oldCapacity = buffer.capacity();
		if (minCapacity <= oldCapacity) return; // no need to grow

		// grow the array by up to 50% (plus a small constant)
		final int growth = Math.min(oldCapacity / 2 + 16, Integer.MAX_VALUE);
		final int newCapacity;
		if (growth > Integer.MAX_VALUE - oldCapacity) {
			// growth would push array over the maximum array size
			newCapacity = Integer.MAX_VALUE;
		}
		else newCapacity = oldCapacity + growth;
		// ensure the array grows by at least the requested minimum capacity
		final int newLength = Math.max(minCapacity, newCapacity);

		// copy the data into a new array
		buffer.position(0);
		final ByteBuffer newBuffer = ByteBuffer.allocate(newLength);
		newBuffer.order(buffer.order());
		newBuffer.put(buffer);
		buffer = newBuffer;
	}
}
