/*
 * #%L
 * SciJava Common shared library for SciJava software.
 * %%
 * Copyright (C) 2009 - 2020 SciJava developers.
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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.scijava.Context;
import org.scijava.ItemIO;
import org.scijava.ItemVisibility;
import org.scijava.MenuPath;
import org.scijava.Priority;
import org.scijava.log.LogService;
import org.scijava.module.ModuleItem;
import org.scijava.plugin.Plugin;
import org.scijava.test.TestUtils;
import org.scijava.util.DigestUtils;
import org.scijava.util.FileUtils;
import org.scijava.widget.WidgetStyle;

/**
 * Tests {@link ScriptInfo}.
 * 
 * @author Curtis Rueden
 * @author Mark Hiner
 */
public class ScriptInfoTest {

	private Context context;
	private ScriptService scriptService;

	// -- Test setup --

	@Before
	public void setUp() {
		context = new Context();
		scriptService = context.service(ScriptService.class);
	}

	@After
	public void tearDown() {
		context.dispose();
	}

	// -- Tests --

	/** Tests script identifiers. */
	@Test
	public void testGetIdentifier() {
		final String name = "strategerize";

		final String namedPath = "victory.bsizes";
		final String named = "" + //
			"#@script(name = '" + name + "')\n" + //
			"zxywvutsrqponmlkjihgfdcba\n";

		final String unnamedPath = "alphabet.bsizes";
		final String unnamed = "" + //
			"ABCDEFGHIJKLMNOPQRSTUVWXYZ\n" + //
			"0123456789\n";

		// Test named, with explicit path.
		assertEquals("script:" + name, id(namedPath, named));

		// Test named, and no path given.
		assertEquals("script:" + name, id(null, named));

		// Test unnamed, with explicit path.
		assertEquals("script:" + unnamedPath, id(unnamedPath, unnamed));

		// Test unnamed, and no path given.
		final String hex = DigestUtils.bestHex(unnamed);
		assertEquals("script:<" + hex + ">", id(null, unnamed));
	}

	/** Tests whether new-style parameter syntax are parsed correctly. */
	@Test
	public void testNewStyle() throws Exception {
		final String script = "" + //
			"##########\n" + //
			"# Inputs #\n" + //
			"##########\n" + //
			"#@input int stuff\n" + //
			"#@input int things\n" + //
			"\n" + //
			"###########\n" + //
			"# Credits #\n" + //
			"###########\n" + //
			"Brought to you by:\n" + //
			"person@example.com\n" + //
			"\n" + //
			"###########\n" + //
			"# Outputs #\n" + //
			"###########\n" + //
			"#@output String blackHoles\n" +
			"#@output String revelations\n" +
			"\n" + //
			"THE END!\n";
		final ScriptModule scriptModule =
			scriptService.run("newStyle.bsizes", script, true).get();

		final String expectedProcessed = script.replaceAll("#@.*", "");
		final String actualProcessed = scriptModule.getInfo().getProcessedScript();
		assertEquals(expectedProcessed, actualProcessed);

		final Object output = scriptModule.getReturnValue();

		if (output == null) fail("null result");
		else if (!(output instanceof Integer)) {
			fail("result is a " + output.getClass().getName());
		}
		else assertEquals(4, ((Integer) output).intValue());
	}

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
			"#@ LogService (required = false) log\n" + //
			"#@ int (label=\"Slider Value\", softMin=5, softMax=15, " + //
			"stepSize=3, value=11, style=\" slidEr,\") sliderValue\n" + //
			"#@ String (persist = false, family='Carnivora', " + //
			"choices={'quick brown fox', 'lazy dog'}) animal\n" + //
			"#@ Double (autoFill = false) notAutoFilled\n" + //
			"#@ String (visibility=MESSAGE) msg\n" + //
			"#@BOTH java.lang.StringBuilder buffer";

		final ScriptInfo info =
			new ScriptInfo(context, "params.bsizes", new StringReader(script));

		final List<?> noChoices = Collections.emptyList();

		final ModuleItem<?> log = info.getInput("log");
		assertItem("log", LogService.class, null, ItemIO.INPUT, false, true, null,
			null, null, null, null, null, null, null, noChoices, log);

		final ModuleItem<?> sliderValue = info.getInput("sliderValue");
		assertItem("sliderValue", int.class, "Slider Value", ItemIO.INPUT, true,
			true, null, " slidEr,", 11, null, null, 5, 15, 3.0, noChoices, sliderValue);
		assertTrue("Case-insensitive trimmed style", WidgetStyle.isStyle(sliderValue, "slider"));

		final ModuleItem<?> animal = info.getInput("animal");
		final List<String> animalChoices = //
			Arrays.asList("quick brown fox", "lazy dog");
		assertItem("animal", String.class, null, ItemIO.INPUT, true, false,
			null, null, null, null, null, null, null, null, animalChoices, animal);
		assertEquals(animal.get("family"), "Carnivora"); // test custom attribute

		final ModuleItem<?> notAutoFilled = info.getInput("notAutoFilled");
		assertFalse(notAutoFilled.isAutoFill());

		final ModuleItem<?> msg = info.getInput("msg");
		assertSame(ItemVisibility.MESSAGE, msg.getVisibility());

		final ModuleItem<?> buffer = info.getOutput("buffer");
		assertItem("buffer", StringBuilder.class, null, ItemIO.BOTH, true, true,
			null, null, null, null, null, null, null, null, noChoices, buffer);

		int inputCount = 0;
		final ModuleItem<?>[] inputs = { log, sliderValue, animal, notAutoFilled, msg, buffer };
		for (final ModuleItem<?> inItem : info.inputs()) {
			assertSame(inputs[inputCount++], inItem);
		}

		int outputCount = 0;
		final ModuleItem<?>[] outputs = { buffer };
		for (final ModuleItem<?> outItem : info.outputs()) {
			assertSame(outputs[outputCount++], outItem);
		}
	}

	/** Tests {@code #@script} directives. */
	@Test
	public void testScriptDirective() {
		final String script = "" + //
			"#@script(name = \"my_script\"" + //
			", label = \"My Script\"" + //
			", description = \"What a great script.\"" + //
			", menuPath = \"Plugins > Do All The Things\"" + //
			", menuRoot = \"special\"" + //
			", iconPath = \"/path/to/myIcon.png\"" + //
			", priority = \"extremely-high\"" + //
			", headless = true" + //
			", foo = \"bar\"" + //
			")\n" +
			"WOOT\n";

		ScriptInfo info = null;
		info =
			new ScriptInfo(context, "scriptHeader.bsizes", new StringReader(script));
		info.inputs(); // HACK: Force lazy initialization.

		assertEquals("my_script", info.getName());
		assertEquals("My Script", info.getLabel());
		assertEquals("What a great script.", info.getDescription());
		final MenuPath menuPath = info.getMenuPath();
		assertEquals(2, menuPath.size());
		assertEquals("Plugins", menuPath.get(0).getName());
		assertEquals("Do All The Things", menuPath.get(1).getName());
		assertEquals("/path/to/myIcon.png", info.getIconPath());
		assertEquals(Priority.EXTREMELY_HIGH, info.getPriority(), 0.0);
		assertTrue(info.canRunHeadless());
		assertEquals("bar", info.get("foo"));
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

	// -- Helper methods --

	private String id(final String path, final String script) {
		final ScriptInfo info = //
			new ScriptInfo(context, path, new StringReader(script));
		info.inputs(); // NB: Force parameter parsing.
		return info.getIdentifier();
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

	// -- Test script language --

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
