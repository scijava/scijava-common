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

import org.junit.Test;
import org.scijava.util.PlatformUtils;

import static org.junit.Assert.*;

/**
 * Tests {@link Accelerator}.
 * 
 * @author Curtis Rueden
 */
public class AcceleratorTest {

	/** Tests {@link Accelerator#create}. */
	@Test
	public void testCreate() {
		assertAccelerator("*", KeyCode.ASTERISK, false, false, false, false, false);
		assertAccelerator("0", KeyCode.NUM0, false, false, false, false, false);
		assertAccelerator("NUMPAD_0", KeyCode.NUMPAD_0, false, false, false, false, false);
		assertAccelerator("+", KeyCode.PLUS, false, false, false, false, false);
		assertAccelerator("shift minus", KeyCode.MINUS, false, false, false, false, true);
		assertAccelerator("ctrl shift +", KeyCode.PLUS, false, false, true, false, true);
		assertAccelerator("meta /", KeyCode.SLASH, false, false, false, true, false);
		assertAccelerator("alt altGr ctrl meta shift a", KeyCode.A, true, true, true, true, true);

		// Test caret shortcut symbol.
		final boolean macos = PlatformUtils.isMac();
		assertAccelerator("^Z", KeyCode.Z, false, false, !macos, macos, false);
	}

	private void assertAccelerator(String shortcut,
		KeyCode keyCode,
		final boolean alt,
		final boolean altGr,
		final boolean ctrl,
		final boolean meta,
		final boolean shift)
	{
		Accelerator acc = Accelerator.create(shortcut);
		assertEquals(acc.getKeyCode(), keyCode);
		InputModifiers mods = acc.getModifiers();
		assertNotNull(mods);
		assertEquals(alt, mods.isAltDown());
		assertEquals(altGr, mods.isAltGrDown());
		assertEquals(ctrl, mods.isCtrlDown());
		assertEquals(meta, mods.isMetaDown());
		assertEquals(shift, mods.isShiftDown());
		assertFalse(mods.isLeftButtonDown());
		assertFalse(mods.isMiddleButtonDown());
		assertFalse(mods.isRightButtonDown());
	}
}
