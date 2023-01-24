/*
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

package org.scijava.io;

import org.scijava.io.location.Location;
import org.scijava.io.location.LocationService;
import org.scijava.plugin.AbstractHandlerService;
import org.scijava.plugin.Parameter;

import java.io.IOException;
import java.net.URISyntaxException;

/**
 * Abstract base class for typed {@link IOPlugin}s.
 * 
 * @author Curtis Rueden
 * @author Deborah Schmidt
 */
public abstract class AbstractTypedIOService<D> extends AbstractHandlerService<Location, IOPlugin<D>> implements TypedIOService<D>
{

	@Parameter
	private LocationService locationService;

	@Parameter
	private IOService ioService;

	@Override
	public D open(String source) throws IOException {
		try {
			return open(locationService.resolve(source));
		} catch (URISyntaxException e) {
			throw new IOException(e);
		}
	}

	@Override
	public D open(Location source) throws IOException {
		IOPlugin<?> opener = ioService().getOpener(source);
		try {
			Class<D> ignored = (Class<D>) opener.getDataType();
			return (D) opener.open(source);
		}
		catch(ClassCastException e) {
			throw new UnsupportedOperationException("No compatible opener found.");
		}
	}

	@Override
	public void save(D data, String destination) throws IOException {
		try {
			save(data, locationService.resolve(destination));
		} catch (URISyntaxException e) {
			throw new IOException(e);
		}
	}

	@Override
	public void save(D data, Location destination) throws IOException {
		IOPlugin<D> saver = ioService().getSaver(data, destination);
		if (saver != null) {
			saver.save(data, destination);
		}
		else {
			throw new UnsupportedOperationException("No compatible saver found.");
		}
	}

	@Override
	public boolean canOpen(String source) {
		try {
			return canOpen(locationService.resolve(source));
		} catch (URISyntaxException e) {
			return false;
		}
	}

	@Override
	public boolean canOpen(Location source) {
		IOPlugin<?> opener = ioService().getOpener(source);
		if (opener == null) return false;
		try {
			Class<D> ignored = (Class<D>) (opener.getDataType());
			return true;
		} catch(ClassCastException e) {
			return false;
		}
	}

	@Override
	public boolean canSave(D data, String source) {
		try {
			return canSave(data, locationService.resolve(source));
		} catch (URISyntaxException e) {
			return false;
		}
	}

	@Override
	public boolean canSave(D data, Location destination) {
		IOPlugin<D> saver = ioService.getSaver(data, destination);
		if (saver == null) return false;
		return saver.supportsSave(destination);
	}

	protected LocationService locationService() {
		return locationService;
	}

	protected IOService ioService() {
		return ioService;
	}
}
