/*
 * #%L
 * SciJava Common shared library for SciJava software.
 * %%
 * Copyright (C) 2009 - 2024 SciJava developers.
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

package org.scijava.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Arrays;

/**
 * Useful methods for working with processes.
 * 
 * @author Johannes Schindelin
 */
public final class ProcessUtils {

	private ProcessUtils() {
		// prevent instantiation of utility class
	}

	/**
	 * Executes a program. This is a convenience method mainly to be able to catch
	 * the output of programs as shell scripts can do by wrapping calls in $(...).
	 * 
	 * @param workingDirectory the directory in which to execute the program
	 * @param err the {@link PrintStream} to print the program's error stream to;
	 *          if null is passed, the error goes straight to Nirvana (not the
	 *          band, though).
	 * @param out the {@link PrintStream} to print the program's output to; if
	 *          null is passed, the output will be accumulated into a
	 *          {@link String} and returned upon exit.
	 * @param args the command-line to execute, split into components
	 * @return the output of the program if {@code out} is null, otherwise an
	 *         empty {@link String}
	 * @throws RuntimeException if interrupted or the program failed to execute
	 *           successfully.
	 */
	public static String exec(final File workingDirectory,
		final PrintStream err, final PrintStream out, final String... args)
	{
		return exec(workingDirectory, null, err, out, args);
	}

	/**
	 * Executes a program. This is a convenience method mainly to be able to catch
	 * the output of programs as shell scripts can do by wrapping calls in $(...).
	 * 
	 * @param workingDirectory the directory in which to execute the program
	 * @param in the {@link InputStream} which gets fed to the program as standard input;
	 * @param err the {@link PrintStream} to print the program's error stream to;
	 *          if null is passed, the error goes straight to Nirvana (not the
	 *          band, though).
	 * @param out the {@link PrintStream} to print the program's output to; if
	 *          null is passed, the output will be accumulated into a
	 *          {@link String} and returned upon exit.
	 * @param args the command-line to execute, split into components
	 * @return the output of the program if {@code out} is null, otherwise an
	 *         empty {@link String}
	 * @throws RuntimeException if interrupted or the program failed to execute
	 *           successfully.
	 */
	public static String exec(final File workingDirectory,
		final InputStream in, final PrintStream err, final PrintStream out,
		final String... args)
	{
		try {
			final Process process =
				Runtime.getRuntime().exec(args, null, workingDirectory);

			final ReadInto inThread;
			if (in == null) {
				inThread = null;
				process.getOutputStream().close();
			} else {
				final PrintStream print = new PrintStream(process.getOutputStream());
				inThread = new ReadInto(in, print, true);
			}

			final ReadInto errThread = new ReadInto(process.getErrorStream(), err);
			final ReadInto outThread = new ReadInto(process.getInputStream(), out);
			try {
				process.waitFor();
				if (inThread != null) {
					inThread.done();
					inThread.join();
				}
				errThread.join();
				outThread.join();
			}
			catch (final InterruptedException e) {
				process.destroy();
				if (inThread != null) inThread.interrupt();
				errThread.done();
				errThread.interrupt();
				outThread.done();
				outThread.interrupt();
				err.println("Interrupted!");
				throw new RuntimeException(e);
			}
			if (process.exitValue() != 0) {
				throw new RuntimeException("exit status " + process.exitValue() +
					": " + Arrays.toString(args) + "\n" + err);
			}
			return outThread.toString();
		}
		catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}

}
