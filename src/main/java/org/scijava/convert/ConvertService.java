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
import java.util.Collection;

import org.scijava.plugin.HandlerService;
import org.scijava.service.SciJavaService;

/**
 * Service for converting between types using an extensible plugin:
 * {@link Converter}. Contains convenience signatures for the
 * {@link #getHandler} and {@link #supports} methods to avoid the need to create
 * {@link ConversionRequest} objects.
 *
 * @see ConversionRequest
 * @author Mark Hiner
 */
public interface ConvertService extends
	HandlerService<ConversionRequest, Converter<?, ?>>, SciJavaService
{

	/**
	 * @see Converter#convert(Object, Type)
	 */
	Object convert(Object src, Type dest);

	/**
	 * @see Converter#convert(Object, Class)
	 */
	<T> T convert(Object src, Class<T> dest);

	/**
	 * @see Converter#convert(ConversionRequest)
	 */
	Object convert(ConversionRequest request);

	/**
	 * @see #getHandler(ConversionRequest)
	 */
	Converter<?, ?> getHandler(Object src, Class<?> dest);

	/**
	 * @see #getHandler(ConversionRequest)
	 */
	Converter<?, ?> getHandler(Object src, Type dest);

	/**
	 * @see #supports(ConversionRequest)
	 */
	boolean supports(Object src, Class<?> dest);

	/**
	 * @see #supports(ConversionRequest)
	 */
	boolean supports(Object src, Type dest);

	/**
	 * @return A collection of instances that could be converted to the
	 *         specified class.
	 */
	Collection<Object> getCompatibleInputs(Class<?> dest);

	/**
	 * @return A collection of all classes that could potentially be converted
	 *         <b>to</b> the specified class.
	 */
	Collection<Class<?>> getCompatibleInputClasses(Class<?> dest);

	/**
	 * @return A collection of all classes that could potentially be converted
	 *         <b>from</b> the specified class.
	 */
	Collection<Class<?>> getCompatibleOutputClasses(Class<?> dest);

	// -- Deprecated API --

	/**
	 * @see #getHandler(ConversionRequest)
	 * @deprecated Use {@link #getHandler(Object, Class)}
	 */
	@Deprecated
	Converter<?, ?> getHandler(Class<?> src, Class<?> dest);

	/**
	 * @see #getHandler(ConversionRequest)
	 * @deprecated Use {@link #getHandler(Object, Type)}
	 */
	@Deprecated
	Converter<?, ?> getHandler(Class<?> src, Type dest);

	/**
	 * @see #supports(ConversionRequest)
	 * @deprecated Use {@link #supports(Object, Class)}
	 */
	@Deprecated
	boolean supports(Class<?> src, Class<?> dest);

	/**
	 * @see #supports(ConversionRequest)
	 * @deprecated Use {@link #supports(Object, Type)}
	 */
	@Deprecated
	boolean supports(Class<?> src, Type dest);
}
