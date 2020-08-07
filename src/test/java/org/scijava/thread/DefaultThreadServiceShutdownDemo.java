/*
 * #%L
 * SciJava Common shared library for SciJava software.
 * %%
 * Copyright (C) 2009 - 2020 SciJava developers.
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

package org.scijava.thread;

import org.scijava.Context;
import org.scijava.command.Command;
import org.scijava.command.CommandService;
import org.scijava.plugin.Parameter;

import java.util.concurrent.ExecutionException;

/**
 * Demonstrates that the {@link DefaultThreadService} keeps the JVM alive
 * exactly as long as required.
 * <p>
 * This is done by using the CommandService which internally calls the
 * ThreadService to executes commands. The command, used as an example,
 * recursively calls itself, to perform a countdown.
 * <p>
 * The rather complicated countdown example demonstrates, that everything works
 * correctly, even for complicated commands that call sub commands.
 *
 * @author Matthias Arzt
 */
public class DefaultThreadServiceShutdownDemo {

	public static void main(String... args)
			throws ExecutionException, InterruptedException
	{
		Context context = new Context();
		CommandService command = context.service(CommandService.class);
		command.run(CountDownCommand.class, true, "count", 10);
	}

	public static class CountDownCommand implements Command {

		@Parameter
		private int count;

		@Parameter
		private CommandService commandService;

		@Override
		public void run() {
			try {
				if(count > 0) {
					System.out.println("Count down: " + count);
					Thread.sleep(1000);
					commandService.run(CountDownCommand.class, true, "count", count - 1);
				} else {
					System.out.println("Hurray!");
				}
			}
			catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
