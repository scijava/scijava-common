/*
 * #%L
 * SciJava Common shared library for SciJava software.
 * %%
 * Copyright (C) 2009 - 2014 Board of Regents of the University of
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

import javax.script.ScriptEngine;
import javax.script.ScriptException;

/**
 * The contract for script interpreters.
 * 
 * @author Johannes Schindelin
 */
public interface ScriptInterpreter {

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
	 * @throws ScriptException
	 */
	void eval(String command) throws ScriptException;

	/**
	 * Returns the associated {@link ScriptEngine}
	 * 
	 * @return the script engine
	 */
	ScriptEngine getEngine();
}
