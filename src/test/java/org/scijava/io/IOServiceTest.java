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
package org.scijava.io;

import org.junit.Test;
import org.scijava.Context;
import org.scijava.io.location.FileLocation;
import org.scijava.plugin.PluginInfo;
import org.scijava.text.AbstractTextFormat;
import org.scijava.text.TextFormat;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class IOServiceTest {

	@Test
	public void testTextFile() throws IOException {
		// create context, add dummy text format
		final Context ctx = new Context();
		ctx.getPluginIndex().add(new PluginInfo<>(DummyTextFormat.class, TextFormat.class));
		final IOService io = ctx.getService(IOService.class);

		// open text file from resources as String
		String localFile = getClass().getResource("test.txt").getPath();
		Object obj = io.open(localFile);
		assertNotNull(obj);
		String content = obj.toString();
		assertTrue(content.contains("content"));

		// open text file from resources as FileLocation
		obj = io.open(new FileLocation(localFile));
		assertNotNull(obj);
		assertEquals(content, obj.toString());
	}


	public static class DummyTextFormat  extends AbstractTextFormat {

		@Override
		public List<String> getExtensions() {
			return Collections.singletonList("txt");
		}

		@Override
		public String asHTML(String text) {
			return text;
		}
	}
}
