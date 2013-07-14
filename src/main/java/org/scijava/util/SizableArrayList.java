/*
 * #%L
 * SciJava Common shared library for SciJava software.
 * %%
 * Copyright (C) 2009 - 2013 Board of Regents of the University of
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
 * 
 * The views and conclusions contained in the software and documentation are
 * those of the authors and should not be interpreted as representing official
 * policies, either expressed or implied, of any organization.
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
			try {
				final Field sizeField = ArrayList.class.getDeclaredField("size");
				sizeField.setAccessible(true);
				sizeField.set(this, size);
			}
			catch (final NoSuchFieldException exc) {
				throw new IllegalStateException(exc);
			}
			catch (final IllegalArgumentException exc) {
				throw new IllegalStateException(exc);
			}
			catch (final IllegalAccessException exc) {
				throw new IllegalStateException(exc);
			}
		}
	}

}
