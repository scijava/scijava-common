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

package org.scijava.input;

import java.util.regex.Pattern;

import org.scijava.util.PlatformUtils;

/**
 * A keyboard shortcut, consisting of a {@link KeyCode} plus
 * {@link InputModifiers}.
 * 
 * @author Curtis Rueden
 * @author Barry DeZonia
 */
public class Accelerator {

	/** Actual key pressed. */
	private final KeyCode keyCode;

	/** Key modifiers used. */
	private final InputModifiers modifiers;

	public Accelerator(final KeyCode keyCode, final InputModifiers modifiers) {
		this.keyCode = keyCode;
		this.modifiers = modifiers;
	}

	// -- Accelerator methods --

	public KeyCode getKeyCode() {
		return keyCode;
	}

	public InputModifiers getModifiers() {
		return modifiers;
	}

	// -- Object methods --

	@Override
	public boolean equals(final Object o) {
		if (!(o instanceof Accelerator)) return false;
		final Accelerator acc = (Accelerator) o;
		if (!getModifiers().equals(acc.getModifiers())) return false;
		if (!getKeyCode().equals(acc.getKeyCode())) return false;
		return true;
	}

	@Override
	public int hashCode() {
		return toString().hashCode();
	}

	@Override
	public String toString() {
		final String modString = modifiers.toString();
		final String keyString = keyCode.name();
		if (modString.length() == 0) return keyString;
		return modString + " " + keyString;
	}

	// -- Utility methods --

	/**
	 * Creates an accelerator from the given string representation. The string
	 * must have the following syntax:
	 * 
	 * <pre>
	 * 	    &lt;modifiers&gt;* &lt;keyCode&gt;
	 * 
	 * 	    modifiers := alt | altGraph | control | meta | shift
	 * 	    keyCode := {@link KeyCode} field (e.g., COMMA or UP)
	 * </pre>
	 * 
	 * For convenience:
	 * <ul>
	 * <li>"control" may be shortened to "ctrl"</li>
	 * <li>"altGraph" may be shortened to "altGr"</li>
	 * <li>The caret character ('^') is expanded to "control " on non-Mac
	 * platforms, and "meta " (i.e., the Mac command key) on Mac platforms.</li>
	 * </ul>
	 * Here are some example strings:
	 * <ul>
	 * <li>"INSERT"</li>
	 * <li>"control DELETE"</li>
	 * <li>"alt shift X"</li>
	 * <li>"^C"</li>
	 * </ul>
	 * 
	 * @see javax.swing.KeyStroke#getKeyStroke(String) for the syntax from which
	 *      this one is derived.
	 * @see KeyCode for the complete list of special character codes.
	 */
	public static Accelerator create(final String acc) {
		if (acc == null || acc.isEmpty()) return null;

		// allow use of caret for control/command
		// (e.g., "^X" to mean "control X" or "meta X")
		final String a = acc.replaceAll(Pattern.quote("^"), expandedCaret());

		final String[] components = a.split(" ");

		// determine which modifiers are used
		boolean alt = false, altGr = false;
		boolean ctrl = false, meta = false, shift = false;
		for (int i = 0; i < components.length - 1; i++) {
			if (components[i].equalsIgnoreCase("alt")) alt = true;
			else if (components[i].equalsIgnoreCase("altGr") ||
				components[i].equalsIgnoreCase("altGraph"))
			{
				altGr = true;
			}
			else if (components[i].equalsIgnoreCase("control") ||
				components[i].equalsIgnoreCase("ctrl"))
			{
				ctrl = true;
			}
			else if (components[i].equalsIgnoreCase("meta")) meta = true;
			else if (components[i].equalsIgnoreCase("shift")) shift = true;
		}

		final InputModifiers modifiers =
			new InputModifiers(alt, altGr, ctrl, meta, shift, false, false, false);

		// upper case the key code
		final String code = components[components.length - 1].toUpperCase();
		final KeyCode keyCode = KeyCode.get(code);

		return new Accelerator(keyCode, modifiers);
	}

	public static boolean isCtrlReplacedWithMeta() {
		// FIXME: Relocate this platform-specific logic.
		return PlatformUtils.isMac();
	}

	// -- Helper methods --

	private static String expandedCaret() {
		return isCtrlReplacedWithMeta() ? "meta " : "control ";
	}

}
