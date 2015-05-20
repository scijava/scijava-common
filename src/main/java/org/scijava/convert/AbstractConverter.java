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
import java.util.Collection;

import org.scijava.object.ObjectService;
import org.scijava.plugin.AbstractHandlerPlugin;
import org.scijava.plugin.Parameter;
import org.scijava.util.ConversionUtils;
import org.scijava.util.GenericUtils;

/**
 * Abstract superclass for {@link Converter} plugins. Performs appropriate
 * dispatching of {@link #canConvert(ConversionRequest)} and
 * {@link #convert(ConversionRequest)} calls based on the actual state of the
 * given {@link ConversionRequest}.
 * <p>
 * Note that the {@link #supports(ConversionRequest)} method is overridden as
 * well, to delegate to the appropriate {@link #canConvert}.
 * </p>
 * <p>
 * NB: by default, the {@link #populateInputCandidates(Collection)} method has a
 * dummy implementation. Effectively, this is opt-in behavior. If a converter
 * implementation would like to suggest candidates for conversion, this method
 * can be overridden.
 * </p>
 *
 * @author Mark Hiner
 */
public abstract class AbstractConverter<I, O> extends
	AbstractHandlerPlugin<ConversionRequest> implements Converter<I, O>
{

	// -- Parameters --

	@Parameter
	private ObjectService objectService;

	// -- ConversionHandler methods --

	@Override
	public boolean canConvert(final ConversionRequest request) {
		Object src = request.sourceObject();
		if (src == null) {
			Class<?> srcClass = request.sourceClass();
			if (request.destType() != null) return canConvert(srcClass, request.destType());
			return canConvert(srcClass, request.destClass());
		}

		if (request.destType() != null) return canConvert(src, request.destType());
		return canConvert(src, request.destClass());
	}

	@Override
	public boolean canConvert(final Object src, final Type dest) {
		if (src == null) return false;
		final Class<?> srcClass = src.getClass();
		return canConvert(srcClass, dest);
	}

	@Override
	public boolean canConvert(final Object src, final Class<?> dest) {
		if (src == null) return false;
		final Class<?> srcClass = src.getClass();

		return canConvert(srcClass, dest);
	}

	@Override
	public boolean canConvert(final Class<?> src, final Class<?> dest) {
		return ConversionUtils.canCast(src, getInputType()) &&
			ConversionUtils.canCast(getOutputType(), dest);
	}

	@Override
	public Object convert(final Object src, final Type dest) {
		final Class<?> destClass = GenericUtils.getClass(dest);
		return convert(src, destClass);
	}

	@Override
	public Object convert(final ConversionRequest request) {
		if (request.destType() != null) {
			return convert(request.sourceObject(), request.destType());
		}
		
		return convert(request.sourceObject(), request.destClass());
	}

	@Override
	public void populateInputCandidates(final Collection<Object> objects) {
		for (final Object candidate : objectService.getObjects(getInputType())) {
			if (canConvert(candidate, getOutputType())) objects.add(candidate);
		}
	}

	// -- Typed methods --

	@Override
	public boolean supports(final ConversionRequest request) {
		return canConvert(request);
	}

	@Override
	public Class<ConversionRequest> getType() {
		return ConversionRequest.class;
	}

	// -- Deprecated API --

	@Override
	@Deprecated
	public boolean canConvert(final Class<?> src, final Type dest) {
		final Class<?> destClass = GenericUtils.getClass(dest);
		return canConvert(src, destClass);
	}
}
