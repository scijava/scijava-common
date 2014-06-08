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

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.scijava.plugin.AbstractWrapperPlugin;

/**
 * Abstract base class for {@link DataHandle} plugins.
 *
 * @author Curtis Rueden
 */
public abstract class AbstractDataHandle<L extends Location> extends
	AbstractWrapperPlugin<L> implements DataHandle<L>
{

	// -- Constants --

	/** Block size to use when searching through the stream. */
	private static final int DEFAULT_BLOCK_SIZE = 256 * 1024; // 256 KB

	/** Maximum number of bytes to search when searching through the stream. */
	private static final int MAX_SEARCH_SIZE = 512 * 1024 * 1024; // 512 MB

	// -- Fields --

	private ByteOrder order = ByteOrder.BIG_ENDIAN;
	private String encoding = "UTF-8";

	// -- DataHandle methods --

	@Override
	public ByteOrder getOrder() {
		return order;
	}

	@Override
	public boolean isLittleEndian() {
		return getOrder() == ByteOrder.LITTLE_ENDIAN;
	}

	@Override
	public void setOrder(final ByteOrder order) {
		this.order = order;
	}

	@Override
	public void setOrder(final boolean little) {
		setOrder(little ? ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN);
	}

	@Override
	public String getEncoding() {
		return encoding;
	}

	@Override
	public void setEncoding(final String encoding) {
		this.encoding = encoding;
	}

	@Override
	public int read(final ByteBuffer buf) throws IOException {
		return read(buf, buf.remaining());
	}

	@Override
	public int read(final ByteBuffer buf, final int len)
		throws IOException
	{
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

	@Override
	public void write(final ByteBuffer buf) throws IOException {
		write(buf, buf.remaining());
	}

	@Override
	public void write(final ByteBuffer buf, final int len)
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

	@Override
	public String readCString() throws IOException {
		final String line = findString("\0");
		return line.length() == 0 ? null : line;
	}

	@Override
	public String readString(int n) throws IOException {
		final long avail = length() - offset();
		if (n > avail) n = (int) avail;
		final byte[] b = new byte[n];
		readFully(b);
		return new String(b, encoding);
	}

	@Override
	public String readString(final String lastChars) throws IOException {
		if (lastChars.length() == 1) return findString(lastChars);
		final String[] terminators = new String[lastChars.length()];
		for (int i = 0; i < terminators.length; i++) {
			terminators[i] = lastChars.substring(i, i + 1);
		}
		return findString(terminators);
	}

	@Override
	public String findString(final String... terminators) throws IOException {
		return findString(true, DEFAULT_BLOCK_SIZE, terminators);
	}

	@Override
	public String findString(final boolean saveString,
		final String... terminators) throws IOException
	{
		return findString(saveString, DEFAULT_BLOCK_SIZE, terminators);
	}

	@Override
	public String findString(final int blockSize, final String... terminators)
		throws IOException
	{
		return findString(true, blockSize, terminators);
	}

	@Override
	public String findString(final boolean saveString, final int blockSize,
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
			new InputStreamReader(new DataHandleInputStream<L>(this), getEncoding());
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

	@Override
	public int read(byte[] b) throws IOException {
		return read(b, 0, b.length);
	}

	@Override
	public long skip(final long n) throws IOException {
		if (n < 0) return 0;
		final long remain = length() - offset();
		final long num = n < remain ? n : remain;
		seek(offset() + num);
		return num;
	}

}
