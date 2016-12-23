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

package org.scijava.script;

import org.scijava.plugin.HandlerService;
import org.scijava.service.SciJavaService;

/**
 * Interface for a {@link HandlerService} capable of mapping
 * {@link ScriptLanguage}s to {@link ScriptHeader}s.
 * <p>
 * NB: although individual/highest priority handlers can be queried as normal
 * via a {@code HandlerService}, the
 * {@link #getHeader(ScriptLanguage)} method will combine the headers
 * for all available {@code ScriptHeader}s for a given {@code ScriptLanguage}.
 * </p>
 *
 * @author Mark Hiner
 */
public interface ScriptHeaderService extends
	HandlerService<ScriptLanguage, ScriptHeader>, SciJavaService
{

	/**
	 * Searches for all {@link ScriptHeader}s capable of handling the given
	 * {@link ScriptLanguage} and combines the result of their
	 * {@link ScriptHeader#getHeader()} output to a single string.
	 *
	 * @param language - Language to look up
	 * @return The combined header text to insert at the top of a script.
	 */
	default String getHeader(final ScriptLanguage language) {
		StringBuilder header = new StringBuilder();
		for (final ScriptHeader scriptHeader : getInstances()) {
			if (scriptHeader.supports(language)) {
				header.append(scriptHeader.getHeader());
				header.append("\n");
			}
		}

		return header.toString();
	}

	// -- HandlerService methods --

	@Override
	default Class<ScriptHeader> getPluginType() {
		return ScriptHeader.class;
	}

	@Override
	default Class<ScriptLanguage> getType() {
		return ScriptLanguage.class;
	}
}
