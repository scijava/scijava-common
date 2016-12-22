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

package org.scijava;

import org.scijava.util.ClassUtils;
import org.scijava.util.MiscUtils;

/**
 * An interface defining details useful for generating relevant user interface
 * elements.
 * 
 * @author Curtis Rueden
 */
public interface UIDetails extends BasicDetails, Prioritized {

	/**
	 * The default, application-level menu root.
	 * 
	 * @see #getMenuRoot()
	 */
	String APPLICATION_MENU_ROOT = "app";

	/**
	 * Gets an appropriate title for the object, for use in a user interface. The
	 * result is prioritized as follows:
	 * <ol>
	 * <li>Item label</li>
	 * <li>Menu path's leaf entry name</li>
	 * <li>Item name</li>
	 * <li>Item's class name, without package prefix</li>
	 * </ol>
	 */
	default String getTitle() {
		// use object label, if available
		if (getLabel() != null && !getLabel().isEmpty()) return getLabel();

		// use name of leaf menu item, if available
		final MenuPath menuPath = getMenuPath();
		if (menuPath != null && menuPath.size() > 0) {
			final MenuEntry menuLeaf = menuPath.getLeaf();
			final String menuName = menuLeaf.getName();
			if (menuName != null && !menuName.isEmpty()) return menuName;
		}

		// use object name, if available
		if (getName() != null && !getName().isEmpty()) return getName();

		// use the unique identifier, if available
		if (this instanceof Identifiable) {
			final String id = ((Identifiable) this).getIdentifier();
			if (id != null) return id;
		}

		// use class name as a last resort
		return getClass().getSimpleName();
	}

	/** Gets the path to the object's suggested position in the menu structure. */
	MenuPath getMenuPath();

	/** Gets the name of the menu structure to which the object belongs. */
	String getMenuRoot();

	/** Gets the resource path to an icon representing the object. */
	String getIconPath();

	/**
	 * Gets whether the object can be selected (e.g., checking and unchecking its
	 * menu item) in the user interface.
	 */
	boolean isSelectable();

	/**
	 * Gets the name of the selection group to which the object belongs. Only one
	 * object in a particular selection group can be selected at a time.
	 */
	String getSelectionGroup();

	/**
	 * Gets whether the object is selected (e.g., its menu item is checked) in the
	 * user interface.
	 */
	boolean isSelected();

	/** Gets whether the object should be enabled in the user interface. */
	boolean isEnabled();

	/** Gets whether the object should be visible in the user interface. */
	boolean isVisible();

	/** Sets the path to the object's suggested position in the menu structure. */
	void setMenuPath(MenuPath menuPath);

	/** Sets the name of the menu structure to which the object belongs. */
	void setMenuRoot(String menuRoot);

	/** Sets the resource path to an icon representing the object. */
	void setIconPath(String iconPath);

	/** Sets whether the object should be enabled in the user interface. */
	void setEnabled(boolean enabled);

	/** Sets whether the object should be visible in the user interface. */
	void setVisible(boolean visible);

	/**
	 * Sets whether the object can be selected (e.g., checking and unchecking its
	 * menu item) in the user interface.
	 */
	void setSelectable(boolean selectable);

	/**
	 * Sets the name of the selection group to which the object belongs. Only one
	 * object in a particular selection group can be selected at a time.
	 */
	void setSelectionGroup(String selectionGroup);

	/**
	 * Sets whether the object is selected (e.g., its menu item is checked) in the
	 * user interface.
	 */
	void setSelected(boolean selected);

	// -- Comparable methods --

	@Override
	default int compareTo(final Prioritized that) {
		if (that == null) return 1;

		// compare priorities
		final int priorityCompare = Priority.compare(this, that);
		if (priorityCompare != 0) return priorityCompare;

		// compare classes
		final int classCompare = ClassUtils.compare(getClass(), that.getClass());
		if (classCompare != 0) return classCompare;

		if (!(that instanceof UIDetails)) return 1;
		final UIDetails uiDetails = (UIDetails) that;

		// compare names
		final String thisName = getName();
		final String thatName = uiDetails.getName();
		final int nameCompare = MiscUtils.compare(thisName, thatName);
		if (nameCompare != 0) return nameCompare;

		// compare titles
		final String thisTitle = getTitle();
		final String thatTitle = uiDetails.getTitle();
		return MiscUtils.compare(thisTitle, thatTitle);
	}
}
