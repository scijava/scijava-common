/*
 * #%L
 * SciJava Common shared library for SciJava software.
 * %%
 * Copyright (C) 2009 - 2014 Board of Regents of the University of
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

package org.scijava.util.conversion;

import java.lang.reflect.Type;

import org.scijava.plugin.HandlerService;

/**
 * Service for converting between types using an extensible plugin:
 * {@link ConversionHandler}. Contains convenience signatures for the
 * {@link #getHandler} and {@link #supports} methods to avoid the need to create
 * {@link ConversionRequest} objects.
 *
 * @see ConversionRequest
 * @author Mark Hiner
 */
public interface ConversionService extends
	HandlerService<ConversionRequest, ConversionHandler>
{
	/**
	 * @see ConversionHandler#convert(Object, Type)
	 */
	Object convert(Object src, Type dest);

	/**
	 * @see ConversionHandler#convert(Object, Class)
	 */
	<T> T convert(Object src, Class<T> dest);

	/**
	 * @see ConversionHandler#convert(ConversionRequest)
	 */
	Object convert(ConversionRequest request);

	/**
	 * @see #getHandler(ConversionRequest)
	 */
	ConversionHandler getHandler(Object src, Class<?> dest);

	/**
	 * @see #getHandler(ConversionRequest)
	 */
	ConversionHandler getHandler(Class<?> src, Class<?> dest);

	/**
	 * @see #getHandler(ConversionRequest)
	 */
	ConversionHandler getHandler(Object src, Type dest);

	/**
	 * @see #getHandler(ConversionRequest)
	 */
	ConversionHandler getHandler(Class<?> src, Type dest);

	/**
	 * @see #supports(ConversionRequest)
	 */
	boolean supports(Object src, Class<?> dest);

	/**
	 * @see #supports(ConversionRequest)
	 */
	boolean supports(Class<?> src, Class<?> dest);

	/**
	 * @see #supports(ConversionRequest)
	 */
	boolean supports(Object src, Type dest);

	/**
	 * @see #supports(ConversionRequest)
	 */
	boolean supports(Class<?> src, Type dest);
}