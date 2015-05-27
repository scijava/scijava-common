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

package org.scijava.script.io;

import java.io.IOException;

import org.scijava.io.AbstractIOPlugin;
import org.scijava.io.FileLocation;
import org.scijava.io.IOPlugin;
import org.scijava.io.Location;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.script.ScriptService;

/**
 * {@link IOPlugin} for scripts.
 * 
 * @author Curtis Rueden
 * @see ScriptService
 */
@Plugin(type = IOPlugin.class)
public class ScriptIOPlugin extends AbstractIOPlugin<String> {

	@Parameter(required = false)
	private ScriptService scriptService;

	// -- IOPlugin methods --

	@Override
	public Class<String> getDataType() {
		return String.class;
	}

	@Override
	public boolean supportsOpen(final Location source) {
		if (scriptService == null) return false; // no service for opening scripts
		// TODO: Update ScriptService to use Location instead of File.
		if (!(source instanceof FileLocation)) return false;
		final FileLocation loc = (FileLocation) source;
		return scriptService.canHandleFile(loc.getFile());
	}

	@Override
	public String open(final Location source) throws IOException {
		if (scriptService == null) return null; // no service for opening scripts
		// TODO: Use the script service to open the file in the script editor.
		return null;
	}

}
