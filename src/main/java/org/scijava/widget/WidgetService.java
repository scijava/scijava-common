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

package org.scijava.widget;

import java.util.List;

import org.scijava.module.Module;
import org.scijava.module.ModuleItem;
import org.scijava.plugin.WrapperService;
import org.scijava.service.SciJavaService;

/**
 * Interface for services that manage available {@link InputWidget}s.
 * 
 * @author Curtis Rueden
 */
public interface WidgetService extends
	WrapperService<WidgetModel, InputWidget<?, ?>>, SciJavaService
{

	// NB: Javadoc overrides.

	// -- WrapperService methods --

	/**
	 * Create a {@link WidgetModel} for the given module input.
	 * 
	 * @param inputPanel
	 * @param module
	 * @param item
	 * @param objectPool
	 */
	default WidgetModel createModel(InputPanel<?, ?> inputPanel, Module module,
		ModuleItem<?> item, List<?> objectPool)
	{
		return new DefaultWidgetModel(getContext(), inputPanel, module, item,
			objectPool);
	}

	// -- PTService methods --

	@Override
	@SuppressWarnings({"rawtypes", "unchecked"})
	default Class<InputWidget<?, ?>> getPluginType() {
		return (Class) InputWidget.class;
	}

	// -- Typed methods --

	@Override
	default Class<WidgetModel> getType() {
		return WidgetModel.class;
	}
}
