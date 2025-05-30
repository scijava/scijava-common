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

import org.scijava.util.Types;

/**
 * Converts numbers to numbers, and throws IllegalArgumentException for null or
 * invalid input.
 *
 * @author Alison Walter
 */
public abstract class NumberToNumberConverter<I extends Number, O extends Number>
	extends AbstractConverter<I, O>
{

	@Override
	public <T> T convert(final Object src, final Class<T> dest) {
		if (src == null || dest == null) //
			throw new IllegalArgumentException("Null input");
		if (!getInputType().isInstance(src)) {
			throw new IllegalArgumentException("Expected input of type " +
				getInputType().getSimpleName() + ", but got " + //
				src.getClass().getSimpleName());
		}
		if (Types.box(dest) != getOutputType()) {
			throw new IllegalArgumentException(
				"Expected output class of " + getOutputType().getSimpleName() +
				", but got " + dest.getSimpleName());
		}
		@SuppressWarnings("unchecked")
		final T result = (T) convert((Number) src);
		return result;
	}

	public abstract O convert(Number n);
}
