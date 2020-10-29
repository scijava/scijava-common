/*-
 * #%L
 * SciJava Common shared library for SciJava software.
 * %%
 * Copyright (C) 2009 - 2020 SciJava developers.
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

	public static boolean isStyle(String widgetStyle, String target) {
		if (widgetStyle == null || target == null)
			return widgetStyle == target;
		for (final String s : widgetStyle.split(",")) {
			if (s.trim().toLowerCase().equals(target.toLowerCase())) return true;
		}
		return false;
	}

	public static boolean isStyle(ModuleItem<?> item, String target) {
		return isStyle(item.getWidgetStyle(), target);
	}

	public static String[] getStyleModifiers(String widgetStyle, String target) {
		if (widgetStyle == null || target == null)
			return null;
		String[] styles = widgetStyle.split(",");
		for (String s : styles) {
			if (s.trim().toLowerCase().startsWith(target.toLowerCase())) {
				String suffix = s.split(":")[1];
				return suffix.split("/");
			}
		}
		return null;
	}
}
