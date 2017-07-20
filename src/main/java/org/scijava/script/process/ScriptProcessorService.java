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

package org.scijava.script.process;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.scijava.plugin.PTService;
import org.scijava.script.ScriptInfo;
import org.scijava.service.SciJavaService;

/**
 * Interface for service that processes scripts. This service discovers
 * available {@link ScriptProcessor} plugins, and provides convenience methods
 * to interact with them.
 * 
 * @author Curtis Rueden
 */
public interface ScriptProcessorService extends
	PTService<ScriptProcessor>, SciJavaService
{

	/**
	 * Invokes all {@link ScriptProcessor} plugins on the given script, line by
	 * line in sequence.
	 */
	default String process(final ScriptInfo info) throws IOException {
		final List<ScriptProcessor> processors = getPlugins().stream().map(
			p -> pluginService().createInstance(p)).collect(Collectors.toList());

		BufferedReader reader = info.getReader();
		if (reader == null) {
			reader = new BufferedReader(new FileReader(info.getPath()));
		}

		for (final ScriptProcessor p : processors) {
			p.begin(info);
		}

		final StringBuilder sb = new StringBuilder();

		try (final BufferedReader in = reader) {
			while (true) {
				String line = in.readLine();
				if (line == null) break;
				for (final ScriptProcessor p : processors) {
					line = p.process(line);
				}
				sb.append(line);
				sb.append("\n");
			}
		}

		for (final ScriptProcessor p : processors) {
			p.end();
		}

		return sb.toString();
	}

	// -- PTService methods --

	@Override
	default Class<ScriptProcessor> getPluginType() {
		return ScriptProcessor.class;
	}
}
