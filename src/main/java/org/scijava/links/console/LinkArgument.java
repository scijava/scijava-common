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
package org.scijava.links.console;

import org.scijava.Priority;
import org.scijava.console.AbstractConsoleArgument;
import org.scijava.console.ConsoleArgument;
import org.scijava.links.LinkService;
import org.scijava.log.Logger;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedList;

/**
 * A {@link ConsoleArgument} plugin to handle URIs passed to the application via the command line.
 *
 * @author Curtis Rueden
 */
@Plugin(type = ConsoleArgument.class, priority = Priority.VERY_HIGH)
public class LinkArgument extends AbstractConsoleArgument {

    @Parameter(required = false)
    private LinkService linkService;

    // -- ConsoleArgument methods --

    @Override
    public void handle(final LinkedList<String> args) {
        if (linkService == null) return; // no service to handle links
        if (args.isEmpty()) return; // no argument to check
        final URI uri = link(args.getFirst());
        if (uri == null) return; // not a URI
        linkService.handle(uri);
    }

    // -- Typed methods --

    @Override
    public boolean supports(final LinkedList<String> args) {
        return !args.isEmpty() && link(args.getFirst()) != null;
    }

    /**
     * Parses a string into a URI, or null if the string
     * does not constitute such a link.
     *
     * @param s The string to parse.
     * @return The URI, or null.
     */
    private URI link(final String s) {
        try {
            URI uri = new URI(s);
            return uri.getScheme() == null ? null : uri;
        }
        catch (final URISyntaxException e) {
            final Logger log = log();
            if (log != null) log.debug(e);
            return null;
        }
    }
}
