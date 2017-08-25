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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.scijava.io.location.Location;

/**
 * Read-only Buffered Handle
 */
public class SparseBufferedHandle extends AbstractHigherOrderHandle<Location> {

	private static final int DEFAULT_PAGE_SIZE = 10_000;
	private final int pageSize;
	private final Map<Integer, byte[]> pages;

	private long offset = 0l;
	private byte[] currentPage;

	/**
	 * Creates a {@link SparseBufferedHandle} that
	 *
	 * @param handle
	 */
	public SparseBufferedHandle(final DataHandle<Location> handle) {
		super(handle);
		this.pageSize = DEFAULT_PAGE_SIZE;
		pages = new HashMap<>();
	}

	public SparseBufferedHandle(final DataHandle<Location> handle,
		final int pageSize)
	{
		super(handle);
		this.pageSize = pageSize;
		pages = new HashMap<>();
	}

	/**
	 * Calculates the offset in the current page for the given global offset
	 */
	private int localOffsetFromGlobal(final long off) {
		return (int) off % pageSize;
	}

	private void ensureBuffered(final long globalOffset) throws IOException {
		final int pageID = (int) (globalOffset / pageSize);
		byte[] page = pages.get(pageID);
		if (page == null) { // page is not buffered
			page = readPage(pageID);
			pages.put(pageID, page);
		}
		currentPage = page;
	}

	/**
	 * reads a page
	 *
	 * @throws IOException
	 */
	private byte[] readPage(final int pageID) throws IOException {
		final byte[] page = new byte[pageSize];
		final long startOfPage = pageID * (long) pageSize;
		handle().seek(startOfPage);
		handle().read(page);
		return page;
	}

	@Override
	public void seek(final long pos) throws IOException {
		this.offset = pos;
	}

	@Override
	public int read(final byte[] b, final int targetOff, final int len)
		throws IOException
	{
		// the last position we will read
		final long endPos = offset + len;

		// the number of bytes we plan to read
		final int readLength = (int) (endPos < length() ? len : length() - offset);

		int read = 0; // the number of bytes we have read
		int localTargetOff = targetOff;

		while (read < readLength) {
			ensureBuffered(offset);

			// calculate offsets
			final int pageOffset = localOffsetFromGlobal(offset);
			int localLength = pageSize - pageOffset;
			if (read + localLength > readLength) {
				localLength = readLength - read;
			}

			// copy the data
			System.arraycopy(currentPage, pageOffset, b, localTargetOff, localLength);

			// update offsets
			read += localLength;
			offset += localLength;
			localTargetOff += localLength;
		}

		return read;
	}

	@Override
	public byte readByte() throws IOException {
		ensureBuffered(offset);
		return currentPage[localOffsetFromGlobal(offset++)];
	}

	@Override
	public boolean isReadable() {
		return false;
	}

	@Override
	public long offset() throws IOException {
		return offset;
	}

	@Override
	protected void cleanup() {
		this.pages.clear();
	}

	@Override
	public void write(final int b) throws IOException {
		throw new IOException("This handle is read-only!");
	}

	@Override
	public void write(final byte[] b, final int off, final int len)
		throws IOException
	{
		throw new IOException("This handle is read-only!");
	}

	@Override
	public void setLength(final long length) throws IOException {
		throw new IOException("This handle is read-only!");
	}
}
