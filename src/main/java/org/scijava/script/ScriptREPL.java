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

package org.scijava.script;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

import javax.script.Bindings;
import javax.script.ScriptException;

import org.scijava.Context;
import org.scijava.Gateway;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.PluginInfo;
import org.scijava.plugin.PluginService;
import org.scijava.service.Service;

/**
 * A REPL for SciJava script engines, which allows dynamic language switching.
 *
 * @author Curtis Rueden
 */
public class ScriptREPL {

	private static final String NULL = "<null>";

	@Parameter
	private Context context;

	@Parameter
	private ScriptService scriptService;

	@Parameter(required = false)
	private PluginService pluginService;

	private final PrintStream out;

	/** List of interpreter-friendly script languages. */
	private List<ScriptLanguage> languages;

	/** The currently active interpreter. */
	private ScriptInterpreter interpreter;

	/** Flag for debug mode. */
	private boolean debug;

	public ScriptREPL(final Context context) {
		this(context, System.out);
	}

	public ScriptREPL(final Context context, final OutputStream out) {
		context.inject(this);
		this.out = out instanceof PrintStream ?
			(PrintStream) out : new PrintStream(out);
	}

	/**
	 * Gets the list of languages compatible with the REPL.
	 * <p>
	 * This list will match those given by {@link ScriptService#getLanguages()},
	 * but filtered to exclude any who report {@code true} for
	 * {@link ScriptLanguage#isCompiledLanguage()}.
	 * </p>
	 */
	public List<ScriptLanguage> getInterpretedLanguages() {
		if (languages == null) initLanguages();
		return languages;
	}

	/** Gets the script interpreter for the currently active language. */
	public ScriptInterpreter getInterpreter() {
		return interpreter;
	}

	/**
	 * Starts a Read-Eval-Print-Loop from the standard input stream, returning
	 * when the loop terminates.
	 */
	public void loop() throws IOException {
		loop(System.in);
	}

	/**
	 * Starts a Read-Eval-Print-Loop from the given input stream, returning when
	 * the loop terminates.
	 * 
	 * @param in Input stream from which commands are read.
	 */
	public void loop(final InputStream in) throws IOException {
		initialize();
		final BufferedReader bin = new BufferedReader(new InputStreamReader(in));
		while (true) {
			prompt();
			final String line = bin.readLine();
			if (line == null) break;
			if (!evaluate(line)) return;
		}
	}

	/** Outputs a greeting, and sets up the initial language of the REPL. */
	public void initialize() {
		out.println("Welcome to the SciJava REPL!");
		out.println();
		help();
		final List<ScriptLanguage> langs = getInterpretedLanguages();
		if (langs.isEmpty()) {
			out.println("--------------------------------------------------------------");
			out.println("Uh oh! There are no SciJava script languages available!");
			out.println("Are any on your classpath? E.g.: org.scijava:scripting-groovy?");
			out.println("--------------------------------------------------------------");
			out.println();
			return;
		}
		out.println("Have fun!");
		out.println();
		lang(langs.get(0).getLanguageName());
		populateBindings(interpreter.getBindings());
	}

	/** Outputs the prompt. */
	public void prompt() {
		out.print(interpreter == null || interpreter.isReady() ? "> " : "\\ ");
	}

	/**
	 * Evaluates the line, including handling of special colon-prefixed REPL
	 * commands.
	 * 
	 * @param line The line to evaluate.
	 * @return False iff the REPL should exit.
	 */
	public boolean evaluate(final String line) {
		try {
			final String tLine = line.trim();
			if (tLine.equals(":help")) help();
			else if (tLine.equals(":vars")) vars();
			else if (tLine.equals(":langs")) langs();
			else if (tLine.equals(":debug")) debug();
			else if (tLine.startsWith(":lang ")) lang(line.substring(6).trim());
			else if (tLine.equals(":quit")) return false;
			else {
				// ensure that a script language is active
				if (interpreter == null) return true;

				// pass the input to the current interpreter for evaluation
				final Object result = interpreter.interpret(line);
				if (result != ScriptInterpreter.MORE_INPUT_PENDING) {
					out.println(s(result));
				}
			}
		}
		catch (final ScriptException exc) {
			// NB: Something went wrong interpreting the line of code.
			// Let's just display the error message, unless we are in debug mode.
			if (debug) exc.printStackTrace(out);
			else {
				final String msg = exc.getMessage();
				out.println(msg == null ? exc.getClass().getName() : msg);
			}
		}
		catch (final Throwable exc) {
			// NB: Something unusual went wrong. Dump the whole exception always.
			exc.printStackTrace(out);
		}
		return true;
	}

	// -- Commands --

	/** Prints a usage guide. */
	public void help() {
		out.println("Available built-in commands:");
		out.println();
		out.println("  :help           | this handy list of commands");
		out.println("  :vars           | dump a list of variables");
		out.println("  :lang <name>    | switch the active language");
		out.println("  :langs          | list available languages");
		out.println("  :debug          | toggle full stack traces");
		out.println("  :quit           | exit the REPL");
		out.println();
		out.println("Or type a statement to evaluate it with the active language.");
		out.println();
	}

	/** Lists variables in the script context. */
	public void vars() {
		if (interpreter == null) return; // no active script language

		final List<String> keys = new ArrayList<>();
		final List<Object> types = new ArrayList<>();
		final Bindings bindings = interpreter.getBindings();
		for (final String key : bindings.keySet()) {
			final Object value = bindings.get(key);
			keys.add(key);
			types.add(type(value));
		}
		printColumns(keys, types);
	}

	/**
	 * Creates a new {@link ScriptInterpreter} to interpret statements, preserving
	 * existing variables from the previous interpreter.
	 * 
	 * @param langName The script language of the new interpreter.
	 * @throws IllegalArgumentException if the requested language is not
	 *           available.
	 */
	public void lang(final String langName) {
		// create the new interpreter
		final ScriptLanguage language = scriptService.getLanguageByName(langName);
		if (language == null) {
			out.println("No such language: " + langName);
			return;
		}
		final ScriptInterpreter newInterpreter =
			new DefaultScriptInterpreter(language);

		// preserve state of the previous interpreter
		try {
			copyBindings(interpreter, newInterpreter);
		}
		catch (final Throwable t) {
			t.printStackTrace(out);
		}
		out.println("language -> " +
			newInterpreter.getLanguage().getLanguageName());
		interpreter = newInterpreter;
	}

	public void langs() {
		final List<String> names = new ArrayList<>();
		final List<String> versions = new ArrayList<>();
		final List<Object> aliases = new ArrayList<>();
		for (final ScriptLanguage lang : getInterpretedLanguages()) {
			names.add(lang.getLanguageName());
			versions.add(lang.getLanguageVersion());
			aliases.add(lang.getNames());
		}
		printColumns(names, versions, aliases);
	}

	public void debug() {
		debug = !debug;
		out.println("debug mode -> " + debug);
	}

	// -- Main method --

	public static void main(final String... args) throws Exception {
		// make a SciJava application context
		final Context context = new Context();

		// create the script interpreter
		final ScriptREPL scriptCLI = new ScriptREPL(context);

		// start the REPL
		scriptCLI.loop();

		// clean up
		context.dispose();
		System.exit(0);
	}

	// -- Helper methods --

	/** Initializes {@link #languages}. */
	private synchronized void initLanguages() {
		if (languages != null) return;
		final List<ScriptLanguage> langs = new ArrayList<>();
		for (final ScriptLanguage lang : scriptService.getLanguages()) {
			if (!lang.isCompiledLanguage()) langs.add(lang);
		}
		languages = langs;
	}

	/** Populates the bindings with the context + services + gateways. */
	private void populateBindings(final Bindings bindings) {
		bindings.put("ctx", context);
		for (final Service service : context.getServiceIndex().getAll()) {
			final String name = serviceName(service);
			bindings.put(name, service);
		}
		for (final Gateway gateway : gateways()) {
			bindings.put(gateway.getShortName(), gateway);
		}
	}

	/** Transfers variables from one interpreter's bindings to another. */
	private void copyBindings(final ScriptInterpreter src,
		final ScriptInterpreter dest)
	{
		if (src == null) return; // nothing to copy
		final Bindings srcBindings = src.getBindings();
		final Bindings destBindings = dest.getBindings();
		for (final String key : src.getBindings().keySet()) {
			final Object value = src.getLanguage().decode(srcBindings.get(key));
			destBindings.put(key, value);
		}
	}

	private List<Gateway> gateways() {
		final ArrayList<Gateway> gateways = new ArrayList<>();
		if (pluginService == null) return gateways;
		// HACK: Instantiating a Gateway with the noargs constructor spins
		// up a second Context, which is not what we want. Perhaps SJC should
		// be changed to prefer a single-argument constructor that accepts a
		// Context, before trying the noargs constructor?
		// In the meantime, we do it manually here.
		final List<PluginInfo<Gateway>> infos =
			pluginService.getPluginsOfType(Gateway.class);
		for (final PluginInfo<Gateway> info : infos) {
			try {
				final Constructor<? extends Gateway> ctor =
					info.loadClass().getConstructor(Context.class);
				final Gateway gateway = ctor.newInstance(context);
				gateways.add(gateway);
			}
			catch (final Throwable t) {
				t.printStackTrace(out);
			}
		}
		return gateways;
	}

	private String serviceName(final Service service) {
		final String serviceName = service.getClass().getSimpleName();
		final String shortName = lowerCamelCase(
			serviceName.replaceAll("^(Default)?(.*)Service$", "$2"));
		return shortName;
	}

	private String type(final Object value) {
		if (value == null) return NULL;
		final Object decoded = interpreter.getLanguage().decode(value);
		if (decoded == null) return NULL;
		return "[" + decoded.getClass().getName() + "]";
	}

	private void printColumns(final List<?>... columns) {
		final int pad = 2;

		// compute width of each column
		final int[] widths = new int[columns.length];
		for (int c = 0; c < columns.length; c++) {
			final List<?> list = columns[c];
			for (final Object o : list) {
				final String s = s(o);
				if (s.length() > widths[c]) widths[c] = s.length();
			}
		}

		// output the columns
		for (int i = 0; i < columns[0].size(); i++) {
			for (int c = 0; c < columns.length; c++) {
				final String s = s(columns[c].get(i));
				out.print(s);
				for (int p = s.length(); p < widths[c] + pad; p++) {
					out.print(' ');
				}
			}
			out.println();
		}
	}

	private static String lowerCamelCase(final String s) {
		final StringBuilder sb = new StringBuilder(s);
		for (int i=0; i<sb.length(); i++) {
			final char c = sb.charAt(i);
			if (c >= 'A' && c <= 'Z') sb.setCharAt(i, (char) (c - 'A' + 'a'));
			else break;
		}
		return sb.toString();
	}

	private static String s(final Object o) {
		return o == null ? NULL : o.toString();
	}

}
