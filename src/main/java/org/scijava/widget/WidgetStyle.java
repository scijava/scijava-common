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
