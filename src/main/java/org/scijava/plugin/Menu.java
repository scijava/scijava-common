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

package org.scijava.plugin;

import java.lang.annotation.Target;

import org.scijava.MenuEntry;
import org.scijava.input.Accelerator;

/**
 * One item of a {@link Plugin}'s menu path. It can be a top-level menu such as
 * File, a nested menu such as Open Recent, or a leaf item such as Exit. A
 * sequential list of Menus defines a {@link Plugin}'s position in the menu
 * structure.
 * <p>
 * Using a list of Menus to define menu position is more verbose than using
 * {@link Plugin#menuPath}, but more powerful in that it allows specification of
 * various menu attributes (e.g., {@link #weight}, {@link #mnemonic},
 * {@link #accelerator} and {@link #iconPath}).
 * 
 * @author Curtis Rueden
 */
@Target({})
public @interface Menu {

	/** The human-readable label to use for the menu item. */
	String label();

	/**
	 * Position within the menu structure. Items at each level are sorted in
	 * ascending order by weight.
	 */
	double weight() default MenuEntry.DEFAULT_WEIGHT;

	/** Mnemonic identifying underlined shortcut character. */
	char mnemonic() default '\0';

	/**
	 * Keyboard shortcut to activate the menu item.
	 * 
	 * @see Accelerator#create(String) for information on the syntax.
	 */
	String accelerator() default "";

	/** Path to the menu's icon (shown in the menu structure). */
	String iconPath() default "";

}
