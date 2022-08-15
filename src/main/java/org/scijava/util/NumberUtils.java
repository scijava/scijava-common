/*
 * #%L
 * SciJava Common shared library for SciJava software.
 * %%
 * Copyright (C) 2009 - 2022 SciJava developers.
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

import java.math.BigDecimal;
import java.math.BigInteger;

import org.scijava.util.Types;


/**
 * Useful methods for working with {@link Number} objects.
 * 
 * @author Curtis Rueden
 * @author Barry DeZonia
 */
public final class NumberUtils {

	private NumberUtils() {
		// prevent instantiation of utility class
	}

	/**
	 * Converts the given object to a {@link Number} of the specified type, or
	 * null if the types are incompatible.
	 */
	public static Number toNumber(final Object value, final Class<?> type) {
		final Object num = ConversionUtils.convert(value, type);
		return num == null ? null : Types.cast(num, Number.class);
	}

	public static BigDecimal asBigDecimal(final Number n) {
		// Using .doubleValue on a long or BigInteger would cause loss of accuracy
		if(BigInteger.class.isInstance(n)){
			return new BigDecimal((BigInteger) n);
		}
		else if(Long.class.isInstance(n)){
			return new BigDecimal(n.longValue());
		}
		return new BigDecimal(n.doubleValue());
	}

	public static BigInteger asBigInteger(final Number n) {
		return BigInteger.valueOf(n.longValue());
	}

	public static Number getMinimumNumber(final Class<?> type) {
		if (Types.isByte(type)) return Byte.MIN_VALUE;
		if (Types.isShort(type)) return Short.MIN_VALUE;
		if (Types.isInteger(type)) return Integer.MIN_VALUE;
		if (Types.isLong(type)) return Long.MIN_VALUE;
		if (Types.isFloat(type)) return -Float.MAX_VALUE;
		if (Types.isDouble(type)) return -Double.MAX_VALUE;
		// Fallback for Number.class
		if (Types.isNumber(type)) return -Double.MAX_VALUE;
		return null;
	}

	public static Number getMaximumNumber(final Class<?> type) {
		if (Types.isByte(type)) return Byte.MAX_VALUE;
		if (Types.isShort(type)) return Short.MAX_VALUE;
		if (Types.isInteger(type)) return Integer.MAX_VALUE;
		if (Types.isLong(type)) return Long.MAX_VALUE;
		if (Types.isFloat(type)) return Float.MAX_VALUE;
		if (Types.isDouble(type)) return Double.MAX_VALUE;
		// Fallback for Number.class
		if (Types.isNumber(type)) return Double.MAX_VALUE;
		return null;
	}

	public static Number getDefaultValue(final Number min, final Number max,
		final Class<?> type)
	{
		if (min != null) return min;
		if (max != null) return max;
		return toNumber("0", type);
	}

	public static Number clampToRange(final Class<?> type, final Number value,
		final Number min, final Number max)
	{
		if (value == null) return getDefaultValue(min, max, type);
		if (Comparable.class.isAssignableFrom(type)) {
			@SuppressWarnings("unchecked")
			final Comparable<Number> cValue = (Comparable<Number>) value;
			if (min != null && cValue.compareTo(min) < 0) return min;
			if (max != null && cValue.compareTo(max) > 0) return max;
		}
		return value;
	}

}
