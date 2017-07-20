/*
 * #%L
 * SciJava Common shared library for SciJava software.
 * %%
 * Copyright (C) 2009 - 2017 Board of Regents of the University of
 * Wisconsin-Madison, Broad Institute of MIT and Harvard, Max Planck
 * Institute of Molecular Cell Biology and Genetics, University of
 * Konstanz, and KNIME GmbH.
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

package org.scijava.util;

import java.util.Collection;

/**
 * An extensible array of {@code char} elements.
 * 
 * @author Mark Hiner
 * @author Johannes Schindelin
 * @author Curtis Rueden
 */
public class CharArray extends AbstractPrimitiveArray<char[], Character> {

	/** The backing array. */
	private char[] array;

	/**
	 * Constructs an extensible array of chars, backed by a fixed-size array.
	 */
	public CharArray() {
		super(Character.TYPE);
	}

	/**
	 * Constructs an extensible array of chars, backed by a fixed-size array.
	 * 
	 * @param size the initial size
	 */
	public CharArray(final int size) {
		super(Character.TYPE, size);
	}

	/**
	 * Constructs an extensible array of chars, backed by the given fixed-size
	 * array.
	 * 
	 * @param array the array to wrap
	 */
	public CharArray(final char[] array) {
		super(Character.TYPE, array);
	}

	// -- CharArray methods --

	public void addValue(final char value) {
		addValue(size(), value);
	}

	public boolean removeValue(final char value) {
		final int index = indexOf(value);
		if (index < 0) return false;
		delete(index, 1);
		return true;
	}

	public char getValue(final int index) {
		checkBounds(index);
		return array[index];
	}

	public char setValue(final int index, final char value) {
		checkBounds(index);
		final char oldValue = getValue(index);
		array[index] = value;
		return oldValue;
	}

	public void addValue(final int index, final char value) {
		insert(index, 1);
		array[index] = value;
	}

	public int indexOf(final char value) {
		for (int i = 0; i < size(); i++) {
			if (array[i] == value) return i;
		}
		return -1;
	}

	public int lastIndexOf(final char value) {
		for (int i = size() - 1; i >= 0; i--) {
			if (array[i] == value) return i;
		}
		return -1;
	}

	public boolean contains(final char value) {
		return indexOf(value) >= 0;
	}

	// -- PrimitiveArray methods --

	@Override
	public char[] getArray() {
		return array;
	}

	@Override
	public void setArray(final char[] array) {
		if (array.length < size()) {
			throw new IllegalArgumentException("Array too small");
		}
		this.array = array;
	}

	// -- List methods --

	@Override
	public Character get(final int index) {
		return getValue(index);
	}

	@Override
	public Character set(final int index, final Character element) {
		return setValue(index, element == null ? defaultValue() : element);
	}

	@Override
	public void add(final int index, final Character element) {
		addValue(index, element);
	}

	// NB: Overridden for performance.
	@Override
	public int indexOf(final Object o) {
		if (!(o instanceof Character)) return -1;
		final char value = (Character) o;
		return indexOf(value);
	}

	// NB: Overridden for performance.
	@Override
	public int lastIndexOf(final Object o) {
		if (!(o instanceof Character)) return -1;
		final char value = (Character) o;
		return lastIndexOf(value);
	}

	// -- Collection methods --

	// NB: Overridden for performance.
	@Override
	public boolean contains(final Object o) {
		if (!(o instanceof Character)) return false;
		final char value = (Character) o;
		return contains(value);
	}

	// NB: Overridden for performance.
	@Override
	public boolean remove(final Object o) {
		if (!(o instanceof Character)) return false;
		final char value = (Character) o;
		return removeValue(value);
	}

	// NB: Overridden for performance.
	@Override
	public boolean containsAll(final Collection<?> c) {
		for (final Object o : c) {
			if (!(o instanceof Character)) return false;
			final char value = (Character) o;
			if (indexOf(value) < 0) return false;
		}
		return true;
	}

	// NB: Overridden for performance.
	@Override
	public boolean addAll(final int index, final Collection<? extends Character> c)
	{
		if (c.size() == 0) return false;
		insert(index, c.size());
		int i = index;
		for (final char e : c) {
			setValue(i++, e);
		}
		return true;
	}

	// NB: Overridden for performance.
	@Override
	public boolean removeAll(final Collection<?> c) {
		boolean changed = false;
		for (final Object o : c) {
			if (!(o instanceof Character)) continue;
			final char value = (Character) o;
			final boolean result = removeValue(value);
			if (result) changed = true;
		}
		return changed;
	}

	@Override
	public Character defaultValue() {
		return 0;
	}
}
