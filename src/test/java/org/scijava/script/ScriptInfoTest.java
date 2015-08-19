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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.scijava.Context;
import org.scijava.plugin.Plugin;
import org.scijava.test.TestUtils;
import org.scijava.util.DigestUtils;
import org.scijava.util.FileUtils;

/** Tests {@link ScriptInfo}. */
public class ScriptInfoTest {

	private static Context context;
	private static ScriptService scriptService;

	// -- Test setup --

	@BeforeClass
	public static void setUp() {
		context = new Context();
		scriptService = context.getService(ScriptService.class);
	}

	@AfterClass
	public static void tearDown() {
		context.dispose();
	}

	// -- Tests --

	/**
	 * Ensures parameters are parsed correctly from scripts, even in the presence
	 * of noise like e-mail addresses.
	 */
	@Test
	public void testParameterParsing() throws Exception {
		final String script = "" + //
			"% @LogService log\n" + //
			"% @OUTPUT Integer output" + //
			"% kraken@blah.net\n";
		final ScriptModule scriptModule =
			scriptService.run("hello.bsizes", script, true).get();

		final Object output = scriptModule.getOutput("result");

		if (output == null || !(output instanceof Integer)) fail();
		assertEquals(3, ((Integer) output).intValue());
	}

	/** Tests {@link ScriptInfo#getVersion()}. */
	@Test
	public void testVersion() throws IOException {
		final String script = "" + //
			"% @LogService log\n" + //
			"% @OUTPUT int output";

		// write script to a temporary directory on disk
		final File tmpDir = TestUtils.createTemporaryDirectory("script-info-test-");
		final String path = "hello.bsizes";
		final File scriptFile = new File(tmpDir, path);
		FileUtils.writeFile(scriptFile, DigestUtils.bytes(script));

		// verify that the version is correct
		final ScriptInfo info = new ScriptInfo(context, scriptFile);
		final String version = info.getVersion();
		final String timestampPattern = "\\d{4}-\\d{2}-\\d{2}-\\d{2}:\\d{2}:\\d{2}";
		final String sha1 = "28f4a2880d604774ac5d604d35f431047a087c9e";
		assertTrue(version.matches("^" + timestampPattern + "-" + sha1 + "$"));

		// clean up the temporary directory
		FileUtils.deleteRecursively(tmpDir);
	}

	@Plugin(type = ScriptLanguage.class)
	public static class BindingSizes extends AbstractScriptLanguage {

		@Override
		public ScriptEngine getScriptEngine() {
			return new BindingSizesEngine();
		}

		@Override
		public List<String> getNames() {
			return Arrays.asList("BindingSizes");
		}

		@Override
		public List<String> getExtensions() {
			return Arrays.asList("bsizes");
		}
	}

	// -- Test script langauge --

	private static class BindingSizesEngine extends AbstractScriptEngine {

		{
			engineScopeBindings = new BindingSizesBindings();
		}

		@Override
		public Object eval(final String script) throws ScriptException {
			return eval(new StringReader(script));
		}

		@Override
		public Object eval(final Reader reader) throws ScriptException {
			final Bindings bindings = getBindings(ScriptContext.ENGINE_SCOPE);
			return bindings.size();
		}
	}

	private static class BindingSizesBindings extends HashMap<String, Object>
		implements Bindings
	{}
}
