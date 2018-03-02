/*-
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
package org.scijava.download;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.scijava.io.handle.AbstractDataHandle;
import org.scijava.io.handle.DataHandle;
import org.scijava.io.location.Location;

/**
 * {@link DataHandle} plugin for writing to multiple {@link DataHandle}s.
 * 
 * @author Curtis Rueden
 */
public class MultiWriteHandle extends AbstractDataHandle<Location> {

	private final List<DataHandle<?>> handles;

	public MultiWriteHandle(final DataHandle<?>... handles) {
		this.handles = new ArrayList<>(Arrays.asList(handles));
	}

	// -- DataHandle methods --

	@Override
	public boolean isReadable() {
		return false;
	}

	@Override
	public boolean isWritable() {
		boolean writable = true;
		// NB: Somewhat arbitrarily, we are writable iff all our constituents are.
		for (final DataHandle<?> h : handles)
			writable &= h.isWritable();
		return writable;
	}

	@Override
	public boolean exists() throws IOException {
		boolean exists = true;
		// NB: Somewhat arbitrarily, we exist iff any of our constituents exist.
		for (final DataHandle<?> h : handles)
			exists |= h.isWritable();
		return exists;
	}

	@Override
	public Date lastModified() throws IOException {
		for (final DataHandle<?> h : handles) {
			final Date lastModified = h.lastModified();
			if (lastModified != null) return lastModified;
		}
		return null;
	}

	@Override
	public String checksum() throws IOException {
		for (final DataHandle<?> h : handles) {
			final String checksum = h.checksum();
			if (checksum != null) return checksum;
		}
		return null;
	}

	@Override
	public long offset() throws IOException {
		return handles.get(0).offset();
	}

	@Override
	public void seek(long pos) throws IOException {
		// TODO: parallelStream().forEach() for performance.
		for (final DataHandle<?> h : handles)
			h.seek(pos);
	}

	@Override
	public long length() throws IOException {
		return handles.get(0).length();
	}

	@Override
	public void setLength(long length) throws IOException {
		for (final DataHandle<?> h : handles)
			h.setLength(length);
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public Class<Location> getType() {
		return null;
	}

	@Override
	public byte readByte() throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void write(final int b) throws IOException {
		// TODO: parallelStream().forEach() for performance.
		for (final DataHandle<?> h : handles)
			h.write(b);
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		// TODO: parallelStream().forEach() for performance.
		for (final DataHandle<?> h : handles)
			h.write(b, off, len);
	}

	@Override
	public void close() throws IOException {
		// TODO: parallelStream().forEach() for performance.
		for (final DataHandle<?> h : handles)
			h.close();
	}
}
