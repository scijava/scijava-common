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
import static org.junit.Assert.assertSame;

import java.io.File;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.List;

import javax.script.ScriptException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.scijava.Context;
import org.scijava.script.ScriptInfoTest.BindingSizes;
import org.scijava.util.AppUtils;
import org.scijava.util.ColorRGB;
import org.scijava.util.ColorRGBA;

/**
 * Tests the {@link DefaultScriptService}.
 * 
 * @author Curtis Rueden
 */
public class ScriptServiceTest {

	private Context context;
	private ScriptService scriptService;

	@Before
	public void setUp() {
		context = new Context(ScriptService.class);
		scriptService = context.service(ScriptService.class);
	}
	
	@After
	public void tearDown() {
		context.dispose();
	}

	/**
	 * Tests that the "scijava.scripts.path" system property is handled correctly.
	 */
	@Test
	public void testSystemProperty() {
		final String slash = File.separator;
		final String sep = File.pathSeparator;
		final String root = new File(slash).getAbsolutePath();
		final String dir1 = root + "foo" + slash + "bar";
		final String dir2 = root + "to" + slash + "the" + slash + "moon";
		System.setProperty("scijava.scripts.path", dir1 + sep + dir2);

		final List<File> scriptDirs = scriptService.getScriptDirectories();
		assertEquals(3, scriptDirs.size());

		final File baseDir = AppUtils.getBaseDirectory(ScriptService.class);
		final String dir0 = baseDir.getPath() + slash + "scripts";
		assertEquals(dir0, scriptDirs.get(0).getAbsolutePath());
		assertEquals(dir1, scriptDirs.get(1).getAbsolutePath());
		assertEquals(dir2, scriptDirs.get(2).getAbsolutePath());
	}

	@Test
	public void testBuiltInAliases() throws ScriptException {
		final Class<?>[] builtIns = { boolean.class, byte.class, char.class,
			double.class, float.class, int.class, long.class, short.class,
			Boolean.class, Byte.class, Character.class, Double.class, Float.class,
			Integer.class, Long.class, Short.class, Context.class, BigDecimal.class,
			BigInteger.class, ColorRGB.class, ColorRGBA.class, Date.class, File.class,
			String.class };

		for (final Class<?> builtIn : builtIns) {
			final Class<?> c = scriptService.lookupClass(builtIn.getSimpleName());
			assertSame(builtIn, c);
		}
	}

	@Test
	public void testArrayAliases() throws ScriptException {
		final Class<?> pInt2D = scriptService.lookupClass("int[][]");
		assertSame(int[][].class, pInt2D);
		final Class<?> pInt1D = scriptService.lookupClass("int[]");
		assertSame(int[].class, pInt1D);
		final Class<?> pInt = scriptService.lookupClass("int");
		assertSame(int.class, pInt);

		final Class<?> oInt2D = scriptService.lookupClass("Integer[][]");
		assertSame(Integer[][].class, oInt2D);
		final Class<?> oInt1D = scriptService.lookupClass("Integer[]");
		assertSame(Integer[].class, oInt1D);
		final Class<?> oInt = scriptService.lookupClass("Integer");
		assertSame(Integer.class, oInt);

		final Class<?> str2D = scriptService.lookupClass("String[][]");
		assertSame(String[][].class, str2D);
		final Class<?> str1D = scriptService.lookupClass("String[]");
		assertSame(String[].class, str1D);
		final Class<?> str = scriptService.lookupClass("String");
		assertSame(String.class, str);
	}

	@Test
	public void testGetScript() {
		String script = "#@ String name\n" + 
				"#@output String greeting\n" + 
				"greeting = \"Hello, \" + name + \"!\"";
		// see ScriptInfoTest for the .bsizes ScriptLanguage used here
		ScriptInfo scriptInfo = scriptService.getScript(".bsizes", script);
		assertEquals(BindingSizes.class, scriptInfo.getLanguage().getClass());
		assertEquals("name", scriptInfo.inputs().iterator().next().getName());
		assertEquals("greeting", scriptInfo.outputs().iterator().next().getName());
	}
}
