/*
 * #%L
 * SciJava Common shared library for SciJava software.
 * %%
 * Copyright (C) 2009 - 2023 SciJava developers.
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
import java.io.Writer;
import java.util.List;

import javax.script.Bindings;
import javax.script.ScriptContext;

/**
 * A minimal implementation of {@link javax.script.ScriptContext} for use in the
 * {@link AbstractScriptEngine}.
 * 
 * @author Johannes Schindelin
 */
public class AbstractScriptContext implements ScriptContext {

	protected Reader reader;
	protected Writer writer, errorWriter;

	@Override
	public Reader getReader() {
		return reader;
	}

	@Override
	public Writer getWriter() {
		return writer;
	}

	@Override
	public Writer getErrorWriter() {
		return errorWriter;
	}

	@Override
	public void setReader(final Reader reader) {
		this.reader = reader;
	}

	@Override
	public void setWriter(final Writer writer) {
		this.writer = writer;
	}

	@Override
	public void setErrorWriter(final Writer errorWriter) {
		this.errorWriter = errorWriter;
	}

	// (Possibly) unsupported operations
	// The SciJava scripting framework does not require them to work properly.

	@Override
	public Object getAttribute(final String key) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object getAttribute(final String key, final int value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int getAttributesScope(final String scope) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Bindings getBindings(final int scope) {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<Integer> getScopes() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object removeAttribute(final String key, final int scope) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void
		setAttribute(final String key, final Object value, final int scope)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void setBindings(final Bindings bindings, final int scope) {
		throw new UnsupportedOperationException();
	}

}
