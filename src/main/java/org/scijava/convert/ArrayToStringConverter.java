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

package org.scijava.convert;

import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.scijava.Priority;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.util.ArrayUtils;
import org.scijava.util.Types;

/**
 * A {@link Converter} that specializes in converting
 * n-dimensional arrays into {@link String}s. This {@link Converter} can convert any array whose
 * component types can be converted into {@link String}s. By default, this
 * {@link Converter} delimits the array elements with commas.
 * 
 * @author Gabriel Selzer
 */
@Plugin(type = Converter.class, priority = Priority.VERY_LOW)
public class ArrayToStringConverter extends AbstractConverter<Object, String> {

	@Parameter private ConvertService convertService;

	@Override public boolean canConvert(final Class<?> src, final Class<?> dest) {
		if (src == null) return false;
		final Class<?> saneSrc = Types.box(src);
		final Class<?> saneDest = Types.box(dest);
		return saneSrc.isArray() && saneDest == String.class;
	}

	@Override public boolean canConvert(final Object src, final Class<?> dest) {
		if (!canConvert(src.getClass(), dest)) return false;
		if (Array.getLength(src) == 0) return true;
		return convertService.supports(Array.get(src, 0), dest);
	}

	@Override public Object convert(Object src, Type dest) {
		final String elementString = ArrayUtils.toCollection(src).stream() //
				.map(object -> convertService.convert(object, String.class)) //
				.collect(Collectors.joining(", "));
		return "{" + elementString + "}";
	}

	@SuppressWarnings("unchecked") @Override
	public <T> T convert(Object src, Class<T> dest) {
		return (T) convert(src, (Type) dest);
	}

	@Override public Class<String> getOutputType() {
		return String.class;
	}

	@Override public Class<Object> getInputType() {
		return Object.class;
	}

}
