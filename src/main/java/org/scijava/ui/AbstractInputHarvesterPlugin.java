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

package org.scijava.ui;

import org.scijava.module.Module;
import org.scijava.module.ModuleException;
import org.scijava.module.process.PreprocessorPlugin;
import org.scijava.plugin.Parameter;
import org.scijava.widget.AbstractInputHarvester;
import org.scijava.widget.InputHarvester;

/**
 * AbstractInputHarvesterPlugin is an {@link InputHarvester} that implements the
 * {@link PreprocessorPlugin} interface. It is intended to be extended by
 * UI-specific implementations such as {@code SwingInputHarvester}.
 * <p>
 * The input harvester will first check whether the default UI matches that of
 * its implementation; for example, the Swing-based input harvester plugin will
 * only harvest inputs if the Swing UI is currently the default one.
 * </p>
 * 
 * @author Curtis Rueden
 * @author Barry DeZonia
 * @param <P> The type of UI component housing the input panel itself.
 * @param <W> The type of UI component housing each input widget.
 */
public abstract class AbstractInputHarvesterPlugin<P, W> extends
	AbstractInputHarvester<P, W> implements PreprocessorPlugin
{

	@Parameter(required = false)
	private UIService uiService;

	private String cancelReason;

	// -- ModuleProcessor methods --

	@Override
	public void process(final Module module) {
		if (uiService == null) return; // no UI service means no input harvesting!

		// do not harvest if the UI is inactive!
		if (!uiService.isVisible(getUI())) return;

		// proceed with input harvesting
		try {
			harvest(module);
		}
		catch (final ModuleException e) {
			cancel(e.getMessage());
		}
	}

	// -- Cancelable methods --

	@Override
	public boolean isCanceled() {
		return cancelReason != null;
	}

	@Override
	public void cancel(final String reason) {
		cancelReason = reason == null ? "" : reason;
	}

	@Override
	public String getCancelReason() {
		return cancelReason;
	}

	// -- Internal methods --

	/** Gets the name (or class name) of the input harvester's affiliated UI. */
	protected abstract String getUI();

}
