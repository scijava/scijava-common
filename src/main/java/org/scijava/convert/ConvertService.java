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
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import org.scijava.plugin.HandlerService;
import org.scijava.service.SciJavaService;

/**
 * Service for converting between types using an {@link Converter} plugins.
 * Contains convenience signatures for the {@link #getHandler} and
 * {@link #supports} methods to avoid the need to create
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
	default Object convert(final Object src, final Type dest) {
		return convert(new ConversionRequest(src, dest));
	}

	/**
	 * @see Converter#convert(Object, Class)
	 */
	default <T> T convert(final Object src, final Class<T> dest) {
		// NB: repeated code with convert(ConversionRequest), because the
		// handler's convert method respects the T provided
		final Converter<?, ?> handler = getHandler(src, dest);
		return handler == null ? null : handler.convert(src, dest);
	}

	/**
	 * @see Converter#convert(ConversionRequest)
	 */
	default Object convert(final ConversionRequest request) {
		final Converter<?, ?> handler = getHandler(request);
		return handler == null ? null : handler.convert(request);
	}

	/**
	 * @see HandlerService#supports(Object)
	 */
	default Converter<?, ?> getHandler(final Object src, final Type dest) {
		return getHandler(new ConversionRequest(src, dest));
	}

	/**
	 * @see HandlerService#supports(Object)
	 */
	default Converter<?, ?> getHandler(final Object src, final Class<?> dest) {
		return getHandler(new ConversionRequest(src, dest));
	}

	/**
	 * @see HandlerService#getHandler(Object)
	 */
	default Converter<?, ?> getHandler(final Class<?> src, final Type dest) {
		return getHandler(new ConversionRequest(src, dest));
	}

	/**
	 * @see HandlerService#getHandler(Object)
	 */
	default Converter<?, ?> getHandler(final Class<?> src, final Class<?> dest) {
		return getHandler(new ConversionRequest(src, dest));
	}

	/**
	 * @see HandlerService#supports(Object)
	 */
	default boolean supports(final Object src, final Type dest) {
		return supports(new ConversionRequest(src, dest));
	}

	/**
	 * @see HandlerService#supports(Object)
	 */
	default boolean supports(final Object src, final Class<?> dest) {
		return supports(new ConversionRequest(src, dest));
	}

	/**
	 * @see HandlerService#supports(Object)
	 */
	default boolean supports(final Class<?> src, final Type dest) {
		return supports(new ConversionRequest(src, dest));
	}

	/**
	 * @see HandlerService#supports(Object)
	 */
	default boolean supports(final Class<?> src, final Class<?> dest) {
		return supports(new ConversionRequest(src, dest));
	}

	/**
	 * @return A collection of instances that could be converted to the
	 *         specified class.
	 */
	default Collection<Object> getCompatibleInputs(final Class<?> dest) {
		final Set<Object> objects = new LinkedHashSet<>();

		for (final Converter<?, ?> c : getInstances()) {
			if (dest.isAssignableFrom(c.getOutputType())) {
				c.populateInputCandidates(objects);
			}
		}

		return objects;
	}

	/**
	 * @return A collection of all classes that could potentially be converted
	 *         <b>to</b> the specified class.
	 */
	default Collection<Class<?>> getCompatibleInputClasses(final Class<?> dest) {
		final Set<Class<?>> compatibleClasses = new HashSet<>();

		for (final Converter<?, ?> converter : getInstances()) {
			if (dest == converter.getOutputType()) //
				compatibleClasses.add(converter.getInputType());
		}

		return compatibleClasses;
	}

	/**
	 * @return A collection of all classes that could potentially be converted
	 *         <b>from</b> the specified class.
	 */
	default Collection<Class<?>> getCompatibleOutputClasses(final Class<?> source) {
		final Set<Class<?>> compatibleClasses = new HashSet<>();

		for (final Converter<?, ?> converter : getInstances()) {
			try {
				if (source == converter.getInputType()) //
					compatibleClasses.add(converter.getOutputType());
			}
			catch (final Throwable t) {
				log().error("Malfunctioning converter plugin: " + //
					converter.getClass().getName(), t);
			}
		}

		return compatibleClasses;
	}

	// -- PTService methods --

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	default Class<Converter<?, ?>> getPluginType() {
		return (Class) Converter.class;
	}

	// -- Typed methods --

	@Override
	default Class<ConversionRequest> getType() {
		return ConversionRequest.class;
	}
}
