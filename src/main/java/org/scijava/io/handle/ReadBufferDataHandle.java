/*
 * #%L
 * SciJava Common shared library for SciJava software.
 * %%
 * Copyright (C) 2009 - 2025 SciJava developers.
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
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.scijava.io.location.Location;

/**
 * Read-only buffered {@link DataHandle}. It buffers the underlying handle into
 * a fixed number of pages, swapping them out when necessary.
 */
public class ReadBufferDataHandle<L extends Location> extends AbstractHigherOrderHandle<L> {

	private static final int DEFAULT_PAGE_SIZE = 10_000;
	private static final int DEFAULT_NUM_PAGES = 10;

	private final int pageSize;
	private final List<byte[]> pages;
	private final int[] slotToPage;
	private final LRUReplacementStrategy replacementStrategy;
	private final Map<Integer, Integer> pageToSlot;

	/**
	 * Cached length value, for performance. When reading data, length is not
	 * expected to change, but querying it (e.g. via native filesystem access)
	 * can be slow, and we need to query the length frequently.
	 */
	private long length = -1;
	private long offset = 0l;
	private byte[] currentPage;
	private int currentPageID = -1;

	/**
	 * Creates a {@link ReadBufferDataHandle} wrapping the provided handle using the
	 * default values for the size of the pages ({@value #DEFAULT_PAGE_SIZE} byte)
	 * and number of pages ({@link #DEFAULT_NUM_PAGES}).
	 *
	 * @param handle
	 *            the handle to wrap
	 */
	public ReadBufferDataHandle(final DataHandle<L> handle) {
		this(handle, DEFAULT_PAGE_SIZE);
	}

	/**
	 * Creates a {@link ReadBufferDataHandle} wrapping the provided handle using the
	 * default value for the number of pages ({@link #DEFAULT_NUM_PAGES}).
	 *
	 * @param handle
	 *            the handle to wrap
	 * @param pageSize
	 *            the size of the used pages
	 */
	public ReadBufferDataHandle(final DataHandle<L> handle, final int pageSize) {
		this(handle, pageSize, DEFAULT_NUM_PAGES);
	}

	/**
	 * Creates a {@link ReadBufferDataHandle} wrapping the provided handle.
	 *
	 * @param handle
	 *            the handle to wrap
	 * @param pageSize
	 *            the size of the used pages
	 * @param numPages
	 *            the number of pages to use
	 */
	public ReadBufferDataHandle(final DataHandle<L> handle, final int pageSize, final int numPages) {
		super(handle);
		this.pageSize = pageSize;

		// init maps
		slotToPage = new int[numPages];
		Arrays.fill(slotToPage, -1);

		pages = new ArrayList<>(numPages);
		for (int i = 0; i < numPages; i++) {
			pages.add(null);
		}

		pageToSlot = new HashMap<>();
		replacementStrategy = new LRUReplacementStrategy(numPages);
	}

	/**
	 * Ensures that the byte at the given offset is buffered, and sets the current
	 * page to be the one containing the specified location.
	 */
	private void ensureBuffered(final long globalOffset) throws IOException {
		ensureOpen();
		final int pageID = (int) (globalOffset / pageSize);
		if (pageID == currentPageID)
			return;

		final int slotID = pageToSlot.computeIfAbsent(pageID, replacementStrategy::pickVictim);
		final int inSlotID = slotToPage[slotID];

		if (inSlotID != pageID) { // desired page is not buffered
			// update the mappings
			slotToPage[slotID] = pageID;
			pageToSlot.put(pageID, slotID);
			pageToSlot.put(inSlotID, null);

			// read the page
			currentPage = readPage(pageID, slotID);
		} else {
			currentPage = pages.get(slotID);
		}
		replacementStrategy.accessed(slotID);
		currentPageID = pageID;
	}

	/**
	 * Reads the page with the id <code>pageID</code> into the slot with the id
	 * <code>slotID</code>.
	 *
	 * @param pageID
	 *            the id of the page to read
	 * @param slotID
	 *            the id of the slot to read the page into
	 * @return the read page
	 * @throws IOException
	 *             if the reading fails
	 */
	private byte[] readPage(final int pageID, final int slotID) throws IOException {
		replacementStrategy.accessed(slotID);
		byte[] page = pages.get(slotID);
		if (page == null) {
			// lazy initialization
			page = new byte[pageSize];
			pages.set(slotID, page);
		}

		final long startOfPage = pageID * (long) pageSize;
		if (handle().offset() != startOfPage) {
			handle().seek(startOfPage);
		}

		// NB: we read repeatedly until the page is full or EOF is reached
		// handle().read(..) might read less bytes than requested
		int off = 0;
		while (off < pageSize) {
			final int read = handle().read(page, off, pageSize - off);
			if (read == -1) { // EOF
				break;
			}
			off += read;
		}
		return page;
	}

	/**
	 * Calculates the offset in the current page for the given global offset
	 */
	private int globalToLocalOffset(final long off) {
		return (int) (off % pageSize);
	}

	@Override
	public void seek(final long pos) throws IOException {
		this.offset = pos;
	}

	@Override
	public long length() throws IOException {
		if (length < 0) length = super.length();
		return length;
	}

	@Override
	public int read(final byte[] b, final int targetOffset, final int len)
		throws IOException
	{
		if (len == 0) return 0;

		// the last position we will read
		final long endPos = offset + len;

		// the number of bytes we plan to read
		final int readLength = (int) (endPos < length() ? len : length() - offset);

		int read = 0; // the number of bytes we have read
		int localTargetOff = targetOffset;

		while (read < readLength) {
			ensureBuffered(offset);

			// calculate local offsets
			final int pageOffset = globalToLocalOffset(offset);
			int localLength = pageSize - pageOffset;
			localLength = Math.min(localLength, readLength - read);
			localLength = Math.min(localLength, b.length - localTargetOff);
			if (localLength == 0) break; // we've read all we can

			// copy the data
			System.arraycopy(currentPage, pageOffset, b, localTargetOff, localLength);

			// update offsets
			read += localLength;
			offset += localLength;
			localTargetOff += localLength;
		}
		// return -1 if we tried to read at least one byte but failed
		return read != 0 ? read : -1;
	}

	@Override
	public byte readByte() throws IOException {
		ensureBuffered(offset);
		return currentPage[globalToLocalOffset(offset++)];
	}

	@Override
	public boolean isReadable() {
		return true;
	}

	@Override
	public long offset() throws IOException {
		return offset;
	}

	@Override
	protected void cleanup() {
		pages.clear();
		currentPage = null;
	}

	@Override
	public void write(final int b) throws IOException {
		throw DataHandles.readOnlyException();
	}

	@Override
	public void write(final byte[] b, final int off, final int len) throws IOException {
		throw DataHandles.readOnlyException();
	}

	@Override
	public void setLength(final long length) throws IOException {
		throw DataHandles.readOnlyException();
	}

	/**
	 * Simple strategy to pick the slot that get's evicted from the cache. This
	 * strategy always picks the least recently used slot.
	 */
	private class LRUReplacementStrategy {

		private final Deque<Integer> queue;

		/**
		 * Creates a {@link LRUReplacementStrategy} with the specified number of slots.
		 *
		 * @param numSlots
		 *            the number of slots to use
		 */
		public LRUReplacementStrategy(final int numSlots) {
			queue = new ArrayDeque<>(numSlots);

			// fill the queue
			for (int i = 0; i < numSlots; i++) {
				queue.add(i);
			}
		}

		/**
		 * Notifies this strategy that a slot has been accessed, pushing it to the end
		 * of the queue.
		 *
		 * @param slotID
		 *            the id of the slot that has been accessed
		 */
		public void accessed(final int slotID) {
			// put accessed element to the end of the queue
			queue.remove(slotID);
			queue.add(slotID);
		}

		public int pickVictim(@SuppressWarnings("unused") final int pageID) {
			return queue.peek();
		}
	}
}
