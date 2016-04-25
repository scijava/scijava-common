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

import java.lang.reflect.Method;

import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;
import org.scijava.prefs.PrefService;

/**
 * The default implementation of a {@link ScriptInterpreter}.
 * <p>
 * Credit to Jason Sachs for the multi-line evaluation (see
 * <a href="http://stackoverflow.com/a/5598207">his post on StackOverflow</a>).
 * </p>
 *  
 * @author Johannes Schindelin
 * @author Curtis Rueden
 */
public class DefaultScriptInterpreter implements ScriptInterpreter {

	private final ScriptLanguage language;
	private final ScriptEngine engine;
	private final History history;

	@Parameter(required = false)
	private PrefService prefs;

	@Parameter(required = false)
	private LogService log;

	private final StringBuilder buffer;
	private int pendingLineCount;
	private boolean expectingMoreInput;

	/**
	 * @deprecated Use {@link #DefaultScriptInterpreter(ScriptLanguage)} instead.
	 */
	@Deprecated
	@SuppressWarnings("unused")
	public DefaultScriptInterpreter(final PrefService prefs,
		final ScriptService scriptService, final ScriptLanguage language)
	{
		this(language);
	}

	/**
	 * Creates a new script interpreter for the given script language.
	 * 
	 * @param language {@link ScriptLanguage} of the interpreter
	 */
	public DefaultScriptInterpreter(final ScriptLanguage language) {
		this(language, null);
	}

	/**
	 * Creates a new script interpreter for the given script language, using the
	 * specified script engine.
	 * 
	 * @param language {@link ScriptLanguage} of the interpreter
	 * @param engine {@link ScriptEngine} to use, or null for the specified
	 *          language's default engine
	 */
	public DefaultScriptInterpreter(final ScriptLanguage language,
		final ScriptEngine engine)
	{
		language.getContext().inject(this);
		this.language = language;
		this.engine = engine == null ? language.getScriptEngine() : engine;
		history = prefs == null ? null :
			new History(prefs, this.engine.getClass().getName());
		readHistory();
		buffer = new StringBuilder();
		reset();
	}

	// -- ScriptInterpreter methods --

	@Override
	public synchronized void readHistory() {
		if (history == null) return;
		history.read();
	}

	@Override
	public synchronized void writeHistory() {
		if (history == null) return;
		history.write();
	}

	@Override
	public synchronized String walkHistory(final String currentCommand,
		final boolean forward)
	{
		if (history == null) return currentCommand;
		history.replace(currentCommand);
		return forward ? history.next() : history.previous();
	}

	@Override
	public Object eval(final String command) throws ScriptException {
		addToHistory(command);
		return engine.eval(command);
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This implementation from Jason Sachs uses the following strategy:
	 * </p>
	 * <ul>
	 * <li>Keep a pending list of input lines not yet evaluated.</li>
	 * <li>Try compiling (but not evaluating) the pending input lines.
	 * <ul>
	 * <li>If the compilation is OK, we may be able to execute pending input
	 * lines.</li>
	 * <li>If the compilation throws an exception, and there is an indication of
	 * the position (line + column number) of the error, and this matches the end
	 * of the pending input, then that's a clue that we're expecting more input,
	 * so swallow the exception and wait for the next line.</li>
	 * <li>Otherwise, we either don't know where the error is, or it happened
	 * prior to the end of the pending input, so rethrow the exception.</li>
	 * </ul>
	 * </li>
	 * <li>If we are not expecting any more input lines, and we only have one line
	 * of pending input, then evaluate it and restart.</li>
	 * <li>If we are not expecting any more input lines, and the last one is a
	 * blank one, and we have more than one line of pending input, then evaluate
	 * it and restart. Python's interactive shell seems to do this.</li>
	 * <li>Otherwise, keep reading input lines.</li>
	 * </ul>
	 * <p>
	 * This helps avoid certain problems:
	 * </p>
	 * <ul>
	 * <li>users getting annoyed having to enter extra blank lines after
	 * single-line inputs</li>
	 * <li>users entering a long multi-line statement and only find out after the
	 * fact that there was a syntax error in the 2nd line.</li>
	 * </ul>
	 * <p>
	 * For further details, see <a href="http://stackoverflow.com/a/5598207">SO
	 * #5584674</a>.
	 * </p>
	 * </p>
	 */
	@Override
	public Object interpret(final String line) throws ScriptException {
		if (line.isEmpty()) {
			if (!shouldEvaluatePendingInput(true)) return MORE_INPUT_PENDING;
		}

		if (pendingLineCount > 0) buffer.append("\n");
		pendingLineCount++;
		buffer.append(line);
		final String command = buffer.toString();

		if (!(engine instanceof Compilable)) {
			// Not a compilable language.
			// Evaluate directly, with no multi-line statements possible.
			try {
				return eval(command);
			}
			finally {
				reset();
			}
		}

		final CompiledScript cs = tryCompiling(command, //
			getPendingLineCount(), line.length());

		if (cs == null) {
			// Command did not compile.
			// Assume it is incomplete and wait for more input on the next line.
			return MORE_INPUT_PENDING;
		}
		if (!shouldEvaluatePendingInput(line.isEmpty())) {
			// We are still expecting more input.
			return MORE_INPUT_PENDING;
		}
		// Command is complete; evaluate the compiled script.
		try {
			addToHistory(command);
			return cs.eval();
		}
		finally {
			reset();
		}
	}

	@Override
	public void reset() {
		buffer.setLength(0);
		pendingLineCount = 0;
		expectingMoreInput = false;
	}

	@Override
	public ScriptLanguage getLanguage() {
		return language;
	}

	@Override
	public ScriptEngine getEngine() {
		return engine;
	}

	@Override
	public Bindings getBindings() {
		return engine.getBindings(ScriptContext.ENGINE_SCOPE);
	}

	@Override
	public boolean isReady() {
		return buffer.length() == 0;
	}

	@Override
	public boolean isExpectingMoreInput() {
		return expectingMoreInput;
	}

	// -- Helper methods --

	private void addToHistory(final String command) {
		if (history != null) history.add(command);
	}

	/**
	 * @return number of lines pending execution
	 */
	private int getPendingLineCount() {
		return pendingLineCount;
	}

	/**
	 * @param lineIsEmpty whether the last line is empty
	 * @return whether we should evaluate the pending input. The default behavior
	 *         is to evaluate if we only have one line of input, or if the user
	 *         enters a blank line. This behavior should be overridden where
	 *         appropriate.
	 */
	private boolean shouldEvaluatePendingInput(final boolean lineIsEmpty) {
		if (isExpectingMoreInput()) return false;
		return getPendingLineCount() == 1 || lineIsEmpty;
	}

	private CompiledScript tryCompiling(final String string, final int lineCount,
		final int lastLineLength) throws ScriptException
	{
		CompiledScript result = null;
		try {
			final Compilable c = (Compilable) engine;
			result = c.compile(string);
		}
		catch (final ScriptException se) {
			boolean rethrow = true;
			if (se.getCause() != null) {
				final Integer col = columnNumber(se);
				final Integer line = lineNumber(se);
				// swallow the exception if it occurs at the last character
				// of the input (we may need to wait for more lines)
				if (isLastCharacter(col, line, lineCount, lastLineLength)) {
					rethrow = false;
				}
				else if (log != null && log.isDebug()) {
					final String msg = se.getCause().getMessage();
					log.debug("L" + line + " C" + col + "(" + lineCount + "," +
						lastLineLength + "): " + msg);
					log.debug("in '" + string + "'");
				}
			}

			if (rethrow) {
				reset();
				throw se;
			}
		}

		expectingMoreInput = result == null;
		return result;
	}

	private boolean isLastCharacter(final Integer col, final Integer line,
		final int lineCount, final int lastLineLength)
	{
		if (col == null || line == null) return false;
		final int colNo = col.intValue(), lineNo = line.intValue();
		return lineNo == lineCount && colNo == lastLineLength ||
			lineNo == lineCount + 1 && colNo == 0;
	}

	private Integer columnNumber(final ScriptException se) {
		if (se.getColumnNumber() >= 0) return se.getColumnNumber();
		return callMethod(se.getCause(), "columnNumber", Integer.class);
	}

	private Integer lineNumber(final ScriptException se) {
		if (se.getLineNumber() >= 0) return se.getLineNumber();
		return callMethod(se.getCause(), "lineNumber", Integer.class);
	}

	private static Method getMethod(final Object object,
		final String methodName)
	{
		try {
			return object.getClass().getMethod(methodName);
		}
		catch (final NoSuchMethodException e) {
			// gulp
			return null;
		}
	}

	private static <T> T callMethod(final Object object, final String methodName,
		final Class<T> cl)
	{
		try {
			final Method m = getMethod(object, methodName);
			if (m != null) {
				final Object result = m.invoke(object);
				return cl.cast(result);
			}
		}
		catch (final Exception e) {
			e.printStackTrace();
		}
		return null;
	}

}
