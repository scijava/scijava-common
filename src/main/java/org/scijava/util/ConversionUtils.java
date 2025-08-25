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

package org.scijava.util;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;

import org.scijava.convert.ConversionRequest;
import org.scijava.convert.ConvertService;
import org.scijava.convert.Converter;

/** @deprecated use {@link ConvertService} and {@link Types} */
@Deprecated
public class ConversionUtils {

	private static List<Converter<?, ?>> converters = Arrays.asList(
		new org.scijava.convert.NullConverter(),
		new org.scijava.convert.CastingConverter(),
		new org.scijava.convert.ArrayConverters.BoolArrayUnwrapper(),
		new org.scijava.convert.ArrayConverters.BoolArrayWrapper(),
		new org.scijava.convert.ArrayConverters.ByteArrayUnwrapper(),
		new org.scijava.convert.ArrayConverters.ByteArrayWrapper(),
		new org.scijava.convert.ArrayConverters.CharArrayUnwrapper(),
		new org.scijava.convert.ArrayConverters.CharArrayWrapper(),
		new org.scijava.convert.ArrayConverters.DoubleArrayUnwrapper(),
		new org.scijava.convert.ArrayConverters.DoubleArrayWrapper(),
		new org.scijava.convert.ArrayConverters.FloatArrayUnwrapper(),
		new org.scijava.convert.ArrayConverters.FloatArrayWrapper(),
		new org.scijava.convert.ArrayConverters.IntArrayUnwrapper(),
		new org.scijava.convert.ArrayConverters.IntArrayWrapper(),
		new org.scijava.convert.ArrayConverters.LongArrayUnwrapper(),
		new org.scijava.convert.ArrayConverters.LongArrayWrapper(),
		new org.scijava.convert.ArrayConverters.ShortArrayUnwrapper(),
		new org.scijava.convert.ArrayConverters.ShortArrayWrapper(),
		new org.scijava.convert.FileListConverters.FileArrayToStringConverter(),
		new org.scijava.convert.FileListConverters.FileToStringConverter(),
		new org.scijava.convert.FileListConverters.StringToFileArrayConverter(),
		new org.scijava.convert.FileListConverters.StringToFileConverter(),
		new org.scijava.convert.NumberConverters.BigIntegerToBigDecimalConverter(),
		new org.scijava.convert.NumberConverters.ByteToBigDecimalConverter(),
		new org.scijava.convert.NumberConverters.ByteToBigIntegerConverter(),
		new org.scijava.convert.NumberConverters.ByteToDoubleConverter(),
		new org.scijava.convert.NumberConverters.ByteToFloatConverter(),
		new org.scijava.convert.NumberConverters.ByteToIntegerConverter(),
		new org.scijava.convert.NumberConverters.ByteToLongConverter(),
		new org.scijava.convert.NumberConverters.ByteToShortConverter(),
		new org.scijava.convert.NumberConverters.DoubleToBigDecimalConverter(),
		new org.scijava.convert.NumberConverters.FloatToBigDecimalConverter(),
		new org.scijava.convert.NumberConverters.FloatToDoubleConverter(),
		new org.scijava.convert.NumberConverters.IntegerToBigDecimalConverter(),
		new org.scijava.convert.NumberConverters.IntegerToBigIntegerConverter(),
		new org.scijava.convert.NumberConverters.IntegerToDoubleConverter(),
		new org.scijava.convert.NumberConverters.IntegerToLongConverter(),
		new org.scijava.convert.NumberConverters.LongToBigDecimalConverter(),
		new org.scijava.convert.NumberConverters.LongToBigIntegerConverter(),
		new org.scijava.convert.NumberConverters.ShortToBigDecimalConverter(),
		new org.scijava.convert.NumberConverters.ShortToBigIntegerConverter(),
		new org.scijava.convert.NumberConverters.ShortToDoubleConverter(),
		new org.scijava.convert.NumberConverters.ShortToFloatConverter(),
		new org.scijava.convert.NumberConverters.ShortToIntegerConverter(),
		new org.scijava.convert.NumberConverters.ShortToLongConverter(),
		new org.scijava.convert.StringToNumberConverter(),
		new org.scijava.convert.DefaultConverter());

	private ConversionUtils() {
		// prevent instantiation of utility class
	}

	// -- ConvertService setter --

	/** @deprecated This method should not be used anymore. */
	@Deprecated
	@SuppressWarnings("unused")
	public static void setDelegateService(final ConvertService convertService,
		final double priority)
	{
		// NB: This method is now a no-op.
	}

	// -- Deprecated methods --

	/**
	 * @deprecated
	 * @see Converter#convert(Object, Type)
	 */
	@Deprecated
	public static Object convert(final Object src, final Type dest) {
		final Converter<?, ?> handler = handler(new ConversionRequest(src, dest));
		return (handler == null ? null : handler.convert(src, dest));
	}

	/**
	 * @deprecated
	 * @see Converter#convert(Object, Class)
	 */
	@Deprecated
	public static <T> T convert(final Object src, final Class<T> dest) {
		final Converter<?, ?> handler = handler(new ConversionRequest(src, dest));
		return (handler == null ? null : handler.convert(src, dest));
	}

	/**
	 * @deprecated
	 * @see Converter#canConvert(Class, Type)
	 */
	@Deprecated
	public static boolean canConvert(final Class<?> src, final Type dest) {
		final Converter<?, ?> handler = handler(new ConversionRequest(src, dest));
		return (handler == null ? false : handler.canConvert(src, dest));
	}

	/**
	 * @deprecated
	 * @see Converter#canConvert(Class, Class)
	 */
	@Deprecated
	public static boolean canConvert(final Class<?> src, final Class<?> dest) {
		final Converter<?, ?> handler = handler(new ConversionRequest(src, dest));
		return (handler == null ? false : handler.canConvert(src, dest));
	}

	/**
	 * @deprecated
	 * @see Converter#canConvert(Object, Type)
	 */
	@Deprecated
	public static boolean canConvert(final Object src, final Type dest) {
		final Converter<?, ?> handler = handler(new ConversionRequest(src, dest));
		return (handler == null ? false : handler.canConvert(src, dest));
	}

	/**
	 * @deprecated
	 * @see Converter#canConvert(Object, Class)
	 */
	@Deprecated
	public static boolean canConvert(final Object src, final Class<?> dest) {
		final Converter<?, ?> handler = handler(new ConversionRequest(src, dest));
		return (handler == null ? false : handler.canConvert(src, dest));
	}

	/** @deprecated use {@link Types#enumValue} */
	@Deprecated
	public static <T> T convertToEnum(final String src, final Class<T> dest) {
		try {
			return Types.enumValue(src, dest);
		}
		catch (final IllegalArgumentException exc) {
			return null;
		}
	}

	/** @deprecated use {@link Types#raw} */
	@Deprecated
	public static Class<?> getClass(final Type type) {
		return Types.raw(type);
	}

	/** @deprecated use {@link Types#cast} */
	@Deprecated
	public static <T> T cast(final Object src, final Class<T> dest) {
		if (!canCast(src, dest)) return null;
		@SuppressWarnings("unchecked")
		final T result = (T) src;
		return result;
	}

	/** @deprecated use {@link Types#isAssignable} */
	@Deprecated
	public static boolean canAssign(final Class<?> src, final Class<?> dest) {
		return canCast(src, dest);
	}

	/** @deprecated use {@link Types#isInstance} */
	@Deprecated
	public static boolean canAssign(final Object src, final Class<?> dest) {
		return canCast(src, dest);
	}

	/** @deprecated use {@link Types#isAssignable} */
	@Deprecated
	public static boolean canCast(final Class<?> src, final Class<?> dest) {
		if (dest == null) return false;
		if (src == null) return true;
		return Types.isAssignable(Types.box(src), Types.box(dest));
	}

	/** @deprecated use {@link Types#isInstance} */
	@Deprecated
	public static boolean canCast(final Object src, final Class<?> dest) {
		if (dest == null) return false;
		return src == null || canCast(src.getClass(), dest);
	}

	/** @deprecated use {@link Types#raws} and {@link Types#component} */
	@Deprecated
	public static Class<?> getComponentClass(final Type type) {
		return Types.raw(Types.component(type));
	}

	/** @deprecated use {@link Types#unbox} */
	@Deprecated
	public static <T> Class<T> getPrimitiveType(final Class<T> type) {
		return Types.unbox(type);
	}

	/** @deprecated use {@link Types#box} */
	@Deprecated
	public static <T> Class<T> getNonprimitiveType(final Class<T> type) {
		return Types.box(type);
	}

	/** @deprecated Use {@link Types#nullValue} instead. */
	@Deprecated
	public static <T> T getNullValue(final Class<T> type) {
		return Types.nullValue(type);
	}

	// -- Helper methods --

	/**
	 * Gets the {@link Converter} to use for the given conversion request.
	 *
	 * @return The {@link Converter} to use for handling the given request.
	 */
	private static Converter<?, ?> handler(final ConversionRequest data) {
		return converters.stream().filter(c -> c.supports(data)).findFirst().orElse(
			null);
	}
}
