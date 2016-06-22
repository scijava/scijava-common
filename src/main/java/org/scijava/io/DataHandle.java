/*
 * #%L
 * SciJava Common shared library for SciJava software.
 * %%
 * Copyright (C) 2009 - 2016 Board of Regents of the University of
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

package org.scijava.io;

import java.io.Closeable;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.scijava.plugin.WrapperPlugin;

/**
 * A <em>data handle</em> is a plugin which provides access to bytes in a data
 * stream (e.g., files or arrays), identified by a {@link Location}.
 * 
 * @author Curtis Rueden
 * @see DataHandleInputStream
 * @see DataHandleOutputStream
 */
public interface DataHandle<L extends Location> extends WrapperPlugin<L>,
	DataInput, DataOutput, Closeable
{

	/** Default block size to use when searching through the stream. */
	int DEFAULT_BLOCK_SIZE = 256 * 1024; // 256 KB

	/** Default bound on bytes to search when searching through the stream. */
	int MAX_SEARCH_SIZE = 512 * 1024 * 1024; // 512 MB

	/** Returns the current offset in the stream. */
	long offset() throws IOException;

	/** Returns the length of the stream. */
	long length() throws IOException;

	/**
	 * Returns the current order of the stream.
	 * 
	 * @return See above.
	 */
	ByteOrder getOrder();

	/** Gets the endianness of the stream. */
	default boolean isLittleEndian() {
		return getOrder() == ByteOrder.LITTLE_ENDIAN;
	}

	/**
	 * Sets the byte order of the stream.
	 * 
	 * @param order Order to set.
	 */
	void setOrder(ByteOrder order);

	/** Sets the endianness of the stream. */
	default void setOrder(final boolean little) {
		setOrder(little ? ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN);
	}

	/** Gets the native encoding of the stream. */
	String getEncoding();

	/** Sets the native encoding of the stream. */
	void setEncoding(String encoding);

	/**
	 * Reads up to {@code buf.remaining()} bytes of data from the stream into a
	 * {@link ByteBuffer}.
	 */
	default int read(final ByteBuffer buf) throws IOException {
		return read(buf, buf.remaining());
	}

	/**
	 * Reads up to {@code len} bytes of data from the stream into a
	 * {@link ByteBuffer}.
	 * 
	 * @return the total number of bytes read into the buffer.
	 */
	default int read(final ByteBuffer buf, final int len) throws IOException {
		final int n;
		if (buf.hasArray()) {
			// read directly into the array
			n = read(buf.array(), buf.arrayOffset(), len);
		}
		else {
			// read into a temporary array, then copy
			final byte[] b = new byte[len];
			n = read(b);
			buf.put(b, 0, n);
		}
		return n;
	}

	/**
	 * Sets the stream pointer offset, measured from the beginning of the stream,
	 * at which the next read or write occurs.
	 */
	void seek(long pos) throws IOException;

	/**
	 * Writes up to {@code buf.remaining()} bytes of data from the given
	 * {@link ByteBuffer} to the stream.
	 */
	default void write(final ByteBuffer buf) throws IOException {
		write(buf, buf.remaining());
	}

	/**
	 * Writes up to len bytes of data from the given ByteBuffer to the stream.
	 */
	default void write(final ByteBuffer buf, final int len)
		throws IOException
	{
		if (buf.hasArray()) {
			// write directly from the buffer's array
			write(buf.array(), buf.arrayOffset(), len);
		}
		else {
			// copy into a temporary array, then write
			final byte[] b = new byte[len];
			buf.get(b);
			write(b);
		}
	}


	/** Reads a string of arbitrary length, terminated by a null char. */
	default String readCString() throws IOException {
		final String line = findString("\0");
		return line.length() == 0 ? null : line;
	}

	/** Reads a string of up to length n. */
	default String readString(int n) throws IOException {
		final long avail = length() - offset();
		if (n > avail) n = (int) avail;
		final byte[] b = new byte[n];
		readFully(b);
		return new String(b, getEncoding());
	}

	/**
	 * Reads a string ending with one of the characters in the given string.
	 * 
	 * @see #findString(String...)
	 */
	default String readString(final String lastChars) throws IOException {
		if (lastChars.length() == 1) return findString(lastChars);
		final String[] terminators = new String[lastChars.length()];
		for (int i = 0; i < terminators.length; i++) {
			terminators[i] = lastChars.substring(i, i + 1);
		}
		return findString(terminators);
	}

	/**
	 * Reads a string ending with one of the given terminating substrings.
	 * 
	 * @param terminators The strings for which to search.
	 * @return The string from the initial position through the end of the
	 *         terminating sequence, or through the end of the stream if no
	 *         terminating sequence is found.
	 */
	default String findString(final String... terminators) throws IOException {
		return findString(true, DEFAULT_BLOCK_SIZE, terminators);
	}

	/**
	 * Reads or skips a string ending with one of the given terminating
	 * substrings.
	 * 
	 * @param saveString Whether to collect the string from the current file
	 *          pointer to the terminating bytes, and return it. If false, returns
	 *          null.
	 * @param terminators The strings for which to search.
	 * @throws IOException If saveString flag is set and the maximum search length
	 *           (512 MB) is exceeded.
	 * @return The string from the initial position through the end of the
	 *         terminating sequence, or through the end of the stream if no
	 *         terminating sequence is found, or null if saveString flag is unset.
	 */
	default String findString(final boolean saveString,
		final String... terminators) throws IOException
	{
		return findString(saveString, DEFAULT_BLOCK_SIZE, terminators);
	}

	/**
	 * Reads a string ending with one of the given terminating substrings, using
	 * the specified block size for buffering.
	 * 
	 * @param blockSize The block size to use when reading bytes in chunks.
	 * @param terminators The strings for which to search.
	 * @return The string from the initial position through the end of the
	 *         terminating sequence, or through the end of the stream if no
	 *         terminating sequence is found.
	 */
	default String findString(final int blockSize, final String... terminators)
		throws IOException
	{
		return findString(true, blockSize, terminators);
	}

	/**
	 * Reads or skips a string ending with one of the given terminating
	 * substrings, using the specified block size for buffering.
	 * 
	 * @param saveString Whether to collect the string from the current file
	 *          pointer to the terminating bytes, and return it. If false, returns
	 *          null.
	 * @param blockSize The block size to use when reading bytes in chunks.
	 * @param terminators The strings for which to search.
	 * @throws IOException If saveString flag is set and the maximum search length
	 *           (512 MB) is exceeded.
	 * @return The string from the initial position through the end of the
	 *         terminating sequence, or through the end of the stream if no
	 *         terminating sequence is found, or null if saveString flag is unset.
	 */
	default String findString(final boolean saveString, final int blockSize,
		final String... terminators) throws IOException
	{
		final StringBuilder out = new StringBuilder();
		final long startPos = offset();
		long bytesDropped = 0;
		final long inputLen = length();
		long maxLen = inputLen - startPos;
		final boolean tooLong = saveString && maxLen > MAX_SEARCH_SIZE;
		if (tooLong) maxLen = MAX_SEARCH_SIZE;
		boolean match = false;
		int maxTermLen = 0;
		for (final String term : terminators) {
			final int len = term.length();
			if (len > maxTermLen) maxTermLen = len;
		}

		@SuppressWarnings("resource")
		final InputStreamReader in =
			new InputStreamReader(new DataHandleInputStream<>(this), getEncoding());
		final char[] buf = new char[blockSize];
		long loc = 0;
		while (loc < maxLen && offset() < length() - 1) {
			// if we're not saving the string, drop any old, unnecessary output
			if (!saveString) {
				final int outLen = out.length();
				if (outLen >= maxTermLen) {
					final int dropIndex = outLen - maxTermLen + 1;
					final String last = out.substring(dropIndex, outLen);
					out.setLength(0);
					out.append(last);
					bytesDropped += dropIndex;
				}
			}

			// read block from stream
			final int r = in.read(buf, 0, blockSize);
			if (r <= 0) throw new IOException("Cannot read from stream: " + r);

			// append block to output
			out.append(buf, 0, r);

			// check output, returning smallest possible string
			int min = Integer.MAX_VALUE, tagLen = 0;
			for (final String t : terminators) {
				final int len = t.length();
				final int start = (int) (loc - bytesDropped - len);
				final int value = out.indexOf(t, start < 0 ? 0 : start);
				if (value >= 0 && value < min) {
					match = true;
					min = value;
					tagLen = len;
				}
			}

			if (match) {
				// reset stream to proper location
				seek(startPos + bytesDropped + min + tagLen);

				// trim output string
				if (saveString) {
					out.setLength(min + tagLen);
					return out.toString();
				}
				return null;
			}

			loc += r;
		}

		// no match
		if (tooLong) throw new IOException("Maximum search length reached.");
		return saveString ? out.toString() : null;
	}

	// -- InputStream look-alikes --

	/**
	 * Reads the next byte of data from the stream.
	 * 
	 * @return the next byte of data, or -1 if the end of the stream is reached.
	 * @throws IOException - if an I/O error occurs.
	 */
	int read() throws IOException;

	/**
	 * Reads up to b.length bytes of data from the stream into an array of bytes.
	 * 
	 * @return the total number of bytes read into the buffer.
	 */
	default int read(byte[] b) throws IOException {
		return read(b, 0, b.length);
	}

	/**
	 * Reads up to len bytes of data from the stream into an array of bytes.
	 * 
	 * @return the total number of bytes read into the buffer.
	 */
	int read(byte[] b, int off, int len) throws IOException;

	/**
	 * Skips over and discards {@code n} bytes of data from the stream. The
	 * {@code skip} method may, for a variety of reasons, end up skipping over
	 * some smaller number of bytes, possibly {@code 0}. This may result from any
	 * of a number of conditions; reaching end of file before {@code n} bytes have
	 * been skipped is only one possibility. The actual number of bytes skipped is
	 * returned. If {@code n} is negative, no bytes are skipped.
	 * 
	 * @param n - the number of bytes to be skipped.
	 * @return the actual number of bytes skipped.
	 * @throws IOException - if an I/O error occurs.
	 */
	default long skip(final long n) throws IOException {
		if (n < 0) return 0;
		final long remain = length() - offset();
		final long num = n < remain ? n : remain;
		seek(offset() + num);
		return num;
	}

}
