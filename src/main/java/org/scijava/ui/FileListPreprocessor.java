package org.scijava.ui;

import java.io.File;

import org.scijava.module.Module;
import org.scijava.module.ModuleItem;
import org.scijava.module.process.AbstractPreprocessorPlugin;
import org.scijava.module.process.PreprocessorPlugin;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.widget.InputHarvester;

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
		final File[] result = uiService.chooseFiles(files, null);
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
