/*
 * #%L
 * SciJava Common shared library for SciJava software.
 * %%
 * Copyright (C) 2009 - 2017 Board of Regents of the University of
 * Wisconsin-Madison, Broad Institute of MIT and Harvard, and Max Planck
 * Institute of Molecular Cell Biology and Genetics.
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

import java.util.Arrays;
import java.util.Collection;

/**
 * An extensible, generic array of {@code Object} elements. Note that this class
 * is a {@link PrimitiveArray} but of course Objects are not primitives.
 * However, this class still facilitates improved conversion of Object array
 * types to collections, and thus remains useful (if not completely accurately
 * congruent with its type hierarchy).
 * <p>
 * This class still provides a convenient way to work around
 * {@link Arrays#asList(Object...)} creating a list with a single element. It
 * also contains improved performance implementations of many {@link Collection}
 * methods.
 * </p>
 * 
 * @author Mark Hiner
 * @author Johannes Schindelin
 * @author Curtis Rueden
 */
public class ObjectArray<E> extends AbstractPrimitiveArray<E[], E> {

	/** The backing array. */
	private E[] array;

	private Class<E> objectClass;

	/**
	 * Constructs an extensible array of objects, backed by a fixed-size array.
	 */
	public ObjectArray(Class<E> arrayType) {
		super(arrayType);
		objectClass = arrayType;
	}

	/**
	 * Constructs an extensible array of objects, backed by a fixed-size array.
	 * 
	 * @param size the initial size
	 */
	public ObjectArray(Class<E> arrayType, final int size) {
		super(arrayType, size);
		objectClass = arrayType;
	}

	/**
	 * Constructs an extensible array of objects, backed by the given fixed-size
	 * array.
	 * 
	 * @param array the array to wrap
	 */
	@SuppressWarnings("unchecked")
	public ObjectArray(final E[] array) {
		super((Class<E>) array.getClass().getComponentType(), array);
		objectClass = (Class<E>) array.getClass().getComponentType();
	}

	// -- ObjectArray methods --

	public void addValue(final E value) {
		addValue(size(), value);
	}

	public boolean removeValue(final E value) {
		final int index = indexOf(value);
		if (index < 0) return false;
		delete(index, 1);
		return true;
	}

	public E getValue(final int index) {
		checkBounds(index);
		return array[index];
	}

	public E setValue(final int index, final E value) {
		checkBounds(index);
		final E oldValue = getValue(index);
		array[index] = value;
		return oldValue;
	}

	public void addValue(final int index, final E value) {
		insert(index, 1);
		array[index] = value;
	}

	// -- PrimitiveArray methods --

	@Override
	public E[] getArray() {
		return array;
	}

	@Override
	public void setArray(final E[] array) {
		if (array.length < size()) {
			throw new IllegalArgumentException("Array too small");
		}
		this.array = array;
	}

	// -- List methods --

	@Override
	public E get(final int index) {
		return getValue(index);
	}

	@Override
	public E set(final int index, final E element) {
		return setValue(index, element == null ? defaultValue() : element);
	}

	@Override
	public void add(final int index, final E element) {
		addValue(index, element);
	}

	// NB: Overridden for performance.
	@Override
	public int indexOf(final Object o) {
		if (!compatibleClass(o)) return -1;
		for (int i = 0; i < size(); i++) {
			if (array[i].equals(o)) return i;
		}
		return -1;
	}

	// NB: Overridden for performance.
	@Override
	public int lastIndexOf(final Object o) {
		if (!compatibleClass(o)) return -1;
		for (int i = size() - 1; i >= 0; i--) {
			if (array[i].equals(o)) return i;
		}
		return -1;
	}

	// -- Collection methods --

	// NB: Overridden for performance.
	@Override
	public boolean contains(final Object o) {
		if (!compatibleClass(o)) return false;
		return indexOf(o) >= 0;
	}

	// NB: Overridden for performance.
	@Override
	public boolean remove(final Object o) {
		if (!compatibleClass(o)) return false;
		final E value = ConversionUtils.cast(o, objectClass);
		return removeValue(value);
	}

	// NB: Overridden for performance.
	@Override
	public boolean containsAll(final Collection<?> c) {
		for (final Object o : c) {
			if (!compatibleClass(o)) return false;
			if (indexOf(o) < 0) return false;
		}
		return true;
	}

	// NB: Overridden for performance.
	@Override
	public boolean addAll(final int index, final Collection<? extends E> c) {
		if (c.size() == 0) return false;
		insert(index, c.size());
		int i = index;
		for (final E e : c) {
			setValue(i++, e);
		}
		return true;
	}

	// NB: Overridden for performance.
	@Override
	public boolean removeAll(final Collection<?> c) {
		boolean changed = false;
		for (final Object o : c) {
			if (!compatibleClass(o)) continue;
			final E value = ConversionUtils.cast(o, objectClass);
			final boolean result = removeValue(value);
			if (result) changed = true;
		}
		return changed;
	}

	@Override
	public E defaultValue() {
		return null;
	}

	// -- Helper methods --

	/**
	 * Returns true iff the given Object is non-null and its class matches the
	 * type of this array.
	 */
	private boolean compatibleClass(Object o) {
		if (o != null && objectClass.isAssignableFrom(o.getClass())) return true;
		return false;
	}
}
