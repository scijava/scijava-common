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

package org.scijava.plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.scijava.object.SortedObjectIndex;

/**
 * Data structure for managing registered plugins.
 * <p>
 * The plugin index is a special type of {@link org.scijava.object.ObjectIndex}
 * that classifies each {@link PluginInfo} object into a type hierarchy
 * compatible with its associated <em>plugin</em> type (i.e.,
 * {@link PluginInfo#getPluginType()}), rather than {@link PluginInfo}'s type
 * hierarchy (i.e., {@link PluginInfo}, {@link org.scijava.UIDetails},
 * {@link org.scijava.Instantiable}, etc.).
 * </p>
 * <p>
 * NB: This type hierarchy will typically <em>not</em> include the plugin class
 * itself; for example, the {@link org.scijava.plugin.DefaultPluginService} has
 * a plugin type of {@link org.scijava.service.Service}, and hence will be
 * categorized beneath {@code Service.class}, not
 * {@code DefaultPluginService.class} or {@code PluginService.class}. The
 * rationale is that to fully classify each plugin including its own class, said
 * class would need to be loaded, which SciJava makes an effort not to do until
 * the plugin is actually needed for the first time.
 * </p>
 * 
 * @author Curtis Rueden
 */
public class PluginIndex extends SortedObjectIndex<PluginInfo<?>> {

	/**
	 * The plugin finder which will be used to discover plugins.
	 * 
	 * @see #discover()
	 */
	private final PluginFinder pluginFinder;

	/** Exception table from last invocation of {@link #discover()}. */
	private Map<String, Throwable> exceptions;

	/**
	 * Constructs a new plugin index which uses a {@link DefaultPluginFinder} to
	 * discover plugins.
	 */
	public PluginIndex() {
		this(new DefaultPluginFinder());
	}
 
	/**
	 * Constructs a new plugin index which uses the given {@link PluginFinder} to
	 * discover plugins.
	 * <p>
	 * A null PluginFinder is allowed, in which case no plugins will be discovered
	 * during {@link #discover()} calls.
	 * </p>
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public PluginIndex(final PluginFinder pluginFinder) {
		// NB: See: http://stackoverflow.com/questions/4765520/
		super((Class) PluginInfo.class);
		this.pluginFinder = pluginFinder;
	}

	// -- PluginIndex methods --

	/**
	 * Adds all plugins discovered by the attached {@link PluginFinder} to this
	 * index, or does nothing if the attached {@link PluginFinder} is null.
	 */
	public void discover() {
		if (pluginFinder == null) return;
		final ArrayList<PluginInfo<?>> plugins = new ArrayList<>();
		exceptions = pluginFinder.findPlugins(plugins);
		addAll(plugins);
	}

	/**
	 * Gets the exceptions which occurred during the last invocation of
	 * {@link #discover()}.
	 */
	public Map<String, Throwable> getExceptions() {
		return exceptions;
	}

	/**
	 * Gets a list of registered plugins compatible with the given type.
	 * <p>
	 * This method is more specific than {@link #get(Class)} since that method
	 * returns only a {@code List<PluginInfo<?>>}, whereas this one is guaranteed
	 * to return a {@code List<PluginInfo<P>>}.
	 * </p>
	 * 
	 * @return Read-only list of registered objects of the given type, or an empty
	 *         list if no such objects exist (this method never returns null).
	 */
	public <PT extends SciJavaPlugin> List<PluginInfo<PT>> getPlugins(
		final Class<PT> type)
	{
		final List<PluginInfo<?>> list = get(type);
		@SuppressWarnings({ "rawtypes", "unchecked" })
		final List<PluginInfo<PT>> result = (List) list;
		return result;
	}

	// -- Internal methods --

	/**
	 * Overrides the type by which the entries are indexed.
	 * 
	 * @see PluginInfo#getPluginType()
	 */
	@Override
	protected Class<?> getType(final PluginInfo<?> info) {
		return info.getPluginType();
	}

	/**
	 * Removes the plugin from all type lists compatible with its plugin type.
	 * <p>
	 * NB: This behavior differs from the default
	 * {@link org.scijava.object.ObjectIndex} behavior in that the {@code info}
	 * object's actual type hierarchy is not used for classification, but rather
	 * the object is classified according to {@link PluginInfo#getPluginType()}.
	 * </p>
	 * 
	 * @see PluginInfo#getPluginType()
	 */
	@Override
	protected boolean remove(final Object o, final boolean batch) {
		if (!(o instanceof PluginInfo)) return false;
		final PluginInfo<?> info = (PluginInfo<?>) o;
		return remove(info, info.getPluginType(), batch);
	}

}
