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

package org.scijava.console;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import org.scijava.plugin.AbstractHandlerPlugin;

/**
 * Abstract superclass of {@link ConsoleArgument} implementations.
 *
 * @author Curtis Rueden
 */
public abstract class AbstractConsoleArgument extends
	AbstractHandlerPlugin<LinkedList<String>> implements ConsoleArgument
{
	private int numArgs;
	private Set<String> flags;

	public AbstractConsoleArgument() {
		this(1, new String[0]);
	}

	public AbstractConsoleArgument(final String... flags) {
		this(1, flags);
	}
	
	public AbstractConsoleArgument(final int requiredArgs, final String... flags) {
		numArgs = requiredArgs;
		this.flags = new HashSet<>();
		for (final String s : flags) this.flags.add(s);
	}

	// -- Typed methods --

	@Override
	public boolean supports(final LinkedList<String> args) {
		if (args == null || args.size() < numArgs) return false;
		return isFlag(args);
	}

	// -- Internal methods --

	/**
	 * Check if the given list of arguments starts with a flag that matches this
	 * {@link ConsoleArgument}.
	 *
	 * @return true iff one of this argument's flags matches the first string in
	 *         the given list, or this argument has no explicit flags.
	 */
	protected boolean isFlag(final LinkedList<String> args) {
		return flags.isEmpty() || flags.contains(args.getFirst());
	}

	/**
	 * If the next argument is an appropriate parameter to a
	 * {@link ConsoleArgument}, retrieves it; otherwise, returns null.
	 *
	 * @return The first argument of the given list, if it does not
	 *         start with a {@code '-'} character; or null otherwise.
	 */
	protected String getParam(final LinkedList<String> args) {
		if (args.isEmpty()) return null;
		final String arg = args.getFirst();
		return arg.startsWith("-") ? null : arg;
	}
}
