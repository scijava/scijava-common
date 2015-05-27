/*
 * #%L
 * SciJava Common shared library for SciJava software.
 * %%
 * Copyright (C) 2009 - 2015 Board of Regents of the University of
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

import java.io.IOException;

import org.scijava.plugin.AbstractHandlerPlugin;

/**
 * Abstract base class for {@link IOPlugin}s.
 * 
 * @author Curtis Rueden
 */
public abstract class AbstractIOPlugin<D> extends
	AbstractHandlerPlugin<Location> implements IOPlugin<D>
{

	// -- IOPlugin methods --

	@Override
	public boolean supportsOpen(final Location source) {
		return false;
	}

	@Override
	public boolean supportsSave(final Location destination) {
		return false;
	}

	@Override
	public boolean supportsSave(final Object data, final Location destination) {
		return supportsSave(destination) && getDataType().isInstance(data);
	}

	@Override
	public D open(final Location source) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void save(final D data, final Location destination) throws IOException {
		throw new UnsupportedOperationException();
	}

	// -- Typed methods --

	@Override
	public boolean supports(final Location location) {
		return supportsOpen(location) || supportsSave(location);
	}

	@Override
	public Class<Location> getType() {
		return Location.class;
	}

}
