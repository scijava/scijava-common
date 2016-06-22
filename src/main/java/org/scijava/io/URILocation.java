/*
 * #%L
 * SciJava Common shared library for SciJava software.
 * %%
 * Copyright (C) 2009 - 2016 Board of Regents of the University of
 * Wisconsin-Madison, Broad Institute of MIT and Harvard, and Max Planck
 * Institute of Molecular Cell Biology and Genetics.
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

package org.scijava.io;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.util.LinkedHashMap;
import java.util.Map;

import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;

/**
 * {@link Location} backed by a {@link URI} string.
 * 
 * @author Curtis Rueden
 */
public class URILocation extends AbstractLocation {

	@Parameter
	private LogService log;

	private final URI uri;

	public URILocation(final URI uri) {
		this.uri = uri;
	}

	public URILocation(final String uriPath) throws URISyntaxException {
		this(new URI(uriPath));
	}

	// -- URILocation methods --

	public Map<String, String> getQueryMap() {
		return decodeQuery(getURI().getRawQuery());
	}

	public String getQueryValue(final String key) {
		return getQueryMap().get(key);
	}

	// FIXME: look up whether anyone has created a mutatable URI class,
	// with individual setters for the various parts. Otherwise, we'll
	// have to handle it here!

	// -- Location methods --

	@Override
	public URI getURI() {
		return uri;
	}

	// -- Helper methods --

	/**
	 * Decodes a query string of ampersand-separated key/value pairs. E.g.:
	 * {@code apples=yummy&bananas=delicious&grapefruits=scrumptious}.
	 * 
	 * @param query The query string to decode.
	 * @return A map of the decoded key/value pairs.
	 */
	private Map<String, String> decodeQuery(final String query) {
		final Map<String, String> map = new LinkedHashMap<>();
		if (query == null) return map;
		for (final String param : query.split("&")) {
			final int equals = param.indexOf("=");
			if (equals < 0) {
				map.put(decode(param), "true");
			}
			else {
				final String key = decode(param.substring(0, equals));
				final String value = decode(param.substring(equals + 1));
				map.put(key, value);
			}
		}
		return map;
	}

	/**
	 * Decodes a single uuencoded string.
	 * 
	 * @see URLDecoder
	 */
	private String decode(final String s) {
		// http://stackoverflow.com/a/6926987
		try {
			return URLDecoder.decode(s.replace("+", "%2B"), "UTF-8");
		}
		catch (UnsupportedEncodingException exc) {
			return null;
		}
	}

}
