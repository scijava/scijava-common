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

import java.lang.reflect.Type;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;
import org.scijava.util.Types;

/**
 * Minimal {@link Converter} implementation to do direct casting.
 *
 * @author Mark Hiner
 */
@Plugin(type = Converter.class, priority = Priority.EXTREMELY_HIGH - 1)
public class CastingConverter extends AbstractConverter<Object, Object> {

	@Override
	public boolean canConvert(final Object src, final Class<?> dest) {
		return Types.isInstance(src, dest);
	}

	@Override
	public boolean canConvert(final Class<?> src, final Type dest) {
		// OK if the existing object can be casted
		return dest != null && Types.isAssignable(src, dest);
	}

	@Override
	public boolean canConvert(final Class<?> src, final Class<?> dest) {
		// NB: Invert functional flow from Converter interface:
		// Converter: canConvert(Class, Type) -> canConvert(Class, Class)
		// becomes: canConvert(Class, Class) -> canConvert(Class, Type)
		final Type destType = dest;
		return canConvert(src, destType);
	}

	@Override
	public <T> T convert(final Object src, final Class<T> dest) {
		return Types.cast(src, dest);
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
