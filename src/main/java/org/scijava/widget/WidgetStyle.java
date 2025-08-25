/*-
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

import org.scijava.module.ModuleItem;

public class WidgetStyle {

	private WidgetStyle() {
		// prevent instantiation of utility class
	}

	/**
	 * Check whether a given widget style contains the target style.
	 *
	 * @param widgetStyle The style declaration to test, usually a comma-separated
	 *          {@code String}; trailing spaces are ignored.
	 * @param target The style being checked, case-insensitive.
	 * @return {@code true} if the target style matches.
	 */
	public static boolean isStyle(String widgetStyle, String target) {
		if (widgetStyle == null || target == null) return widgetStyle == target;
		for (final String s : widgetStyle.split(",")) {
			if (s.trim().toLowerCase().equals(target.toLowerCase())) return true;
		}
		return false;
	}

	/**
	 * Check whether a given {@link ModuleItem} has the target style.
	 *
	 * @param item The module item to test.
	 * @param target The style being checked, case-insensitive.
	 * @return {@code true} if the module item has the target style.
	 */
	public static boolean isStyle(ModuleItem<?> item, String target) {
		return isStyle(item.getWidgetStyle(), target);
	}

	/**
	 * Get the modifying value for a given style attribute in a style declaration.
	 * <p>
	 * For example, for {@code style="format:#0.00"}, this will return
	 * {@code "#0.00"}.
	 * </p>
	 *
	 * @param widgetStyle The style declaration string, e.g.
	 *          <code>"format:#0.00"</code>.
	 * @param target The target style attribute, e.g. <code>"format"</code>.
	 * @return The modifier for the given target, e.g. <code>"#0.00"</code>.
	 */
	public static String getStyleModifier(String widgetStyle, String target) {
		if (widgetStyle == null || target == null) return null;
		String[] styles = widgetStyle.split(",");
		for (String s : styles) {
			if (s.trim().toLowerCase().startsWith(target.toLowerCase())) {
				return s.split(":")[1];
			}
		}
		return null;
	}

	/**
	 * Get an array of all modifying values for a given style attribute.
	 * <p>
	 * For example, for {@code style="extensions:png/gif/bmp"}, this will return
	 * {@code ["png", "gif", "bmp"]}.
	 * </p>
	 *
	 * @param widgetStyle The style declaration string, e.g.
	 *          <code>"extensions:png/gif/bmp"</code>.
	 * @param target The target style attribute, e.g. <code>"extensions"</code>.
	 * @return An array of modifiers for the given target, e.g.
	 *         <code>["png", "gif", "bmp"]</code>.
	 */
	public static String[] getStyleModifiers(String widgetStyle, String target) {
		String suffix = getStyleModifier(widgetStyle, target);
		if (suffix == null) return null;
		return suffix.split("/");
	}

}
