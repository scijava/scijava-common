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

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.scijava.io.location.Location;

/**
 * A {@link DataHandle} backed by an {@link InputStream} and/or
 * {@link OutputStream}.
 *
 * @author Curtis Rueden
 * @author Melissa Linkert
 * @author Gabriel Einsdorf
 */
public interface StreamHandle<L extends Location> extends DataHandle<L> {

	// -- StreamHandle methods --

	/**
	 * Gets an input stream for reading data, positioned at the current offset.
	 *
	 * @return the appropriate input stream, or null if the handle is write-only.
	 * @throws IOException
	 */
	InputStream in() throws IOException;

	/**
	 * Gets an output stream for writing data, positioned at the current offset.
	 *
	 * @return the appropriate output stream, or null if the handle is read-only.
	 */
	OutputStream out() throws IOException;

	/**
	 * Sets the offset of the handle to the given position.
	 * <p>
	 * This method is intended to be called only in conjunction with reading from
	 * the input stream, or writing to the output stream. Otherwise, the contents
	 * may get out of sync.
	 * </p>
	 */
	void setOffset(long offset);

	/**
	 * Increments the handle's offset by the given amount.
	 * <p>
	 * This method is intended to be called only in conjunction with reading from
	 * the input stream, or writing to the output stream. Otherwise, the contents
	 * may get out of sync.
	 * </p>
	 */
	default void advance(final long bytes) throws IOException {
		setOffset(offset() + bytes);
	}

	// -- DataHandle methods --

	@Override
	default void seek(final long pos) throws IOException {
		if (pos == offset()) return;
		if (pos > offset()) {
			jump(pos - offset());
		}
		else {
			throw new UnsupportedOperationException(
				"Can't seek backwards through this StreamHandle");
		}
	}

	/**
	 * Resets the stream to its start.
	 *
	 * @throws IOException If something goes wrong with the reset
	 */
	void resetStream() throws IOException;

	default void jump(final long n) throws IOException, EOFException {
		long remain = n;
		while (remain > 0) {
			final long r = in().skip(remain);
			if (r < 0) throw new EOFException();
			remain -= r;
		}
	}

	@Override
	default void ensureReadable(final long count) throws IOException {
		if (in() == null) throw new IOException("This handle is write-only.");
		DataHandle.super.ensureReadable(count);
	}

	@Override
	default boolean ensureWritable(final long count) throws IOException {
		if (out() == null) throw new IOException("This handle is read-only.");
		return DataHandle.super.ensureWritable(count);
	}

	@Override
	default int read() throws IOException {
		ensureReadable(0);
		final int v = in().read();
		if (v >= 0) advance(1);
		return v;
	}

	@Override
	default byte readByte() throws IOException {
		int ch = this.read();
		if (ch < 0) throw new EOFException();
		return (byte) (ch);
	}

	@Override
	default int read(final byte[] b, final int off, final int len)
		throws IOException
	{
		final int n = in().read(b, off, len);
		if (n >= 0) advance(n);
		return n;
	}

	// -- DataOutput methods --

	@Override
	default void write(final int v) throws IOException {
		ensureWritable(1);
		out().write(v);
		advance(1);
	}

	@Override
	default void writeByte(int v) throws IOException {
		write(v);
	}

	@Override
	default void write(final byte[] b, final int off, final int len)
		throws IOException
	{
		ensureWritable(len);
		out().write(b, off, len);
		advance(len);
	}

	// -- Closeable methods --

	@Override
	default void close() throws IOException {
		// TODO: Double check this logic.
		try (final InputStream in = in()) {
			if (in != null) in.close();
		}
		try (final OutputStream out = out()) {
			if (out != null) out.close();
		}
	}

}
