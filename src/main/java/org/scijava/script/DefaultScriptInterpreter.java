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

import org.scijava.prefs.PrefService;

/**
 * The default implementation of a {@link ScriptInterpreter}.
 *  
 * @author Johannes Schindelin
 */
public class DefaultScriptInterpreter implements ScriptInterpreter {

	private ScriptEngine engine;
	private History history;
	private String currentCommand = "";

	/**
	 * Constructs a new {@link DefaultScriptInterpreter}.
	 * 
	 * @param scriptService the script service
	 * @param engine the script engine
	 */
	public DefaultScriptInterpreter(final PrefService prefs, final ScriptService scriptService, final ScriptEngine engine) {
		this.engine = engine;
		history = new History(prefs, engine.getClass().getName());
		readHistory();
	}

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
	public synchronized String walkHistory(final String currentCommand, boolean forward) {
		this.currentCommand = currentCommand;
		if (history == null) return currentCommand;
		history.replace(currentCommand);
		return forward ? history.next() : history.previous();
	}

	/**
	 * Evaluates a command.
	 * 
	 * @param command the command to evaluate
	 * @throws ScriptException
	 */
	@Override
	public void eval(String command) throws ScriptException {
		if (history != null) history.add(command);
		if (engine == null) throw new java.lang.IllegalArgumentException();
		engine.eval(command);
	}

	/**
	 * Returns the current script engine.
	 * 
	 * @return
	 */
	@Override
	public ScriptEngine getEngine() {
		return engine;
	}

}
