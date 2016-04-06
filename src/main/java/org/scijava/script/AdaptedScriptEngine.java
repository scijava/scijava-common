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

import java.io.Reader;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptException;

/**
 * Abstract superclass for {@link ScriptEngine} implementations which adapt an
 * existing {@link ScriptEngine}.
 * <p>
 * This is useful for situations where a JSR-223-compliant script engine has
 * been provided, but whose behavior we need to extend or tweak.
 * </p>
 *
 * @author Curtis Rueden
 */
public class AdaptedScriptEngine implements ScriptEngine {

	private final ScriptEngine engine;

	public AdaptedScriptEngine(final ScriptEngine engine) {
		this.engine = engine;
	}

	// -- ScriptEngine methods --

	@Override
	public Object eval(final String script, final ScriptContext context)
		throws ScriptException
	{
		return engine.eval(script, context);
	}

	@Override
	public Object eval(final Reader reader, final ScriptContext context)
		throws ScriptException
	{
		return engine.eval(reader, context);
	}

	@Override
	public Object eval(final String script) throws ScriptException {
		return engine.eval(script);
	}

	@Override
	public Object eval(final Reader reader) throws ScriptException {
		return engine.eval(reader);
	}

	@Override
	public Object eval(final String script, final Bindings n)
		throws ScriptException
	{
		return engine.eval(script, n);
	}

	@Override
	public Object eval(final Reader reader, final Bindings n)
		throws ScriptException
	{
		return engine.eval(reader, n);
	}

	@Override
	public void put(final String key, final Object value) {
		engine.put(key, value);
	}

	@Override
	public Object get(final String key) {
		return engine.get(key);
	}

	@Override
	public Bindings getBindings(final int scope) {
		return engine.getBindings(scope);
	}

	@Override
	public void setBindings(final Bindings bindings, final int scope) {
		engine.setBindings(bindings, scope);
	}

	@Override
	public Bindings createBindings() {
		return engine.createBindings();
	}

	@Override
	public ScriptContext getContext() {
		return engine.getContext();
	}

	@Override
	public void setContext(final ScriptContext context) {
		engine.setContext(context);
	}

	@Override
	public ScriptEngineFactory getFactory() {
		return engine.getFactory();
	}

}
