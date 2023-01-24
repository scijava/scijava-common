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

package org.scijava.module.process;

import java.lang.reflect.InvocationTargetException;

import org.scijava.Context;
import org.scijava.Gateway;
import org.scijava.Priority;
import org.scijava.log.LogService;
import org.scijava.module.Module;
import org.scijava.module.ModuleItem;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 * The gateway preprocessor automatically populates module inputs that implement
 * {@link Gateway}.
 * <p>
 * Gateways are instantiated as needed, wrapping this preprocessor instance's
 * application context.
 * </p>
 * 
 * @author Curtis Rueden
 */
@Plugin(type = PreprocessorPlugin.class, priority = 2 * Priority.VERY_HIGH)
public class GatewayPreprocessor extends AbstractPreprocessorPlugin {

	@Parameter
	private LogService log;

	// -- ModuleProcessor methods --

	@Override
	public void process(final Module module) {
		for (final ModuleItem<?> input : module.getInfo().inputs()) {
			if (!input.isAutoFill()) continue; // skip unfillable inputs
			if (module.isInputResolved(input.getName())) continue; // skip resolved inputs
			final Class<?> type = input.getType();
			if (Gateway.class.isAssignableFrom(type)) {
				// input is a gateway
				@SuppressWarnings("unchecked")
				final ModuleItem<? extends Gateway> gatewayInput =
					(ModuleItem<? extends Gateway>) input;
				setGatewayValue(getContext(), module, gatewayInput);
			}
		}
	}

	// -- Helper methods --

	private <G extends Gateway> void setGatewayValue(final Context context,
		final Module module, final ModuleItem<G> input)
	{
		final Class<G> type = input.getType();
		G gateway = null;
		Exception exception = null;
		try {
			gateway = type.getConstructor(Context.class).newInstance(context);
		}
		catch (final IllegalArgumentException | SecurityException
				| InstantiationException | IllegalAccessException
				| InvocationTargetException | NoSuchMethodException exc)
		{
			exception = exc;
		}
		if (exception != null) {
			log.warn("Could not instantiate gateway of type: " + type, exception);
			return;
		}
		input.setValue(module, gateway);
		module.resolveInput(input.getName());
	}

}
