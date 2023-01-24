/*
 * #%L
 * SciJava Common shared library for SciJava software.
 * %%
 * Copyright (C) 2009 - 2023 SciJava developers.
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
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;
import org.scijava.util.ArrayUtils;
import org.scijava.util.ConversionUtils;
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
 * Object
 * </li>
 * </ul>
 *
 * @author Mark Hiner
 */
@Plugin(type = Converter.class, priority = Priority.EXTREMELY_LOW)
public class DefaultConverter extends AbstractConverter<Object, Object> {

	// -- ConversionHandler methods --

	@Override
	public Object convert(final Object src, final Type dest) {

		// Handle array types, including generic array types.
		final Type componentType = Types.component(dest);
		if (componentType != null) {
			// NB: Destination is an array type.
			return convertToArray(src, Types.raw(componentType));
		}

		// Handle parameterized collection types.
		if (dest instanceof ParameterizedType && isCollection(dest)) {
			return convertToCollection(src, (ParameterizedType) dest);
		}

		// This wasn't a collection or array, so convert it as a single element.
		return convert(src, Types.raw(dest));
	}

	@Override
	public <T> T convert(final Object src, final Class<T> dest) {
		// ensure type is well-behaved, rather than a primitive type
		final Class<T> saneDest = Types.box(dest);

		// Handle array types
		if (isArray(dest)) {
			@SuppressWarnings("unchecked")
			T array = (T) convertToArray(src, Types.raw(Types.component(dest)));
			return array;
		}

		// special case for conversion from number to number
		if (src instanceof Number) {
			final Number number = (Number) src;
			if (saneDest == Byte.class) {
				final Byte result = number.byteValue();
				@SuppressWarnings("unchecked")
				final T typedResult = (T) result;
				return typedResult;
			}
			if (saneDest == Double.class) {
				final Double result = number.doubleValue();
				@SuppressWarnings("unchecked")
				final T typedResult = (T) result;
				return typedResult;
			}
			if (saneDest == Float.class) {
				final Float result = number.floatValue();
				@SuppressWarnings("unchecked")
				final T typedResult = (T) result;
				return typedResult;
			}
			if (saneDest == Integer.class) {
				final Integer result = number.intValue();
				@SuppressWarnings("unchecked")
				final T typedResult = (T) result;
				return typedResult;
			}
			if (saneDest == Long.class) {
				final Long result = number.longValue();
				@SuppressWarnings("unchecked")
				final T typedResult = (T) result;
				return typedResult;
			}
			if (saneDest == Short.class) {
				final Short result = number.shortValue();
				@SuppressWarnings("unchecked")
				final T typedResult = (T) result;
				return typedResult;
			}
		}

		// special cases for strings
		if (src instanceof String) {
			// source type is String
			final String s = (String) src;
			if (s.isEmpty()) {
				// return null for empty strings
				return Types.nullValue(dest);
			}

			// use first character when converting to Character
			if (saneDest == Character.class) {
				final Character c = new Character(s.charAt(0));
				@SuppressWarnings("unchecked")
				final T result = (T) c;
				return result;
			}

			// special case for conversion to enum
			if (dest.isEnum()) {
				final T result = ConversionUtils.convertToEnum(s, dest);
				if (result != null) return result;
			}
		}
		if (saneDest == String.class) {
			// destination type is String; use Object.toString() method
			final String sValue = src.toString();
			@SuppressWarnings("unchecked")
			final T result = (T) sValue;
			return result;
		}

		// wrap the original object with one of the new type, using a constructor
		try {
			final Constructor<?> ctor = getConstructor(saneDest, src.getClass());
			if (ctor == null) return null;
			@SuppressWarnings("unchecked")
			final T instance = (T) ctor.newInstance(src);
			return instance;
		}
		catch (final Exception exc) {
			// TODO: Best not to catch blanket Exceptions here.
			// no known way to convert
			return null;
		}
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

	private boolean isCollection(final Type type) {
		return Types.isAssignable(Types.raw(type), Collection.class);
	}

	private Object
		convertToArray(final Object value, final Class<?> componentType)
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
		final ParameterizedType pType)
	{
		final Collection<Object> collection = createCollection(Types.raw(pType));
		if (collection == null) return null;

		// Populate the collection.
		final Collection<?> items = ArrayUtils.toCollection(value);
		// TODO: The following can fail; e.g. "Foo extends ArrayList<String>"
		final Type collectionType = pType.getActualTypeArguments()[0];
		for (final Object item : items) {
			collection.add(convert(item, collectionType));
		}

		return collection;
	}

	private Collection<Object> createCollection(final Class<?> type) {
		// If we were given an interface or abstract class, and not a concrete
		// class, we attempt to make default implementations.
		if (type.isInterface() || Modifier.isAbstract(type.getModifiers())) {
			// We don't have a concrete class. If it's a set or a list, we use
			// the typical default implementation. Otherwise we won't convert.
			if (Types.isAssignable(type, List.class)) return new ArrayList<>();
			if (Types.isAssignable(type, Set.class)) return new HashSet<>();
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

	// -- Deprecated API --

	@Override
	@Deprecated
	public boolean canConvert(final Class<?> src, final Type dest) {

		// Handle array types, including generic array types.
		// The logic follows from the types that ArrayUtils.toCollection
		// can convert
		if (isArray(dest)){
			// toCollection handles any type of Collection
			if (Collection.class.isAssignableFrom(src)) return true;
			// toCollection handles any type of array
			if (src.isArray()) return true;
			// toCollection can wrap objects into a Singleton list,
			// but we only want to wrap up a T if the dest type is a T[].
			return Types.isAssignable(src, Types.component(dest));
		}

		// Handle parameterized collection types.
		if (dest instanceof ParameterizedType && isCollection(dest) &&
			createCollection(Types.raw(dest)) != null)
		{
			return true;
		}
		
		return super.canConvert(src, dest);
	}

	@Override
	@Deprecated
	public boolean canConvert(final Class<?> src, final Class<?> dest) {
		// ensure type is well-behaved, rather than a primitive type
		final Class<?> saneDest = Types.box(dest);

		// OK for numerical conversions
		if (Types.isAssignable(Types.box(src), Number.class) && //
			(Types.isByte(dest) || Types.isDouble(dest) || Types.isFloat(dest) ||
				Types.isInteger(dest) || Types.isLong(dest) || Types.isShort(dest)))
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
		try {
			return getConstructor(saneDest, src) != null;
		}
		catch (final Exception exc) {
			// TODO: Best not to catch blanket Exceptions here.
			// no known way to convert
			return false;
		}
	}
}
