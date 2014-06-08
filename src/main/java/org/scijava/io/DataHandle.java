/*
 * #%L
 * SciJava Common shared library for SciJava software.
 * %%
 * Copyright (C) 2009 - 2015 Board of Regents of the University of
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
	boolean isLittleEndian();

	/**
	 * Sets the byte order of the stream.
	 * 
	 * @param order Order to set.
	 */
	void setOrder(ByteOrder order);

	/** Sets the endianness of the stream. */
	void setOrder(final boolean little);

	/** Gets the native encoding of the stream. */
	String getEncoding();

	/** Sets the native encoding of the stream. */
	void setEncoding(String encoding);

	/**
	 * Reads up to {@code buf.remaining()} bytes of data from the stream into a
	 * {@link ByteBuffer}.
	 */
	int read(ByteBuffer buf) throws IOException;

	/**
	 * Reads up to {@code len} bytes of data from the stream into a
	 * {@link ByteBuffer}.
	 * 
	 * @return the total number of bytes read into the buffer.
	 */
	int read(ByteBuffer buf, int len) throws IOException;

	/**
	 * Sets the stream pointer offset, measured from the beginning of the stream,
	 * at which the next read or write occurs.
	 */
	void seek(long pos) throws IOException;

	/**
	 * Writes up to {@code buf.remaining()} bytes of data from the given
	 * {@link ByteBuffer} to the stream.
	 */
	void write(ByteBuffer buf) throws IOException;

	/**
	 * Writes up to len bytes of data from the given ByteBuffer to the stream.
	 */
	void write(ByteBuffer buf, int len) throws IOException;

	/** Reads a string of arbitrary length, terminated by a null char. */
	String readCString() throws IOException;

	/** Reads a string of up to length n. */
	String readString(int n) throws IOException;

	/**
	 * Reads a string ending with one of the characters in the given string.
	 * 
	 * @see #findString(String...)
	 */
	String readString(String lastChars) throws IOException;

	/**
	 * Reads a string ending with one of the given terminating substrings.
	 * 
	 * @param terminators The strings for which to search.
	 * @return The string from the initial position through the end of the
	 *         terminating sequence, or through the end of the stream if no
	 *         terminating sequence is found.
	 */
	String findString(String... terminators) throws IOException;

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
	String findString(boolean saveString, String... terminators)
		throws IOException;

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
	String findString(int blockSize, String... terminators) throws IOException;

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
	String findString(boolean saveString, int blockSize, String... terminators)
		throws IOException;

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
	int read(byte[] b) throws IOException;

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
	long skip(long n) throws IOException;

}
