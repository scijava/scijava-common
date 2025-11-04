/*
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

package org.scijava.input;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

import org.junit.Test;
import org.scijava.plugin.PluginInfo;

/**
 * Tests {@link KeyCode}.
 * 
 * @author Curtis Rueden
 */
public class KeyCodeTest {

	@Test
	public void testGetInt() {
		assertEquals(KeyCode.ENTER, KeyCode.get(0x0a));
		assertEquals(KeyCode.PLUS, KeyCode.get(0x0209));
		assertEquals(KeyCode.NUM0, KeyCode.get(0x30));
		assertEquals(KeyCode.NUMPAD_0, KeyCode.get(0x60));
		assertEquals(KeyCode.A, KeyCode.get(0x41));
		assertEquals(KeyCode.Z, KeyCode.get(0x5a));

		assertEquals(KeyCode.UNDEFINED, KeyCode.get(0xaaaa));
		assertEquals(KeyCode.UNDEFINED, KeyCode.get(0xffff));
	}

	@Test
	public void testGetChar() {
		assertEquals(KeyCode.ENTER, KeyCode.get('\n'));
		assertEquals(KeyCode.ENTER, KeyCode.get('\r'));
		assertEquals(KeyCode.PLUS, KeyCode.get('+'));
		assertEquals(KeyCode.NUM0, KeyCode.get('0'));
		assertEquals(KeyCode.A, KeyCode.get('a'));
		assertEquals(KeyCode.A, KeyCode.get('A'));
		assertEquals(KeyCode.Z, KeyCode.get('z'));
		assertEquals(KeyCode.Z, KeyCode.get('Z'));

		assertEquals(KeyCode.UNDEFINED, KeyCode.get('\0'));

		// The following should maybe be considered a bug.
		assertEquals(KeyCode.UNDEFINED, KeyCode.get('|'));
	}

	@Test
	public void testGetString() {
		assertEquals(KeyCode.PLUS, KeyCode.get("PLUS"));
		assertEquals(KeyCode.NUM0, KeyCode.get("NUM0"));
		assertEquals(KeyCode.NUMPAD_0, KeyCode.get("NUMPAD_0"));
		assertEquals(KeyCode.A, KeyCode.get("A"));
		assertEquals(KeyCode.Z, KeyCode.get("Z"));

		assertEquals(KeyCode.UNDEFINED, KeyCode.get("UNDEFINED"));
		assertEquals(KeyCode.UNDEFINED, KeyCode.get(""));
		assertEquals(KeyCode.UNDEFINED, KeyCode.get("aa"));
		assertEquals(KeyCode.UNDEFINED, KeyCode.get("asdf"));
	}
}
