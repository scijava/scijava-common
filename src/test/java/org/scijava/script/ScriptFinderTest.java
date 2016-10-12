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
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.script.ScriptEngine;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.scijava.Context;
import org.scijava.MenuPath;
import org.scijava.plugin.Plugin;
import org.scijava.test.TestUtils;
import org.scijava.util.AppUtils;
import org.scijava.util.FileUtils;

/**
 * Tests the {@link ScriptFinder}.
 * 
 * @author Curtis Rueden
 */
public class ScriptFinderTest {

	private static File scriptsDir;

	// -- Test setup --

	@BeforeClass
	public static void setUp() throws IOException {
		scriptsDir = TestUtils.createTemporaryDirectory("script-finder-");
		final String[] scriptPaths = { //
			"ignored.foo", //
			"Scripts/quick.foo", //
			"Scripts/brown.foo", //
			"Scripts/fox.foo", //
			"Scripts/The_Lazy_Dog.foo", //
			"Math/add.foo", //
			"Math/subtract.foo", //
			"Math/multiply.foo", //
			"Math/divide.foo", //
			"Math/Trig/cos.foo", //
			"Math/Trig/sin.foo", //
			"Math/Trig/tan.foo", //
		};
		for (final String scriptPath : scriptPaths) {
			TestUtils.createPath(scriptsDir, scriptPath);
		}
	}

	@AfterClass
	public static void tearDown() {
		FileUtils.deleteRecursively(scriptsDir);
	}

	// -- Unit tests --

	@Test
	public void testFindScripts() {
		final ScriptService scriptService = createScriptService();
		scriptService.addScriptDirectory(scriptsDir);

		final ArrayList<ScriptInfo> scripts = findScripts(scriptService);

		final String[] expected = { //
			"Scripts > The Lazy Dog", //
			"Math > add", //
			"Scripts > brown", //
			"Math > Trig > cos", //
			"Math > divide", //
			"Scripts > fox", //
			"Math > multiply", //
			"Math > pow", //
			"Scripts > quick", //
			"Math > Trig > sin", //
			"Math > subtract", //
			"Math > Trig > tan", //
		};
		assertMenuPaths(expected, scripts);
		assertURLsMatch(scripts);
	}

	/**
	 * Tests that menu prefixes work as expected when
	 * {@link ScriptService#addScriptDirectory(File, org.scijava.MenuPath)} is
	 * called.
	 */
	@Test
	public void testMenuPrefixes() {
		final ScriptService scriptService = createScriptService();

		final MenuPath menuPrefix = new MenuPath("Foo > Bar");
		assertEquals(2, menuPrefix.size());
		assertEquals("Bar", menuPrefix.getLeaf().getName());
		scriptService.addScriptDirectory(scriptsDir, menuPrefix);

		final ArrayList<ScriptInfo> scripts = findScripts(scriptService);

		final String[] expected = { //
			"Foo > Bar > Scripts > The Lazy Dog", //
			"Foo > Bar > Math > add", //
			"Foo > Bar > Scripts > brown", //
			"Foo > Bar > Math > Trig > cos", //
			"Foo > Bar > Math > divide", //
			"Foo > Bar > Scripts > fox", //
			"Foo > Bar > ignored", //
			"Foo > Bar > Math > multiply", //
			"Math > pow", //
			"Foo > Bar > Scripts > quick", //
			"Foo > Bar > Math > Trig > sin", //
			"Foo > Bar > Math > subtract", //
			"Foo > Bar > Math > Trig > tan", //
		};
		assertMenuPaths(expected, scripts);
		assertURLsMatch(scripts);
	}

	/**
	 * Tests that scripts are discovered only once when present in multiple base
	 * directories.
	 */
	@Test
	public void testOverlappingDirectories() {
		final ScriptService scriptService = createScriptService();

		// Scripts -> Plugins
		scriptService.addScriptDirectory(new File(scriptsDir, "Scripts"),
			new MenuPath("Plugins"));
		// everything else "in place"
		scriptService.addScriptDirectory(scriptsDir);

		final ArrayList<ScriptInfo> scripts = findScripts(scriptService);

		final String[] expected = { //
			"Plugins > The Lazy Dog", //
			"Math > add", //
			"Plugins > brown", //
			"Math > Trig > cos", //
			"Math > divide", //
			"Plugins > fox", //
			"Math > multiply", //
			"Math > pow", //
			"Plugins > quick", //
			"Math > Trig > sin", //
			"Math > subtract", //
			"Math > Trig > tan", //
		};
		assertMenuPaths(expected, scripts);
		assertURLsMatch(scripts);
	}

	// -- Helper methods --

	private ScriptService createScriptService() {
		final Context context = new Context(ScriptService.class);
		final ScriptService scriptService = context.service(ScriptService.class);
		final File defaultScriptsDir =
			new File(AppUtils.getBaseDirectory(ScriptFinder.class), "scripts");
		scriptService.removeScriptDirectory(defaultScriptsDir);
		return scriptService;
	}

	private ArrayList<ScriptInfo> findScripts(final ScriptService scriptService) {
		final ScriptFinder scriptFinder = new ScriptFinder(scriptService.context());
		final ArrayList<ScriptInfo> scripts = new ArrayList<>();
		scriptFinder.findScripts(scripts);
		Collections.sort(scripts);
		return scripts;
	}

	private void assertMenuPaths(final String[] expected,
		final ArrayList<ScriptInfo> scripts)
	{
		assertEquals(expected.length, scripts.size());
		for (int i=0; i<expected.length; i++) {
			final String actual = scripts.get(i).getMenuPath().getMenuString();
			assertEquals(expected[i], actual);
		}
	}

	private void assertURLsMatch(final ArrayList<ScriptInfo> scripts) {
		for (final ScriptInfo info : scripts) {
			final String urlPath = info.getURL().getPath();
			final String path = info.getPath();
			assertTrue(urlPath + " <> " + path, urlPath.endsWith("/" + path));
		}
	}

	// -- Helper classes --

	/** "Handles" scripts with .foo extension. */
	@Plugin(type = ScriptLanguage.class)
	public static class FooScriptLanguage extends AbstractScriptLanguage {

		@Override
		public List<String> getExtensions() {
			return Arrays.asList("foo");
		}

		@Override
		public ScriptEngine getScriptEngine() {
			// NB: Should never be called by the unit tests.
			throw new IllegalStateException();
		}

	}

}
