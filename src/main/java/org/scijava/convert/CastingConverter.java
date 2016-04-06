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

import org.scijava.Priority;
import org.scijava.plugin.Plugin;
import org.scijava.util.ClassUtils;
import org.scijava.util.ConversionUtils;
import org.scijava.util.GenericUtils;

/**
 * Minimal {@link Converter} implementation to do direct casting.
 *
 * @author Mark Hiner
 */
@Plugin(type = Converter.class, priority = Priority.FIRST_PRIORITY)
public class CastingConverter extends AbstractConverter<Object, Object> {

	@SuppressWarnings("deprecation")
	@Override
	public boolean canConvert(final Object src, final Class<?> dest) {
		return ClassUtils.canCast(src, dest);
	}

	@Override
	public boolean canConvert(final Class<?> src, final Class<?> dest) {
		// OK if the existing object can be casted
		if (ConversionUtils.canCast(src, dest))
			return true;

		return false;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T convert(final Object src, final Class<T> dest) {
		// NB: Regardless of whether the destination type is an array or
		// collection, we still want to cast directly if doing so is possible.
		// But note that in general, this check does not detect cases of
		// incompatible generic parameter types. If this limitation becomes a
		// problem in the future we can extend the logic here to provide
		// additional signatures of canCast which operate on Types in general
		// rather than only Classes. However, the logic could become complex
		// very quickly in various subclassing cases, generic parameters
		// resolved vs. propagated, etc.
		final Class<?> c = GenericUtils.getClass(dest);
		return (T) ConversionUtils.cast(src, c);
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
