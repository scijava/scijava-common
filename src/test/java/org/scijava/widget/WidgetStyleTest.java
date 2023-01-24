/*-
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
package org.scijava.widget;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Enclosed.class)
public class WidgetStyleTest {

	@RunWith(Parameterized.class)
	public static class TestIsStyle {

		static String[] styleStrings = { "foo, bar, someThing", " FOO, BAR, SOMEthing ", "foo  ", "  bar",
				"trash, sOmEtHiNg", null };

		static String[] stylesToTest = { "foo", "bar", "someThing", null };

		static boolean[][] stylesToHave = { // foo, bar, someThing
				new boolean[] { true, true, true, false }, new boolean[] { true, true, true, false },
				new boolean[] { true, false, false, false }, new boolean[] { false, true, false, false },
				new boolean[] { false, false, true, false }, new boolean[] { false, false, false, true } };

		@Parameters(name = "{0}")
		public static List<Object[]> params() {
			return IntStream.range(0, styleStrings.length)
					.mapToObj(i -> new Object[] { styleStrings[i], stylesToHave[i] }).collect(Collectors.toList());
		}

		@Parameter
		public String styleString;

		@Parameter(1)
		public boolean[] targetStyles;

		@Test
		public void testSimpleStyles() {
			for (int i = 0; i < stylesToTest.length; i++) {
				assertEquals("style: " + stylesToTest[i], targetStyles[i],
						WidgetStyle.isStyle(styleString, stylesToTest[i]));
			}
		}
	}

	public static class TestStyleModifiers {
		@Test
		public void testStyleModifiers() {
			String style = "open, extensions:tiff/tif/jpeg/jpg";
			Set<String> extensions = new HashSet<>(Arrays.asList(WidgetStyle.getStyleModifiers(style, "extensions")));
			Set<String> expected = new HashSet<>(Arrays.asList("tiff", "jpg", "jpeg", "tif"));
			assertEquals(expected, extensions);
		}
	}
}
