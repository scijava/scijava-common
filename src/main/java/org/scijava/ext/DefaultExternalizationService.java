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
package org.scijava.ext;

import java.io.IOException;

import org.scijava.plugin.AbstractSingletonService;
import org.scijava.plugin.Plugin;

@SuppressWarnings("rawtypes")
@Plugin(type = ExternalizationService.class)
public class DefaultExternalizationService extends AbstractSingletonService<Externalizer>
		implements ExternalizationService {

	@Override
	public Class<Externalizer> getPluginType() {
		return Externalizer.class;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <I extends ExternalizerInput, O extends ExternalizerOutput, T> Externalizer<I, O, T> get(
			final Class<I> inputType, final Class<O> outputType, final T type) {

		for (final Externalizer ext : getInstances()) {
			if (ext.canExternalize(inputType, outputType, type)) {
				return ext;
			}
		}

		throw new IllegalStateException("No externalizer found for input type: " + inputType.getSimpleName()
				+ " Output type: " + outputType.getSimpleName());
	}

	@SuppressWarnings("unchecked")
	@Override
	public <I extends ExternalizerInput, T> T read(final I in, final Class<T> type) throws IOException {
		return (T) in.read().read(in);
	}

	@Override
	public <O extends ExternalizerOutput, T> void write(final Externalizer<?, O, T> ext, final O out, final T obj)
			throws IOException {
		out.write(ext);
		ext.write(out, obj);
	}

}
