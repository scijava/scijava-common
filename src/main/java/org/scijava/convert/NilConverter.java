/*
 * #%L
 * SciJava Common shared library for SciJava software.
 * %%
 * Copyright (C) 2009 - 2016 Board of Regents of the University of
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

package org.scijava.convert;

import java.lang.reflect.Type;

import org.scijava.Priority;
import org.scijava.convert.AbstractConverter;
import org.scijava.convert.Converter;
import org.scijava.plugin.Plugin;
import org.scijava.types.Nil;
import org.scijava.util.Types;

/**
 * {@link Converter} implementation for handling {@link Nil} values.
 *
 * @author Curtis Rueden
 */
@Plugin(type = Converter.class, priority = Priority.VERY_HIGH_PRIORITY)
public class NilConverter extends AbstractConverter<Nil<?>, Object> {

	@Override
	public boolean canConvert(final Object src, final Type dest) {
		return src instanceof Nil && dest != null;
	}

	@Override
	public boolean canConvert(final Object src, final Class<?> dest) {
		return src instanceof Nil && dest != null;
	}

	@Override
	public boolean canConvert(final Class<?> src, final Class<?> dest) {
		return Nil.class.isAssignableFrom(src) && dest != null;
	}

	@Override
	public Object convert(final Object src, final Type dest) {
		if (!(src instanceof Nil)) return null;
		final Nil<?> nil = (Nil<?>) src;

		// special case for conversion to Nil of another type
		final Class<?> destClass = Types.raw(dest);
		if (destClass == Nil.class) {
			// convert to target Nil type, preserving the source's callbacks
			return Nil.of(((Nil<?>) dest).getType(), src);
		}

		// Return a proxy object of the destination type,
		// with callbacks from the source Nil.
		// NB: The proxy might not _actually_ be of the requested type,
		// although it will implement all the same interfaces as that type.
		// CTR TODO: Consider whether to fail the conversion in this case.
		return Nil.of(dest, nil).proxy();
	}

	@Override
	public Class<Object> getOutputType() {
		return Object.class;
	}

	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Class<Nil<?>> getInputType() {
		return (Class) Nil.class;
	}

	@Override
	public <T> T convert(final Object src, final Class<T> dest) {
		final Type type = dest;
		@SuppressWarnings("unchecked")
		final T result = (T) convert(src, type);
		return result;
	}

}
