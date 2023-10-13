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

import org.scijava.log.Logger;
import org.scijava.plugin.HandlerService;
import org.scijava.service.SciJavaService;

import java.awt.*;
import java.net.URI;
import java.util.Optional;

/**
 * Service interface for handling URIs.
 *
 * @author Curtis Rueden
 * @see LinkHandler
 */
public interface LinkService extends HandlerService<URI, LinkHandler>,
    SciJavaService
{

    default void handle(final URI uri) {
        // Find the highest-priority link handler plugin which matches, if any.
        final Optional<LinkHandler> match = getInstances().stream() //
            .filter(handler -> handler.supports(uri)) //
            .findFirst();
        if (!match.isPresent()) {
            // No appropriate link handler plugin was found.
            final Logger log = log();
            if (log != null) log.debug("No handler for URI: " + uri);
            return; // no handler for this URI
        }
        // Handle the URI using the matching link handler.
        match.get().handle(uri);
    }

    // -- PTService methods --

    @Override
    default Class<LinkHandler> getPluginType() {
        return LinkHandler.class;
    }

    // -- Service methods --

    @Override
    default void initialize() {
        HandlerService.super.initialize();
        // Register URI handler with the desktop system, if possible.
        if (!Desktop.isDesktopSupported()) return;
        final Desktop desktop = Desktop.getDesktop();
        if (!desktop.isSupported(Desktop.Action.APP_OPEN_URI)) return;
        desktop.setOpenURIHandler(event -> handle(event.getURI()));
    }

    // -- Typed methods --

    @Override
    default Class<URI> getType() {
        return URI.class;
    }
}
