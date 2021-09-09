/*
 * #%L
 * SciJava Common shared library for SciJava software.
 * %%
 * Copyright (C) 2009 - 2021 SciJava developers.
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.scijava.Context;
import org.scijava.plugin.Plugin;

/**
 * Basic tests for the {@link ScriptService}.
 * 
 * @author Johannes Schindelin
 * @author Curtis Rueden
 */
public class ScriptEngineTest {

	private Context context;
	private ScriptService scriptService;

	@Before
	public void setUp() {
		context = new Context(ScriptService.class);
		scriptService = context.getService(ScriptService.class);
	}

	@After
	public void tearDown() {
		context.dispose();
		context = null;
		scriptService = null;
	}

	@Test
	public void testRot13() throws Exception {
		final ScriptLanguage hello = scriptService.getLanguageByName("Hello");
		assertNotNull(hello);
		final ScriptLanguage rot13 = scriptService.getLanguageByName("Rot13");
		assertEquals(hello, rot13);
		assertEquals("Svool", rot13.getScriptEngine().eval("Hello"));
	}

	@Test
	public void testScriptModuleValue() throws Exception {
		final ScriptModule module =
			scriptService.run("test.rot13", ScriptModule.class.getName(), false,
				(Map<String, Object>) null).get();
		final ScriptModule scriptModule = Rot13Engine.latestModule;
		assertEquals(module, scriptModule);
		assertNotNull(scriptModule);
		final ScriptInfo info = scriptModule.getInfo();
		assertEquals(context, info.context());
	}

	@Test
	public void testAutoCompleter() {
		final ScriptLanguage hello = scriptService.getLanguageByName("Hello");
		final ScriptEngine engine = hello.getScriptEngine();
		final AutoCompleter ac = hello.getAutoCompleter();

		// test all matches
		engine.put("thing", new Object());
		final AutoCompletionResult result = ac.autocomplete("thing.", engine);
		assertEquals(0, result.getStartIndex());
		final List<String> matches = result.getMatches();
		final List<String> expected = Arrays.asList("thing.equals(",
			"thing.getClass(", "thing.hashCode(", "thing.notify(", "thing.notifyAll(",
			"thing.toString(", "thing.wait(");
		assertEquals(matches, expected);

		// test prefix
		engine.put("hello", "world");
		final AutoCompletionResult cWords = ac.autocomplete("hello.c", engine);
		assertEquals(0, cWords.getStartIndex());
		final List<String> cMatches = cWords.getMatches();
		final List<String> cExpected = Arrays.asList("hello.CASE_INSENSITIVE_ORDER",
			"hello.charAt(", "hello.chars(", "hello.codePointAt(",
			"hello.codePointBefore(", "hello.codePointCount(", "hello.codePoints(",
			"hello.compareTo(", "hello.compareToIgnoreCase(", "hello.concat(",
			"hello.contains(", "hello.contentEquals(", "hello.copyValueOf(");
		assertEquals(cMatches, cExpected);
	}

	@Plugin(type = ScriptLanguage.class)
	public static class Rot13 extends AbstractScriptLanguage {

		@Override
		public ScriptEngine getScriptEngine() {
			return new Rot13Engine();
		}

		@Override
		public List<String> getNames() {
			return Arrays.asList("Hello", "World", "Rot13");
		}

		@Override
		public List<String> getExtensions() {
			return Arrays.asList("rot13");
		}
	}

	private static class Rot13Engine extends AbstractScriptEngine {

		{
			engineScopeBindings = new Rot13Bindings();
		}

		private static ScriptModule latestModule;

		@Override
		public Object eval(String script) throws ScriptException {
			return eval(new StringReader(script));
		}

		@Override
		public Object eval(Reader reader) throws ScriptException {
			latestModule = (ScriptModule) get(ScriptModule.class.getName());
			final StringBuilder builder = new StringBuilder();
			try {
				for (;;) {
					int c = reader.read();
					if (c < 0) {
						break;
					}
					if (c >= 'A' && c <= 'Z') {
						c = 'Z' - c + 'A';
					} else if (c >= 'a' && c <= 'z') {
						c = 'z' - c + 'a';
					}
					builder.append((char) c);
				}
			} catch (final IOException e) {
				throw new ScriptException(e);
			}
			return builder.toString();
		}
	}

	private static class Rot13Bindings extends HashMap<String, Object> implements Bindings {
		private static final long serialVersionUID = 1L;
	}
}
