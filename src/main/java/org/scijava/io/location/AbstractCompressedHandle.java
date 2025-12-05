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

package org.scijava.io.location;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.scijava.io.handle.AbstractStreamHandle;
import org.scijava.io.handle.DataHandle;
import org.scijava.io.handle.DataHandleService;
import org.scijava.io.handle.ResettableStreamHandle;
import org.scijava.plugin.Parameter;

/**
 * Abstract superclass for {@link DataHandle}s that operate on compressed data.
 *
 * @author Gabriel Einsdorf
 */
public abstract class AbstractCompressedHandle<L extends AbstractHigherOrderLocation>
	extends AbstractStreamHandle<L> implements ResettableStreamHandle<L>
{

	private DataHandle<Location> rawHandle;
	protected InputStream inputStream;

	@Parameter
	private DataHandleService dataHandleService;

	public AbstractCompressedHandle() {
		super();
	}

	@Override
	public void resetStream() throws IOException {

		if (raw() instanceof ResettableStreamHandle<?>) {
			((ResettableStreamHandle<Location>) rawHandle).resetStream();
		}
		else {
			rawHandle.seek(0);
		}
		initInputStream();
	}

	@Override
	public InputStream in() throws IOException {
		if (inputStream == null) {
			initInputStream();
		}
		return inputStream;
	}

	@Override
	public long skip(long n) throws IOException {
		long skipped = in().skip(n);
		setOffset(offset() + skipped);
		return skipped; 
	}

	protected abstract void initInputStream() throws IOException;

	@Override
	public OutputStream out() throws IOException {
		return null;
	}

	@Override
	public boolean isWritable() {
		return false;
	}

	@Override
	public boolean isReadable() {
		return true;
	}

	@Override
	public long length() throws IOException {
		return raw().length();
	}

	@Override
	public boolean exists() throws IOException {
		return raw().exists();
	}

	@Override
	public void setLength(long length) throws IOException {
		throw new IOException("This handle " + this.getClass().getSimpleName() +
			" is read-only!");
	}

	/**
	 * @return the raw underlying DataHandle (not decompressed)
	 */
	protected DataHandle<Location> raw() {
		if (rawHandle == null) {
			rawHandle = dataHandleService.create(get().getBaseLocation());
		}
		return rawHandle;
	}

}
