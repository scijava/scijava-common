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

package org.scijava.script;

import java.io.Reader;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptException;

import org.scijava.log.LogService;
import org.scijava.log.Logged;
import org.scijava.log.StderrLogService;

/**
 * This class implements dummy versions for ScriptEngine's methods that are not
 * needed by the SciJava scripting framework.
 * 
 * @author Johannes Schindelin
 */
public abstract class AbstractScriptEngine implements ScriptEngine, Logged {

	// Abstract methods

	@Override
	public abstract Object eval(String script) throws ScriptException;

	@Override
	public abstract Object eval(Reader reader) throws ScriptException;

	// Fields

	protected Bindings engineScopeBindings;

	protected ScriptContext scriptContext = new AbstractScriptContext();

	private LogService log;

	// log service

	@Override
	public synchronized LogService log() {
		if (log == null) {
			log = new StderrLogService();
		}
		return log;
	}

	public void setLogService(final LogService log) {
		if (log != null) this.log = log;
	}

	// Bindings

	@Override
	public Object get(final String key) {
		return engineScopeBindings.get(key);
	}

	@Override
	public Bindings getBindings(final int scope) {
		if (scope == ScriptContext.ENGINE_SCOPE) return engineScopeBindings;
		throw new UnsupportedOperationException();
	}

	@Override
	public void put(final String key, final Object value) {
		engineScopeBindings.put(key, value);
	}

	// ScriptContext

	@Override
	public ScriptContext getContext() {
		return scriptContext;
	}

	@Override
	public void setContext(final ScriptContext context) {
		scriptContext = context;
	}

	// (Possibly) unsupported operations
	// (subclasses may, or may not, override with proper implementations)
	// The SciJava scripting framework does not require them to work properly.

	@Override
	public ScriptEngineFactory getFactory() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object eval(final String script, final ScriptContext context)
		throws ScriptException
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Object eval(final Reader reader, final ScriptContext context)
		throws ScriptException
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Bindings createBindings() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object eval(final String script, final Bindings n)
		throws ScriptException
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Object eval(final Reader reader, final Bindings n)
		throws ScriptException
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void setBindings(final Bindings bindings, final int scope) {
		throw new UnsupportedOperationException();
	}

}
