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

package org.scijava.script;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

/**
 * The contract for script interpreters.
 * 
 * @author Johannes Schindelin
 * @author Curtis Rueden
 */
public interface ScriptInterpreter {

	/**
	 * A special object returned by {@link #interpret(String)} when the
	 * interpreter is expecting additional input before finishing the evaluation.
	 */
	Object MORE_INPUT_PENDING = new Object();

	/**
	 * Reads the persisted history of the current script interpreter.
	 */
	void readHistory();

	/**
	 * Persists the history of the current script interpreter.
	 */
	void writeHistory();

	/**
	 * Obtains the next/previous command in the command history.
	 * 
	 * @param currentCommand the current command (will be stored in the history)
	 * @param forward if true, the next history entry is returned (more recent),
	 *          if false, the previous one
	 * @return the next/previous command
	 */
	String walkHistory(String currentCommand, boolean forward);

	/**
	 * Evaluates a command.
	 * 
	 * @param command the command to evaluate
	 * @return result of the evaluation
	 * @throws ScriptException
	 */
	Object eval(String command) throws ScriptException;

	/**
	 * Interprets the given line of code, which might be part of a multi-line
	 * statement.
	 * 
	 * @param line line of code to interpret
	 * @return value of the line, or {@link #MORE_INPUT_PENDING} if there is still
	 *         pending input
	 * @throws ScriptException in case of an exception
	 */
	Object interpret(String line) throws ScriptException;

	/**
	 * Clears the buffer of not-yet-evaluated lines of code, accumulated from
	 * previous calls to {@link #interpret}. In other words: start over with a new
	 * (potentially multi-line) statement, discarding the current partial one.
	 * 
	 * @see #interpret
	 */
	void reset();

	/**
	 * Returns the associated {@link ScriptLanguage}.
	 */
	ScriptLanguage getLanguage();

	/**
	 * Returns the associated {@link ScriptEngine}.
	 * 
	 * @return the script engine
	 */
	ScriptEngine getEngine();

	/**
	 * Returns the {@link Bindings} of the associated {@link ScriptEngine} at
	 * {@link ScriptContext#ENGINE_SCOPE} scope.
	 */
	Bindings getBindings();

	/**
	 * @return whether the interpreter is ready for a brand new statement.
	 * @see #interpret(String)
	 */
	boolean isReady();

	/**
	 * @return whether the interpreter expects more input. A true value means
	 *         there is definitely more input needed. A false value means no more
	 *         input is needed, but it may not yet be appropriate to evaluate all
	 *         the pending lines. (there's some ambiguity depending on the
	 *         language)
	 * @see #interpret(String)
	 */
	boolean isExpectingMoreInput();

}
