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

package org.scijava.main.console;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.scijava.console.AbstractConsoleArgument;
import org.scijava.console.ConsoleArgument;
import org.scijava.log.LogService;
import org.scijava.main.MainService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 * Handles the {@code --main} command line argument, which launches an
 * alternative main class.
 * 
 * @author Curtis Rueden
 */
@Plugin(type = ConsoleArgument.class)
public class MainArgument extends AbstractConsoleArgument {

	@Parameter(required = false)
	private MainService mainService;

	@Parameter(required = false)
	private LogService log;

	// -- Constructor --

	public MainArgument() {
		super(2, "--main", "--main-class");
	}

	// -- ConsoleArgument methods --

	@Override
	public void handle(final LinkedList<String> args) {
		if (!supports(args)) return;

		args.removeFirst(); // --main / --main-class
		final String className = args.removeFirst();

		final List<String> argList = new ArrayList<>();
		while (!args.isEmpty() && !isFlag(args) && !isSeparator(args)) {
			argList.add(args.removeFirst());
		}
		if (isSeparator(args)) args.removeFirst(); // remove the -- separator
		final String[] mainArgs = argList.toArray(new String[argList.size()]);

		mainService.addMain(className, mainArgs);
	}

	// -- Typed methods --

	@Override
	public boolean supports(final LinkedList<String> args) {
		return mainService != null && super.supports(args);
	}

	// -- Helper methods --

	private boolean isSeparator(final LinkedList<String> args) {
		if (args == null || args.isEmpty()) return false;
		return args.getFirst().equals("--");
	}

}
