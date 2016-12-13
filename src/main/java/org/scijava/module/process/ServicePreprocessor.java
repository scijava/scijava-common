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

package org.scijava.module.process;

import org.scijava.Context;
import org.scijava.Priority;
import org.scijava.command.Command;
import org.scijava.module.Module;
import org.scijava.module.ModuleInfo;
import org.scijava.module.ModuleItem;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.service.Service;

/**
 * The service preprocessor automatically populates module inputs that implement
 * {@link Service}.
 * <p>
 * Services are obtained from this preprocessor instance's application context.
 * </p>
 * <p>
 * Many modules (e.g., most {@link Command}s) use @{@link Parameter}-annotated
 * service fields, resulting in those parameters being populated when the
 * SciJava application context is injected (via {@link Context#inject(Object)}.
 * However, some modules may have service parameters which are programmatically
 * generated (i.e., returned directly as inputs from {@link ModuleInfo#inputs()}
 * and as such not populated by context injection. E.g., this situation is the
 * case for scripts, since module inputs are parsed from the script header
 * rather than declared via the @{@link Parameter} annotation. In such cases, we
 * need this service preprocessor to fill in the service values.
 * </p>
 * 
 * @author Curtis Rueden
 */
@Plugin(type = PreprocessorPlugin.class, //
	priority = 2 * Priority.VERY_HIGH_PRIORITY)
public class ServicePreprocessor extends AbstractPreprocessorPlugin {

	// -- ModuleProcessor methods --

	@Override
	public void process(final Module module) {
		for (final ModuleItem<?> input : module.getInfo().inputs()) {
			if (!input.isAutoFill()) continue; // cannot auto-fill this input
			final Class<?> type = input.getType();
			if (Service.class.isAssignableFrom(type)) {
				// input is a service
				@SuppressWarnings("unchecked")
				final ModuleItem<? extends Service> serviceInput =
					(ModuleItem<? extends Service>) input;
				setServiceValue(getContext(), module, serviceInput);
			}
			if (type.isAssignableFrom(getContext().getClass())) {
				// input is a compatible context
				final String name = input.getName();
				module.setInput(name, getContext());
				module.resolveInput(name);
			}
		}
	}

	// -- Helper methods --

	private <S extends Service> void setServiceValue(final Context context,
		final Module module, final ModuleItem<S> input)
	{
		final S service = context.getService(input.getType());
		input.setValue(module, service);
		module.resolveInput(input.getName());
	}

}
