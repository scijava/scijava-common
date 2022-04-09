/*
 * #%L
 * SciJava Common shared library for SciJava software.
 * %%
 * Copyright (C) 2009 - 2022 SciJava developers.
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

import org.scijava.io.location.Location;

/**
 * Abstract superclass for {@link DataHandle}s that operate over other
 * {@link DataHandle}s.
 *
 * @author Gabriel Einsdorf
 */
public abstract class AbstractHigherOrderHandle<L extends Location> extends
	AbstractDataHandle<L>
{

	private DataHandle<L> handle;
	private boolean closed;

	public AbstractHigherOrderHandle(final DataHandle<L> handle) {
		this.handle = handle;
		set(handle.get()); // provides access to underlying location
	}

	@Override
	public boolean isReadable() {
		return !closed && handle.isReadable();
	}

	@Override
	public boolean isWritable() {
		return !closed && handle.isWritable();
	}

	@Override
	public long length() throws IOException {
		ensureOpen();
		return handle.length();
	}

	@Override
	public Class<L> getType() {
		return handle.getType();
	}

	@Override
	public boolean exists() throws IOException {
		return handle.exists();
	}

	@Override
	public void close() throws IOException {
		if (!closed) {
			cleanup();
			closed = true;
			handle.close();
			handle = null;
		}
	}

	protected void ensureOpen() throws IOException {
		if (closed) {
			throw new IOException("This handle is closed!");
		}
	}

	/**
	 * Clean up data structures after a handle has been closed in the
	 * {@link #close()} method.
	 *
	 * @throws IOException
	 */
	protected abstract void cleanup() throws IOException;

	/**
	 * @return the {@link DataHandle} wrapped by this
	 *         {@link AbstractHigherOrderHandle}
	 */
	protected DataHandle<L> handle() {
		return handle;
	}

}
