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
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.scijava.Priority;
import org.scijava.parse.Item;
import org.scijava.parse.Items;
import org.scijava.parse.ParseService;
import org.scijava.parsington.Token;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.util.Types;

/**
 * A {@link Converter} that specializes in converting {@link String}s to
 * n-dimensional arrays. This {@link Converter} can convert any array whose
 * component types can be created from a {@link String}. By default, this
 * {@link Converter} delimits the {@link String} based on commas.
 * 
 * @author Gabriel Selzer
 */
@Plugin(type = Converter.class, priority = Priority.VERY_LOW)
public class StringToArrayConverter extends AbstractConverter<String, Object> {

	@Parameter(required = false)
	private ConvertService convertService;

	@Parameter(required = false)
	private ParseService parseService;

	@Override
	public boolean canConvert(final Object src, final Type dest) {
		return canConvert(src, Types.raw(dest));
	}

	@Override
	public boolean canConvert(final Object src, final Class<?> dest) {
		if (convertService == null || parseService == null) return false;

		// First, ensure the base types conform
		if (!canConvert(src.getClass(), dest)) return false;
		// Then, ensure we can parse the string
		try {
			parseService.parse((String) src, false);
		}
		catch (final IllegalArgumentException e) {
			return false;
		}
		return true;
	}

	@Override
	public boolean canConvert(final Class<?> src, final Class<?> dest) {
		return src == String.class && dest.isArray();
	}

	@Override
	public Object convert(final Object src, final Type dest) {
		final Type componentType = Types.component(dest);
		if (componentType == null) {
			throw new IllegalArgumentException(dest + " is not an array type!");
		}
		final List<?> items = parse((String) src);
		return convertToArray(items, Types.raw(componentType));
	}

	@Override
	public <T> T convert(final Object src, final Class<T> dest) {
		// NB: Invert functional flow from Converter interface:
		// Converter: convert(Object, Type) calling convert(Object, Class)
		// becomes: convert(Object, Class) calling convert(Object, Type)
		final Type destType = dest;
		@SuppressWarnings("unchecked")
		T result = (T) convert(src, destType);
		return result;
	}

	@Override
	public Class<Object> getOutputType() {
		return Object.class;
	}

	@Override
	public Class<String> getInputType() {
		return String.class;
	}

	// -- Helper methods --

	/**
	 * Converts {@code src} into an array of component type {@code componentType}.
	 * 
	 * @param tree the {@link String} to convert
	 * @param componentType the component type of the output array
	 * @return an array of {@code componentType} whose elements were created from
	 *         {@code src}
	 */
	private Object convertToArray(final List<?> tree,
		final Class<?> componentType)
	{
		// Create the array
		final Object array = Array.newInstance(componentType, tree.size());
		// Set each element of the array
		for (int i = 0; i < tree.size(); i++) {
			Object element = tree.get(i);
			final Object converted = convertService.convert(element, componentType);
			Array.set(array, i, converted);
		}
		return array;
	}

	/** Parses a string to a list, using the {@link ParseService}. */
	private List<?> parse(final String s) {
		try {
			Items items = parseService.parse(s, false);
			return (List<?>) unwrap(items);
		}
		catch (final IllegalArgumentException e) {
			return null;
		}
	}

	private Object unwrap(final Object o) {
		if (o instanceof Collection) {
			return ((Collection<?>) o).stream() //
				.map(item -> unwrap(item)) //
				.collect(Collectors.toList());
		}
		if (o instanceof Item) {
			return unwrap(((Item) o).value());
		}
		if (o instanceof Token) {
			return ((Token) o).getToken();
		}
		return o;
	}
}
