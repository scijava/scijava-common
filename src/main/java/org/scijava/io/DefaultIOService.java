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

import java.io.IOException;

import org.scijava.event.EventService;
import org.scijava.io.event.DataOpenedEvent;
import org.scijava.io.event.DataSavedEvent;
import org.scijava.log.LogService;
import org.scijava.plugin.AbstractHandlerService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.service.Service;

/**
 * Default implementation of {@link IOService}.
 * 
 * @author Curtis Rueden
 */
@Plugin(type = Service.class)
public final class DefaultIOService
	extends AbstractHandlerService<String, IOPlugin<?>> implements IOService
{

	@Parameter
	private LogService log;

	@Parameter
	private EventService eventService;

	// -- IOService methods --

	@Override
	public Object open(final String source) throws IOException {
		final IOPlugin<?> opener = getOpener(source);
		if (opener == null) return null; // no appropriate IOPlugin

		final Object data = opener.open(source);
		if (data == null) return null; // IOPlugin returned no data; canceled?

		eventService.publish(new DataOpenedEvent(source, data));
		return data;
	}

	@Override
	public void save(final Object data, final String destination)
		throws IOException
	{
		final IOPlugin<Object> saver = getSaver(data, destination);
		if (saver != null) {
			saver.save(data, destination);
			eventService.publish(new DataSavedEvent(destination, data));
		}
	}
}
