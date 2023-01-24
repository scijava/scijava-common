/*-
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

import org.scijava.plugin.Plugin;
import org.scijava.util.Types;

/**
 * Converts a {@link String} to a {@link Number}. Currently handles all boxed
 * and unboxed primitives, along with the Numbers. In particular,
 * {@link String}s converted {@link Number}s are just {@link Double}s, as this
 * features the broadest range of integers.
 *
 * @author Gabriel Selzer
 */
@Plugin(type = Converter.class)
public class StringToNumberConverter extends AbstractConverter<String, Number> {

	@Override
	@SuppressWarnings("unchecked")
	public <T> T convert(Object src, Class<T> dest) {
		// ensure type is well-behaved, rather than a primitive type
		Class<T> saneDest = sane(dest);
		if (!(src instanceof String)) throw new IllegalArgumentException(
			"Expected src to be a String but got a " + src.getClass());
		if (!(Number.class.isAssignableFrom(saneDest)))
			throw new IllegalArgumentException(
				"Expected dest to be Number.class (or a subclass of Number, or a numerical primitive), but got " +
					saneDest);
		String srcString = (String) src;
		if (saneDest == Byte.class) return (T) new Byte(srcString);
		if (saneDest == Short.class) return (T) new Short(srcString);
		if (saneDest == Integer.class) return (T) new Integer(srcString);
		if (saneDest == Long.class) return (T) new Long(srcString);
		if (saneDest == Float.class) return (T) new Float(srcString);
		if (saneDest == Double.class) return (T) new Double(srcString);
		else throw new IllegalArgumentException("Unknown destination type: " +
			saneDest);
	}

	@Override
	public Class<Number> getOutputType() {
		return Number.class;
	}

	@Override
	public Class<String> getInputType() {
		return String.class;
	}

	@Override
	public boolean canConvert(Object src, Class<?> dest) {
		if (!Types.isAssignable(src.getClass(), String.class)) return false;
		// The only way to know if the conversion is valid is to actually do it.
		try {
			String srcString = (String) src;
			sane(dest).getConstructor(String.class).newInstance(srcString);
			return true;
		}
		catch (Exception e) {
			return false;
		}
	}

	// -- Helper functionality -- //

	@SuppressWarnings("unchecked")
	private <T> Class<T> sane(Class<T> c) {
		if (c == Number.class) return (Class<T>) Double.class;
		return Types.box(c);
	}
}
