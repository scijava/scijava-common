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

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;

/**
 * An {@link ArrayList} whose size can be adjusted more efficiently.
 * <p>
 * When sizing down, elements at the end of the list are removed in one
 * operation. When sizing up, null elements are appended to the list.
 * </p>
 * 
 * @author Curtis Rueden
 * @param <E> The type of data stored in the list.
 */
public class SizableArrayList<E> extends ArrayList<E> implements Sizable {

	private static final long serialVersionUID = 1L;

	// -- Constructors --

	public SizableArrayList(final int initialCapacity) {
		super(initialCapacity);
	}

	public SizableArrayList() {
		super();
	}

	public SizableArrayList(final Collection<? extends E> c) {
		super(c);
	}

	// -- ArrayList methods --

	@Override
	public void ensureCapacity(int capacity) {
		// HACK: With Java 1.7.0_45 (but not 1.7.0_21), when calling ensureCapacity
		// with a value <= 10, while the size is still 0, does not actually affect
		// the capacity. This seems like a bug, but we will work around it!
		super.ensureCapacity(capacity < 11 ? 11 : capacity);
	}

	// -- Sizable methods --

	@Override
	public void setSize(final int size) {
		final int oldSize = size();
		if (oldSize == size) return; // no size change
		if (size < oldSize) {
			// need to remove extra elements
			removeRange(size, oldSize);
		}
		else {
			// need to add some elements
			ensureCapacity(size);
			final boolean hackSuccessful = hackSize(size);
			if (!hackSuccessful) {
				// explicitly increase the size by adding nulls
				while (size() < size) add(null);
			}
		}
	}

	// -- Helper methods --

	private boolean hackSize(final int size) {
		// HACK: Override the size field directly.
		final int oldSize;
		try {
			final Field sizeField = ArrayList.class.getDeclaredField("size");
			sizeField.setAccessible(true);
			oldSize = (Integer) sizeField.get(this);
			sizeField.set(this, size);

			// NB: Check that it worked. In the case of Java 1.7.0_45, it is possible
			// for the capacity to not *actually* be ensured, in which case
			// subsequently attempting to get the (size - 1)th value results in
			// ArrayIndexOutOfBoundsException. So let's be safe and verify it here.
			try {
				get(size - 1);
			}
			catch (final Exception exc) {
				// NB: Restore the previous size, then fail.
				sizeField.set(this, oldSize);
				return false;
			}
		}
		catch (final Exception exc) {
			return false;
		}
		return true;
	}

}
