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

package org.scijava.ui.dnd;

import java.io.File;
import java.io.IOException;

import org.scijava.Priority;
import org.scijava.display.Display;
import org.scijava.display.DisplayService;
import org.scijava.io.IOService;
import org.scijava.io.location.FileLocation;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 * Drag-and-drop handler for files.
 * 
 * @author Curtis Rueden
 * @author Barry DeZonia
 */
@Plugin(type = DragAndDropHandler.class, priority = Priority.LOW)
public class FileDragAndDropHandler extends
	AbstractDragAndDropHandler<File>
{

	@Parameter(required = false)
	private IOService ioService;

	@Parameter(required = false)
	private DisplayService displayService;

	@Parameter(required = false)
	private LogService log;

	// -- DragAndDropHandler methods --

	@Override
	public boolean supports(final File file) {
		if (ioService == null || displayService == null) return false;
		if (!super.supports(file)) return false;

		// verify that the file can be opened somehow
		final FileLocation loc = new FileLocation(file);
		return ioService.getOpener(loc) != null;
	}

	@Override
	public boolean drop(final File file, final Display<?> display) {
		if (ioService == null || displayService == null) return false;
		check(file, display);
		if (file == null) return true; // trivial case

		// load the data
		final Object data;
		try {
			data = ioService.open(new FileLocation(file));
		}
		catch (final IOException exc) {
			if (log != null) log.error("Error opening file: " + file, exc);
			return false;
		}

		// display the result
		displayService.createDisplay(data);
		return true;
	}

	// -- Typed methods --

	@Override
	public Class<File> getType() {
		return File.class;
	}

}
