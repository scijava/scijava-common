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

package org.scijava.io.location;

import org.scijava.io.ByteArrayByteBank;
import org.scijava.io.ByteBank;
import org.scijava.util.ByteArray;

/**
 * {@link Location} backed by a {@link ByteBank}.
 *
 * @author Curtis Rueden
 * @author Gabriel Einsdorf
 */
public class BytesLocation extends AbstractLocation {

	private final ByteBank bytes;

	private final String name;

	/**
	 * Creates a {@link BytesLocation} backed by the specified
	 * {@link ByteBank}.
	 *
	 * @param bytes the {@link ByteBank} that will back this {@link Location}
	 */
	public BytesLocation(final ByteBank bytes) {
		this(bytes, null);
	}

	/**
	 * Creates a {@link BytesLocation} backed by the specified {@link ByteBank}.
	 *
	 * @param bytes the {@link ByteBank} that will back this {@link Location}
	 * @param name the name of this {@link Location}
	 */
	public BytesLocation(final ByteBank bytes, final String name) {
		this.bytes = bytes;
		this.name = name;
	}

	/**
	 * Creates a {@link BytesLocation} backed by a {@link ByteArrayByteBank} with
	 * the specified initial capacity, but with a reported size of 0. This method
	 * can be used to avoid needing to grow the underlying {@link ByteBank}.
	 */
	public BytesLocation(final int initialCapacity) {
		this(initialCapacity, null);
	}

	/**
	 * Creates a {@link BytesLocation} backed by a {@link ByteArrayByteBank} with
	 * the specified initial capacity, but with a reported size of 0. This method
	 * can be used to avoid needing to grow the underlying {@link ByteBank}.
	 *
	 * @param name the name of this {@link Location}
	 */
	public BytesLocation(final int initialCapacity, final String name) {
		this.bytes = new ByteArrayByteBank(initialCapacity);
		this.name = name;
	}

	/**
	 * Creates a {@link BytesLocation} backed by a {@link ByteArrayByteBank}
	 * that wraps the specified {@link ByteArray}.
	 */
	public BytesLocation(final ByteArray bytes) {
		this(bytes, null);
	}

	/**
	 * Creates a {@link BytesLocation} backed by a {@link ByteArrayByteBank} that
	 * wraps the specified {@link ByteArray}.
	 *
	 * @param name the name of this Location.
	 */
	public BytesLocation(final ByteArray bytes, final String name) {
		this.bytes = new ByteArrayByteBank(bytes);
		this.name = name;
	}

	/**
	 * Creates a {@link BytesLocation} backed by a {@link ByteArrayByteBank}
	 * which wraps the specified array.
	 *
	 * @param bytes the array to wrap
	 */
	public BytesLocation(final byte[] bytes) {
		this(bytes, null);
	}

	/**
	 * Creates a {@link BytesLocation} backed by a {@link ByteArrayByteBank} which
	 * wraps the specified array.
	 *
	 * @param bytes the array to wrap
	 * @param name the name of this Location.
	 */
	public BytesLocation(final byte[] bytes, final String name) {
		this.bytes = new ByteArrayByteBank(bytes);
		this.name = name;
	}

	/**
	 * Creates a {@link BytesLocation} backed by a {@link ByteArrayByteBank} with
	 * the specified initial capacity and the provided data.
	 * 
	 * @param bytes the bytes to copy into the new {@link BytesLocation}
	 * @param offset the offset in the bytes array to start copying from
	 * @param length the number of bytes to copy, starting from the offset
	 */
	public BytesLocation(final byte[] bytes, final int offset, final int length) {
		this(bytes, offset, length, null);
	}

	/**
	 * Creates a {@link BytesLocation} backed by a {@link ByteArrayByteBank} with
	 * the specified initial capacity and the provided data.
	 *
	 * @param bytes the bytes to copy into the new {@link BytesLocation}
	 * @param offset the offset in the bytes array to start copying from
	 * @param length the number of bytes to copy, starting from the offset
	 * @param name the name of this Location.
	 */
	public BytesLocation(final byte[] bytes, final int offset, final int length,
		final String name)
	{
		this.bytes = new ByteArrayByteBank(length);
		this.bytes.setBytes(0l, bytes, offset, length);
		this.name = name;
	}

	// -- BytesLocation methods --

	/** Gets the backing {@link ByteBank}. */
	public ByteBank getByteBank() {
		return bytes;
	}

	@Override
	public String getName() {
		return name != null ? name : defaultName();
	}

	// -- Object methods --

	@Override
	public int hashCode() {
		return System.identityHashCode(this);
	}

	@Override
	public boolean equals(final Object obj) {
		return obj == this;
	}
}
