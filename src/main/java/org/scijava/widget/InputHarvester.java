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

package org.scijava.widget;

import org.scijava.Priority;
import org.scijava.module.Module;
import org.scijava.module.ModuleCanceledException;
import org.scijava.module.ModuleException;
import org.scijava.module.ModuleItem;

/**
 * An input harvester collects {@link Module} input values, according to the
 * following steps:
 * <ol>
 * <li>Create an {@link InputPanel} with widgets corresponding to the module
 * inputs.</li>
 * <li>Present the panel, if in a UI context.</li>
 * <li>Harvest the final widget values from the panel, updating the
 * {@link Module}'s input values to match the harvested values.</li>
 * <li>Perform any other needed processing of the results (marking inputs as
 * resolved, storing persisted values to preferences, etc.).</li>
 * </ol>
 * 
 * @author Curtis Rueden
 * @param <P> The type of UI component housing the input panel itself.
 * @param <W> The type of UI component housing each input widget.
 */
public interface InputHarvester<P, W> {

	double PRIORITY = Priority.VERY_LOW;

	/**
	 * Performs the harvesting process.
	 * 
	 * @param module The module whose inputs should be harvest.
	 * @throws ModuleException If the process goes wrong, or is canceled.
	 */
	default void harvest(final Module module) throws ModuleException {
		final InputPanel<P, W> inputPanel = createInputPanel();
		buildPanel(inputPanel, module);
		if (!inputPanel.hasWidgets()) return; // no inputs left to harvest

		final boolean ok = harvestInputs(inputPanel, module);
		if (!ok) throw new ModuleCanceledException();

		processResults(inputPanel, module);
	}

	/**
	 * Constructs an empty {@link InputPanel}. Widgets are added later using the
	 * {@link #buildPanel} method.
	 */
	InputPanel<P, W> createInputPanel();

	/**
	 * Populates the given {@link InputPanel} with widgets corresponding to the
	 * given {@link Module} instance.
	 * 
	 * @param inputPanel The panel to populate.
	 * @param module The module whose inputs should be translated into widgets.
	 * @throws ModuleException if the panel cannot be populated for some reason.
	 *           This may occur due to an input of unsupported type.
	 */
	void buildPanel(InputPanel<P, W> inputPanel, Module module)
		throws ModuleException;

	/**
	 * Gathers input values from the user or other source. For example, a
	 * graphical user interface could present a dialog box.
	 */
	boolean harvestInputs(InputPanel<P, W> inputPanel, Module module);

	/** Does any needed processing, after input values have been harvested. */
	@SuppressWarnings("unused")
	default void processResults(final InputPanel<P, W> inputPanel,
		final Module module) throws ModuleException
	{
		final Iterable<ModuleItem<?>> inputs = module.getInfo().inputs();

		for (final ModuleItem<?> item : inputs) {
			final String name = item.getName();
			module.resolveInput(name);
		}
	}
}
