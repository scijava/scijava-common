/*
 * #%L
 * SciJava Common shared library for SciJava software.
 * %%
 * Copyright (C) 2009 - 2023 SciJava developers.
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

package org.scijava.command.console;

import java.util.LinkedList;
import java.util.Map;

import org.scijava.command.CommandInfo;
import org.scijava.command.CommandService;
import org.scijava.console.AbstractConsoleArgument;
import org.scijava.console.ConsoleArgument;
import org.scijava.console.ConsoleUtils;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 * @deprecated Use {@link org.scijava.run.console.RunArgument} instead.
 */
@Deprecated
@Plugin(type = ConsoleArgument.class)
public class RunArgument extends AbstractConsoleArgument {

	@Parameter
	private CommandService commandService;

	@Parameter
	private LogService log;

	// -- Constructor --

	public RunArgument() {
		super(2, "--class");
	}

	// -- ConsoleArgument methods --

	@Override
	public void handle(final LinkedList<String> args) {
		if (!supports(args))
			return;

		log.warn("The --class flag is deprecated, and will\n" +
			"be removed in a future release. Use --run instead.");

		args.removeFirst(); // --class
		final String commandToRun = args.removeFirst();
		final String paramString = ConsoleUtils.hasParam(args) ? args.removeFirst() : "";

		run(commandToRun, paramString);
	}

	// -- Typed methods --

	@Override
	public boolean supports(final LinkedList<String> args) {
		if (!super.supports(args))
			return false;
		return getInfo(args.get(1)) != null;
	}

	// -- Helper methods --

	/** Implements the {@code --run} command line argument. */
	private void run(final String commandToRun, final String optionString) {
		// get the command info
		final CommandInfo info = getInfo(commandToRun);

		// couldn't find anything to run
		if (info == null)
			return;

		// TODO: parse the optionString a la ImageJ1
		final Map<String, Object> inputMap = ConsoleUtils.parseParameterString(optionString, info, log);

		try {
			commandService.run(info, true, inputMap).get();
		} catch (final Exception exc) {
			log.error(exc);
		}
	}

	/**
	 * Try to convert the given string to a {@link CommandInfo}
	 */
	private CommandInfo getInfo(final String commandToRun) {
		CommandInfo info = commandService.getCommand(commandToRun);
		if (info == null) {
			// command was not a class name; search for command by title instead
			final String label = commandToRun.replace('_', ' ');
			for (final CommandInfo ci : commandService.getCommands()) {
				if (label.equals(ci.getTitle())) {
					info = ci;
					break;
				}
			}
		}
		return info;
	}
}
