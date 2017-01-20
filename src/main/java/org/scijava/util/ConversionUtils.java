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

import java.lang.reflect.Type;

import org.scijava.convert.ConversionRequest;
import org.scijava.convert.ConvertService;
import org.scijava.convert.Converter;
import org.scijava.convert.DefaultConverter;

/**
 * Useful methods for converting and casting between classes and types.
 * <p>
 * For extensible type conversion, use {@link ConvertService}.
 * </p>
 *
 * @author Curtis Rueden
 * @author Mark Hiner
 */
public class ConversionUtils {

	private static ConvertService convertService;

	private static Converter<?, ?> converterNoContext;

	private static double servicePriority = 0.0;

	private ConversionUtils() {
		// prevent instantiation of utility class
	}

	// -- Type casting --

	/**
	 * Converts the given string value to an enumeration constant of the specified
	 * type.
	 *
	 * @param src The value to convert.
	 * @param dest The type of the enumeration constant.
	 * @return The converted enumeration constant, or null if the type is not an
	 *         enumeration type or has no such constant.
	 */
	public static <T> T convertToEnum(final String src, final Class<T> dest) {
		if (src == null || !dest.isEnum()) return null;
		try {
			@SuppressWarnings({ "rawtypes", "unchecked" })
			final Enum result = Enum.valueOf((Class) dest, src);
			@SuppressWarnings("unchecked")
			final T typedResult = (T) result;
			return typedResult;
		}
		catch (final IllegalArgumentException exc) {
			// no such enum constant
			return null;
		}
	}

	/**
	 * Casts the given object to the specified type, or null if the types are
	 * incompatible.
	 */
	public static <T> T cast(final Object src, final Class<T> dest) {
		if (!canCast(src, dest)) return null;
		@SuppressWarnings("unchecked")
		final T result = (T) src;
		return result;
	}

	/**
	 * Checks whether objects of the given class can be cast to the specified
	 * type.
	 *
	 * @return true If the destination class is assignable from the source one, or
	 *         if the source class is null and destination class is non-null.
	 * @see #cast(Object, Class)
	 */
	public static boolean canCast(final Class<?> src, final Class<?> dest) {
		if (dest == null) return false;
		return src == null || dest.isAssignableFrom(src);
	}

	/**
	 * Checks whether the given object can be cast to the specified type.
	 *
	 * @return true If the destination class is assignable from the source
	 *         object's class, or if the source object is null and destionation
	 *         class is non-null.
	 * @see #cast(Object, Class)
	 */
	public static boolean canCast(final Object src, final Class<?> dest) {
		if (dest == null) return false;
		return src == null || canCast(src.getClass(), dest);
	}

	/**
	 * Returns the primitive {@link Class} closest to the given type.
	 * <p>
	 * Specifically, the following type conversions are done:
	 * <ul>
	 * <li>Boolean.class becomes boolean.class</li>
	 * <li>Byte.class becomes byte.class</li>
	 * <li>Character.class becomes char.class</li>
	 * <li>Double.class becomes double.class</li>
	 * <li>Float.class becomes float.class</li>
	 * <li>Integer.class becomes int.class</li>
	 * <li>Long.class becomes long.class</li>
	 * <li>Short.class becomes short.class</li>
	 * <li>Void.class becomes void.class</li>
	 * </ul>
	 * All other types are unchanged.
	 * </p>
	 */
	public static <T> Class<T> getPrimitiveType(final Class<T> type) {
		final Class<?> destType;
		if (type == Boolean.class) destType = boolean.class;
		else if (type == Byte.class) destType = byte.class;
		else if (type == Character.class) destType = char.class;
		else if (type == Double.class) destType = double.class;
		else if (type == Float.class) destType = float.class;
		else if (type == Integer.class) destType = int.class;
		else if (type == Long.class) destType = long.class;
		else if (type == Short.class) destType = short.class;
		else if (type == Void.class) destType = void.class;
		else destType = type;
		@SuppressWarnings("unchecked")
		final Class<T> result = (Class<T>) destType;
		return result;
	}

	/**
	 * Returns the non-primitive {@link Class} closest to the given type.
	 * <p>
	 * Specifically, the following type conversions are done:
	 * <ul>
	 * <li>boolean.class becomes Boolean.class</li>
	 * <li>byte.class becomes Byte.class</li>
	 * <li>char.class becomes Character.class</li>
	 * <li>double.class becomes Double.class</li>
	 * <li>float.class becomes Float.class</li>
	 * <li>int.class becomes Integer.class</li>
	 * <li>long.class becomes Long.class</li>
	 * <li>short.class becomes Short.class</li>
	 * <li>void.class becomes Void.class</li>
	 * </ul>
	 * All other types are unchanged.
	 * </p>
	 */
	public static <T> Class<T> getNonprimitiveType(final Class<T> type) {
		final Class<?> destType;
		if (type == boolean.class) destType = Boolean.class;
		else if (type == byte.class) destType = Byte.class;
		else if (type == char.class) destType = Character.class;
		else if (type == double.class) destType = Double.class;
		else if (type == float.class) destType = Float.class;
		else if (type == int.class) destType = Integer.class;
		else if (type == long.class) destType = Long.class;
		else if (type == short.class) destType = Short.class;
		else if (type == void.class) destType = Void.class;
		else destType = type;
		@SuppressWarnings("unchecked")
		final Class<T> result = (Class<T>) destType;
		return result;
	}

	/**
	 * Gets the "null" value for the given type. For non-primitives, this will
	 * actually be null. For primitives, it will be zero for numeric types, false
	 * for boolean, and the null character for char.
	 */
	public static <T> T getNullValue(final Class<T> type) {
		final Object defaultValue;
		if (type == boolean.class) defaultValue = false;
		else if (type == byte.class) defaultValue = (byte) 0;
		else if (type == char.class) defaultValue = '\0';
		else if (type == double.class) defaultValue = 0d;
		else if (type == float.class) defaultValue = 0f;
		else if (type == int.class) defaultValue = 0;
		else if (type == long.class) defaultValue = 0L;
		else if (type == short.class) defaultValue = (short) 0;
		else defaultValue = null;
		@SuppressWarnings("unchecked")
		final T result = (T) defaultValue;
		return result;
	}

	// -- ConvertService setter --

	/**
	 * Sets the {@link ConvertService} to use for handling conversion requests.
	 */
	public static void setDelegateService(final ConvertService convertService,
		final double priority)
	{
		if (ConversionUtils.convertService == null ||
			Double.compare(priority, servicePriority) > 0)
		{
			ConversionUtils.convertService = convertService;
			servicePriority = priority;
		}
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

	/** @deprecated use {@link GenericUtils#getClass(Type)} */
	@Deprecated
	public static Class<?> getClass(final Type type) {
		return GenericUtils.getClass(type);
	}

	/** @deprecated use {@link GenericUtils#getComponentClass(Type)} */
	@Deprecated
	public static Class<?> getComponentClass(final Type type) {
		return GenericUtils.getComponentClass(type);
	}

//-- Helper methods --

	/**
	 * Gets the {@link Converter} to use for the given conversion request. If the
	 * delegate {@link ConvertService} has not been explicitly set, then a
	 * {@link DefaultConverter} will be used.
	 *
	 * @return The {@link Converter} to use for handling the given request.
	 */
	private static Converter<?, ?> handler(final ConversionRequest data) {
		if (convertService != null) return convertService.getHandler(data);

		if (converterNoContext == null) converterNoContext = new DefaultConverter();

		return converterNoContext;
	}
}
