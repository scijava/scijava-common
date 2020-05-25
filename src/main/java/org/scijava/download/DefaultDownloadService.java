/*-
 * #%L
 * SciJava Common shared library for SciJava software.
 * %%
 * Copyright (C) 2009 - 2020 SciJava developers.
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
import java.util.Date;

import org.scijava.io.handle.DataHandle;
import org.scijava.io.handle.DataHandleService;
import org.scijava.io.handle.DataHandles;
import org.scijava.io.location.Location;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.service.AbstractService;
import org.scijava.service.Service;
import org.scijava.task.Task;
import org.scijava.task.TaskService;

/**
 * Default implementation of {@link DownloadService}.
 *
 * @author Curtis Rueden
 */
@Plugin(type = Service.class)
public class DefaultDownloadService extends AbstractService implements
	DownloadService
{

	@Parameter
	private DataHandleService dataHandleService;

	@Parameter
	private TaskService taskService;

	@Override
	public Download download(final Location source, final Location destination) {
		final Task task = taskService.createTask("Download");
		return new DefaultDownload(source, destination, task, () -> {
			try (final DataHandle<Location> in = dataHandleService.create(source);
					final DataHandle<Location> out = dataHandleService.create(
						destination))
			{
				task.setStatusMessage("Downloading " + source.getURI());
				DataHandles.copy(in, out, task);
			}
			catch (final IOException exc) {
				// TODO: Improve error handling:
				// 1. Consider a better exception handling design here.
				// 2. Retry at least a few times if something goes wrong.
				throw new RuntimeException(exc);
			}
		});
	}

	@Override
	public Download download(final Location source, final Location destination,
		final LocationCache cache)
	{
		if (cache == null || !cache.canCache(source)) {
			// Caching this location is not supported.
			return download(source, destination);
		}

		final Task task = taskService.createTask("Download");
		return new DefaultDownload(source, destination, task, () -> {
			final Location cached = cache.cachedLocation(source);
			try (
				final DataHandle<Location> sourceHandle = dataHandleService.create(source);
				final DataHandle<Location> cachedHandle = dataHandleService.create(cached);
				final DataHandle<Location> destHandle = dataHandleService.create(destination)
			)
			{
				if (isCachedHandleValid(source, cache, sourceHandle, cachedHandle)) {
					// The data is cached; download from the cached source instead.
					task.setStatusMessage("Retrieving " + source.getURI());
					DataHandles.copy(cachedHandle, destHandle, task);
				}
				else {
					// Data is not yet cached; write to the destination _and_ the cache.
					task.setStatusMessage("Downloading + caching " + source.getURI());
					DataHandles.copy(sourceHandle, //
						new MultiWriteHandle(cachedHandle, destHandle), task);
				}
			}
			catch (final IOException exc) {
				// TODO: Improve error handling:
				// 1. Consider a better exception handling design here.
				// 2. Retry at least a few times if something goes wrong.
				throw new RuntimeException(exc);
			}
		});
	}

	// -- Helper methods --

	private boolean isCachedHandleValid(final Location source,
		final LocationCache cache, final DataHandle<Location> sourceHandle,
		final DataHandle<Location> cachedHandle) throws IOException
	{
		if (!cachedHandle.exists()) return false; // No cached data is present.

		// Compare data lengths.
		final long sourceLen = sourceHandle.length();
		final long cachedLen = cachedHandle.length();
		if (sourceLen >= 0 && cachedLen >= 0 && sourceLen != cachedLen) {
			// Original and cached sources report different lengths; cache is invalid.
			return false;
		}

		// Compare last modified timestamps.
		final Date sourceDate = sourceHandle.lastModified();
		final Date cachedDate = cachedHandle.lastModified();
		if (sourceDate != null && cachedDate != null && //
			sourceDate.after(cachedDate))
		{
			// Source was changed after cache was written; cache is invalid.
			return false;
		}

		// Compare checksums.
		final String sourceChecksum = sourceHandle.checksum();
		final String cachedChecksum = cache.loadChecksum(source);
		if (sourceChecksum != null && cachedChecksum != null && //
			!sourceChecksum.equals(cachedChecksum))
		{
			// Checksums do not match; cache is invalid.
			return false;
		}

		// Everything matched; we're all good.
		return true;
	}

	// -- Helper classes --

	private class DefaultDownload implements Download {

		private Location source;
		private Location destination;
		private Task task;

		private DefaultDownload(final Location source, final Location destination,
			final Task task, final Runnable r)
		{
			this.source = source;
			this.destination = destination;
			this.task = task;
			task.run(r);
		}

		@Override
		public Location source() {
			return source;
		}

		@Override
		public Location destination() {
			return destination;
		}

		@Override
		public Task task() {
			return task;
		}
	}
}
