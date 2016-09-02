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

// Portions of this class were derived from the loci.common.DataTools class of
// the Bio-Formats library, licensed according to Simplified BSD, as follows:
//
// Copyright (C) 2005 - 2015 Open Microscopy Environment:
//   - Board of Regents of the University of Wisconsin-Madison
//   - Glencoe Software, Inc.
//   - University of Dundee
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions are met:
//
// 1. Redistributions of source code must retain the above copyright notice,
//    this list of conditions and the following disclaimer.
// 2. Redistributions in binary form must reproduce the above copyright notice,
//    this list of conditions and the following disclaimer in the documentation
//    and/or other materials provided with the distribution.
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
// AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
// IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
// ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
// LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
// CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
// SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
// INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
// CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
// ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
// POSSIBILITY OF SUCH DAMAGE.

package org.scijava.util;

import java.util.Collection;
import java.util.List;

/**
 * Utility class for creating and manipulating {@link PrimitiveArray} instances.
 * 
 * @author Mark Hiner
 * @author Curtis Rueden
 * @author Melissa Linkert
 * @author Chris Allan
 */
public final class ArrayUtils {

	private ArrayUtils() {
		// prevent instantiation of utility class
	}

	// -- ArrayUtils methods --

	/** Creates an array of the given type, containing the specified values. */
	@SafeVarargs
	public static <T> T[] array(final T... values) {
		return values;
	}

	/**
	 * Converts the provided Object to a {@link Collection} implementation. If the
	 * object is an array type, a {@link PrimitiveArray} wrapper will be created.
	 */
	public static Collection<?> toCollection(final Object value) {
		// If the value is null or we we have a collection, just return it
		if (value == null || Collection.class.isAssignableFrom(value.getClass())) {
			return (Collection<?>) value;
		}
		// Check for primitive array types
		if (value instanceof char[]) {
			return new CharArray((char[]) value);
		}
		if (value instanceof byte[]) {
			return new ByteArray((byte[]) value);
		}
		if (value instanceof boolean[]) {
			return new BoolArray((boolean[]) value);
		}
		if (value instanceof short[]) {
			return new ShortArray((short[]) value);
		}
		if (value instanceof int[]) {
			return new IntArray((int[]) value);
		}
		if (value instanceof long[]) {
			return new LongArray((long[]) value);
		}
		if (value instanceof float[]) {
			return new FloatArray((float[]) value);
		}
		if (value instanceof double[]) {
			return new DoubleArray((double[]) value);
		}
		if (value instanceof Object[]) {
			return new ObjectArray<>((Object[]) value);
		}
		// This object is neither an array nor a collection.
		// So we wrap it in a list and return.
		final List<Object> list = new ObjectArray<>(Object.class);
		list.add(value);
		return list;
	}

	/**
	 * Allocates a 1-dimensional byte array matching the product of the given
	 * sizes.
	 *
	 * @param sizes list of sizes from which to allocate the array
	 * @return a byte array of the appropriate size
	 * @throws IllegalArgumentException if the total size exceeds 2GB, which is
	 *           the maximum size of an array in Java; or if any size argument is
	 *           zero or negative
	 */
	public static byte[] allocate(final long... sizes)
		throws IllegalArgumentException
	{
		if (sizes == null) return null;
		if (sizes.length == 0) return new byte[0];
		final int total = safeMultiply32(sizes);
		return new byte[total];
	}

	/**
	 * Checks that the product of the given sizes does not exceed the 32-bit
	 * integer limit (i.e., {@link Integer#MAX_VALUE}).
	 *
	 * @param sizes list of sizes from which to compute the product
	 * @return the product of the given sizes
	 * @throws IllegalArgumentException if the total size exceeds 2GiB, which is
	 *           the maximum size of an int in Java; or if any size argument is
	 *           zero or negative
	 */
	public static int safeMultiply32(final long... sizes)
		throws IllegalArgumentException
	{
		if (sizes.length == 0) return 0;
		long total = 1;
		for (final long size : sizes) {
			if (size < 1) {
				throw new IllegalArgumentException("Invalid array size: " +
					sizeAsProduct(sizes));
			}
			total *= size;
			if (total > Integer.MAX_VALUE) {
				throw new IllegalArgumentException("Array size too large: " +
					sizeAsProduct(sizes));
			}
		}
		// NB: The downcast to int is safe here, due to the checks above.
		return (int) total;
	}

	/**
	 * Checks that the product of the given sizes does not exceed the 64-bit
	 * integer limit (i.e., {@link Long#MAX_VALUE}).
	 *
	 * @param sizes list of sizes from which to compute the product
	 * @return the product of the given sizes
	 * @throws IllegalArgumentException if the total size exceeds 8EiB, which is
	 *           the maximum size of a long in Java; or if any size argument is
	 *           zero or negative
	 */
	public static long safeMultiply64(final long... sizes)
		throws IllegalArgumentException
	{
		if (sizes.length == 0) return 0;
		long total = 1;
		for (final long size : sizes) {
			if (size < 1) {
				throw new IllegalArgumentException("Invalid array size: " +
					sizeAsProduct(sizes));
			}
			if (willOverflow(total, size)) {
				throw new IllegalArgumentException("Array size too large: " +
					sizeAsProduct(sizes));
			}
			total *= size;
		}
		return total;
	}

	/** Returns true if the given value is contained in the specified array. */
	public static boolean contains(final byte[] array, final byte value) {
		return indexOf(array, value) != -1;
	}

	/** Returns true if the given value is contained in the specified array. */
	public static boolean contains(final boolean[] array, final boolean value) {
		return indexOf(array, value) != -1;
	}

	/** Returns true if the given value is contained in the specified array. */
	public static boolean contains(final char[] array, final char value) {
		return indexOf(array, value) != -1;
	}

	/** Returns true if the given value is contained in the specified array. */
	public static boolean contains(final double[] array, final double value) {
		return indexOf(array, value) != -1;
	}

	/** Returns true if the given value is contained in the specified array. */
	public static boolean contains(final float[] array, final float value) {
		return indexOf(array, value) != -1;
	}

	/** Returns true if the given value is contained in the specified array. */
	public static boolean contains(final int[] array, final int value) {
		return indexOf(array, value) != -1;
	}

	/** Returns true if the given value is contained in the specified array. */
	public static boolean contains(final long[] array, final long value) {
		return indexOf(array, value) != -1;
	}

	/** Returns true if the given value is contained in the specified array. */
	public static boolean contains(final short[] array, final short value) {
		return indexOf(array, value) != -1;
	}

	/** Returns true if the given value is contained in the specified array. */
	public static boolean contains(final Object[] array, final Object value) {
		return indexOf(array, value) != -1;
	}

	/**
	 * Returns the index of the first occurrence of the given value in the given
	 * array. If the value is not in the array, returns -1.
	 */
	public static int indexOf(final boolean[] array, final boolean value) {
		for (int i = 0; i < array.length; i++) {
			if (array[i] == value) return i;
		}
		return -1;
	}

	/**
	 * Returns the index of the first occurrence of the given value in the given
	 * array. If the value is not in the array, returns -1.
	 */
	public static int indexOf(final byte[] array, final byte value) {
		for (int i = 0; i < array.length; i++) {
			if (array[i] == value) return i;
		}
		return -1;
	}

	/**
	 * Returns the index of the first occurrence of the given value in the given
	 * array. If the value is not in the array, returns -1.
	 */
	public static int indexOf(final char[] array, final char value) {
		for (int i = 0; i < array.length; i++) {
			if (array[i] == value) return i;
		}
		return -1;
	}

	/**
	 * Returns the index of the first occurrence of the given value in the given
	 * array. If the value is not in the array, returns -1.
	 */
	public static int indexOf(final double[] array, final double value) {
		for (int i = 0; i < array.length; i++) {
			if (array[i] == value) return i;
		}
		return -1;
	}

	/**
	 * Returns the index of the first occurrence of the given value in the given
	 * array. If the value is not in the array, returns -1.
	 */
	public static int indexOf(final float[] array, final float value) {
		for (int i = 0; i < array.length; i++) {
			if (array[i] == value) return i;
		}
		return -1;
	}

	/**
	 * Returns the index of the first occurrence of the given value in the given
	 * array. If the value is not in the array, returns -1.
	 */
	public static int indexOf(final int[] array, final int value) {
		for (int i = 0; i < array.length; i++) {
			if (array[i] == value) return i;
		}
		return -1;
	}

	/**
	 * Returns the index of the first occurrence of the given value in the given
	 * array. If the value is not in the array, returns -1.
	 */
	public static int indexOf(final long[] array, final long value) {
		for (int i = 0; i < array.length; i++) {
			if (array[i] == value) return i;
		}
		return -1;
	}

	/**
	 * Returns the index of the first occurrence of the given value in the given
	 * array. If the value is not in the array, returns -1.
	 */
	public static int indexOf(final short[] array, final short value) {
		for (int i = 0; i < array.length; i++) {
			if (array[i] == value) return i;
		}
		return -1;
	}

	/**
	 * Returns the index of the first occurrence of the given value in the given
	 * Object array. If the value is not in the array, returns -1.
	 */
	public static int indexOf(final Object[] array, final Object value) {
		for (int i = 0; i < array.length; i++) {
			if (value == null) {
				if (array[i] == null) return i;
			}
			else if (value.equals(array[i])) return i;
		}
		return -1;
	}

	// -- Helper methods --

	private static String sizeAsProduct(final long... sizes) {
		final StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (final long size : sizes) {
			if (first) first = false;
			else sb.append(" x ");
			sb.append(size);
		}
		return sb.toString();
	}

	private static boolean willOverflow(final long v1, final long v2) {
		return Long.MAX_VALUE / v1 < v2;
	}

}
