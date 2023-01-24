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

package org.scijava.ui;

import java.io.File;

import org.scijava.module.Module;
import org.scijava.module.ModuleItem;
import org.scijava.module.process.AbstractPreprocessorPlugin;
import org.scijava.module.process.PreprocessorPlugin;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.widget.InputHarvester;

/**
 * A preprocessor plugin that handles single unresolved parameters of type
 * {@link File} using a UI prompt.
 * 
 * @author Curtis Rueden
 * @see UserInterface#chooseFile(File, String)
 */
@Plugin(type = PreprocessorPlugin.class,
	priority = InputHarvester.PRIORITY + 0.5)
public class FilePreprocessor extends AbstractPreprocessorPlugin {

	@Parameter(required = false)
	private UIService uiService;

	// -- ModuleProcessor methods --

	@Override
	public void process(final Module module) {
		if (uiService == null) return;
		final ModuleItem<File> fileInput = getFileInput(module);
		if (fileInput == null) return;

		final File file = fileInput.getValue(module);
		final String style = fileInput.getWidgetStyle();

		// show file chooser dialog box
		final File result = uiService.chooseFile(file, style);
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
	private ModuleItem<File> getFileInput(final Module module) {
		ModuleItem<File> result = null;
		for (final ModuleItem<?> input : module.getInfo().inputs()) {
			if (module.isInputResolved(input.getName())) continue;
			final Class<?> type = input.getType();
			if (!File.class.isAssignableFrom(type)) {
				// not a File parameter; abort
				return null;
			}
			if (result != null) {
				// second File parameter; abort
				return null;
			}
			@SuppressWarnings("unchecked")
			final ModuleItem<File> fileInput = (ModuleItem<File>) input;
			result = fileInput;
		}
		return result;
	}

}
