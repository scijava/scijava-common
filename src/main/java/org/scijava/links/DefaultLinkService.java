/*-
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
package org.scijava.links;

import org.scijava.event.ContextCreatedEvent;
import org.scijava.event.EventHandler;
import org.scijava.plugin.AbstractHandlerService;
import org.scijava.plugin.Plugin;
import org.scijava.service.Service;

import java.awt.Desktop;
import java.net.URI;

/**
 * Default implementation of {@link LinkService}.
 *
 * @author Curtis Rueden
 */
@Plugin(type = Service.class)
public class DefaultLinkService extends AbstractHandlerService<URI, LinkHandler> implements LinkService {

	@EventHandler
	private void onEvent(final ContextCreatedEvent evt) {
		// Register URI handler with the desktop system, if possible.
		if (!Desktop.isDesktopSupported()) return;
		final Desktop desktop = Desktop.getDesktop();
		if (!desktop.isSupported(Desktop.Action.APP_OPEN_URI)) return;
		desktop.setOpenURIHandler(event -> handle(event.getURI()));
	}

}
