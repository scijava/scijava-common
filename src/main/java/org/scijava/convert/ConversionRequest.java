/*
 * #%L
 * SciJava Common shared library for SciJava software.
 * %%
 * Copyright (C) 2009 - 2015 Board of Regents of the University of
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

import org.scijava.util.GenericUtils;

/**
 * Currency for use in {@link Converter} and {@link ConvertService}
 * methods.
 * <p>
 * {@link #ConversionRequest} provides a variety of constructors. Note that only
 * one destination type needs to be set (e.g. either a {@link Type} or a
 * {@link Class}).
 * </p>
 * <p>
 * Only the {@link Class} source needs to be set for {@link Converter}
 * lookup, such as through
 * {@link ConvertService#getHandler(ConversionRequest)}. However, to perform
 * an actual conversion, e.g. using
 * {@link Converter#convert(ConversionRequest)}, you must provide an
 * {@link Object} source.
 * </p>
 * <p>
 * NB: once a {@link Converter} has been acquired, the
 * {@code ConversionRequest} used for lookup can be reused to cast to the same
 * destination type, simply by updating the source object using the
 * {@link #setSourceObject(Object)} method.
 * </p>
 *
 * @author Mark Hiner
 * @author Curtis Rueden
 */
public class ConversionRequest {

	// -- Fields --

	private final Type srcType;
	private final Type destType;

	private Object src;

	// -- Constructors --

	public ConversionRequest(final Object src, final Type destType) {
		this(src, src == null ? null : src.getClass(), destType);
	}

	public ConversionRequest(final Type srcType, final Type destType) {
		this(null, srcType, destType);
	}

	public ConversionRequest(final Object src, final Type srcType,
		final Type destType)
	{
		this.src = src;
		this.srcType = srcType;
		this.destType = destType;
	}

	// -- Accessors --

	/**
	 * @return Source type for conversion or lookup.
	 */
	public Type sourceType() {
		return srcType;
	}

	/**
	 * @return Source class for conversion or lookup.
	 */
	public Class<?> sourceClass() {
		return GenericUtils.getClass(srcType);
	}

	/**
	 * @return Source object for conversion.
	 */
	public Object sourceObject() {
		return src;
	}

	/**
	 * @return Destination type for conversion.
	 */
	public Type destType() {
		return destType;
	}

	/**
	 * @return Destination class for conversion.
	 */
	public Class<?> destClass() {
		return GenericUtils.getClass(destType);
	}

	// -- Setters --

	/**
	 * Sets the source object for this {@link ConversionRequest}.
	 *
	 * @throws IllegalArgumentException If the class of the provided object does
	 *           not match {@link #sourceClass()}.
	 */
	public void setSourceObject(final Object o) {
		// TODO: More careful check against srcType itself.
		if (!sourceClass().isInstance(o)) {
			throw new IllegalArgumentException("Object of type: " + o.getClass() +
				" provided. Expected: " + srcType);
		}

		src = o;
	}

}
