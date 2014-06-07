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

package org.scijava.text.io;

import java.io.IOException;

import org.scijava.Priority;
import org.scijava.io.AbstractIOPlugin;
import org.scijava.io.FileLocation;
import org.scijava.io.IOPlugin;
import org.scijava.io.Location;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.text.TextService;

/**
 * {@link IOPlugin} for text conversion to HTML.
 * 
 * @author Curtis Rueden
 * @see TextService
 */
@Plugin(type = IOPlugin.class, priority = Priority.LOW_PRIORITY - 1)
public class TextIOPlugin extends AbstractIOPlugin<String> {

	@Parameter(required = false)
	private TextService textService;

	// -- IOPlugin methods --

	@Override
	public Class<String> getDataType() {
		return String.class;
	}

	@Override
	public boolean supportsOpen(final Location source) {
		if (textService == null) return false; // no service for opening text files
		if (!(source instanceof FileLocation)) return false;
		final FileLocation loc = (FileLocation) source;
		return textService.supports(loc.getFile());
	}

	@Override
	public String open(final Location source) throws IOException {
		if (textService == null) return null; // no service for opening text files
		if (!(source instanceof FileLocation)) throw new IllegalArgumentException();
		final FileLocation loc = (FileLocation) source;
		return textService.asHTML(loc.getFile());
	}

}
