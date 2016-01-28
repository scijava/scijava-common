/*
 * #%L
 * SciJava Common shared library for SciJava software.
 * %%
 * Copyright (C) 2009 - 2015 Board of Regents of the University of
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

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

import org.scijava.prefs.PrefService;

/**
 * The default implementation of a {@link ScriptInterpreter}.
 *  
 * @author Johannes Schindelin
 */
public class DefaultScriptInterpreter implements ScriptInterpreter {

	private final ScriptLanguage language;
	private final ScriptEngine engine;
	private final History history;

	@Parameter(required = false)
	private PrefService prefs;

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
		if (history != null) history.add(command);
		if (engine == null) throw new java.lang.IllegalArgumentException();
		return engine.eval(command);
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

}
