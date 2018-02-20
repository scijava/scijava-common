/*
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

package org.scijava.io.location;

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
	 * Turns the given string into an {@link URI}, then resolves it to a
	 * {@link Location}
	 * 
	 * @param uri the uri to resolve
	 * @return the resolved {@link Location}
	 * @throws URISyntaxException if the URI is malformed
	 */
	default Location resolve(final String uri) throws URISyntaxException {
		return resolve(new URI(uri));
	}

	/**
	 * Resolves the given {@link URI} to a location.
	 * 
	 * @param uri the uri to resolve
	 * @return the resolved {@link Location} or <code>null</code> if no resolver
	 *         could be found.
	 * @throws URISyntaxException if the URI is malformed
	 */
	default Location resolve(final URI uri) throws URISyntaxException {
		final LocationResolver resolver = getResolver(uri);
		return resolver != null ? resolver.resolve(uri) : null;
	}

	/**
	 * Returns a {@link LocationResolver} capable of resolving URL like the one
	 * provided to this method. Allows faster repeated resolving of similar URIs
	 * without going through this service.
	 * 
	 * @param uri the uri
	 * @return the {@link LocationResolver} for this uri type, or
	 *         <code>null</code> if no resolver could be found.
	 */
	LocationResolver getResolver(URI uri);

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
