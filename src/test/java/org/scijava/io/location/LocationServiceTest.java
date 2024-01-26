/*
 * #%L
 * SciJava Common shared library for SciJava software.
 * %%
 * Copyright (C) 2009 - 2024 SciJava developers.
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

import org.junit.Test;
import org.scijava.Context;

/**
 * Tests {@link LocationService}.
 * 
 * @author Gabriel Einsdorf
 */
public class LocationServiceTest {

	@Test
	public void testResolve() throws URISyntaxException {
		final Context ctx = new Context(LocationService.class);
		final LocationService loc = ctx.getService(LocationService.class);

		final URI uri = new File(new File(".").getAbsolutePath()).toURI();
		final LocationResolver res = loc.getHandler(uri);

		assertTrue(res instanceof FileLocationResolver);
		assertEquals(uri, res.resolve(uri).getURI());
		assertEquals(uri, loc.resolve(uri).getURI());
		assertEquals(uri, loc.resolve(uri.toString()).getURI());
	}

	@Test
	public void testFallBack() throws URISyntaxException {
		final Context ctx = new Context(LocationService.class);
		final LocationService loc = ctx.getService(LocationService.class);

		final String uri = new File(".").getAbsolutePath();
		final Location res = loc.resolve(uri);

		assertTrue(res instanceof FileLocation);
		FileLocation resFile = (FileLocation) res;
		assertEquals(uri, resFile.getFile().getAbsolutePath());
	}

}
