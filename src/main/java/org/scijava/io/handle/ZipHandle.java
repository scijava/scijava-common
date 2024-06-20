/*
 * #%L
 * SCIFIO library for reading and converting scientific file formats.
 * %%
 * Copyright (C) 2011 - 2016 Board of Regents of the University of
 * Wisconsin-Madison
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

import java.io.File;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.scijava.io.location.AbstractCompressedHandle;
import org.scijava.io.location.Location;
import org.scijava.io.location.ZipLocation;
import org.scijava.plugin.Plugin;

/**
 * StreamHandle implementation for reading from Zip-compressed files or byte
 * arrays. Instances of ZipHandle are read-only.
 *
 * @see StreamHandle
 * @author Melissa Linkert
 * @author Gabriel Einsdorf
 */
@Plugin(type = DataHandle.class)
public class ZipHandle extends AbstractCompressedHandle<ZipLocation> {

	// -- Fields --

	private DataHandle<Location> in;

	private String entryName;

	private ZipEntry entry;

	private long entryLength = -1l;

	// -- ZipHandle API methods --

	/** Get the name of the backing Zip entry. */
	public String getEntryName() {
		return entryName;
	}

	@Override
	public void resetStream() throws IOException {

		if (raw() instanceof ResettableStreamHandle<?>) {
			((ResettableStreamHandle<?>) raw()).resetStream();
		}
		else {
			raw().seek(0l);
		}

		inputStream = new ZipInputStream(new DataHandleInputStream<>(raw()));
		// FIXME add Buffering

		seekToEntry();

	}

	// -- IRandomAccess API methods --

	@Override
	public void close() throws IOException {
		inputStream = null;
		entryName = null;
		entryLength = -1;
		if (in != null) in.close();
		in = null;
	}

	// -- Helper methods --

	/**
	 * Seeks to the relevant ZIP entry, populating the stream length accordingly.
	 */
	private void seekToEntry() throws IOException {

		while (true) {
			final ZipEntry e = ((ZipInputStream) inputStream).getNextEntry();
			if (entryName == null) {
				entry = e;
				entryName = e.getName();
			}
			if (entryName.equals(e.getName())) {
				// found the matching entry name (or first entry if the name is
				// null)
				if (entryLength < 0) {
					final boolean resetNeeded = populateLength(e.getSize());
					if (resetNeeded) {
						// stream length was calculated by force, need to reset
						resetStream();
					}
				}
				break;
			}
		}
	}

	/**
	 * Sets the stream length, computing it by force if necessary.
	 *
	 * @return if the Stream needs to be reset
	 */
	private boolean populateLength(final long size) throws IOException {
		if (size >= 0) {
			entryLength = size;
			return false;
		}
		// size is unknown, so we must read the stream manually
		long length = 0;
		final DataHandle<Location> stream = raw();
		while (true) {
			final long skipped = stream.skip(Long.MAX_VALUE);
			if (skipped == 0) {
				// NB: End of stream, we hope. Technically there is no contract
				// for when skip(long) returns 0, but in practice it seems to be
				// when end of stream is reached.
				break;
			}
			length += skipped;
		}

		entryLength = length;
		return true;
	}

	@Override
	public Class<ZipLocation> getType() {
		return ZipLocation.class;
	}

	@Override
	protected void initInputStream() throws IOException {
		inputStream = new ZipInputStream(new DataHandleInputStream<>(raw()));

		entry = get().getEntry();
		if (entry == null) {
			// strip off .zip extension and directory prefix
			final String n = raw().get().getName();
			String name = n.substring(0, n.length() - 4);

			int slash = name.lastIndexOf(File.separator);
			if (slash < 0) slash = name.lastIndexOf("/");
			if (slash >= 0) name = name.substring(slash + 1);

			// look for Zip entry with same prefix as the Zip file itself
			boolean matchFound = false;
			ZipEntry ze;
			while ((ze = ((ZipInputStream) inputStream).getNextEntry()) != null) {
				if (entryName == null) entryName = ze.getName();
				if (!matchFound && ze.getName().startsWith(name)) {
					// found entry with matching name
					entryName = ze.getName();
					entry = ze;
					matchFound = true;
				}
			}
			resetStream();
		}
	}

	@Override
	public long length() throws IOException {
		if (entry == null) {
			return -1;
		}
		return entry.getSize();
	}

	public long getEntryLength() {
		return entryLength;
	}
}
