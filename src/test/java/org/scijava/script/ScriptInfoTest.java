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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.scijava.Context;
import org.scijava.ItemIO;
import org.scijava.ItemVisibility;
import org.scijava.log.LogService;
import org.scijava.module.ModuleItem;
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
		scriptService = context.service(ScriptService.class);
	}

	@AfterClass
	public static void tearDown() {
		context.dispose();
	}

	// -- Tests --

	/**
	 * Tests that the return value <em>is</em> appended as an extra output when no
	 * explicit outputs were declared.
	 */
	@Test
	public void testReturnValueAppended() throws Exception {
		final String script = "" + //
			"% @LogService log\n" + //
			"% @int value\n";
		final ScriptModule scriptModule =
			scriptService.run("include-return-value.bsizes", script, true).get();

		final Map<String, Object> outputs = scriptModule.getOutputs();
		assertEquals(1, outputs.size());
		assertTrue(outputs.containsKey(ScriptModule.RETURN_VALUE));
	}

	/**
	 * Tests that the return value is <em>not</em> appended as an extra output
	 * when explicit outputs were declared.
	 */
	@Test
	public void testReturnValueExcluded() throws Exception {
		final String script = "" + //
			"% @LogService log\n" + //
			"% @OUTPUT int value\n";
		final ScriptModule scriptModule =
			scriptService.run("exclude-return-value.bsizes", script, true).get();

		final Map<String, Object> outputs = scriptModule.getOutputs();
		assertEquals(1, outputs.size());
		assertTrue(outputs.containsKey("value"));
		assertFalse(outputs.containsKey(ScriptModule.RETURN_VALUE));
	}


	/**
	 * Ensures parameters are parsed correctly from scripts, even in the presence
	 * of noise like e-mail addresses.
	 */
	@Test
	public void testNoisyParameters() throws Exception {
		final String script = "" + //
			"% @LogService log\n" + //
			"% @OUTPUT Integer output" + //
			"% kraken@blah.net\n";
		final ScriptModule scriptModule =
			scriptService.run("hello.bsizes", script, true).get();

		final Object output = scriptModule.getReturnValue();

		if (output == null) fail("null result");
		else if (!(output instanceof Integer)) {
			fail("result is a " + output.getClass().getName());
		}
		else assertEquals(3, ((Integer) output).intValue());
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
		final String sha1 = "28f4a2880d604774ac5d604d35f431047a087c9e";
		assertTrue(version.matches("^" + sha1 + "$"));

		// clean up the temporary directory
		FileUtils.deleteRecursively(tmpDir);
	}

	/**
	 * Tests {@link ScriptInfo} parameter declarations, including
	 * {@link ScriptInfo#inputs()}, {@link ScriptInfo#outputs()},
	 * {@link ScriptInfo#getInput(String)} and
	 * {@link ScriptInfo#getOutput(String)}.
	 */
	@Test
	public void testParameters() {
		final String script = "" + //
			"% @LogService(required = false) log\n" + //
			"% @int(label=\"Slider Value\", softMin=5, softMax=15, " + //
			"stepSize=3, value=11, style=\"slider\") sliderValue\n" + //
			"% @String(persist = false, family='Carnivora', " + //
			"choices={'quick brown fox', 'lazy dog'}) animal\n" + //
			"% @String(visibility=MESSAGE) msg\n" + //
			"% @BOTH java.lang.StringBuilder buffer";

		final ScriptInfo info =
			new ScriptInfo(context, "params.bsizes", new StringReader(script));

		final List<?> noChoices = Collections.emptyList();

		final ModuleItem<?> log = info.getInput("log");
		assertItem("log", LogService.class, null, ItemIO.INPUT, false, true, null,
			null, null, null, null, null, null, null, noChoices, log);

		final ModuleItem<?> sliderValue = info.getInput("sliderValue");
		assertItem("sliderValue", int.class, "Slider Value", ItemIO.INPUT, true,
			true, null, "slider", 11, null, null, 5, 15, 3.0, noChoices, sliderValue);

		final ModuleItem<?> animal = info.getInput("animal");
		final List<String> animalChoices = //
			Arrays.asList("quick brown fox", "lazy dog");
		assertItem("animal", String.class, null, ItemIO.INPUT, true, false,
			null, null, null, null, null, null, null, null, animalChoices, animal);
		assertEquals(animal.get("family"), "Carnivora"); // test custom attribute

		final ModuleItem<?> msg = info.getInput("msg");
		assertSame(ItemVisibility.MESSAGE, msg.getVisibility());

		final ModuleItem<?> buffer = info.getOutput("buffer");
		assertItem("buffer", StringBuilder.class, null, ItemIO.BOTH, true, true,
			null, null, null, null, null, null, null, null, noChoices, buffer);

		int inputCount = 0;
		final ModuleItem<?>[] inputs = { log, sliderValue, animal, msg, buffer };
		for (final ModuleItem<?> inItem : info.inputs()) {
			assertSame(inputs[inputCount++], inItem);
		}

		int outputCount = 0;
		final ModuleItem<?>[] outputs = { buffer };
		for (final ModuleItem<?> outItem : info.outputs()) {
			assertSame(outputs[outputCount++], outItem);
		}
	}

	private void assertItem(final String name, final Class<?> type,
		final String label, final ItemIO ioType, final boolean required,
		final boolean persist, final String persistKey, final String style,
		final Object value, final Object min, final Object max,
		final Object softMin, final Object softMax, final Number stepSize,
		final List<?> choices, final ModuleItem<?> item)
	{
		assertEquals(name, item.getName());
		assertSame(type, item.getType());
		assertEquals(label, item.getLabel());
		assertSame(ioType, item.getIOType());
		assertEquals(required, item.isRequired());
		assertEquals(persist, item.isPersisted());
		assertEquals(persistKey, item.getPersistKey());
		assertEquals(style, item.getWidgetStyle());
		assertEquals(value, item.getDefaultValue());
		assertEquals(min, item.getMinimumValue());
		assertEquals(max, item.getMaximumValue());
		assertEquals(softMin, item.getSoftMinimum());
		assertEquals(softMax, item.getSoftMaximum());
		assertEquals(stepSize, item.getStepSize());
		assertEquals(choices, item.getChoices());
	}

	/**
	 * Ensures the ScriptInfos Reader can be reused for multiple executions of the
	 * script.
	 */
	@Test
	public void testReaderSanity() throws Exception {
		final String script = "" + //
			"% @LogService log\n" + //
			"% @OUTPUT Integer output";

		final ScriptInfo info =
			new ScriptInfo(context, "hello.bsizes", new StringReader(script));
		final BufferedReader reader1 = info.getReader();
		final BufferedReader reader2 = info.getReader();

		assertEquals("Readers are not independent.", reader1.read(), reader2.read());

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
	{
		// NB: No implementation needed.
	}

}
