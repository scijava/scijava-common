/*
 * #%L
 * SciJava Common shared library for SciJava software.
 * %%
 * Copyright (C) 2009 - 2025 SciJava developers.
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.scijava.AbstractContextual;
import org.scijava.convert.ConvertService;
import org.scijava.log.LogService;
import org.scijava.module.Module;
import org.scijava.module.ModuleException;
import org.scijava.module.ModuleItem;
import org.scijava.object.ObjectService;
import org.scijava.plugin.Parameter;

/**
 * Abstract superclass for {@link InputHarvester}s.
 * <p>
 * An input harvester obtains a module's unresolved input parameter values from
 * the user. Parameters are collected using an {@link InputPanel} dialog box.
 * </p>
 * 
 * @author Curtis Rueden
 * @param <P> The type of UI component housing the input panel itself.
 * @param <W> The type of UI component housing each input widget.
 */
public abstract class AbstractInputHarvester<P, W> extends AbstractContextual
	implements InputHarvester<P, W>
{

	@Parameter
	private LogService log;

	@Parameter
	private WidgetService widgetService;

	@Parameter
	private ObjectService objectService;

	@Parameter
	private ConvertService convertService;

	// -- InputHarvester methods --

	@Override
	public void buildPanel(final InputPanel<P, W> inputPanel, final Module module)
		throws ModuleException
	{
		final Iterable<ModuleItem<?>> inputs = module.getInfo().inputs();

		final ArrayList<WidgetModel> models = new ArrayList<>();

		for (final ModuleItem<?> item : inputs) {
			final WidgetModel model = addInput(inputPanel, module, item);
			if (model != null) models.add(model);
		}

		// mark all models as initialized
		for (final WidgetModel model : models)
			model.setInitialized(true);

		// compute initial preview
		module.preview();
	}

	// -- Helper methods --

	private <T> WidgetModel addInput(final InputPanel<P, W> inputPanel,
		final Module module, final ModuleItem<T> item) throws ModuleException
	{
		final String name = item.getName();
		final boolean resolved = module.isInputResolved(name);
		if (resolved) return null; // skip resolved inputs

		final Class<T> type = item.getType();
		final WidgetModel model =
			widgetService.createModel(inputPanel, module, item, getObjects(type));

		final Class<W> widgetType = inputPanel.getWidgetComponentType();
		final InputWidget<?, ?> widget = widgetService.create(model);
		if (widget == null) {
			log.debug("No widget found for input: " + model.getItem().getName());
		}
		if (widget != null && widget.getComponentType() == widgetType) {
			@SuppressWarnings("unchecked")
			final InputWidget<?, W> typedWidget = (InputWidget<?, W>) widget;
			inputPanel.addWidget(typedWidget);
			return model;
		}

		if (item.isRequired()) {
			final List<String> vowelSoundPrefixes = Arrays.asList(
				"a", "e", "i", "o", "u", "honor", "honour", "hour", "xml"
			);
			final String typeName = type.getSimpleName();
			final String article = vowelSoundPrefixes.stream().anyMatch(
				prefix -> typeName.toLowerCase().startsWith(prefix)
			) ? "An" : "A";
			throw new ModuleException(article + " " + typeName +
				" is required but none is available.");
		}

		// item is not required; we can skip it
		return null;
	}

	/** Asks the object service and convert service for valid choices */
	private List<Object> getObjects(final Class<?> type) {
		// Start with the known, unconverted objects of the desired type
		List<Object> objects = new ArrayList<>(objectService.getObjects(type));

		// Get all the known objects that can be converted to the destination type
		Collection<Object> compatibleInputs = convertService.getCompatibleInputs(type);

		// HACK: Add each convertible object that doesn't share a name with any other object
		// Our goal here is to de-duplicate by avoiding similar inputs that could be converted
		// to the same effective output (e.g. an ImageDisplay and a Dataset that map to the same
		// ImgPlus)
		Set<String> knownNames = objects.stream().map(Object::toString).collect(Collectors.toSet());
		for (Object o : compatibleInputs) {
			final String s = o.toString();
			if (!knownNames.contains(s)) {
				objects.add(o);
				knownNames.add(s);
			}
		}
		return objects;
	}
}
