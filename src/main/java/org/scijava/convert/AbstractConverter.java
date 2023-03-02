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

import java.util.Collection;

import org.scijava.object.ObjectService;
import org.scijava.plugin.AbstractHandlerPlugin;
import org.scijava.plugin.Parameter;

/**
 * Abstract superclass for {@link Converter} plugins. Performs appropriate
 * dispatching of {@link #canConvert(ConversionRequest)} and
 * {@link #convert(ConversionRequest)} calls based on the actual state of the
 * given {@link ConversionRequest}.
 *
 * @author Mark Hiner
 */
public abstract class AbstractConverter<I, O> extends
	AbstractHandlerPlugin<ConversionRequest> implements Converter<I, O>
{

	// -- Parameters --

	@Parameter(required = false)
	private ObjectService objectService;

	// -- Converter methods --

	@Override
	public void populateInputCandidates(final Collection<Object> objects) {
		if (objectService == null) return;
		for (final Object candidate : objectService.getObjects(getInputType())) {
			if (canConvert(candidate, getOutputType())) objects.add(candidate);
		}
	}

	// -- Typed methods --

	@Override
	public boolean supports(final ConversionRequest request) {
		// NB: Overridden just for backwards compatibility, so that
		// downstream classes which call super.supports do the right thing.
		return Converter.super.supports(request);
	}
}
