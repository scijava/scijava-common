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

package org.scijava.script.run;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import javax.script.ScriptException;

import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.run.AbstractCodeRunner;
import org.scijava.run.CodeRunner;
import org.scijava.script.ScriptService;

/**
 * Runs the given script.
 * 
 * @author Curtis Rueden
 * @author Mark Hiner
 */
@Plugin(type = CodeRunner.class)
public class ScriptCodeRunner extends AbstractCodeRunner {

	@Parameter
	private ScriptService scriptService;

	// -- CodeRunner methods --

	@Override
	public void run(final Object code, final Object... args)
		throws InvocationTargetException
	{
		try {
			waitFor(scriptService.run(getScript(code), true, args));
		}
		catch (final FileNotFoundException exc) {
			throw new InvocationTargetException(exc);
		}
		catch (final ScriptException exc) {
			throw new InvocationTargetException(exc);
		}
	}

	@Override
	public void run(final Object code, final Map<String, Object> inputMap)
		throws InvocationTargetException
	{
		try {
			waitFor(scriptService.run(getScript(code), true, inputMap));
		}
		catch (final FileNotFoundException exc) {
			throw new InvocationTargetException(exc);
		}
		catch (final ScriptException exc) {
			throw new InvocationTargetException(exc);
		}
	}

	// -- Typed methods --

	@Override
	public boolean supports(final Object code) {
		return getScript(code) != null;
	}

	// -- Helper methods --

	private File getScript(final Object code) {
		final File scriptFile;
		if (code instanceof File) scriptFile = (File) code;
		else if (code instanceof String) scriptFile = new File((String) code);
		else return null;

		if (!scriptFile.exists()) return null;
		if (!scriptService.canHandleFile(scriptFile)) return null;

		return scriptFile;
	}

}
