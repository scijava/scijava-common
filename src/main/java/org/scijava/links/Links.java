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

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Utility class for working with {@link URI} objects.
 *
 * @author Curtis Rueden
 */
public final class Links {
    private Links() {
        // NB: Prevent instantiation of utility class.
    }

    public static String path(final URI uri) {
        final String path = uri.getPath();
        if (path == null) return null;
        return path.startsWith("/") ? path.substring(1) : path;
    }

    public static String operation(final URI uri) {
        final String path = path(uri);
        if (path == null) return null;
        final int slash = path.indexOf("/");
        return slash < 0 ? path : path.substring(0, slash);
    }

    public static String[] pathFragments(final URI uri) {
        final String path = path(uri);
        if (path == null) return null;
        return path.isEmpty() ? new String[0] : path.split("/");
    }

    public static String subPath(final URI uri) {
        final String path = path(uri);
        if (path == null) return null;
        final int slash = path.indexOf("/");
        return slash < 0 ? "" : path.substring(slash + 1);
    }

    public static Map<String, String> query(final URI uri) {
        final LinkedHashMap<String, String> map = new LinkedHashMap<>();
        final String query = uri.getQuery();
        final String[] tokens = query == null ? new String[0] : query.split("&");
        for (final String token : tokens) {
            final String[] kv = token.split("=", 2);
            final String k = kv[0];
            final String v = kv.length > 1 ? kv[1] : null;
            map.put(k, v);
        }
        return map;
    }
}
