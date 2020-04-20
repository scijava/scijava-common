/*-
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
package org.scijava.common.ui;

import java.io.File;

import org.scijava.common.module.Module;
import org.scijava.common.module.ModuleItem;
import org.scijava.common.module.process.AbstractPreprocessorPlugin;
import org.scijava.common.module.process.PreprocessorPlugin;
import org.scijava.common.plugin.Parameter;
import org.scijava.common.plugin.Plugin;
import org.scijava.common.widget.InputHarvester;

@Plugin(type = PreprocessorPlugin.class, priority = InputHarvester.PRIORITY + 1.0)
public class FileListPreprocessor extends AbstractPreprocessorPlugin {

	@Parameter(required = false)
	private UIService uiService;

	@Override
	public void process(final Module module) {
		if (uiService == null) return;
		final ModuleItem<File[]> fileInput = getFilesInput(module);
		if (fileInput == null) return;

		final File[] files = fileInput.getValue(module);

		// show file chooser dialog box
		// TODO decide how to create filter from style attributes
		// TODO retrieve parent folder??
		final File[] result = uiService.chooseFiles(null, files, null, fileInput.getWidgetStyle());
		if (result == null) {
			cancel("");
			return;
		}

		fileInput.setValue(module, result);
		module.resolveInput(fileInput.getName());
	}

	// -- Helper methods --

	/**
	 * Gets the single unresolved {@link File} input parameter. If there is not
	 * exactly one unresolved {@link File} input parameter, or if there are other
	 * types of unresolved parameters, this method returns null.
	 */
	private ModuleItem<File[]> getFilesInput(final Module module) {
		ModuleItem<File[]> result = null;
		for (final ModuleItem<?> input : module.getInfo().inputs()) {
			if (module.isInputResolved(input.getName())) continue;
			final Class<?> type = input.getType();
			if (!File[].class.isAssignableFrom(type)) {
				// not a File[] parameter; abort
				return null;
			}
			if (result != null) {
				// second File parameter; abort
				return null;
			}
			@SuppressWarnings("unchecked")
			final ModuleItem<File[]> fileInput = (ModuleItem<File[]>) input;
			result = fileInput;
		}
		return result;
	}
}
