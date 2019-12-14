/*
 * #%L
 * SciJava Common shared library for SciJava software.
 * %%
 * Copyright (C) 2009 - 2017 Board of Regents of the University of
 * Wisconsin-Madison, Broad Institute of MIT and Harvard, Max Planck
 * Institute of Molecular Cell Biology and Genetics, University of
 * Konstanz, and KNIME GmbH.
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

package org.scijava.command;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.scijava.Context;
import org.scijava.module.process.PreprocessorPlugin;

/**
 * A way to build a dynamic set of inputs, whose values are then harvested by
 * the preprocessing framework.
 * <p>
 * The {@link #run()} method of this command does nothing. If you want something
 * custom to happen during execution, use a normal {@link Command} instead:
 * either implement {@link Command directly}, or extend {@link ContextCommand}
 * or {@link DynamicCommand}.
 * </p>
 * <p>
 * Here is are some examples of usage:
 * </p>
 *
 * <pre>
 * {@code
 * // Single input, no configuration.
 * Inputs inputs = new Inputs(context);
 * inputs.addInput("sigma", Double.class);
 * Double sigma = (Double) inputs.harvest().get("sigma");
 *
 * // Two inputs, no configuration.
 * Inputs inputs = new Inputs(context);
 * inputs.addInput("name", String.class);
 * inputs.addInput("age", Integer.class);
 * Map<String, Object> values = inputs.harvest();
 * String name = (String) values.get("name");
 * Integer age = (Integer) values.get("age");
 *
 * // Inputs with configuration.
 * Inputs inputs = new Inputs(context);
 * MutableModuleItem<String> wordInput = inputs.addInput("word", String.class);
 * wordInput.setLabel("Favorite word");
 * wordInput.setChoices(Arrays.asList("quick", "brown", "fox"));
 * wordInput.setDefaultValue("fox");
 * MutableModuleItem<Double> opacityInput = inputs.addInput("opacity", Double.class);
 * opacityInput.setMinimumValue(0.0);
 * opacityInput.setMaximumValue(1.0);
 * opacityInput.setDefaultValue(0.5);
 * opacityInput.setWidgetStyle(NumberWidget.SCROLL_BAR_STYLE);
 * inputs.harvest();
 * String word = wordInput.getValue(inputs);
 * Double opacity = opacityInput.getValue(inputs);
 * }
 * </pre>
 *
 * @author Curtis Rueden
 */
public final class Inputs extends DynamicCommand {

	public Inputs(final Context context) {
		context.inject(this);
	}

	public Map<String, Object> harvest() {
		try {
			final List<PreprocessorPlugin> pre = //
				pluginService.createInstancesOfType(PreprocessorPlugin.class);
			return moduleService.run(this, pre, null).get().getInputs();
		}
		catch (final InterruptedException | ExecutionException exc) {
			throw new RuntimeException(exc);
		}
	}
}
