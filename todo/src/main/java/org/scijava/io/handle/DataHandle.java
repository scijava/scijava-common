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

package org.scijava.io.handle;

import java.io.Closeable;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;

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

	public enum ByteOrder {
		LITTLE_ENDIAN, BIG_ENDIAN
	}

	/** Default block size to use when searching through the stream. */
	int DEFAULT_BLOCK_SIZE = 256 * 1024; // 256 KB

	/** Default bound on bytes to search when searching through the stream. */
	int MAX_SEARCH_SIZE = 512 * 1024 * 1024; // 512 MB

	/** Gets whether reading from this handle is supported. */
	boolean isReadable();

	/** Gets whether writing to this handle is supported. */
	boolean isWritable();

	/**
	 * Tests whether this handle's location actually exists at the source.
	 * 
	 * @return True if the location exists; false if not.
	 * @throws IOException If something goes wrong with the existence check.
	 */
	boolean exists() throws IOException;

	/**
	 * Gets the last modified timestamp of the location.
	 * 
	 * @return The last modified timestamp, or null if the handle does not support
	 *         this feature or if the location does not exist.
	 * @throws IOException If something goes wrong with the last modified check.
	 */
	default Date lastModified() throws IOException {
		return null;
	}

	/**
	 * Gets a "fast" checksum which succinctly represents the contents of the data
	 * stream. The term "fast" here refers to the idea that the checksum be
	 * retrievable quickly, without actually performing a thorough computation
	 * across the entire data stream. Typically, such a thing is feasible because
	 * the checksum was calculated a priori; e.g., artifacts deployed to remote
	 * Maven repositories are always deployed with corresponding checksum files.
	 * <p>
	 * No guarantee is made about the exact nature of the checksum (e.g., SHA-1 or
	 * MD5), only that the value is deterministic for this particular location
	 * with its current contents. In other words: if a checksum differs from a
	 * previous inquiry, you can be sure the contents have changed; conversely, if
	 * the checksum is still the same, the contents are highly likely to be
	 * unchanged.
	 * </p>
	 * 
	 * @return The checksum, or null if the handle does not support this feature.
	 * @throws IOException If something goes wrong when accessing the checksum.
	 */
	default String checksum() throws IOException {
		return null;
	}

	/** Returns the current offset in the stream. */
	long offset() throws IOException;

	/**
	 * Sets the stream offset, measured from the beginning of the stream, at which
	 * the next read or write occurs.
	 */
	void seek(long pos) throws IOException;

	/**
	 * Returns the length of the data in bytes.
	 * 
	 * @return The length, or -1 if the length is unknown.
	 */
	long length() throws IOException;

	/**
	 * Sets the new length of the handle.
	 * 
	 * @param length New length.
	 * @throws IOException If there is an error changing the handle's length.
	 */
	void setLength(long length) throws IOException;

	/**
	 * Gets the number of bytes which can be read from, or written to, the
	 * data handle, bounded by the specified number of bytes.
	 * <p>
	 * In the case of reading, attempting to read the returned number of bytes is
	 * guaranteed not to throw {@link EOFException}. However, be aware that the
	 * following methods <em>might still process fewer bytes</em> than indicated
	 * by this method:
	 * </p>
	 * <ul>
	 * <li>{@link #read(byte[])}</li>
	 * <li>{@link #read(byte[], int, int)}</li>
	 * <li>{@link #skip(long)}</li>
	 * <li>{@link #skipBytes(int)}</li>
	 * </ul>
	 * <p>
	 * In the case of writing, attempting to write the returned number of bytes is
	 * guaranteed not to expand the length of the handle; i.e., the write will
	 * only overwrite bytes already within the handle's bounds.
	 * </p>
	 *
	 * @param count Desired number of bytes to read/write.
	 * @return The actual number of bytes which could be read/written,
	 *         which might be less than the requested value.
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
	 * @throws IOException If the handle is write-only, or something goes wrong
	 *           with the check.
	 */
	default void ensureReadable(final long count) throws IOException {
		if (!isReadable()) throw new IOException("This handle is write-only.");
		if (available(count) < count) throw new EOFException();
	}

	/**
	 * Ensures that the handle has the correct length to be written to, and
	 * extends it as required.
	 * 
	 * @param count Number of bytes to write.
	 * @return {@code true} if the handle's length was sufficient, or
	 *         {@code false} if the handle's length required an extension.
	 * @throws IOException If the handle is read-only, or something goes wrong
	 *           with the check, or there is an error changing the handle's
	 *           length.
	 */
	default boolean ensureWritable(final long count) throws IOException {
		if (!isWritable()) throw new IOException("This handle is read-only.");
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
	 * @param saveString Whether to collect the string from the current offset to
	 *          the terminating bytes, and return it. If false, returns null.
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
		final InputStreamReader in = new InputStreamReader(
			new DataHandleInputStream<>(this), getEncoding());
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

	/**
	 * Writes the provided string, followed by a newline character.
	 *
	 * @param string The string to write.
	 * @throws IOException If an I/O error occurs.
	 */
	default void writeLine(final String string) throws IOException {
		writeBytes(string);
		writeBytes("\n");
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
	default int read(final byte[] b) throws IOException {
		return read(b, 0, b.length);
	}

	/**
	 * Reads up to {@code len} bytes of data from the stream into an array of
	 * bytes.
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
	default void readFully(final byte[] b) throws IOException {
		readFully(b, 0, b.length);
	}

	@Override
	default void readFully(final byte[] b, final int off, final int len)
		throws IOException
	{
		// NB: Adapted from java.io.DataInputStream.readFully(byte[], int, int).
		if (len < 0) throw new IndexOutOfBoundsException();
		int n = 0;
		while (n < len) {
			final int count = read(b, off + n, len - n);
			if (count < 0) throw new EOFException();
			n += count;
		}
	}

	@Override
	default int skipBytes(final int n) throws IOException {
		// NB: Cast here is safe since the value of n bounds the result to an int.
		final int skip = (int) available(n);
		if (skip < 0) return 0;
		seek(offset() + skip);
		return skip;
	}

	@Override
	default boolean readBoolean() throws IOException {
		return readByte() != 0;
	}

	@Override
	default int readUnsignedByte() throws IOException {
		return readByte() & 0xff;
	}

	@Override
	default short readShort() throws IOException {
		final int ch0;
		final int ch1;
		if (isBigEndian()) {
			ch0 = read();
			ch1 = read();
		}
		else {
			ch1 = read();
			ch0 = read();
		}
		if ((ch0 | ch1) < 0) throw new EOFException();
		return (short) ((ch0 << 8) + (ch1 << 0));
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
		final int ch0;
		final int ch1;
		final int ch2;
		final int ch3;
		if (isBigEndian()) {
			ch0 = read();
			ch1 = read();
			ch2 = read();
			ch3 = read();
		}
		else {
			ch3 = read();
			ch2 = read();
			ch1 = read();
			ch0 = read();
		}
		if ((ch0 | ch1 | ch2 | ch3) < 0) throw new EOFException();
		return ((ch0 << 24) + (ch1 << 16) + (ch2 << 8) + (ch3 << 0));
	}

	@Override
	default long readLong() throws IOException {
		final int ch0;
		final int ch1;
		final int ch2;
		final int ch3;
		final int ch4;
		final int ch5;
		final int ch6;
		final int ch7;
		if (isBigEndian()) {
			ch0 = read();
			ch1 = read();
			ch2 = read();
			ch3 = read();
			ch4 = read();
			ch5 = read();
			ch6 = read();
			ch7 = read();
		}
		else {
			ch7 = read();
			ch6 = read();
			ch5 = read();
			ch4 = read();
			ch3 = read();
			ch2 = read();
			ch1 = read();
			ch0 = read();
		}
		if ((ch0 | ch1 | ch2 | ch3 | ch4 | ch5 | ch6 | ch7) < 0) {
			throw new EOFException();
		}
		// TODO: Double check this inconsistent code.
		return ((long) ch0 << 56) + //
			((long) (ch1 & 255) << 48) + //
			((long) (ch2 & 255) << 40) + //
			((long) (ch3 & 255) << 32) + //
			((long) (ch4 & 255) << 24) + //
			((ch5 & 255) << 16) + //
			((ch6 & 255) << 8) + //
			((ch7 & 255) << 0);
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
					final long cur = offset();
					if (read() != '\n') seek(cur);
					break;
				default:
					input.append((char) c);
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
		return DataInputStream.readUTF(this);
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
	default void writeShort(final int v) throws IOException {
		if (isBigEndian()) {
			write((v >>> 8) & 0xFF);
			write((v >>> 0) & 0xFF);
		}
		else {
			write((v >>> 0) & 0xFF);
			write((v >>> 8) & 0xFF);
		}
	}

	@Override
	default void writeChar(final int v) throws IOException {
		if (isBigEndian()) {
			write((v >>> 8) & 0xFF);
			write((v >>> 0) & 0xFF);
		}
		else {
			write((v >>> 0) & 0xFF);
			write((v >>> 8) & 0xFF);
		}
	}

	@Override
	default void writeInt(final int v) throws IOException {
		if (isBigEndian()) {
			write((v >>> 24) & 0xFF);
			write((v >>> 16) & 0xFF);
			write((v >>> 8) & 0xFF);
			write((v >>> 0) & 0xFF);
		}
		else {
			write((v >>> 0) & 0xFF);
			write((v >>> 8) & 0xFF);
			write((v >>> 16) & 0xFF);
			write((v >>> 24) & 0xFF);
		}
	}

	@Override
	default void writeLong(final long v) throws IOException {
		if (isBigEndian()) {
			write((byte) (v >>> 56));
			write((byte) (v >>> 48));
			write((byte) (v >>> 40));
			write((byte) (v >>> 32));
			write((byte) (v >>> 24));
			write((byte) (v >>> 16));
			write((byte) (v >>> 8));
			write((byte) (v >>> 0));
		}
		else {
			write((byte) (v >>> 0));
			write((byte) (v >>> 8));
			write((byte) (v >>> 16));
			write((byte) (v >>> 24));
			write((byte) (v >>> 32));
			write((byte) (v >>> 40));
			write((byte) (v >>> 48));
			write((byte) (v >>> 56));
		}
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
	default void writeChars(final String s) throws IOException {
		final int len = s.length();
		for (int i = 0; i < len; i++) {
			final int v = s.charAt(i);
			write((v >>> 8) & 0xFF);
			write((v >>> 0) & 0xFF);
		}
	}

	@Override
	default void writeUTF(final String str) throws IOException {
		DataHandles.writeUTF(str, this);
	}
}
