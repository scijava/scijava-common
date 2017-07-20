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

package org.scijava.console;

import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.scijava.plugin.Plugin;

/**
 * Handles the {@code -D} command line argument, in an analogous way to tools
 * such as Java and Maven.
 * 
 * @author Curtis Rueden
 */
@Plugin(type = ConsoleArgument.class)
public class SystemPropertyArgument extends AbstractConsoleArgument {

	private static final String SYS_PROP_REGEX = "-D([\\w\\._-]+)(=(.*))?";
	private static final Pattern SYS_PROP_PAT = Pattern.compile(SYS_PROP_REGEX);

	// -- Constructor --

	public SystemPropertyArgument() {
		super(1);
	}

	// -- ConsoleArgument methods --

	@Override
	public void handle(final LinkedList<String> args) {
		if (!supports(args)) return;

		final String arg = args.removeFirst();
		final Matcher m = SYS_PROP_PAT.matcher(arg);
		if (!m.matches()) {
			throw new IllegalArgumentException("Invalid argument: " + arg);
		}
		final String key = m.group(1);
		final String value = m.group(3);
		System.setProperty(key, value == null ? "" : value);
	}

	// -- Typed methods --

	@Override
	public boolean supports(final LinkedList<String> args) {
		if (!super.supports(args)) return false;
		final String arg = args.getFirst();
		if (!arg.startsWith("-D")) return false;
		return SYS_PROP_PAT.matcher(arg).matches();
	}


}
