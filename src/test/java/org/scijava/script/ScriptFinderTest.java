/*
 * #%L
 * SciJava Common shared library for SciJava software.
 * %%
 * Copyright (C) 2009 - 2014 Board of Regents of the University of
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
		TestUtils.createPath(scriptsDir, "ignored.foo");
		TestUtils.createPath(scriptsDir, "Scripts/quick.foo");
		TestUtils.createPath(scriptsDir, "Scripts/brown.foo");
		TestUtils.createPath(scriptsDir, "Scripts/fox.foo");
		TestUtils.createPath(scriptsDir, "Scripts/The_Lazy_Dog.foo");
		TestUtils.createPath(scriptsDir, "Math/add.foo");
		TestUtils.createPath(scriptsDir, "Math/subtract.foo");
		TestUtils.createPath(scriptsDir, "Math/multiply.foo");
		TestUtils.createPath(scriptsDir, "Math/divide.foo");
		TestUtils.createPath(scriptsDir, "Math/Trig/cos.foo");
		TestUtils.createPath(scriptsDir, "Math/Trig/sin.foo");
		TestUtils.createPath(scriptsDir, "Math/Trig/tan.foo");
	}

	@AfterClass
	public static void tearDown() {
		FileUtils.deleteRecursively(scriptsDir);
	}

	// -- Unit tests --

	@Test
	public void testFindScripts() {
		final Context context = new Context(ScriptService.class);
		final ScriptService scriptService = context.service(ScriptService.class);
		scriptService.addScriptDirectory(scriptsDir);

		final ArrayList<ScriptInfo> scripts = findScripts(scriptService);

		assertEquals(11, scripts.size());
		assertMenuPath("Scripts > The Lazy Dog", scripts, 0);
		assertMenuPath("Math > add", scripts, 1);
		assertMenuPath("Scripts > brown", scripts, 2);
		assertMenuPath("Math > Trig > cos", scripts, 3);
		assertMenuPath("Math > divide", scripts, 4);
		assertMenuPath("Scripts > fox", scripts, 5);
		assertMenuPath("Math > multiply", scripts, 6);
		assertMenuPath("Scripts > quick", scripts, 7);
		assertMenuPath("Math > Trig > sin", scripts, 8);
		assertMenuPath("Math > subtract", scripts, 9);
		assertMenuPath("Math > Trig > tan", scripts, 10);
	}

	/**
	 * Tests that menu prefixes work as expected when
	 * {@link ScriptService#addScriptDirectory(File, org.scijava.MenuPath)} is
	 * called.
	 */
	@Test
	public void testMenuPrefixes() {
		final Context context = new Context(ScriptService.class);
		final ScriptService scriptService = context.service(ScriptService.class);

		final MenuPath menuPrefix = new MenuPath("Foo > Bar");
		assertEquals(2, menuPrefix.size());
		assertEquals("Bar", menuPrefix.getLeaf().getName());
		scriptService.addScriptDirectory(scriptsDir, menuPrefix);

		final ArrayList<ScriptInfo> scripts = findScripts(scriptService);

		assertEquals(12, scripts.size());
		assertMenuPath("Foo > Bar > Scripts > The Lazy Dog", scripts, 0);
		assertMenuPath("Foo > Bar > Math > add", scripts, 1);
		assertMenuPath("Foo > Bar > Scripts > brown", scripts, 2);
		assertMenuPath("Foo > Bar > Math > Trig > cos", scripts, 3);
		assertMenuPath("Foo > Bar > Math > divide", scripts, 4);
		assertMenuPath("Foo > Bar > Scripts > fox", scripts, 5);
		assertMenuPath("Foo > Bar > ignored", scripts, 6);
		assertMenuPath("Foo > Bar > Math > multiply", scripts, 7);
		assertMenuPath("Foo > Bar > Scripts > quick", scripts, 8);
		assertMenuPath("Foo > Bar > Math > Trig > sin", scripts, 9);
		assertMenuPath("Foo > Bar > Math > subtract", scripts, 10);
		assertMenuPath("Foo > Bar > Math > Trig > tan", scripts, 11);
	}

	/**
	 * Tests that scripts are discovered only once when present in multiple base
	 * directories.
	 */
	@Test
	public void testOverlappingDirectories() {
		final Context context = new Context(ScriptService.class);
		final ScriptService scriptService = context.service(ScriptService.class);

		// Scripts -> Plugins
		scriptService.addScriptDirectory(new File(scriptsDir, "Scripts"),
			new MenuPath("Plugins"));
		// everything else "in place"
		scriptService.addScriptDirectory(scriptsDir);

		final ArrayList<ScriptInfo> scripts = findScripts(scriptService);

		assertEquals(11, scripts.size());
		assertMenuPath("Plugins > The Lazy Dog", scripts, 0);
		assertMenuPath("Math > add", scripts, 1);
		assertMenuPath("Plugins > brown", scripts, 2);
		assertMenuPath("Math > Trig > cos", scripts, 3);
		assertMenuPath("Math > divide", scripts, 4);
		assertMenuPath("Plugins > fox", scripts, 5);
		assertMenuPath("Math > multiply", scripts, 6);
		assertMenuPath("Plugins > quick", scripts, 7);
		assertMenuPath("Math > Trig > sin", scripts, 8);
		assertMenuPath("Math > subtract", scripts, 9);
		assertMenuPath("Math > Trig > tan", scripts, 10);
	}

	// -- Helper methods --

	private ArrayList<ScriptInfo> findScripts(final ScriptService scriptService) {
		final ScriptFinder scriptFinder = new ScriptFinder(scriptService);
		final ArrayList<ScriptInfo> scripts = new ArrayList<ScriptInfo>();
		scriptFinder.findScripts(scripts);
		Collections.sort(scripts);
		return scripts;
	}

	private void assertMenuPath(final String menuString,
		final ArrayList<ScriptInfo> scripts, final int i)
	{
		assertEquals(menuString, scripts.get(i).getMenuPath().getMenuString());
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
