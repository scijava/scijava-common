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

import java.util.LinkedHashMap;
import java.util.Map;

import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.script.ScriptImport;

/**
 * A {@link ScriptProcessor} which parses the {@code #@import} directive.
 * <p>
 * The syntax is:
 * </p>
 * 
 * <pre>
 * #@import("myutils", scope="util")
 * #@import("utils")
 * </pre>
 * 
 * @author Jan Eglinger
 */
@Plugin(type = ScriptProcessor.class)
public class ImportDirectiveScriptProcessor extends DirectiveScriptProcessor {

	public ImportDirectiveScriptProcessor() {
		super(directive -> "import".equals(directive));
	}

	@Parameter
	private LogService log;

	// -- Internal DirectiveScriptProcessor methods --

	@Override
	protected String process(final String directive, final Map<String, Object> attrs, final String theRest) {
		String importItem = null;
		String scope = null;
		for (final String k : attrs.keySet()) {
			if (k == null || is(k, "name"))
				importItem = as(attrs.get(k), String.class);
			else if (is(k, "scope"))
				scope = as(attrs.get(k), String.class);
			else
				throw new IllegalArgumentException("Wrong import argument");
		}
		if (importItem != null)
			addImport(importItem, scope);
		else
			throw new IllegalArgumentException("No import name provided");
		return "";
	}

	// -- Helper methods --

	private void addImport(String importItem, String scope) {
		@SuppressWarnings("unchecked")
		Map<String, ScriptImport> map = (Map<String, ScriptImport>) info().getProperty("imports");
		if (map==null) map = new LinkedHashMap<>();
		map.put(importItem, new ScriptImport(scope));
		info().setProperty("imports", map);
	}
}
