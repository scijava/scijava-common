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

import java.util.Collections;
import java.util.List;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;

import org.scijava.module.Module;
import org.scijava.plugin.Plugin;
import org.scijava.plugin.RichPlugin;
import org.scijava.plugin.SingletonPlugin;
import org.scijava.util.VersionUtils;

/**
 * The base interface for scripting language adapters.
 * <p>
 * Every SciJava scripting language implements this interface, which is based on
 * <a href="https://jcp.org/aboutJava/communityprocess/final/jsr223/">JSR
 * 223</a>, Scripting for the Java Platform, included in Java 6 and later in the
 * {@link javax.script} package. This {@link ScriptLanguage} interface extends
 * {@link ScriptEngineFactory}, meaning it can act as a JSR 223 Java scripting
 * language, while also providing additional functionality necessary for full
 * support within applications such as ImageJ. In particular, this interface
 * adds API for code generation of scripts to replicate SciJava {@link Module}
 * executions (i.e., for "script recording" of SciJava commands).
 * </p>
 * <p>
 * Script languages discoverable at runtime must implement this interface and be
 * annotated with @{@link Plugin} with attribute {@link Plugin#type()} =
 * {@link ScriptLanguage}.class. While it possible to create a scripting
 * language adapter merely by implementing this interface, it is encouraged to
 * instead extend {@link AbstractScriptLanguage}, for convenience.
 * </p>
 * 
 * @author Johannes Schindelin
 */
public interface ScriptLanguage extends ScriptEngineFactory, RichPlugin,
	SingletonPlugin
{

	/** True iff this language requires a compilation step. */
	default boolean isCompiledLanguage() {
		return false;
	}

	/**
	 * Performs any necessary conversion of an encoded object retrieved from the
	 * language's script engine.
	 * 
	 * @see ScriptEngine#get(String)
	 */
	default Object decode(final Object object) {
		// NB: No decoding by default.
		return object;
	}

	// -- ScriptEngineFactory methods --

	@Override
	default String getMethodCallSyntax(final String obj, final String m,
		final String... args)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	default String getOutputStatement(final String toDisplay) {
		throw new UnsupportedOperationException();
	}

	@Override
	default String getProgram(final String... statements) {
		throw new UnsupportedOperationException();
	}

	@Override
	default List<String> getExtensions() {
		return Collections.<String> emptyList();
	}

	@Override
	default List<String> getNames() {
		return Collections.<String> singletonList(getEngineName());
	}

	@Override
	default String getLanguageVersion() {
		return VersionUtils.getVersion(getClass());
	}

	@Override
	default List<String> getMimeTypes() {
		return Collections.<String> emptyList();
	}

	@Override
	default Object getParameter(final String key) {
		if (key.equals(ScriptEngine.ENGINE)) {
			return getEngineName();
		}
		else if (key.equals(ScriptEngine.ENGINE_VERSION)) {
			return getEngineVersion();
		}
		else if (key.equals(ScriptEngine.NAME)) {
			final List<String> list = getNames();
			return list.size() > 0 ? list.get(0) : null;
		}
		else if (key.equals(ScriptEngine.LANGUAGE)) {
			return getLanguageName();
		}
		else if (key.equals(ScriptEngine.LANGUAGE_VERSION)) {
			return getLanguageVersion();
		}
		return null;
	}

	@Override
	default String getEngineVersion() {
		return "0.0";
	}

}
