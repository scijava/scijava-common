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

package org.scijava;

import java.util.ArrayList;
import java.util.Collection;

/**
 * A path in a hierarchical menu structure, for use with {@link UIDetails}.
 * 
 * @author Curtis Rueden
 */
public class MenuPath extends ArrayList<MenuEntry> {

	private static final long serialVersionUID = 1L;

	/** The separator between elements of a menu path string. */
	public static final String PATH_SEPARATOR = ">";

	/** Creates an empty menu path. */
	public MenuPath() {
		// default constructor
	}

	/**
	 * Creates a menu path with the given entries. Passing a {@link MenuPath} as
	 * the argument will make a copy.
	 */
	public MenuPath(final Collection<? extends MenuEntry> menuEntries) {
		if (menuEntries != null) addAll(menuEntries);
	}

	/**
	 * Creates a menu path with entries parsed from the given string. Assumes
	 * {@code >} as the separator (e.g., {@code File>New>Image}).
	 * 
	 * @see #PATH_SEPARATOR
	 */
	public MenuPath(final String path) {
		this(path, PATH_SEPARATOR);
	}

	/**
	 * Creates a menu path with entries parsed from the given string, splitting on
	 * the specified separator.
	 */
	public MenuPath(final String path, final String separator) {
		this(path, separator, true);
	}

	/**
	 * Creates a menu path with entries parsed from the given string, splitting on
	 * the specified separator, and trimming whitespace if indicated.
	 */
	public MenuPath(final String path, final String separator,
		final boolean trim)
	{
		if (path != null && !path.isEmpty()) {
			final String[] tokens = path.split(separator);
			for (final String token : tokens) {
				add(new MenuEntry(trim ? token.trim() : token));
			}
		}
	}

	/** Gets the final element of the menu path. */
	public MenuEntry getLeaf() {
		if (size() == 0) return null;
		return get(size() - 1);
	}

	/** Gets the menu path as a string. */
	public String getMenuString() {
		return getMenuString(true);
	}

	/** Gets the menu path as a string, with or without the final element. */
	public String getMenuString(final boolean includeLeaf) {
		final StringBuilder sb = new StringBuilder();
		final int size = size();
		final int last = includeLeaf ? size : size - 1;
		for (int i = 0; i < last; i++) {
			final MenuEntry menu = get(i);
			if (i > 0) sb.append(" " + PATH_SEPARATOR + " ");
			sb.append(menu);
		}
		return sb.toString();
	}

}
