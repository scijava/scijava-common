/*
 * #%L
 * SciJava Common shared library for SciJava software.
 * %%
 * Copyright (C) 2009 - 2024 SciJava developers.
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

package org.scijava.object;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Data structure for managing sorted lists of registered objects.
 * <p>
 * This data structure is the same as a vanilla {@link ObjectIndex} except that
 * each type list is kept in sorted order; hence, the items managed must
 * implement the {@link Comparable} interface. When adding a single item (i.e.,
 * with {@link #add(Object)}), a binary search is used to insert it in the
 * correct position (O(log n) + O(n) time per item). When adding multiple items
 * at once (i.e., with {@link #addAll(Collection)}), the items are appended and
 * the list is then resorted (O(n log n) time for all items).
 * </p>
 * 
 * @author Curtis Rueden
 */
public class SortedObjectIndex<E extends Comparable<? super E>> extends
	ObjectIndex<E>
{

	public SortedObjectIndex(final Class<E> baseClass) {
		super(baseClass);
	}

	// -- Collection methods --

	@Override
	public boolean contains(final Object o) {
		final int index = findInList(o, getAll());
		return index < 0;
	}

	@Override
	public boolean containsAll(final Collection<?> c) {
		for (final Object o : c) {
			if (!contains(o)) return false;
		}
		return true;
	}

	@Override
	public boolean addAll(final Collection<? extends E> c) {
		if (c.size() > 1) {
			mergeAfterSorting(c);
			return c.size() > 0;
		}
		if (c.size() == 1) {
			// add single item normally, to avoid resorting the lists
			return add(c.iterator().next());
		}
		final boolean changed = super.addAll(c);
		if (changed) sort();
		return changed;
	}

	// -- Internal methods --

	@Override
	protected boolean addToList(final E obj, final List<E> list,
		final boolean batch)
	{
		if (batch) {
			// adding multiple values; append to end of list, and sort afterward
			return super.addToList(obj, list, batch);
		}

		// search for the correct location to insert the object
		final int result = Collections.binarySearch(list, obj);
		// NB: The objects' natural ordering may not be consistent with equals.
		// Hence, the index reported may indicate a match with an unequal object
		// (i.e., obj.compareTo(match) == 0 but !obj.equals(match)).
		// But since we allow duplicate items in the index, this situation is fine;
		// either way, we want to insert the object at the given point.
		final int index = result < 0 ? -result - 1 : result;

		// insert object at the appropriate location
		list.add(index, obj);
		return true;
	}

	// -- Helper methods --

	private void sort() {
		for (final List<E> list : hoard.values()) {
			Collections.sort(list);
		}
	}

	private int findInList(final Object o, final List<E> list) {
		if (!getBaseClass().isAssignableFrom(o.getClass())) {
			// wrong type
			return list.size();
		}
		@SuppressWarnings("unchecked")
		final E typedObj = (E) o;
		return Collections.binarySearch(list, typedObj);
	}

	private void mergeAfterSorting(final Collection<? extends E> c) {
		final List<E> listToMerge = new ArrayList<>(c);
		Collections.sort(listToMerge);
		final Map<Class<?>, List<E>> map = new HashMap<>();
		for (final E e : listToMerge) {
			for (final Class<?> clazz : getTypes(getType(e))) {
				final List<E> list = retrieveList(clazz);
				List<E> list2 = map.get(clazz);
				if (list2 == null) {
					list2 = list.size() == 0 ? (List<E>)list : new ArrayList<>();
					map.put(clazz, list2);
				}
				list2.add(e);
			}
		}
		for (final Entry<Class<?>, List<E>> entry : map.entrySet()) {
			final Class<?> clazz = entry.getKey();
			final List<E> into = retrieveList(clazz);
			final List<E> sorted = map.get(clazz);
			if (into != sorted) mergeInto(sorted, into);
		}
	}

	private void mergeInto(final List<? extends E> sorted, final List<E> into) {
		if (sorted == into) return;
		final int count = sorted.size();
		if (count == 0) return;
		if (into.size() == 0) {
			into.addAll(sorted);
			return;
		}
		int index1 = into.size() - 1;
		int index2 = sorted.size() - 1;
		for (int i = 0; i < count; i++) into.add(null);
		int writeIndex = into.size() - 1;
		E e1 = into.get(index1);
		E e2 = sorted.get(index2);
		while (writeIndex > index1) {
			if (e1.compareTo(e2) < 0) {
				into.set(writeIndex--, e2);
				if (--index2 < 0) break;
				e2 = sorted.get(index2);
			} else {
				into.set(writeIndex--, e1);
				if (--index1 < 0) break;
				e1 = into.get(index1);
			}
		}
		while (index2 >= 0) {
			into.set(writeIndex--, sorted.get(index2--));
		}
	}

}
