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

package org.scijava.menu;

import org.scijava.UIDetails;
import org.scijava.module.ModuleInfo;
import org.scijava.service.SciJavaService;

/**
 * Interface for service that tracks the application's menu structure.
 * 
 * @author Curtis Rueden
 */
public interface MenuService extends SciJavaService {

	/** Gets the root node of the application menu structure. */
	default ShadowMenu getMenu() {
		return getMenu(UIDetails.APPLICATION_MENU_ROOT);
	}

	/**
	 * Gets the root node of a menu structure.
	 * 
	 * @param menuRoot the root of the desired menu structure (see
	 *          {@link ModuleInfo#getMenuRoot()}).
	 */
	ShadowMenu getMenu(String menuRoot);

	/**
	 * Populates a UI-specific application menu structure.
	 * 
	 * @param creator the {@link MenuCreator} to use to populate the menus.
	 * @param menu the destination menu structure to populate.
	 */
	default <T> T createMenus(final MenuCreator<T> creator, final T menu) {
		return createMenus(UIDetails.APPLICATION_MENU_ROOT, creator, menu);
	}

	/**
	 * Populates a UI-specific menu structure.
	 * 
	 * @param menuRoot the root of the menu structure to generate (see
	 *          {@link ModuleInfo#getMenuRoot()}).
	 * @param creator the {@link MenuCreator} to use to populate the menus.
	 * @param menu the destination menu structure to populate.
	 */
	default <T> T createMenus(final String menuRoot,
		final MenuCreator<T> creator, final T menu)
	{
		creator.createMenus(getMenu(menuRoot), menu);
		return menu;
	}
}
