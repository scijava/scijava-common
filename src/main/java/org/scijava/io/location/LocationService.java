/*
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

package org.scijava.io.location;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

import org.scijava.plugin.HandlerService;
import org.scijava.service.SciJavaService;

/**
 * A service that allows resolving of URIs to Locations, using
 * {@link LocationResolver} plugins for translation.
 * 
 * @author Gabriel Einsdorf
 */
public interface LocationService extends HandlerService<URI, LocationResolver>,
	SciJavaService
{

	/**
	 * Turns the given string into a {@link URI}, then resolves it to a
	 * {@link Location}.
	 *
	 * @param uriString the uri to resolve
	 * @return the resolved {@link Location}
	 * @throws URISyntaxException if the URI is malformed
	 */
	default Location resolve(final String uriString) throws URISyntaxException {
	    try {
	        return resolve(new URI(uriString));
	    }
	    catch (final URISyntaxException exc) {
	        // In general, filenames are not valid URI strings.
	        // Particularly on Windows, there are backslashes, which are invalid in URIs.
	        // So we explicitly turn this string into a file if an error happens above.
	        return resolve(new File(uriString).toURI());
	    }
	}

	/**
	 * Resolves the given {@link URI} to a location. If the {@code scheme} part of
	 * the URI is {@code null} the path component is resolved as a local file.
	 *
	 * @param uri the uri to resolve
	 * @return the resolved {@link Location} or <code>null</code> if no resolver
	 *         could be found.
	 * @throws URISyntaxException if the URI is malformed
	 */
	default Location resolve(URI uri) throws URISyntaxException {
		if (uri.getScheme() == null) { // Fallback for local files
			uri = new File(uri.getPath()).toURI();
		}
		final LocationResolver resolver = getResolver(uri);
		return resolver != null ? resolver.resolve(uri) : null;
	}

	/** @deprecated Use {@link #getHandler} instead. */
	@Deprecated
	default LocationResolver getResolver(URI uri) {
		return getHandler(uri);
	}

	// -- PTService methods --

	@Override
	default Class<LocationResolver> getPluginType() {
		return LocationResolver.class;
	}

	// -- Typed methods --

	@Override
	default Class<URI> getType() {
		return URI.class;
	}
}
