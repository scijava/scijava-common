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

import javax.script.ScriptEngineFactory;

import org.scijava.plugin.AbstractRichPlugin;
import org.scijava.plugin.PluginInfo;

/**
 * Abstract superclass for {@link ScriptLanguage} implementations.
 * <p>
 * This class implements dummy versions of {@link ScriptEngineFactory}'s methods
 * that are not needed by the SciJava scripting framework.
 * </p>
 * 
 * @author Johannes Schindelin
 */
public abstract class AbstractScriptLanguage extends AbstractRichPlugin
	implements ScriptLanguage
{

	// -- Object methods --

	@Override
	public String toString() {
		return getLanguageName();
	}

	// -- Default implementations --

	@Override
	public String getEngineName() {
		return inferNameFromClassName();
	}

	@Override
	public String getLanguageName() {
		String name = null;
		final PluginInfo<?> info = getInfo();
		if (info != null) name = info.getName();
		return name != null && !name.isEmpty() ? name : inferNameFromClassName();
	}

	// -- Helper methods --

	private String inferNameFromClassName() {
		String className = getClass().getSimpleName();
		if (className.endsWith("ScriptLanguage")) {
			// strip off "ScriptLanguage" suffix
			className = className.substring(0, className.length() - 14);
		}
		// replace underscores with spaces
		className = className.replace('_', ' ');
		return className;
	}

}
