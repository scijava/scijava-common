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

package org.scijava.command.run;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import org.scijava.command.Command;
import org.scijava.command.CommandInfo;
import org.scijava.command.CommandService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.run.AbstractCodeRunner;
import org.scijava.run.CodeRunner;

/**
 * Runs the given {@link Command} class.
 * 
 * @author Curtis Rueden
 */
@Plugin(type = CodeRunner.class)
public class CommandCodeRunner extends AbstractCodeRunner {

	@Parameter
	private CommandService commandService;

	// -- CodeRunner methods --

	@Override
	public void run(final Object code, final Object... args)
		throws InvocationTargetException
	{
		final Class<? extends Command> c = getCommandClass(code);
		if (c != null) waitFor(commandService.run(c, true, args));

		final CommandInfo info = getCommandInfo(code);
		if (info != null) waitFor(commandService.run(info, true, args));
	}

	@Override
	public void run(final Object code, final Map<String, Object> inputMap)
		throws InvocationTargetException
	{
		final Class<? extends Command> c = getCommandClass(code);
		if (c != null) waitFor(commandService.run(c, true, inputMap));

		final CommandInfo info = getCommandInfo(code);
		if (info != null) waitFor(commandService.run(info, true, inputMap));
	}

	// -- Typed methods --

	@Override
	public boolean supports(final Object code) {
		return getCommandClass(code) != null || getCommandInfo(code) != null;
	}

	// -- Helper methods --

	private Class<? extends Command> getCommandClass(final Object code) {
		if (!(code instanceof Class)) return null;
		final Class<?> c = (Class<?>) code;
		if (!Command.class.isAssignableFrom(c)) return null;
		@SuppressWarnings("unchecked")
		final Class<? extends Command> commandClass = (Class<? extends Command>) c;
		return commandClass;
	}

	private CommandInfo getCommandInfo(final Object code) {
		if (!(code instanceof String)) return null;
		final String command = (String) code;

		final CommandInfo info = commandService.getCommand(command);
		if (info != null) return info;

		// command was not a class name; search for command by title instead
		for (final CommandInfo ci : commandService.getCommands()) {
			if (command.equals(ci.getTitle())) return ci;
		}

		return null;
	}

}
