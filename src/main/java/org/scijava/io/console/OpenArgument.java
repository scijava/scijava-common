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

package org.scijava.io.console;

import java.io.IOException;
import java.util.LinkedList;

import org.scijava.console.AbstractConsoleArgument;
import org.scijava.console.ConsoleArgument;
import org.scijava.display.DisplayService;
import org.scijava.io.IOService;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.startup.StartupService;

/**
 * Handles the {@code --open} command line argument.
 * 
 * @author Curtis Rueden
 */
@Plugin(type = ConsoleArgument.class)
public class OpenArgument extends AbstractConsoleArgument {

	@Parameter(required = false)
	private IOService ioService;

	@Parameter(required = false)
	private DisplayService displayService;

	@Parameter(required = false)
	private StartupService startupService;

	@Parameter(required = false)
	private LogService log;

	// -- Constructor --

	public OpenArgument() {
		super(2, "--open");
	}

	// -- ConsoleArgument methods --

	@Override
	public void handle(final LinkedList<String> args) {
		if (!supports(args)) return;

		args.removeFirst(); // --open
		final String source = args.removeFirst();

		// open the source after the UI is shown
		startupService.addOperation(() -> {
			try {
				final Object o = ioService.open(source);
				displayService.createDisplay(o);
			}
			catch (final IOException exc) {
				if (log != null) log.error(exc);
			}
		});
	}

	// -- Typed methods --

	@Override
	public boolean supports(final LinkedList<String> args) {
		return startupService != null && ioService != null &&
			displayService != null && super.supports(args);
	}
}
