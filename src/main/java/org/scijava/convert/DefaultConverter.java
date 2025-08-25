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

package org.scijava.convert;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;
import org.scijava.util.ArrayUtils;
import org.scijava.util.Types;

/**
 * Default {@link Converter} implementation. Provides useful conversion
 * functionality for many common conversion cases.
 * <p>
 * Supported conversions include:
 * </p>
 * <ul>
 * <li>Object to Array</li>
 * <li>Object to Collection</li>
 * <li>Number to Number</li>
 * <li>Object to String</li>
 * <li>String to Character</li>
 * <li>String to Enum</li>
 * <li>Objects where the destination Class has a constructor which takes that
 * Object</li>
 * </ul>
 *
 * @author Mark Hiner
 */
@Plugin(type = Converter.class, priority = Priority.EXTREMELY_LOW)
public class DefaultConverter extends AbstractConverter<Object, Object> {

	// -- ConversionHandler methods --

	@Override
	public Object convert(final Object src, final Type dest) {
		// special case: CharSequence -> char[]
		// otherwise, String -> char[] ends up length 1 with first char only
		if (src instanceof CharSequence && dest == char[].class) {
			return ((CharSequence) src).toString().toCharArray();
		}

		// Handle array types, including generic array types.
		final Type componentType = Types.component(dest);
		if (componentType != null) {
			// NB: Destination is an array type.
			return convertToArray(src, Types.raw(componentType));
		}

		// Handle collection types, either raw or parameterized.
		Class<?> cClass = collectionClass(dest);
		if (cClass != null) {
			Type elementType = Types.param(dest, Collection.class, 0);
			if (elementType == null) elementType = Object.class; // raw collection
			final Object collection = convertToCollection(src, cClass, elementType);
			if (collection != null) return collection;
			// NB: If this conversion failed, it might still succeed later
			// when looking for a wrapping constructor. So let's keep going.
			// In particular, see ConvertServiceTest#testConvertSubclass().
		}

		// Ensure type is a well-behaved class, rather than a primitive type.
		final Class<?> destClass = Types.raw(dest);
		final Class<?> saneDest = Types.box(destClass);

		// Object is already the requested type.
		if (Types.isInstance(src, saneDest)) return src;

		// special case for conversion from number to number
		if (src instanceof Number) {
			final Number number = (Number) src;
			if (saneDest == Byte.class) return number.byteValue();
			if (saneDest == Double.class) return number.doubleValue();
			if (saneDest == Float.class) return number.floatValue();
			if (saneDest == Integer.class) return number.intValue();
			if (saneDest == Long.class) return number.longValue();
			if (saneDest == Short.class) return number.shortValue();
		}

		// special cases for strings
		if (src instanceof String) {
			// source type is String
			final String s = (String) src;
			if (s.isEmpty()) {
				// return null for empty strings
				return Types.nullValue(saneDest);
			}

			// use first character when converting to Character
			if (saneDest == Character.class) {
				return new Character(s.charAt(0));
			}

			// special case for conversion to enum
			if (saneDest.isEnum()) {
				try {
					return Types.enumFromString(s, saneDest);
				}
				catch (final IllegalArgumentException exc) {
					// NB: No action needed.
				}
			}
		}

		if (saneDest == String.class) {
			// destination type is String; use Object.toString() method
			return src.toString();
		}

		// wrap the original object with one of the new type, using a constructor
		try {
			final Constructor<?> ctor = getConstructor(saneDest, src.getClass());
			if (ctor == null) return null;
			return ctor.newInstance(src);
		}
		catch (final InstantiationException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException exc)
		{
			// no known way to convert
			return Types.nullValue(destClass);
		}
	}

	@Override
	public <T> T convert(final Object src, final Class<T> dest) {
		// NB: Invert functional flow from Converter interface:
		// Converter: convert(Class, Type) calling convert(Class, Class)
		// becomes: convert(Class, Class) calling convert(Class, Type)
		final Type destType = dest;
		@SuppressWarnings("unchecked")
		final T result = (T) convert(src, destType);
		return result;
	}

	@Override
	public Class<Object> getOutputType() {
		return Object.class;
	}

	@Override
	public Class<Object> getInputType() {
		return Object.class;
	}

	// -- Helper methods --

	private Constructor<?> getConstructor(final Class<?> type,
		final Class<?> argType)
	{
		for (final Constructor<?> ctor : type.getConstructors()) {
			final Class<?>[] params = ctor.getParameterTypes();
			if (params.length == 1 && //
				Types.isAssignable(Types.box(argType), Types.box(params[0])))
			{
				return ctor;
			}
		}
		return null;
	}

	private boolean isArray(final Type type) {
		return Types.component(type) != null;
	}

	private Class<?> collectionClass(final Type type) {
		return Types.raws(type).stream() //
			.filter(t -> Types.isAssignable(t, Collection.class)) //
			.findFirst().orElse(null);
	}

	private Object convertToArray(final Object value,
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

	private Object convertToCollection(final Object value,
		final Class<?> collectionType, final Type elementType)
	{
		final Collection<Object> collection = createCollection(collectionType);
		if (collection == null) return null;

		// Populate the collection.
		final Collection<?> items = ArrayUtils.toCollection(value);
		for (final Object item : items) {
			collection.add(convert(item, elementType));
		}

		return collection;
	}

	private Collection<Object> createCollection(Class<?> type) {
		// Support conversion to common collection interface types.
		if (type == Queue.class || type == Deque.class) type = ArrayDeque.class;
		else if (type == Set.class) type = LinkedHashSet.class;
		else if (type == List.class || type == Collection.class) type =
			ArrayList.class;
		else if (type.isInterface() || Modifier.isAbstract(type.getModifiers())) {
			// We were given an interface or abstract class, and not a concrete
			// class, and we don't know what default implementation to use.
			return null;
		}

		// We now have a concrete type. Instantiate it.
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

	// -- Deprecated API --

	@Override
	public boolean canConvert(final Class<?> src, final Class<?> dest) {
		// OK for array and collection types.
		if (isArray(dest)) return true;
		Class<?> cClass = collectionClass(dest);
		if (cClass != null && createCollection(cClass) != null) return true;

		// ensure type is well-behaved, rather than a primitive type
		final Class<?> saneDest = Types.box(dest);

		// OK for numerical conversions
		if (Types.isAssignable(Types.box(src), Number.class) && //
			(Types.isByte(saneDest) || Types.isDouble(saneDest) || //
				Types.isFloat(saneDest) || Types.isInteger(saneDest) || //
				Types.isLong(saneDest) || Types.isShort(saneDest)))
		{
			return true;
		}

		// OK if string
		if (saneDest == String.class) return true;

		if (Types.isAssignable(src, String.class)) {
			// OK if source type is string and destination type is character
			// (in this case, the first character of the string would be used)
			if (saneDest == Character.class) return true;

			// OK if source type is string and destination type is an enum
			if (dest.isEnum()) return true;
		}

		// OK if appropriate wrapper constructor exists
		return getConstructor(saneDest, src) != null;
	}
}
