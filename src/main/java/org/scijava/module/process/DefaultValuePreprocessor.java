/*
 * #%L
 * SciJava Common shared library for SciJava software.
 * %%
 * Copyright (C) 2009 - 2024 SciJava developers.
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

import java.util.Objects;

import org.scijava.Priority;
import org.scijava.module.Module;
import org.scijava.module.ModuleItem;
import org.scijava.module.ModuleService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.util.Types;

/**
 * A preprocessor plugin that populates default parameter values.
 * <p>
 * Default values are determined using {@link ModuleService#getDefaultValue}.
 * </p>
 *
 * @author Curtis Rueden
 */
@Plugin(type = PreprocessorPlugin.class, priority = Priority.VERY_HIGH)
public class DefaultValuePreprocessor extends AbstractPreprocessorPlugin {

	@Parameter
	private ModuleService moduleService;

	// -- ModuleProcessor methods --

	@Override
	public void process(final Module module) {
		for (final ModuleItem<?> input : module.getInfo().inputs()) {
			assignDefaultValue(module, input);
		}
		for (final ModuleItem<?> output : module.getInfo().outputs()) {
			assignDefaultValue(module, output);
		}
	}

	// -- Helper methods --

	private <T> void assignDefaultValue(final Module module,
		final ModuleItem<T> item)
	{
		if (module.isInputResolved(item.getName())) return;
		final T nullValue = Types.nullValue(item.getType());
		if (!Objects.equals(item.getValue(module), nullValue)) return;
		final T defaultValue = moduleService.getDefaultValue(item);
		if (defaultValue == null) return;
		item.setValue(module, defaultValue);
	}

}
