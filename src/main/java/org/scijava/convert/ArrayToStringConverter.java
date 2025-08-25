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
import java.lang.reflect.Type;
import java.util.stream.Collectors;

import org.scijava.Priority;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.util.ArrayUtils;

/**
 * A {@link Converter} that specializes in converting n-dimensional arrays into
 * {@link String}s. This {@link Converter} can convert any array whose component
 * types can be converted into {@link String}s. By default, this
 * {@link Converter} delimits the array elements with commas.
 *
 * @author Gabriel Selzer
 */
@Plugin(type = Converter.class, priority = Priority.VERY_LOW)
public class ArrayToStringConverter extends AbstractConverter<Object, String> {

	@Parameter(required = false)
	private ConvertService convertService;

	@Override
	public boolean canConvert(final Class<?> src, final Class<?> dest) {
		return src != null && src.isArray() && dest == String.class;
	}

	@Override
	public boolean canConvert(final Object src, final Class<?> dest) {
		if (convertService == null || src == null) return false;
		if (!canConvert(src.getClass(), dest)) return false;
		if (Array.getLength(src) == 0) return true;
		return convertService.supports(Array.get(src, 0), dest);
	}

	@Override
	public Object convert(Object src, final Type dest) {
		// Preprocess the "string-likes"
		final Class<?> srcClass = src.getClass();
		if (srcClass == String[].class || //
			srcClass == Character[].class || //
			srcClass == char[].class) //
		{
			src = preprocessCharacters(src);
		}
		// Convert each element to Strings
		final String elementString = ArrayUtils.toCollection(src).stream() //
			.map(object -> convertService.convert(object, String.class)) //
			.collect(Collectors.joining(", "));
		return "{" + elementString + "}";
	}

	private String[] preprocessStrings(final Object src) {
		final int numElements = Array.getLength(src);
		final String[] processed = new String[numElements];
		for (int i = 0; i < numElements; i++) {
			processed[i] = preprocessString(Array.get(src, i));
		}
		return processed;
	}

	private String preprocessString(final Object o) {
		if (o == null) return null;
		String s = o.toString();
		s = s.replace("\\", "\\\\");
		s = s.replace("\"", "\\\"");
		return "\"" + s + "\"";
	}

	private String[] preprocessCharacters(Object src) {
		final String[] processed = new String[Array.getLength(src)];
		for (int i = 0; i < processed.length; i++) {
			final Object value = Array.get(src, i);
			processed[i] = value == null ? null : value.toString();
		}
		return preprocessStrings(processed);
	}

	@Override
	public <T> T convert(final Object src, final Class<T> dest) {
		final Type destType = dest;
		@SuppressWarnings("unchecked")
		final T converted = (T) convert(src, destType);
		return converted;
	}

	@Override
	public Class<String> getOutputType() {
		return String.class;
	}

	@Override
	public Class<Object> getInputType() {
		return Object.class;
	}
}
