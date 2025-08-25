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

package org.scijava.io;

import org.scijava.io.location.Location;
import org.scijava.io.location.LocationService;
import org.scijava.plugin.AbstractHandlerPlugin;
import org.scijava.plugin.Parameter;

import java.io.IOException;
import java.net.URISyntaxException;

/**
 * Abstract base class for {@link IOPlugin}s.
 *
 * @author Curtis Rueden
 */
public abstract class AbstractIOPlugin<D> extends
	AbstractHandlerPlugin<Location> implements IOPlugin<D>
{

	@Parameter
	private LocationService locationService;

	@Override
	public boolean supportsOpen(final String source) {
		try {
			return supportsOpen(locationService.resolve(source));
		}
		catch (URISyntaxException e) {
			return false;
		}
	}

	@Override
	public boolean supportsSave(final String destination) {
		try {
			return supportsSave(locationService.resolve(destination));
		}
		catch (URISyntaxException e) {
			return false;
		}
	}

	@Override
	public void save(final D data, final String destination) throws IOException {
		try {
			save(data, locationService.resolve(destination));
		}
		catch (URISyntaxException e) {
			throw new IOException(e);
		}
	}

	@Override
	public D open(final String destination) throws IOException {
		try {
			return open(locationService.resolve(destination));
		}
		catch (URISyntaxException e) {
			throw new IOException(e);
		}
	}

}
