/*
 * #%L
 * SciJava Common shared library for SciJava software.
 * %%
 * Copyright (C) 2009 - 2016 Board of Regents of the University of
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

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * A simple container for {@code N} last-recently-used items.
 * 
 * @author Johannes Schindelin
 */
public class LastRecentlyUsed<T> implements Collection<T> {
	private final Object[] entries;
	private final Map<T, Integer> map;
	/**
	 * The double-linked list pointers.
	 * <p>
	 * The {@code top} variable points to the most recently added, the
	 * {@code bottom} variable to the oldest entry. The {@code next} and
	 * {@code previous} arrays point to the next newer/next older entry.
	 * </p>
	 * <p>
	 * For initialization performance, all of {@code next}, {@code previous},
	 * {@code top} and {@code bottom} are initialized to {@code 0}, meaning that
	 * you need to decrement the values by one in order to obtain the entry index.
	 * Example: the index of the most recently added entry is {@code top -1}, and
	 * {@code next[top - 1]} is {@code 0} because there is no newer entry than the
	 * newest entry.
	 * </p>
	 */
	private final int[] next, previous;
	private int top, bottom;

	public LastRecentlyUsed(int size) {
		entries = new Object[2 * size];
		next = new int[2 * size];
		previous = new int[2 * size];
		map = new HashMap<>();
	}

	/**
	 * Given the index of an entry, returns the index of the next newer entry.
	 * 
	 * @param index the index of the current entry, or -1 to wrap around to the oldest entry.
	 * @return the index of the next newer entry, or -1 when there is no such entry.
	 */
	public int next(int index) {
		return index < 0 ? bottom - 1 : next[index] - 1;
	}

	/**
	 * Given the index of an entry, returns the index of the next older entry.
	 * 
	 * @param index the index of the current entry, or -1 to wrap around to the newest entry.
	 * @return the index of the next older entry, or -1 when there is no such entry.
	 */
	public int previous(int index) {
		return index < 0 ? top - 1 : previous[index] - 1;
	}

	/**
	 * Returns the entry for the given index.
	 * 
	 * @param index the index of the entry
	 * @return the entry
	 */
	@SuppressWarnings("unchecked")
	public T get(int index) {
		return (T) entries[index];
	}

	/**
	 * Looks up the index for a given entry.
	 * 
	 * @param value the value of the entry to find
	 * @return the corresponding index, or {@code -1} if the entry was not found
	 */
	public int lookup(final T value) {
		final Integer result = map.get(value);
		return result == null ? -1 : (int) result;
	}

	/**
	 * Add a new newest entry.
	 * 
	 * @param value the value of the entry
	 * @return whether the entry was added
	 */
	@Override
	public boolean add(final T value) {
		return add(value, false);
	}

	/**
	 * Add a new oldest entry.
	 * <p>
	 * This method helps recreating {@link LastRecentlyUsed} instances given the
	 * entries in the order newest first, oldest last.
	 * </p>
	 * 
	 * @param value the value of the entry to add
	 */
	public void addToEnd(final T value) {
		add(value, true);
	}

	public boolean replace(final int index, T newValue) {
		final Object previousValue = get(index);
		if (previousValue == null) {
			throw new IllegalArgumentException("No current entry at position " +
				index);
		}
		if (newValue.equals(previous)) return false;
		map.remove(previousValue);
		map.put(newValue, index);
		entries[index] = newValue;
		return true;
	}

	/**
	 * Empties the data structure.
	 */
	@Override
	public void clear() {
		top = bottom = 0;
		map.clear();
		for (int i = 0; i < entries.length; i++) {
			entries[i] = null;
			next[i] = previous[i] = 0;
		}
	}

	@Override
	public boolean addAll(final Collection<? extends T> values) {
		for (final T value : values) {
			add(value);
		}
		return true;
	}

	@Override
	public boolean contains(final Object value) {
		return map.containsKey(value);
	}

	@Override
	public boolean containsAll(final Collection<?> values) {
		return map.keySet().containsAll(values);
	}

	@Override
	public boolean isEmpty() {
		return top == 0;
	}

	@Override
	public boolean remove(Object value) {
		final Integer index = map.get(value);
		if (index == null) return false;
		remove(index.intValue());
		return true;
	}

	@Override
	public boolean removeAll(Collection<?> values) {
		boolean result = true;
		for (final Object value : values) {
			result = remove(value) && result;
		}
		return result;
	}

	@Override
	public boolean retainAll(Collection<?> values) {
		for (int index = top - 1; index >= 0; ) {
			final int prev = previous[index] - 1;
			if (!values.contains(get(index))) {
				remove(index);
			}
			index = prev;
		}
		return containsAll(values);
	}

	@Override
	public int size() {
		return map.size();
	}

	@Override
	public Object[] toArray() {
		final Object[] result = new Object[size()];
		for (int i = 0, index = top - 1; index >= 0; i++, index =
			previous[index] - 1)
		{
			result[i] = get(index);
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <S> S[] toArray(final S[] array) {
		final int size = size();
		if (array.length >= size) {
			for (int i = 0, index = top - 1; index >= 0; i++, index =
					previous[index] - 1)
				{
					array[i] = (S) get(index);
				}
			return array;
		}
		final S[] result =
			(S[]) Array.newInstance(array.getClass().getComponentType(), size);
		for (int i = 0, index = top - 1; index >= 0; i++, index =
				previous[index] - 1)
			{
				result[i] = (S) get(index);
			}
			return result;
	}

	/**
	 * Returns an {@link Iterator}.
	 * 
	 * @return the iterator
	 */
	@Override
	public Iterator<T> iterator() {
		return new Iterator<T>() {

			private int position = top - 1;

			@Override
			public boolean hasNext() {
				return position >= 0;
			}

			@Override
			public T next() {
				@SuppressWarnings("unchecked")
				final T result = (T) entries[position];
				position = previous[position] - 1;
				return result;
			}

			@Override
			public void remove() {
				LastRecentlyUsed.this.remove(position == 0 ? top - 1 : next[position] - 1);
			}

		};
	}

	// -- private methods

	private void remove(int position) {
		assert(entries[position] != null);
		map.remove(entries[position]);
		entries[position] = null;
		if (next[position] == 0) {
			top = previous[position];
		}
		else {
			previous[next[position] - 1] = previous[position];
		}
		if (previous[position] == 0) {
			bottom = next[position];
		}
		else {
			next[previous[position] - 1] = next[position];
		}
		next[position] = previous[position] = 0;
	}

	private boolean add(final T value, boolean addAtEnd) {
		final Integer existing = map.get(value);
		int insert;
		if (existing != null) {
			insert = existing;
			remove(insert);
		}
		else if (map.size() == entries.length / 2) {
			insert = bottom - 1;
			remove(insert);
		}
		else {
			insert = value.hashCode() % entries.length;
			if (insert < 0) insert += entries.length;
			while (insert < entries.length && entries[insert] != null) insert++;
		}
		add(insert, value, addAtEnd);
		return existing == null;
	}

	private void add(int position, T value, boolean atEnd) {
		assert(next[position] == 0);
		assert(previous[position] == 0);
		assert(entries[position] == null);

		map.put(value, position);
		entries[position] = value;
		if (atEnd) {
			next[position] = bottom;
			if (bottom > 0) previous[bottom - 1] = position + 1;
			bottom = position + 1;
			if (top == 0) top = bottom;
		}
		else {
			previous[position] = top;
			if (top > 0) {
				next[top - 1] = position + 1;
			}
			top = position + 1;
			if (bottom == 0) bottom = top;
		}
	}

	// For testing
	protected void assertConsistency() {
		if (top == 0) {
			assert(bottom == 0);
			assert(map.size() == 0);
			for (int i = 0; i < entries.length; i++) {
				assert(entries[i] == null);
				assert(next[i] == 0);
				assert(previous[i] == 0);
			}
			return;
		}
		assert(bottom != 0);
		final Set<Integer> indices = new HashSet<>(map.values());
		assert(indices.size() == map.size());
		for (int i = 0; i < entries.length; i++) {
			if (indices.contains(i)) {
				assert(entries[i] != null);
				assert(map.get(entries[i]) == i);
				if (i == top - 1 || top == bottom) {
					assert(next[i] == 0);
				}
				else {
					assert(next[i] > 0);
					assert(previous[next[i] - 1] == i + 1);
				}
				if (i == bottom - 1 || top == bottom) {
					assert(previous[i] == 0);
				}
				else {
					assert(previous[i] > 0);
					assert(next[previous[i] - 1] == i + 1);
				}
			}
			else {
				assert(entries[i] == null);
				assert(next[i] == 0);
				assert(previous[i] == 0);
			}
		}
	}
}
