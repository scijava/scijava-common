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

package org.scijava.module;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.scijava.Identifiable;
import org.scijava.MenuPath;
import org.scijava.Priority;
import org.scijava.convert.ConvertService;
import org.scijava.event.EventService;
import org.scijava.input.Accelerator;
import org.scijava.log.LogService;
import org.scijava.module.event.ModulesAddedEvent;
import org.scijava.module.event.ModulesRemovedEvent;
import org.scijava.module.process.ModulePostprocessor;
import org.scijava.module.process.ModulePreprocessor;
import org.scijava.module.process.PostprocessorPlugin;
import org.scijava.module.process.PreprocessorPlugin;
import org.scijava.object.ObjectService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.plugin.PluginService;
import org.scijava.prefs.PrefService;
import org.scijava.service.AbstractService;
import org.scijava.service.Service;
import org.scijava.thread.ThreadService;
import org.scijava.util.ClassUtils;

/**
 * Default service for keeping track of and executing available modules.
 * 
 * @author Curtis Rueden
 * @see Module
 * @see ModuleInfo
 */
@Plugin(type = Service.class)
public class DefaultModuleService extends AbstractService implements
	ModuleService
{

	@Parameter
	private LogService log;

	@Parameter
	private EventService eventService;

	@Parameter
	private PluginService pluginService;

	@Parameter
	private ObjectService objectService;

	@Parameter
	private ThreadService threadService;

	@Parameter
	private PrefService prefService;

	@Parameter
	private ConvertService convertService;

	/** Index of registered modules. */
	private ModuleIndex moduleIndex;

	// -- ModuleService methods --

	@Override
	public ModuleIndex getIndex() {
		return moduleIndex;
	}

	@Override
	public void addModule(final ModuleInfo module) {
		if (moduleIndex.add(module)) {
			eventService.publish(new ModulesAddedEvent(module));
		}
	}

	@Override
	public void removeModule(final ModuleInfo module) {
		if (moduleIndex.remove(module)) {
			eventService.publish(new ModulesRemovedEvent(module));
		}
	}

	@Override
	public void addModules(final Collection<? extends ModuleInfo> modules) {
		if (moduleIndex.addAll(modules)) {
			eventService.publish(new ModulesAddedEvent(modules));
		}
	}

	@Override
	public void removeModules(final Collection<? extends ModuleInfo> modules) {
		if (moduleIndex.removeAll(modules)) {
			eventService.publish(new ModulesRemovedEvent(modules));
		}
	}

	@Override
	public List<ModuleInfo> getModules() {
		return moduleIndex.getAll();
	}

	@Override
	public ModuleInfo getModuleById(final String id) {
		// TODO: Cache identifiers in a hash?
		for (final ModuleInfo info : getModules()) {
			if (!(info instanceof Identifiable)) continue;
			final String infoID = ((Identifiable) info).getIdentifier();
			if (id.equals(infoID)) return info;
		}
		return null;
	}

	@Override
	public ModuleInfo getModuleForAccelerator(final Accelerator acc) {
		for (final ModuleInfo info : getModules()) {
			final MenuPath menuPath = info.getMenuPath();
			if (menuPath == null || menuPath.isEmpty()) continue;
			if (acc.equals(menuPath.getLeaf().getAccelerator())) return info;
		}
		return null;
	}

	@Override
	public Module createModule(final ModuleInfo info) {
		final Module existing = getRegisteredModuleInstance(info);
		if (existing != null) return existing;

		try {
			final Module module = info.createModule();
			getContext().inject(module);
			Priority.inject(module, info.getPriority());
			return module;
		}
		catch (final ModuleException exc) {
			log.error("Cannot create module: " + info.getDelegateClassName(), exc);
		}
		return null;
	}

	@Override
	public Future<Module> run(final ModuleInfo info, final boolean process,
		final Object... inputs)
	{
		return run(info, pre(process), post(process), inputs);
	}

	@Override
	public Future<Module> run(final ModuleInfo info, final boolean process,
		final Map<String, Object> inputMap)
	{
		return run(info, pre(process), post(process), inputMap);
	}

	@Override
	public Future<Module> run(final ModuleInfo info,
		final List<? extends ModulePreprocessor> pre,
		final List<? extends ModulePostprocessor> post, final Object... inputs)
	{
		return run(info, pre, post, createMap(inputs));
	}

	@Override
	public Future<Module> run(final ModuleInfo info,
		final List<? extends ModulePreprocessor> pre,
		final List<? extends ModulePostprocessor> post,
		final Map<String, Object> inputMap)
	{
		final Module module = createModule(info);
		if (module == null) return null;
		return run(module, pre, post, inputMap);
	}

	@Override
	public <M extends Module> Future<M> run(final M module,
		final boolean process, final Object... inputs)
	{
		return run(module, pre(process), post(process), inputs);
	}

	@Override
	public <M extends Module> Future<M> run(final M module,
		final boolean process, final Map<String, Object> inputMap)
	{
		return run(module, pre(process), post(process), inputMap);
	}

	@Override
	public <M extends Module> Future<M> run(final M module,
		final List<? extends ModulePreprocessor> pre,
		final List<? extends ModulePostprocessor> post, final Object... inputs)
	{
		return run(module, pre, post, createMap(inputs));
	}

	@Override
	public <M extends Module> Future<M> run(final M module,
		final List<? extends ModulePreprocessor> pre,
		final List<? extends ModulePostprocessor> post,
		final Map<String, Object> inputMap)
	{
		assignInputs(module, inputMap);
		final ModuleRunner runner =
			new ModuleRunner(getContext(), module, pre, post);
		@SuppressWarnings("unchecked")
		final Callable<M> callable = (Callable<M>) runner;
		final Future<M> future = threadService.run(callable);
		return future;
	}

	@Override
	public <M extends Module> M waitFor(final Future<M> future) {
		try {
			return future.get();
		}
		catch (final InterruptedException e) {
			log.error("Module execution interrupted", e);
		}
		catch (final ExecutionException e) {
			log.error("Error during module execution", e);
		}
		return null;
	}

	@Override
	public <T> ModuleItem<T> getSingleInput(final Module module,
		final Class<T> type)
	{
		return getTypedSingleItem(module, type, module.getInfo().inputs());
	}

	@Override
	public <T> ModuleItem<T> getSingleOutput(final Module module,
		final Class<T> type)
	{
		return getTypedSingleItem(module, type, module.getInfo().outputs());
	}

	@Override
	public ModuleItem<?> getSingleInput(Module module, Collection<Class<?>> types) {
		return getSingleItem(module, types, module.getInfo().inputs());
	}

	@Override
	public ModuleItem<?> getSingleOutput(Module module, Collection<Class<?>> types) {
		return getSingleItem(module, types, module.getInfo().outputs());
	}

	@Override
	public <T> void save(final ModuleItem<T> item, final T value) {
		if (!item.isPersisted()) return;

		if (Objects.equals(item.getDefaultValue(), value)) {
			// NB: Do not persist the value if it is the default.
			// This is nice if the default value might change later,
			// such as when iteratively developing a script.
			return;
		}

		final String sValue = value == null ? "" : value.toString();

		// do not persist if object cannot be converted back from a string
		if (!convertService.supports(sValue, item.getType())) return;

		final String persistKey = item.getPersistKey();
		if (persistKey == null || persistKey.isEmpty()) {
			final Class<?> prefClass = delegateClass(item);
			final String prefKey = item.getName();
			prefService.put(prefClass, prefKey, sValue);
		}
		else prefService.put(persistKey, sValue);
	}

	@Override
	public <T> T load(final ModuleItem<T> item) {
		// if there is nothing to load from persistence return nothing
		if (!item.isPersisted()) return null;

		final String sValue;
		final String persistKey = item.getPersistKey();
		if (persistKey == null || persistKey.isEmpty()) {
			final Class<?> prefClass = delegateClass(item);
			final String prefKey = item.getName();
			sValue = prefService.get(prefClass, prefKey);
		}
		else sValue = prefService.get(persistKey);

		// if persisted value has never been set before return null
		if (sValue == null) return null;

		return convertService.convert(sValue, item.getType());
	}
	
	@Override
	public <T> T getDefaultValue(final ModuleItem<T> item) {
		final T defaultValue = item.getDefaultValue();
		if (defaultValue != null) return defaultValue;
		final T min = item.getMinimumValue();
		if (min != null) return min;
		final T softMin = item.getSoftMinimum();
		if (softMin != null) return softMin;
		final T max = item.getMaximumValue();
		if (max != null) return max;
		final T softMax = item.getSoftMaximum();
		if (softMax != null) return softMax;
		if (Number.class.isAssignableFrom(item.getType())) {
			final T zero = convertService.convert("0", item.getType());
			if (zero != null) return zero;
		}
		// no known default value
		return null;
	}

	// -- Service methods --

	@Override
	public void initialize() {
		moduleIndex = new ModuleIndex();
	}

	// -- Helper methods --

	/** Creates the preprocessor chain. */
	private List<? extends PreprocessorPlugin> pre(final boolean process) {
		if (!process) return null;
		return pluginService.createInstancesOfType(PreprocessorPlugin.class);
	}

	/** Creates the postprocessor chain. */
	private List<? extends PostprocessorPlugin> post(final boolean process) {
		if (!process) return null;
		return pluginService.createInstancesOfType(PostprocessorPlugin.class);
	}

	/**
	 * Checks the {@link ObjectService} for a single registered {@link Module}
	 * instance of the given {@link ModuleInfo}. In this way, if you want to
	 * repeatedly reuse the same module instance instead of creating a new one
	 * with each execution, you can register it with the {@link ObjectService} and
	 * it will be automatically returned by the {@link #createModule(ModuleInfo)}
	 * method (and hence automatically used whenever a {@link #run} method with
	 * {@link ModuleInfo} argument is called).
	 */
	private Module getRegisteredModuleInstance(final ModuleInfo info) {
		final Class<?> type = ClassUtils.loadClass(info.getDelegateClassName());
		if (type == null || !Module.class.isAssignableFrom(type)) return null;

		// the module metadata's delegate class extends Module, so there is hope
		@SuppressWarnings("unchecked")
		final Class<? extends Module> moduleType = (Class<? extends Module>) type;

		// ask the object service for an instance of the delegate type
		final List<? extends Module> objects = objectService.getObjects(moduleType);
		if (objects == null || objects.isEmpty()) {
			// the object service has no such instances
			return null;
		}
		if (objects.size() > 1) {
			// there are multiple instances; it's not clear which one to use
			log.warn("Ignoring multiple candidate module instances for class: " +
				type.getName());
			return null;
		}
		// found exactly one instance; return it!
		return objects.get(0);
	}

	/** Converts the given list of name/value pairs into an input map. */
	private Map<String, Object> createMap(final Object[] values) {
		if (values == null || values.length == 0) return null;

		final HashMap<String, Object> inputMap = new HashMap<>();

		if (values.length % 2 != 0) {
			log.error("Ignoring extraneous argument: " + values[values.length - 1]);
		}

		// loop over list of key/value pairs
		final int numPairs = values.length / 2;
		for (int i = 0; i < numPairs; i++) {
			final Object key = values[2 * i];
			final Object value = values[2 * i + 1];
			if (!(key instanceof String)) {
				log.error("Invalid input name: " + key);
				continue;
			}
			final String name = (String) key;
			inputMap.put(name, value);
		}

		return inputMap;
	}

	/** Sets the given module's input values to those in the given map. */
	private void assignInputs(final Module module,
		final Map<String, Object> inputMap)
	{
		if (inputMap == null) return; // no inputs to assign

		for (final String name : inputMap.keySet()) {
			final ModuleItem<?> input = module.getInfo().getInput(name);
			final Object value = inputMap.get(name);
			final Object converted;
			if (input == null) {
				// inputs whose name starts with a dot are implicitly known by convention
				if (!name.startsWith(".")) {
					log.warn("Unmatched input: " + name);
				}
				converted = value;
			}
			else {
				final Class<?> type = input.getType();
				converted = convertService.convert(value, type);
				if (value != null && converted == null) {
					log.error("For input " + name + ": incompatible object " +
						value.getClass().getName() + " for type " + type.getName());
					continue;
				}
			}
			module.setInput(name, converted);
			module.resolveInput(name);
		}
	}

	private <T> ModuleItem<T> getTypedSingleItem(final Module module,
		final Class<T> type, final Iterable<ModuleItem<?>> items)
	{
		Set<Class<?>> types = new HashSet<>();
		types.add(type);
		@SuppressWarnings("unchecked")
		ModuleItem<T> result = (ModuleItem<T>) getSingleItem(module, types, items);
		return result;
	}

	private ModuleItem<?> getSingleItem(final Module module,
		final Collection<Class<?>> types, final Iterable<ModuleItem<?>> items)
	{
		ModuleItem<?> result = null;

		for (final ModuleItem<?> item : items) {
			final String name = item.getName();
			final boolean resolved = module.isInputResolved(name);
			if (resolved) continue; // skip resolved inputs
			if (!item.isAutoFill()) continue; // skip unfillable inputs
			final Class<?> itemType = item.getType();
			for (final Class<?> type : types) {
				if (type.isAssignableFrom(itemType)) {
					if (result != null) return null; // multiple matching module items
					result = item;
					// This module item matches, so no need to check more classes.
					break;
				}
			}
		}
		return result;
	}

	private <T> Class<?> delegateClass(final ModuleItem<T> item) {
		try {
			return item.getInfo().loadDelegateClass();
		}
		catch (final ClassNotFoundException exc) {
			throw new IllegalStateException(exc);
		}
	}

}
