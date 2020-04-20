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

package org.scijava.common.run.console;

import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;

import org.scijava.common.console.AbstractConsoleArgument;
import org.scijava.common.console.ConsoleArgument;
import org.scijava.common.log.LogService;
import org.scijava.common.parse.Items;
import org.scijava.common.parse.ParseService;
import org.scijava.common.plugin.Parameter;
import org.scijava.common.plugin.Plugin;
import org.scijava.common.run.RunService;
import org.scijava.common.startup.StartupService;

/**
 * Handles the {@code --run} command line argument.
 *
 * @author Curtis Rueden
 */
@Plugin(type = ConsoleArgument.class)
public class RunArgument extends AbstractConsoleArgument {

	@Parameter
	private RunService runService;

	@Parameter
	private StartupService startupService;

	@Parameter
	private ParseService parser;

	@Parameter
	private LogService log;

	// -- Constructor --

	public RunArgument() {
		super(2, "--run");
	}

	// -- ConsoleArgument methods --

	@Override
	public void handle(final LinkedList<String> args) {
		if (!supports(args)) return;

		args.removeFirst(); // --run
		final String code = args.removeFirst();
		final String arg = getParam(args);
		if (arg != null) args.removeFirst(); // argument list was given

		startupService.addOperation(() -> {
			try {
				if (arg == null) runService.run(code);
				else {
					final Items items = parser.parse(arg);
					if (items.isMap()) runService.run(code, items.asMap());
					else if (items.isList()) runService.run(code, items.toArray());
					else {
						throw new IllegalArgumentException("Arguments are inconsistent. " +
							"Please pass either a list of key/value pairs, " +
							"or a list of values.");
					}
				}
			}
			catch (final InvocationTargetException exc) {
				throw new RuntimeException(exc);
			}
		});
	}

	// -- Typed methods --

	@Override
	public boolean supports(final LinkedList<String> args) {
		if (!super.supports(args)) return false;
		return runService.supports(args.get(1));
	}

}
