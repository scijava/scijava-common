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

package org.scijava.types;

import java.util.HashMap;
import java.util.Map;

import org.scijava.plugin.AbstractSingletonService;
import org.scijava.plugin.Plugin;
import org.scijava.service.Service;

/**
 * Default {@link TypeService} implementation.
 *
 * @author Curtis Rueden
 */
@Plugin(type = Service.class)
public class DefaultTypeService extends
	AbstractSingletonService<TypeExtractor<?>> implements TypeService
{

	private Map<Class<?>, TypeExtractor<?>> extractors;

	// -- TypeService methods --

	@Override
	@SuppressWarnings("unchecked")
	public <T> TypeExtractor<T> getExtractor(final Class<T> c) {
		return (TypeExtractor<T>) extractors().get(c);
	}

	// -- Helper methods --

	private Map<Class<?>, TypeExtractor<?>> extractors() {
		if (extractors == null) initExtractors();
		return extractors;
	}

	private synchronized void initExtractors() {
		if (extractors != null) return;

		final HashMap<Class<?>, TypeExtractor<?>> map = new HashMap<>();

		for (final TypeExtractor<?> typeExtractor : getInstances()) {
			final Class<?> key = typeExtractor.getRawType();
			if (!map.containsKey(key)) map.put(key, typeExtractor);
		}

		extractors = map;
	}

}
