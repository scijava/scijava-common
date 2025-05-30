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

package org.scijava.plugin;

import java.net.URL;
import java.util.Collection;
import java.util.Optional;

import org.scijava.AbstractUIDetails;
import org.scijava.Identifiable;
import org.scijava.Instantiable;
import org.scijava.InstantiableException;
import org.scijava.Locatable;
import org.scijava.MenuEntry;
import org.scijava.MenuPath;
import org.scijava.Priority;
import org.scijava.UIDetails;
import org.scijava.Versioned;
import org.scijava.input.Accelerator;
import org.scijava.util.StringMaker;
import org.scijava.util.Types;
import org.scijava.util.VersionUtils;

/**
 * A collection of metadata about a particular plugin.
 * <p>
 * For performance reasons, the metadata is populated without actually loading
 * the plugin class, by reading from an efficient binary cache (see
 * {@link org.scijava.plugin.DefaultPluginService} for details). As such, we can
 * very quickly build a complex structure containing all available plugins
 * without waiting for the Java class loader.
 * </p>
 * 
 * @param <PT> The <em>type</em> of plugin described by this metadata. See
 *          {@link SciJavaPlugin} for a list of common plugin types.
 * @author Curtis Rueden
 * @see Plugin
 * @see PluginService
 */
public class PluginInfo<PT extends SciJavaPlugin> extends AbstractUIDetails
	implements Instantiable<PT>, Identifiable, Locatable, Versioned
{

	/** Fully qualified class name of this plugin. */
	private String className;

	/** Class object for this plugin. Lazily loaded. */
	private Class<? extends PT> pluginClass;

	/** Type of this entry's plugin; e.g., {@link org.scijava.service.Service}. */
	private Class<PT> pluginType;

	/** Annotation describing the plugin. */
	private Plugin annotation;

	/** Class loader to use when loading the class with {@link #loadClass()}. */
	private ClassLoader classLoader;

	/**
	 * Creates a new plugin metadata object.
	 * 
	 * @param className The name of the class, which must implement
	 *          {@link SciJavaPlugin}.
	 * @param pluginType The <em>type</em> of plugin described by this metadata.
	 *          See {@link SciJavaPlugin} for a list of common plugin types.
	 */
	public PluginInfo(final String className, final Class<PT> pluginType) {
		this(className, null, pluginType, null, null);
	}

	/**
	 * Creates a new plugin metadata object.
	 * 
	 * @param className The name of the class, which must implement
	 *          {@link SciJavaPlugin}.
	 * @param pluginType The <em>type</em> of plugin described by this metadata.
	 *          See {@link SciJavaPlugin} for a list of common plugin types.
	 * @param annotation The @{@link Plugin} annotation to associate with this
	 *          metadata object.
	 */
	public PluginInfo(final String className, final Class<PT> pluginType,
		final Plugin annotation)
	{
		this(className, null, pluginType, annotation, null);
	}

	/**
	 * Creates a new plugin metadata object.
	 * 
	 * @param className The name of the class, which must implement
	 *          {@link SciJavaPlugin}.
	 * @param pluginType The <em>type</em> of plugin described by this metadata.
	 *          See {@link SciJavaPlugin} for a list of common plugin types.
	 * @param annotation The @{@link Plugin} annotation to associate with this
	 *          metadata object.
	 * @param classLoader The {@link ClassLoader} to use when loading the class
	 *          via {@link #loadClass()}, or null to use the current thread's
	 *          context class loader by default.
	 */
	public PluginInfo(final String className, final Class<PT> pluginType,
		final Plugin annotation, final ClassLoader classLoader)
	{
		this(className, null, pluginType, annotation, classLoader);
	}

	/**
	 * Creates a new plugin metadata object.
	 * 
	 * @param pluginClass The plugin class, which must implement
	 *          {@link SciJavaPlugin}.
	 * @param pluginType The <em>type</em> of plugin described by this metadata.
	 *          See {@link SciJavaPlugin} for a list of common plugin types.
	 */
	public PluginInfo(final Class<? extends PT> pluginClass,
		final Class<PT> pluginType)
	{
		this(null, pluginClass, pluginType, null, null);
	}

	/**
	 * Creates a new plugin metadata object.
	 * 
	 * @param pluginClass The plugin class, which must implement
	 *          {@link SciJavaPlugin}.
	 * @param pluginType The <em>type</em> of plugin described by this metadata.
	 *          See {@link SciJavaPlugin} for a list of common plugin types.
	 * @param annotation The @{@link Plugin} annotation to associate with this
	 *          metadata object.
	 */
	public PluginInfo(final Class<? extends PT> pluginClass,
		final Class<PT> pluginType, final Plugin annotation)
	{
		this(null, pluginClass, pluginType, annotation, null);
	}

	protected PluginInfo(final String className,
		final Class<? extends PT> pluginClass, final Class<PT> pluginType,
		final Plugin annotation, final ClassLoader classLoader)
	{
		if (pluginClass != null) {
			if (className != null) {
				throw new IllegalArgumentException(
					"className and pluginClass are mutually exclusive");
			}
			setPluginClass(pluginClass);
		}
		else {
			this.className = className;
		}
		setPluginType(pluginType);
		setMenuPath(null);
		setMenuRoot(UIDetails.APPLICATION_MENU_ROOT);
		if (annotation == null) {
			// attempt to obtain the annotation from the plugin class, if available
			if (pluginClass != null) {
				this.annotation = pluginClass.getAnnotation(Plugin.class);
			}
		}
		else this.annotation = annotation;
		populateValues();
		this.classLoader = classLoader;
	}

	// -- PluginInfo methods --

	/**
	 * Explicitly sets the {@link Class} of the item objects.
	 * <p>
	 * This is useful if your class is produced by something other than the system
	 * classloader.
	 * </p>
	 */
	public void setPluginClass(final Class<? extends PT> pluginClass) {
		this.pluginClass = pluginClass;
	}

	/**
	 * Obtains the {@link Class} of the item objects, if that class has already
	 * been loaded.
	 * 
	 * @return The {@link Class}, or null if it has not yet been loaded by
	 *         {@link #loadClass}.
	 */
	public Class<? extends PT> getPluginClass() {
		return pluginClass;
	}

	/**
	 * Sets the <em>type</em> of plugin described by the metadata.
	 * @see SciJavaPlugin for a list of common plugin types.
	 */
	public void setPluginType(final Class<PT> pluginType) {
		this.pluginType = pluginType;
	}

	/**
	 * Gets the <em>type</em> of plugin described by the metadata.
	 * @see SciJavaPlugin for a list of common plugin types.
	 */
	public Class<PT> getPluginType() {
		return pluginType;
	}

	/** Gets the associated @{@link Plugin} annotation. */
	public Plugin getAnnotation() {
		return annotation;
	}

	/**
	 * Gets the URL corresponding to the icon resource path.
	 * 
	 * @see #getIconPath()
	 */
	public URL getIconURL() throws InstantiableException {
		final String iconPath = getIconPath();
		if (iconPath == null || iconPath.isEmpty()) return null;
		return loadClass().getResource(iconPath);
	}

	/**
	 * Injects the metadata into the given object. Note that this is only possible
	 * if the given object implements the {@link HasPluginInfo} interface.
	 * 
	 * @param o The object to which the metadata should be assigned.
	 * @return true If the metadata was successfully injected.
	 */
	public boolean inject(final Object o) {
		if (!(o instanceof HasPluginInfo)) return false;
		final HasPluginInfo hi = (HasPluginInfo) o;
		hi.setInfo(this);
		return true;
	}

	// -- Object methods --

	@Override
	public String toString() {
		final StringMaker sm = new StringMaker();
		sm.append("class", className);
		sm.append(super.toString());
		sm.append("pluginType", pluginType);
		return sm.toString();
	}

	// -- Instantiable methods --

	@Override
	public String getClassName() {
		if (pluginClass != null) return pluginClass.getName();
		return className;
	}

	@Override
	public Class<? extends PT> loadClass() throws InstantiableException {
		if (pluginClass == null) {
			try {
				final Class<?> c = Types.load(className, classLoader, false);
				@SuppressWarnings("unchecked")
				final Class<? extends PT> typedClass = (Class<? extends PT>) c;
				pluginClass = typedClass;
			}
			catch (final IllegalArgumentException exc) {
				throw new InstantiableException("Class not found: " + className, exc);
			}
		}

		return pluginClass;
	}

	@Override
	public PT createInstance() throws InstantiableException {
		final Class<? extends PT> c = loadClass();

		// instantiate plugin
		final PT instance;
		try {
			instance = c.newInstance();
			inject(instance);
			Priority.inject(instance, getPriority());
		}
		catch (final InstantiationException e) {
			throw new InstantiableException(e);
		}
		catch (final IllegalAccessException e) {
			throw new InstantiableException(e);
		}
		return instance;
	}

	// -- Identifiable methods --

	@Override
	public String getIdentifier() {
		return "plugin:" + getClassName();
	}

	// -- Locatable methods --

	@Override
	public String getLocation() {
		try {
			return Types.location(loadClass()).toExternalForm();
		}
		catch (InstantiableException exc) {
			return null;
		}
	}

	// -- Versioned methods --

	@Override
	public String getVersion() {
		try {
			return VersionUtils.getVersion(loadClass());
		}
		catch (InstantiableException exc) {
			return null;
		}
	}

	// -- Utility methods --

	/**
	 * Finds a {@link PluginInfo} of the given plugin class in the specified
	 * {@link PluginIndex}. <em>Note that to avoid loading plugin classes, class
	 * identity is determined by class name equality only.</em>
	 * 
	 * @param pluginClass The concrete class of the plugin whose
	 *          {@link PluginInfo} is desired.
	 * @param pluginIndex The {@link PluginIndex} to search for a matching
	 *          {@link PluginInfo}.
	 * @return The matching {@link PluginInfo}, or null if none found.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static <P extends SciJavaPlugin> PluginInfo<?> get(
		final Class<P> pluginClass, final PluginIndex pluginIndex)
	{
		return get(pluginClass, (Collection) pluginIndex.getAll());
	}

	/**
	 * Finds a {@link PluginInfo} of the given plugin class and plugin type in the
	 * specified {@link PluginIndex}. <em>Note that to avoid loading plugin
	 * classes, class identity is determined by class name equality only.</em>
	 * 
	 * @param pluginClass The concrete class of the plugin whose
	 *          {@link PluginInfo} is desired.
	 * @param pluginType The type of the plugin; see {@link #getPluginType()}.
	 * @param pluginIndex The {@link PluginIndex} to search for a matching
	 *          {@link PluginInfo}.
	 * @return The matching {@link PluginInfo}, or null if none found.
	 */
	public static <P extends PT, PT extends SciJavaPlugin> PluginInfo<PT> get(
		final Class<P> pluginClass, final Class<PT> pluginType,
		final PluginIndex pluginIndex)
	{
		return get(pluginClass, pluginIndex.getPlugins(pluginType));
	}

	/**
	 * Finds a {@link PluginInfo} of the given plugin class in the specified list
	 * of plugins. <em>Note that to avoid loading plugin classes, class identity
	 * is determined by class name equality only.</em>
	 * 
	 * @param pluginClass The concrete class of the plugin whose
	 *          {@link PluginInfo} is desired.
	 * @param plugins The list of plugins to search for a match.
	 * @return The matching {@link PluginInfo}, or null if none found.
	 */
	public static <P extends PT, PT extends SciJavaPlugin> PluginInfo<PT> get(
		final Class<P> pluginClass,
		final Collection<? extends PluginInfo<PT>> plugins)
	{
		final String className = pluginClass.getName();
		final Optional<? extends PluginInfo<PT>> result = plugins.stream() //
			.filter(info -> info.getClassName().equals(className)) //
			.findFirst();
		return result.isPresent() ? result.get() : null;
	}

	/**
	 * Creates a {@link PluginInfo} for the given plugin class. The class must be
	 * a concrete class annotated with the @{@link Plugin} annotation, from which
	 * the plugin type will be inferred.
	 * 
	 * @param pluginClass The concrete class of the plugin for which a new
	 *          {@link PluginInfo} is desired.
	 * @return A newly created {@link PluginInfo} for the given plugin class.
	 * @throws IllegalArgumentException if the given class is not annotated
	 *           with @{@link Plugin}, or its annotated {@link Plugin#type()
	 *           type()} is not a supertype of the plugin class.
	 */
	public static PluginInfo<?> create(
		final Class<? extends SciJavaPlugin> pluginClass)
	{
		@SuppressWarnings({ "rawtypes", "unchecked" })
		final PluginInfo<?> info = new PluginInfo(pluginClass, //
			pluginType(pluginClass));
		return info;
	}

	/**
	 * Creates a {@link PluginInfo} for the given plugin class of the specified
	 * plugin type.
	 * 
	 * @param pluginClass The concrete class of the plugin for which a new
	 *          {@link PluginInfo} is desired.
	 * @param pluginType The type of the plugin; see {@link #getPluginType()}.
	 * @return A newly created {@link PluginInfo} for the given plugin class.
	 */
	public static <P extends PT, PT extends SciJavaPlugin> PluginInfo<PT> create(
		final Class<P> pluginClass, final Class<PT> pluginType)
	{
		return new PluginInfo<>(pluginClass, pluginType);
	}

	/**
	 * Obtains a {@link PluginInfo} for the given plugin class. If one already
	 * exists in the specified {@link PluginIndex}, it is retrieved (see
	 * {@link #get(Class, PluginIndex)}); otherwise, a new one is created (see
	 * {@link #create(Class)}) but not added to the index.
	 * 
	 * @param pluginClass The concrete class of the plugin whose
	 *          {@link PluginInfo} is desired.
	 * @param pluginIndex The {@link PluginIndex} to search for a matching
	 *          {@link PluginInfo}.
	 * @throws IllegalArgumentException when creating a new {@link PluginInfo} if
	 *           the associated plugin type cannot be inferred; see
	 *           {@link #create(Class)}.
	 */
	public static <P extends SciJavaPlugin> PluginInfo<?> getOrCreate(
		final Class<P> pluginClass, final PluginIndex pluginIndex)
	{
		final PluginInfo<?> existing = get(pluginClass, pluginIndex);
		return existing == null ? create(pluginClass) : existing;
	}

	/**
	 * Obtains a {@link PluginInfo} for the given plugin class. If one already
	 * exists in the specified {@link PluginIndex}, it is retrieved (see
	 * {@link #get(Class, PluginIndex)}); otherwise, a new one is created (see
	 * {@link #create(Class)}) but not added to the index.
	 * 
	 * @param pluginClass The concrete class of the plugin whose
	 *          {@link PluginInfo} is desired.
	 * @param pluginType The type of the plugin; see {@link #getPluginType()}.
	 * @param pluginIndex The {@link PluginIndex} to search for a matching
	 *          {@link PluginInfo}.
	 */
	public static <P extends PT, PT extends SciJavaPlugin> PluginInfo<PT>
		getOrCreate(final Class<P> pluginClass, final Class<PT> pluginType,
			final PluginIndex pluginIndex)
	{
		final PluginInfo<PT> existing = get(pluginClass, pluginType, pluginIndex);
		return existing == null ? create(pluginClass, pluginType) : existing;
	}

	// -- Helper methods --

	/** Populates the entry to match the associated @{@link Plugin} annotation. */
	private void populateValues() {
		final Plugin ann = getAnnotation();
		if (ann == null) return;
		setName(ann.name());
		setLabel(ann.label());
		setDescription(ann.description());

		final MenuPath menuPath;
		final Menu[] menu = ann.menu();
		if (menu.length > 0) {
			menuPath = parseMenuPath(menu);
		}
		else {
			// parse menuPath attribute
			menuPath = new MenuPath(ann.menuPath());
		}
		setMenuPath(menuPath);

		setMenuRoot(ann.menuRoot());

		final String iconPath = ann.iconPath();
		setIconPath(iconPath);
		setPriority(ann.priority());
		setEnabled(ann.enabled());
		setVisible(ann.visible());
		setSelectable(ann.selectable());
		setSelectionGroup(ann.selectionGroup());

		// add default icon if none attached to leaf
		final MenuEntry menuLeaf = menuPath.getLeaf();
		if (menuLeaf != null && !iconPath.isEmpty()) {
			final String menuIconPath = menuLeaf.getIconPath();
			if (menuIconPath == null || menuIconPath.isEmpty()) {
				menuLeaf.setIconPath(iconPath);
			}
		}

		// populate extra attributes
		for (final Attr attr : ann.attrs()) {
			final String name = attr.name();
			final String value = attr.value();
			set(name, value);
		}
	}

	private MenuPath parseMenuPath(final Menu[] menu) {
		final MenuPath menuPath = new MenuPath();
		for (int i = 0; i < menu.length; i++) {
			final String name = menu[i].label();
			final double weight = menu[i].weight();
			final char mnemonic = menu[i].mnemonic();
			final Accelerator acc = Accelerator.create(menu[i].accelerator());
			final String iconPath = menu[i].iconPath();
			menuPath.add(new MenuEntry(name, weight, mnemonic, acc, iconPath));
		}
		return menuPath;
	}

	/** Extracts the plugin type from a class's @{@link Plugin} annotation. */
	private static <P extends SciJavaPlugin> Class<? super P> pluginType(
		final Class<P> pluginClass)
	{
		final Plugin annotation = pluginClass.getAnnotation(Plugin.class);
		if (annotation == null) {
			throw new IllegalArgumentException(
				"Cannot infer plugin type from class '" + pluginClass.getName() +
					"' with no @Plugin annotation.");
		}
		final Class<?> type = annotation.type();
		if (!type.isAssignableFrom(pluginClass)) {
			throw new IllegalArgumentException("Invalid plugin type '" + //
				type.getName() + "' for class '" + pluginClass.getName() + "'");
		}
		@SuppressWarnings("unchecked")
		final Class<? super P> pluginType = (Class<? super P>) type;
		return pluginType;
	}
}
