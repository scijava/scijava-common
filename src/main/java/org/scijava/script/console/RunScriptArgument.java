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
package org.scijava.script.console;

import java.io.File;
import java.util.LinkedList;
import java.util.Map;

import org.scijava.console.AbstractConsoleArgument;
import org.scijava.console.ConsoleArgument;
import org.scijava.console.ConsoleUtils;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.script.ScriptInfo;
import org.scijava.script.ScriptService;

/**
 * @deprecated Use {@link org.scijava.run.console.RunArgument} instead.
 */
@Deprecated
@Plugin(type = ConsoleArgument.class)
public class RunScriptArgument extends AbstractConsoleArgument {

	@Parameter
	private ScriptService scriptService;

	@Parameter
	private LogService log;

	// -- Constructor --

	public RunScriptArgument() {
		super(2, "--script");
	}

	// -- ConsoleArgument methods --

	@Override
	public void handle(final LinkedList<String> args) {
		if (!supports(args))
			return;

		log.warn("The --script flag is deprecated, and will\n" +
			"be removed in a future release. Use --run instead.");

		args.removeFirst(); // --script
		final String scriptToRun = args.removeFirst();
		final String paramString = ConsoleUtils.hasParam(args) ? args.removeFirst() : "";

		run(scriptToRun, paramString);
	}

	// -- Typed methods --

	@Override
	public boolean supports(final LinkedList<String> args) {
		if (!super.supports(args))
			return false;
		return getScript(args.get(1)) != null;
	}

	// -- Helper methods --

	/**
	 * Run the script
	 */
	private void run(final String scriptToRun, final String paramString) {
		final File script = getScript(scriptToRun);

		if (script == null) {
			// couldn't find anything to run
			throw new UnsupportedOperationException(//
				"Not a script: '" + scriptToRun + "'");
		}

		final ScriptInfo info = scriptService.getScript(script);

		final Map<String, Object> inputMap = ConsoleUtils.parseParameterString(paramString, info, log);

		try {
			scriptService.run(info, true, inputMap).get();
		} catch (final Exception exc) {
			log.error(exc);
		}
	}

	/**
	 * Try to convert the given string to a {@link File} representing a
	 * supported script type.
	 */
	private File getScript(final String string) {
		final File scriptFile = new File(string);
		return scriptFile.exists() && scriptService.canHandleFile(scriptFile) ? scriptFile : null;
	}
}
