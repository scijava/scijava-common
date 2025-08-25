/*-
 * #%L
 * SciJava Common shared library for SciJava software.
 * %%
 * Copyright (C) 2009 - 2025 SciJava developers.
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

package org.scijava.text;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.scijava.Context;
import org.scijava.plugin.Plugin;

/** Tests {@link TextService}. */
public class TextServiceTest {

	private Context ctx;
	private TextService textService;

	@Before
	public void setUp() {
		ctx = new Context(TextService.class);
		textService = ctx.service(TextService.class);
	}

	@After
	public void tearDown() {
		ctx.dispose();
	}

	@Test
	public void testGetHandler() {
		final TextFormat fooHandler = textService.getHandler(new File("data.foo"));
		assertNotNull(fooHandler);
		assertEquals(FooTextFormat.class, fooHandler.getClass());
		System.out.println(fooHandler);

		final TextFormat barHandler = textService.getHandler(new File("data.bar"));
		assertNull(barHandler);
	}

	@Plugin(type = TextFormat.class)
	public static class FooTextFormat extends AbstractTextFormat {

		@Override
		public List<String> getExtensions() {
			return Arrays.asList("foo");
		}

		@Override
		public String asHTML(final String text) {
			return "[FOO]" + text + "[/FOO]";
		}
	}
}
