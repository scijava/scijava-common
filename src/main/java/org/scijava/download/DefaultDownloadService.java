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

import org.scijava.io.handle.DataHandle;
import org.scijava.io.handle.DataHandleService;
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
		final Download download = new Download() {

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
		};
		task.run(() -> {
			try (final DataHandle<Location> in = dataHandleService.create(source);
					final DataHandle<Location> out = dataHandleService.create(
						destination))
			{
				task.setStatusMessage("Downloading " + source.getURI());
				copy(task, in, out);
			}
			catch (final IOException exc) {
				// TODO: Improve error handling:
				// 1. Consider a better exception handling design here.
				// 2. Retry at least a few times if something goes wrong.
				throw new RuntimeException(exc);
			}
		});
		return download;
	}

	// -- Helper methods --

	private void copy(final Task task, final DataHandle<Location> in,
		final DataHandle<Location> out) throws IOException
	{
		long length;
		try {
			length = in.length();
		}
		catch (final IOException exc) {
			// Assume unknown length.
			length = 0;
		}
		if (length > 0) task.setProgressMaximum(length);

		final int chunkSize = 64 * 1024; // TODO: Make size configurable.
		final byte[] buf = new byte[chunkSize];
		while (true) {
			if (task.isCanceled()) return;
			final int r = in.read(buf);
			if (r <= 0) break; // EOF
			if (task.isCanceled()) return;
			out.write(buf, 0, r);
			if (length > 0) task.setProgressValue(task.getProgressValue() + r);
		}
	}
}
