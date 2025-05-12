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

package org.scijava.script;

import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.scijava.Context;
import org.scijava.log.LogLevel;
import org.scijava.log.LogService;
import org.scijava.module.Module;

/**
 * A command-line entry point for running SciJava scripts.
 *
 * @author Curtis Rueden
 */
public class ScriptCLI {

	private static final String USAGE = "" + //
		"Usage: " + ScriptCLI.class.getSimpleName() + //
		" [-d] [-h] [-l language] [-o] [-r] /path/to/script [script-args]\n" + //
		"\n" + //
		"Options:\n" + //
		"  -d, --debug              : enable debug-level log output\n" + //
		"  -h, --help               : display this help message\n" + //
		"  -l, --language           : specify language of script to execute\n" + //
		"                             otherwise, inferred from script extension\n" + //
		"  -o, --print-outputs      : print output values\n" + //
		"  -r, --print-return-value : print return value\n" + //
		"\n" + //
		"To read from stdin, use a dash (-) symbol for the script path.\n" + //
		"\n" + //
		"For script-args, give space-separated key=value pairs,\n" + //
		"while will be passed in as SciJava script arguments.";
	
	public static void main(String... args) throws Exception {
		final Map<String, Object> inputs = new HashMap<>();
		File file = null;
		String language = null;
		boolean printOutputs = false;
		boolean printReturnValue = false;
		boolean parsingOptions = true;
		try (final Context context = new Context()) {
			// Parse command-line arguments.
			if (args.length == 0) args = new String[] {"-h"};
			for (int i = 0; i < args.length; i++) {
				if (parsingOptions) {
					// Parse options and filename.
					if (args[i].equals("-d") || args[i].equals("--debug")) {
						final LogService log = context.getService(LogService.class);
						if (log != null) log.setLevel(LogLevel.DEBUG);
					}
					else if (args[i].equals("-h") || args[i].equals("--help")) {
						System.err.println(USAGE);
						System.exit(1);
					}
					else if (i < args.length - 1 && //
						args[i].equals("-l") || args[i].equals("--language"))
					{
						language = args[++i];
					}
					else if (args[i].equals("-o") || args[i].equals("--print-outputs")) {
						printOutputs = true;
					}
					else if (args[i].equals("-r") || args[i].equals("--print-return-value")) {
						printReturnValue = true;
					}
					else if (args[i].equals("-")) {
						// read from stdin
						parsingOptions = false;
					}
					else if (i < args.length - 1 && args[i].equals("--")) {
						// argument after the -- separator must be the filename.
						file = new File(args[++i]);
						parsingOptions = false;
					}
					else if (new File(args[i]).exists()) {
						file = new File(args[i]);
						parsingOptions = false;
					}
					else {
						System.err.println("Invalid argument: " + args[i]);
						System.exit(2);
					}
				}
				else {
					// Parse script arguments.
					final int equals = args[i].indexOf("=");
					if (equals < 0) {
						System.err.println("Invalid argument: " + args[i]);
						System.exit(3);
					}
					final String key = args[i].substring(0, equals);
					final String val = args[i].substring(equals + 1);
					inputs.put(key, val);
				}
			}

			final ScriptService ss = context.getService(ScriptService.class);
			if (ss == null) {
				System.err.println("Error: No script service available.");
				System.exit(4);
			}
			if (file == null && language == null) {
				System.err.println("Error: Must specify language when using stdin.");
				System.exit(5);
			}

			final Module m;
			if (language == null) {
				m = ss.run(file, true, inputs).get();
			}
			else {
				ScriptLanguage lang = ss.getLanguageByName(language);
				if (lang == null) lang = ss.getLanguageByExtension(language);
				if (lang == null) {
					System.err.println("Error: Unsupported language: " + language);
					System.exit(6);
				}
				final Reader reader = file == null ? //
					new InputStreamReader(System.in) : //
					new FileReader(file);
				m = ss.run("." + language, reader, true, inputs).get();
			}
			if (printOutputs) {
				for (Entry<String, Object> output : m.getOutputs().entrySet()) {
					System.out.println(output.getKey() + " = " + output.getValue());
				}
			}
			if (printReturnValue && m instanceof ScriptModule) {
				System.out.println(((ScriptModule) m).getReturnValue());
			}
		}
		System.exit(0);
	}
}
