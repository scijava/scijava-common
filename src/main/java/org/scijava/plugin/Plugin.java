/*
 * #%L
 * SciJava Common shared library for SciJava software.
 * %%
 * Copyright (C) 2009 - 2013 Board of Regents of the University of
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
 * 
 * The views and conclusions contained in the software and documentation are
 * those of the authors and should not be interpreted as representing official
 * policies, either expressed or implied, of any organization.
 * #L%
 */

package org.scijava.plugin;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import net.java.sezpoz.Indexable;

import org.scijava.Priority;

/**
 * Annotation identifying a plugin, which gets loaded by SciJava's dynamic
 * discovery mechanism.
 * 
 * @author Curtis Rueden
 * @see SciJavaPlugin
 * @see PluginService
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Indexable(type = SciJavaPlugin.class)
public @interface Plugin {

	String APPLICATION_MENU_ROOT = "app";
	String CONTEXT_MENU_ROOT = "context";

	/** The type of plugin; e.g., {@link org.scijava.service.Service}. */
	Class<? extends SciJavaPlugin> type();

	/** The name of the plugin. */
	String name() default "";

	/** The human-readable label to use (e.g., in the menu structure). */
	String label() default "";

	/** A longer description of the plugin (e.g., for use as a tool tip). */
	String description() default "";

	/**
	 * Abbreviated menu path defining where the plugin is shown in the menu
	 * structure. Uses greater than signs (>) as a separator; e.g.:
	 * "Image > Overlay > Properties..." defines a "Properties..." menu item
	 * within the "Overlay" submenu of the "Image" menu. Use either
	 * {@link #menuPath} or {@link #menu} but not both.
	 */
	String menuPath() default "";

	/**
	 * Full menu path defining where the plugin is shown in the menu structure.
	 * This construction allows menus to be fully specified including mnemonics,
	 * accelerators and icons. Use either {@link #menuPath} or {@link #menu} but
	 * not both.
	 */
	Menu[] menu() default {};

	/**
	 * String identifier naming the menu to which this plugin belongs, or in the
	 * case of a tool, the context menu that should be displayed while the tool is
	 * active. The default value of {@link #APPLICATION_MENU_ROOT} references the
	 * menu structure of the primary application window.
	 */
	String menuRoot() default APPLICATION_MENU_ROOT;

	/** Path to the plugin's icon (e.g., shown in the menu structure). */
	String iconPath() default "";

	/**
	 * The plugin index returns plugins sorted by priority. For example, this is
	 * useful for {@link org.scijava.service.Service}s to control which service
	 * implementation is chosen when multiple implementations are present in the
	 * classpath, as well as to force instantiation of one service over another
	 * when the dependency hierarchy does not dictate otherwise.
	 * <p>
	 * Any double value is allowed, but for convenience, there are some presets:
	 * </p>
	 * <ul>
	 * <li>{@link Priority#FIRST_PRIORITY}</li>
	 * <li>{@link Priority#VERY_HIGH_PRIORITY}</li>
	 * <li>{@link Priority#HIGH_PRIORITY}</li>
	 * <li>{@link Priority#NORMAL_PRIORITY}</li>
	 * <li>{@link Priority#LOW_PRIORITY}</li>
	 * <li>{@link Priority#VERY_LOW_PRIORITY}</li>
	 * <li>{@link Priority#LAST_PRIORITY}</li>
	 * </ul>
	 * 
	 * @see org.scijava.service.Service
	 */
	double priority() default Priority.NORMAL_PRIORITY;

	/**
	 * Whether the plugin can be selected in the user interface. A plugin's
	 * selection state (if any) is typically rendered in the menu structure using
	 * a checkbox or radio button menu item (see {@link #selectionGroup}).
	 */
	boolean selectable() default false;

	/**
	 * For selectable plugins, specifies a name defining a group of linked
	 * plugins, only one of which is selected at any given time. Typically this is
	 * rendered in the menu structure as a group of radio button menu items. If no
	 * group is given, the plugin is assumed to be a standalone toggle, and
	 * typically rendered as a checkbox menu item.
	 */
	String selectionGroup() default "";

	/**
	 * When false, the plugin is grayed out in the user interface, if applicable.
	 */
	boolean enabled() default true;

	/** When false, the plugin is not displayed in the user interface. */
	boolean visible() default true;

	/**
	 * Provides a "hint" as to whether the plugin would behave correctly in a
	 * headless context.
	 * <p>
	 * Plugin developers should not specify {@code headless = true} unless the
	 * plugin refrains from using any UI-specific features (e.g., AWT or Swing
	 * calls).
	 * </p>
	 * <p>
	 * Of course, merely setting this flag does not guarantee that the plugin will
	 * not invoke any headless-incompatible functionality, but it provides an
	 * extra safety net for downstream headless code that wishes to be
	 * conservative in which plugins it allows to execute.
	 * </p>
	 */
	boolean headless() default false;
	
	/**
	 * When true, if this plugin is an {@link org.scijava.service.Service},
	 * the context will not try to load this service unless explicitly requested.
	 * <p>
	 * NB: Annotating a service field the {@link org.scijava.plugin.Parameter}
	 * annotation will cause that service to be loaded.
	 * </p>
	 */
	boolean lazy() default false;

	/** Defines a function that is called to initialize the plugin in some way. */
	String initializer() default "";

	/**
	 * A list of additional attributes which can be used to extend this annotation
	 * beyond its built-in capabilities.
	 */
	Attr[] attrs() default {};

}
