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

package org.scijava.convert;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import org.scijava.plugin.Plugin;
import org.scijava.service.Service;

/**
 * Default {@link ConvertService} implementation.
 *
 * @author Mark Hiner
 */
@Plugin(type = Service.class)
public class DefaultConvertService extends AbstractConvertService
{

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Class<Converter<?, ?>> getPluginType() {
		return (Class)Converter.class;
	}

	@Override
	public Class<ConversionRequest> getType() {
		return ConversionRequest.class;
	}

	// -- ConversionService methods --

	@Override
	public Converter<?, ?> getHandler(final Object src, final Class<?> dest) {
		return getHandler(new ConversionRequest(src, dest));
	}

	@Override
	public Converter<?, ?> getHandler(final Class<?> src, final Class<?> dest) {
		return getHandler(new ConversionRequest(src, dest));
	}

	@Override
	public Converter<?, ?> getHandler(final Object src, final Type dest) {
		return getHandler(new ConversionRequest(src, dest));
	}

	@Override
	public Converter<?, ?> getHandler(final Class<?> src, final Type dest) {
		return getHandler(new ConversionRequest(src, dest));
	}

	@Override
	public boolean supports(final Object src, final Class<?> dest) {
		return supports(new ConversionRequest(src, dest));
	}

	@Override
	public boolean supports(final Class<?> src, final Class<?> dest) {
		return supports(new ConversionRequest(src, dest));
	}

	@Override
	public boolean supports(final Object src, final Type dest) {
		return supports(new ConversionRequest(src, dest));
	}

	@Override
	public boolean supports(final Class<?> src, final Type dest) {
		return supports(new ConversionRequest(src, dest));
	}

	@Override
	public Collection<Object> getCompatibleInputs(Class<?> dest) {
		Set<Object> objects = new LinkedHashSet<Object>();

		for (final Converter<?, ?> c : getInstances()) {
			if (dest.isAssignableFrom(c.getOutputType())) {
				c.populateInputCandidates(objects);
			}
		}

		return new ArrayList<Object>(objects);
	}
}
