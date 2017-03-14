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

import java.io.IOException;
import java.util.Arrays;

import org.scijava.io.location.DummyLocation;
import org.scijava.plugin.Plugin;

/**
 * A {@link DataHandle} which reads all zeroes, and writes no actual data.
 *
 * @author Curtis Rueden
 */
@Plugin(type = DataHandle.class)
public class DummyHandle extends AbstractDataHandle<DummyLocation> {

	// -- Fields --

	private long offset;
	private long length;

	// -- DataHandle methods --

	@Override
	public long offset() throws IOException {
		return offset;
	}

	@Override
	public void seek(final long pos) throws IOException {
		if (pos > length()) setLength(pos);
		offset = pos;
	}

	@Override
	public long length() throws IOException {
		return length;
	}

	@Override
	public void setLength(final long length) throws IOException {
		this.length = length;
	}

	@Override
	public boolean isReadable() {
		return true;
	}

	@Override
	public boolean isWritable() {
		return true;
	}

	// -- DataInput methods --

	@Override
	public byte readByte() throws IOException {
		final long r = available(1);
		if (r <= 0) return -1;
		offset++;
		return 0;
	}

	@Override
	public int read(final byte[] b, final int off, final int len)
		throws IOException
	{
		final int r = (int) available(len);
		offset += r;
		Arrays.fill(b, off, off + r, (byte) 0);
		return r;
	}

	// -- DataOutput methods --

	@Override
	public void write(final int v) throws IOException {
		ensureWritable(1);
		offset++;
	}

	@Override
	public void write(final byte[] b, final int off, final int len)
		throws IOException
	{
		ensureWritable(len);
		offset += len;
	}

	// -- Closeable methods --

	@Override
	public void close() {
		// NB: No action needed.
	}

	// -- Typed methods --

	@Override
	public Class<DummyLocation> getType() {
		return DummyLocation.class;
	}

}
