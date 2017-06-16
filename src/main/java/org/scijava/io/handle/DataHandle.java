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

import java.io.Closeable;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.scijava.io.location.Location;
import org.scijava.plugin.WrapperPlugin;

/**
 * A <em>data handle</em> is a plugin which provides both streaming and random
 * access to bytes at a {@link Location} (e.g., files or arrays).
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

	/**
	 * Sets the stream offset, measured from the beginning of the stream, at which
	 * the next read or write occurs.
	 */
	void seek(long pos) throws IOException;

	/** Returns the length of the stream. */
	long length() throws IOException;

	/**
	 * Sets the new length of the handle.
	 * 
	 * @param length New length.
	 * @throws IOException If there is an error changing the handle's length.
	 */
	void setLength(long length) throws IOException;

	/**
	 * Verifies that the handle has sufficient bytes available to read, returning
	 * the actual number of bytes which will be possible to read, which might
	 * be less than the requested value.
	 * 
	 * @param count Number of bytes to read.
	 * @return The actual number of bytes available to be read.
	 * @throws IOException If something goes wrong with the check.
	 */
	default long available(final long count) throws IOException {
		final long remain = length() - offset();
		return remain < count ? remain : count;
	}

	/**
	 * Ensures that the handle has sufficient bytes available to read.
	 * 
	 * @param count Number of bytes to read.
	 * @see #available(long)
	 * @throws EOFException If there are insufficient bytes available.
	 * @throws IOException If something goes wrong with the check.
	 */
	default void ensureReadable(final long count) throws IOException {
		if (available(count) < count) throw new EOFException();
	}

	/**
	 * Ensures that the handle has the correct length to be written to and extends
	 * it as required.
	 * 
	 * @param count Number of bytes to write.
	 * @return {@code true} if the handle's length was sufficient, or
	 *         {@code false} if the handle's length required an extension.
	 * @throws IOException If something goes wrong with the check, or there is an
	 *           error changing the handle's length.
	 */
	default boolean ensureWritable(final long count) throws IOException {
		final long minLength = offset() + count;
		if (length() < minLength) {
			setLength(minLength);
			return false;
		}
		return true;
	}

	/** Returns the byte order of the stream. */
	ByteOrder getOrder();

	/**
	 * Sets the byte order of the stream.
	 * 
	 * @param order Order to set.
	 */
	void setOrder(ByteOrder order);

	/**
	 * Returns true iff the stream's order is {@link ByteOrder#BIG_ENDIAN}.
	 * 
	 * @see #getOrder()
	 */
	default boolean isBigEndian() {
		return getOrder() == ByteOrder.BIG_ENDIAN;
	}

	/**
	 * Returns true iff the stream's order is {@link ByteOrder#LITTLE_ENDIAN}.
	 * 
	 * @see #getOrder()
	 */
	default boolean isLittleEndian() {
		return getOrder() == ByteOrder.LITTLE_ENDIAN;
	}

	/**
	 * Sets the endianness of the stream.
	 * 
	 * @param little If true, sets the order to {@link ByteOrder#LITTLE_ENDIAN};
	 *          otherwise, sets the order to {@link ByteOrder#BIG_ENDIAN}.
	 * @see #setOrder(ByteOrder)
	 */
	default void setLittleEndian(final boolean little) {
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
	default String readString(final int n) throws IOException {
		final int r = (int) available(n);
		final byte[] b = new byte[r];
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
	 * @param saveString Whether to collect the string from the current offset to
	 *          the terminating bytes, and return it. If false, returns null.
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
	 * @param saveString Whether to collect the string from the current offset
	 *          to the terminating bytes, and return it. If false, returns null.
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
	default int read() throws IOException {
		return offset() < length() ? readByte() & 0xff : -1;
	}

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
		final long skip = available(n);
		if (skip <= 0) return 0;
		seek(offset() + skip);
		return skip;
	}

	// -- DataInput methods --

	@Override
	default boolean readBoolean() throws IOException {
		return readByte() != 0;
	}

	@Override
	default void readFully(final byte[] b) throws IOException {
		readFully(b, 0, b.length);
	}

	@Override
	default int readUnsignedByte() throws IOException {
		return readByte() & 0xff;
	}

	@Override
	default short readShort() throws IOException {
		final int ch1 = read();
		final int ch2 = read();
		if ((ch1 | ch2) < 0) throw new EOFException();
		return (short) ((ch1 << 8) + (ch2 << 0));
	}

	@Override
	default int readUnsignedShort() throws IOException {
		return readShort() & 0xffff;
	}

	@Override
	default char readChar() throws IOException {
		return (char) readShort();
	}

	@Override
	default int readInt() throws IOException {
		int ch1 = read();
		int ch2 = read();
		int ch3 = read();
		int ch4 = read();
		if ((ch1 | ch2 | ch3 | ch4) < 0) throw new EOFException();
		return ((ch1 << 24) + (ch2 << 16) + (ch3 << 8) + (ch4 << 0));
	}

	@Override
	default long readLong() throws IOException {
		int ch1 = read();
		int ch2 = read();
		int ch3 = read();
		int ch4 = read();
		int ch5 = read();
		int ch6 = read();
		int ch7 = read();
		int ch8 = read();
		if ((ch1 | ch2 | ch3 | ch4 | ch5 | ch6 | ch7 | ch8) < 0) {
			throw new EOFException();
		}
		// TODO: Double check this inconsistent code.
		return ((long) ch1 << 56) + //
			((long) (ch2 & 255) << 48) + //
			((long) (ch3 & 255) << 40) + //
			((long) (ch4 & 255) << 32) + //
			((long) (ch5 & 255) << 24) + //
			((ch6 & 255) << 16) + //
			((ch7 & 255) << 8) + //
			((ch8 & 255) << 0);
	}

	@Override
	default float readFloat() throws IOException {
		return Float.intBitsToFloat(readInt());
	}

	@Override
	default double readDouble() throws IOException {
		return Double.longBitsToDouble(readLong());
	}

	@Override
	default String readLine() throws IOException {
		// NB: Adapted from java.io.RandomAccessFile.readLine().

		final StringBuffer input = new StringBuffer();
		int c = -1;
		boolean eol = false;

		while (!eol) {
			switch (c = read()) {
				case -1:
				case '\n':
					eol = true;
					break;
				case '\r':
					eol = true;
					long cur = offset();
					if (read() != '\n') seek(cur);
					break;
				default:
					input.append((char)c);
					break;
			}
		}

		if (c == -1 && input.length() == 0) {
			return null;
		}
		return input.toString();
	}

	@Override
	default String readUTF() throws IOException {
		final int length = readUnsignedShort();
		final byte[] b = new byte[length];
		read(b);
		return new String(b, "UTF-8");
	}

	@Override
	default int skipBytes(final int n) throws IOException {
		// NB: Cast here is safe since the value of n bounds the result to an int.
		final int skip = (int) available(n);
		if (skip < 0) return 0;
		seek(offset() + skip);
		return skip;
	}

	// -- DataOutput methods --

	@Override
	default void write(final byte[] b) throws IOException {
		write(b, 0, b.length);
	}

	@Override
	default void writeBoolean(final boolean v) throws IOException {
		write(v ? 1 : 0);
	}

	@Override
	default void writeByte(final int v) throws IOException {
		write(v);
	}

	@Override
	default void writeChar(final int v) throws IOException {
		write((v >>> 8) & 0xFF);
		write((v >>> 0) & 0xFF);
	}

	@Override
	default void writeInt(final int v) throws IOException {
		write((v >>> 24) & 0xFF);
		write((v >>> 16) & 0xFF);
		write((v >>> 8) & 0xFF);
		write((v >>> 0) & 0xFF);
	}

	@Override
	default void writeLong(final long v) throws IOException {
		write((byte) (v >>> 56));
		write((byte) (v >>> 48));
		write((byte) (v >>> 40));
		write((byte) (v >>> 32));
		write((byte) (v >>> 24));
		write((byte) (v >>> 16));
		write((byte) (v >>> 8));
		write((byte) (v >>> 0));
	}

	@Override
	default void writeFloat(final float v) throws IOException {
		writeInt(Float.floatToIntBits(v));
	}

	@Override
	default void writeDouble(final double v) throws IOException {
		writeLong(Double.doubleToLongBits(v));
	}

	@Override
	default void writeBytes(final String s) throws IOException {
		write(s.getBytes("UTF-8"));
	}

	@Override
	default void writeUTF(final String str) throws IOException {
		final byte[] b = str.getBytes("UTF-8");
		writeShort(b.length);
		write(b);
	}

}
