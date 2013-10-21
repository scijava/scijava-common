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

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Useful methods for converting and casting between classes and types.
 * 
 * @author Curtis Rueden
 * @author Mark Hiner
 */
public class ConversionUtils {

	private ConversionUtils() {
		// prevent instantiation of utility class
	}

	// -- Type conversion and casting --

	/**
	 * As {@link #convert(Object, Class)} but capable of creating and populating
	 * multi-element objects ({@link Collection}s and array types). If a single
	 * element type is provided, it will be converted the same as
	 * {@link #convert(Object, Class)}. If a multi-element type is detected, then
	 * the value parameter will be interpreted as potential collection of values.
	 * An appropriate container will be created, and the full set of values will
	 * be type converted and added.
	 * <p>
	 * NB: This method should be capable of creating any array type, but if a
	 * {@link Collection} interface or abstract class is provided we can only make
	 * a best guess as to what container type to instantiate. Defaults are
	 * provided for {@link Set} and {@link List} subclasses.
	 * </p>
	 * 
	 * @param value The object to convert.
	 * @param type Type to which the object should be converted.
	 */
	public static Object convert(final Object value, final Type type) {
		// NB: Regardless of whether the destination type is an array or collection,
		// we still want to cast directly if doing so is possible. But note that in
		// general, this check does not detect cases of incompatible generic
		// parameter types. If this limitation becomes a problem in the future we
		// can extend the logic here to provide additional signatures of canCast
		// which operate on Types in general rather than only Classes. However, the
		// logic could become complex very quickly in various subclassing cases,
		// generic parameters resolved vs. propagated, etc.
		final Class<?> c = getClass(type);
		if (c != null && canCast(value, c)) return cast(value, c);

		// Handle array types, including generic array types.
		if (isArray(type)) {
			return convertToArray(value, getComponentClass(type));
		}

		// Handle parameterized collection types.
		if (type instanceof ParameterizedType && isCollection(type)) {
			return convertToCollection(value, (ParameterizedType) type);
		}

		// This wasn't a collection or array, so convert it as a single element.
		return convert(value, getClass(type));
	}

	/**
	 * Converts the given object to an object of the specified type. The object is
	 * casted directly if possible, or else a new object is created using the
	 * destination type's public constructor that takes the original object as
	 * input (except when converting to {@link String}, which uses the
	 * {@link Object#toString()} method instead). In the case of primitive types,
	 * returns an object of the corresponding wrapped type. If the destination
	 * type does not have an appropriate constructor, returns null.
	 * 
	 * @param <T> Type to which the object should be converted.
	 * @param value The object to convert.
	 * @param type Type to which the object should be converted.
	 */
	public static <T> T convert(final Object value, final Class<T> type) {
		if (type == null) return null;
		if (value == null) return getNullValue(type);

		// ensure type is well-behaved, rather than a primitive type
		final Class<T> saneType = getNonprimitiveType(type);

		// cast the existing object, if possible
		if (canCast(value, saneType)) return cast(value, saneType);

		// special case for conversion from number to number
		if (value instanceof Number) {
			final Number number = (Number) value;
			if (saneType == Byte.class) {
				final Byte result = number.byteValue();
				@SuppressWarnings("unchecked")
				final T typedResult = (T) result;
				return typedResult;
			}
			if (saneType == Double.class) {
				final Double result = number.doubleValue();
				@SuppressWarnings("unchecked")
				final T typedResult = (T) result;
				return typedResult;
			}
			if (saneType == Float.class) {
				final Float result = number.floatValue();
				@SuppressWarnings("unchecked")
				final T typedResult = (T) result;
				return typedResult;
			}
			if (saneType == Integer.class) {
				final Integer result = number.intValue();
				@SuppressWarnings("unchecked")
				final T typedResult = (T) result;
				return typedResult;
			}
			if (saneType == Long.class) {
				final Long result = number.longValue();
				@SuppressWarnings("unchecked")
				final T typedResult = (T) result;
				return typedResult;
			}
			if (saneType == Short.class) {
				final Short result = number.shortValue();
				@SuppressWarnings("unchecked")
				final T typedResult = (T) result;
				return typedResult;
			}
		}

		// special cases for strings
		if (value instanceof String) {
			// source type is String
			final String s = (String) value;
			if (s.isEmpty()) {
				// return null for empty strings
				return getNullValue(type);
			}

			// use first character when converting to Character
			if (saneType == Character.class) {
				final Character c = new Character(s.charAt(0));
				@SuppressWarnings("unchecked")
				final T result = (T) c;
				return result;
			}
		}
		if (saneType == String.class) {
			// destination type is String; use Object.toString() method
			final String sValue = value.toString();
			@SuppressWarnings("unchecked")
			final T result = (T) sValue;
			return result;
		}

		// wrap the original object with one of the new type, using a constructor
		try {
			final Constructor<?> ctor = getConstructor(saneType, value.getClass());
			if (ctor == null) return null;
			@SuppressWarnings("unchecked")
			final T instance = (T) ctor.newInstance(value);
			return instance;
		}
		catch (final Exception exc) {
			// no known way to convert
			return null;
		}
	}

	/**
	 * Checks whether objects of the given class can be converted to the specified
	 * type.
	 * 
	 * @see #convert(Object, Class)
	 */
	public static boolean canConvert(final Class<?> c, final Class<?> type) {
		// ensure type is well-behaved, rather than a primitive type
		final Class<?> saneType = getNonprimitiveType(type);

		// OK if the existing object can be casted
		if (canCast(c, saneType)) return true;

		// OK for numerical conversions
		if (canCast(getNonprimitiveType(c), Number.class) &&
			(ClassUtils.isByte(type) || ClassUtils.isDouble(type) ||
				ClassUtils.isFloat(type) || ClassUtils.isInteger(type) ||
				ClassUtils.isLong(type) || ClassUtils.isShort(type)))
		{
			return true;
		}

		// OK if string
		if (saneType == String.class) return true;

		// OK if source type is string and destination type is character
		// (in this case, the first character of the string would be used)
		if (canCast(c, String.class) && saneType == Character.class) {
			return true;
		}

		// OK if appropriate wrapper constructor exists
		try {
			return getConstructor(saneType, c) != null;
		}
		catch (final Exception exc) {
			// no known way to convert
			return false;
		}
	}

	/**
	 * Checks whether the given object can be converted to the specified type.
	 * 
	 * @see #convert(Object, Class)
	 */
	public static boolean canConvert(final Object value, final Class<?> type) {
		if (value == null) return true;
		return canConvert(value.getClass(), type);
	}

	/**
	 * Casts the given object to the specified type, or null if the types are
	 * incompatible.
	 */
	public static <T> T cast(final Object obj, final Class<T> type) {
		if (!canCast(obj, type)) return null;
		@SuppressWarnings("unchecked")
		final T result = (T) obj;
		return result;
	}

	/**
	 * Checks whether objects of the given class can be cast to the specified
	 * type.
	 * 
	 * @see #cast(Object, Class)
	 */
	public static boolean canCast(final Class<?> c, final Class<?> type) {
		return type.isAssignableFrom(c);
	}

	/**
	 * Checks whether the given object can be cast to the specified type.
	 * 
	 * @see #cast(Object, Class)
	 */
	public static boolean canCast(final Object obj, final Class<?> type) {
		return obj == null || canCast(obj.getClass(), type);
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

	/**
	 * Gets the raw class corresponding to the given type.
	 * <p>
	 * If the type is a {@link Class} it is simply casted. In the case of a
	 * {@link ParameterizedType}, then {@link ParameterizedType#getRawType()} is
	 * returned. Otherwise, returns null.
	 * </p>
	 */
	public static Class<?> getClass(final Type type) {
		if (type instanceof Class) return (Class<?>) type;

		if (type instanceof ParameterizedType) {
			return getClass(((ParameterizedType) type).getRawType());
		}

		return null;
	}

	/**
	 * Gets the component type of the given array type, or null if not an array.
	 * Supports both regular array types (i.e., {@link Class#getComponentType()}
	 * if {@code type} is a {@link Class}) and generic array types (i.e.,
	 * {@link GenericArrayType#getGenericComponentType()} if {@code type} is a
	 * {@link GenericArrayType}).
	 */
	public static Class<?> getComponentClass(final Type type) {
		if (type instanceof Class) return ((Class<?>) type).getComponentType();
		if (type instanceof GenericArrayType) {
			return getClass(((GenericArrayType) type).getGenericComponentType());
		}
		return null;
	}

	// -- Helper methods --

	private static Constructor<?> getConstructor(final Class<?> type,
		final Class<?> argType)
	{
		for (final Constructor<?> ctor : type.getConstructors()) {
			final Class<?>[] params = ctor.getParameterTypes();
			if (params.length == 1 && canCast(argType, params[0])) {
				return ctor;
			}
		}
		return null;
	}

	private static boolean isArray(final Type type) {
		return getComponentClass(type) != null;
	}

	private static boolean isCollection(final Type type) {
		return canCast(getClass(type), Collection.class);
	}

	private static Object convertToArray(final Object value,
		final Class<?> componentType)
	{
		// First we make sure the value is a collection. This provides the simplest
		// interface for iterating over all the elements. We use SciJava's
		// PrimitiveArray collection implementations internally, so that this
		// conversion is always wrapping by reference, for performance.
		final Collection<?> items = ArrayUtils.toCollection(value);

		final Object array = Array.newInstance(componentType, items.size());

		// Populate the array by converting each item in the value collection
		// to the component type.
		int index = 0;
		for (final Object item : items) {
			Array.set(array, index++, convert(item, componentType));
		}
		return array;
	}

	private static Object convertToCollection(final Object value,
		final ParameterizedType pType)
	{
		final Collection<Object> collection = createCollection(getClass(pType));
		if (collection == null) return null;

		// Populate the collection.
		final Collection<?> items = ArrayUtils.toCollection(value);
		final Type collectionType = pType.getActualTypeArguments()[0];
		for (final Object item : items) {
			collection.add(convert(item, collectionType));
		}

		return collection;
	}

	private static Collection<Object> createCollection(final Class<?> type) {
		// If we were given an interface or abstract class, and not a concrete
		// class, we attempt to make default implementations.
		if (type.isInterface() || Modifier.isAbstract(type.getModifiers())) {
			// We don't have a concrete class. If it's a set or a list, we use
			// the typical default implementation. Otherwise we won't convert.
			if (canCast(type, List.class)) return new ArrayList<Object>();
			if (canCast(type, Set.class)) return new HashSet<Object>();
			return null;
		}

		// Got a concrete type. Instantiate it.
		try {
			@SuppressWarnings("unchecked")
			final Collection<Object> c = (Collection<Object>) type.newInstance();
			return c;
		}
		catch (final InstantiationException exc) {
			return null;
		}
		catch (final IllegalAccessException exc) {
			return null;
		}
	}

}
