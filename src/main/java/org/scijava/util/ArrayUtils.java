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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Utility class for creating and manipulating {@link PrimitiveArray} instances.
 * 
 * @author Mark Hiner
 */
public final class ArrayUtils {

	private ArrayUtils() {
		// prevent instantiation of utility class
	}

	// -- ArrayUtils methods --

	/**
	 * Converts the provided Object to a {@link Collection} implementation. If the
	 * object is an array type, a {@link PrimitiveArray} wrapper will be created.
	 */
	public static Collection<?> toCollection(Object value) {
		// If the value is null or we we have a collection, just return it
		if (value == null || Collection.class.isAssignableFrom(value.getClass())) {
			return (Collection<?>) value;
		}
		// Check for primitive array types
		else if (value instanceof char[]) {
			return new CharArray((char[]) value);
		}
		else if (value instanceof byte[]) {
			return new ByteArray((byte[]) value);
		}
		else if (value instanceof boolean[]) {
			return new BoolArray((boolean[]) value);
		}
		else if (value instanceof short[]) {
			return new ShortArray((short[]) value);
		}
		else if (value instanceof int[]) {
			return new IntArray((int[]) value);
		}
		else if (value instanceof long[]) {
			return new LongArray((long[]) value);
		}
		else if (value instanceof float[]) {
			return new FloatArray((float[]) value);
		}
		else if (value instanceof double[]) {
			return new DoubleArray((double[]) value);
		}
		else if (value instanceof Object[]) {
			return makeObjectArray((Object[]) value, value.getClass()
				.getComponentType());
		}
		else {
			// This object is a neither an array nor a collection. So we wrap it in a
			// list and return.
			List<Object> list = new ArrayList<Object>();
			list.add(value);
			return list;
		}
	}

	/**
	 * Created a new {@link ObjectArray} instance wrapping the provided Object[],
	 * parameterized using the given Class instance.
	 */
	@SuppressWarnings("unchecked")
	private static <T> ObjectArray<T> makeObjectArray(Object[] values, Class<T> type) {
		return new ObjectArray<T>((T[]) values);
	}
}
