/*
 * #%L
 * SciJava Common shared library for SciJava software.
 * %%
 * Copyright (C) 2009 - 2023 SciJava developers.
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

import org.scijava.app.StatusService;
import org.scijava.event.EventHandler;
import org.scijava.plugin.AbstractHandlerService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.service.Service;
import org.scijava.ui.dnd.event.DragEnterEvent;
import org.scijava.ui.dnd.event.DragExitEvent;
import org.scijava.ui.dnd.event.DragOverEvent;
import org.scijava.ui.dnd.event.DropEvent;

/**
 * Default service for handling drag and drop events.
 * 
 * @author Curtis Rueden
 */
@Plugin(type = Service.class)
public class DefaultDragAndDropService extends
	AbstractHandlerService<Object, DragAndDropHandler<Object>> implements
	DragAndDropService
{

	private static final String SUPPORTED = "Drag and Drop";
	private static final String UNSUPPORTED = "Unsupported Object";

	@Parameter
	private StatusService statusService;

	// -- Event handlers --

	@EventHandler
	protected void onEvent(final DragEnterEvent e) {
		// determine whether the given drop operation is supported
		final boolean compatible = supports(e.getData(), e.getDisplay());

		// update the status accordingly
		final String message = compatible ? SUPPORTED : UNSUPPORTED;
		statusService.showStatus("< <" + message + "> >");

		// accept the drag if the operation is supported
		if (compatible) e.setAccepted(true);
	}

	@EventHandler
	protected void onEvent(@SuppressWarnings("unused") final DragExitEvent e) {
		statusService.clearStatus();
	}

	@EventHandler
	protected void onEvent(@SuppressWarnings("unused") final DragOverEvent e) {
		// NB: No action needed.
	}

	@EventHandler
	protected void onEvent(final DropEvent e) {
		if (!supports(e.getData(), e.getDisplay())) return;

		// perform the drop
		final boolean success = drop(e.getData(), e.getDisplay());
		e.setSuccessful(success);
	}

}
