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

package org.scijava.script.process;

import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.script.ScriptInfo;
import org.scijava.script.ScriptLanguage;
import org.scijava.script.ScriptService;

/**
 * A {@link ScriptProcessor} which looks for a {@code #!} at the beginning of a
 * script, and set the language accordingly.
 * 
 * @author Curtis Rueden
 */
@Plugin(type = ScriptProcessor.class)
public class ShebangScriptProcessor implements ScriptProcessor {

	@Parameter
	private ScriptService scriptService;

	@Parameter
	private LogService log;

	private ScriptInfo info;
	private boolean first = true;

	// -- ScriptProcessor methods --

	@Override
	public void begin(final ScriptInfo scriptInfo) {
		info = scriptInfo;
	}

	@Override
	public String process(final String line) {
		if (!first) return line;
		first = false;
		if (line.startsWith("#!")) {
			// shebang!
			final String langName = line.substring(2);
			final ScriptLanguage lang = scriptService.getLanguageByName(langName);
			if (lang != null) info.setLanguage(lang);
			else log.warn("Unknown script language: " + langName);
			return "";
		}
		return line;
	}
}
