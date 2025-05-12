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

import java.lang.reflect.Type;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;
import org.scijava.util.Types;

/**
 * {@link Converter} implementation for handling {@code null} values. Returns
 * {@code null} when given a {@code null} source or {@code null} destination.
 * <p>
 * By running at {@link Priority#EXTREMELY_HIGH}, other converters should not
 * need to worry about {@code null} source or destination parameters.
 * </p>
 *
 * @author Mark Hiner
 */
@Plugin(type = Converter.class, priority = Priority.EXTREMELY_HIGH)
public class NullConverter extends AbstractConverter<Object, Object> {

	@Override
	public boolean canConvert(final Object src, final Type dest) {
		return src == null || dest == null;
	}

	@Override
	public boolean canConvert(final Object src, final Class<?> dest) {
		return src == null || dest == null;
	}

	@Override
	public boolean canConvert(final Class<?> src, final Type dest) {
		return src == null || dest == null;
	}

	@Override
	public boolean canConvert(final Class<?> src, final Class<?> dest) {
		return src == null || dest == null;
	}

	@Override
	public <T> T convert(final Object src, final Class<T> dest) {
		if (dest == null) return null;
		if (src == null) return Types.nullValue(dest);
		throw new IllegalArgumentException("Attempting non-null conversion: " +
			src + " -> " + dest + " using NullConverter.");
	}

	@Override
	public Class<Object> getOutputType() {
		return Object.class;
	}

	@Override
	public Class<Object> getInputType() {
		return Object.class;
	}
}
