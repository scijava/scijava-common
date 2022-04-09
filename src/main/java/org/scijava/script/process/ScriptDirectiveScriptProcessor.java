/*
 * #%L
 * SciJava Common shared library for SciJava software.
 * %%
 * Copyright (C) 2009 - 2022 SciJava developers.
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

package org.scijava.script.process;

import java.util.Map;

import org.scijava.MenuPath;
import org.scijava.Priority;
import org.scijava.log.LogService;
import org.scijava.module.ModuleInfo;
import org.scijava.module.ModuleService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 * A {@link ScriptProcessor} which parses the {@code #@script} directive.
 * <p>
 * The syntax is:
 * </p>
 * 
 * <pre>
 * #@script(key1=value1, key2=value2, ...)
 * </pre>
 * <p>
 * Supported keys include:
 * </p>
 * <ul>
 * <li>{@code name} - The name of the script.</li>
 * <li>{@code label} - The human-readable label to use (e.g., in the menu
 * structure).</li>
 * <li>{@code description} - A longer description of the script (e.g., for use
 * as a tool tip).</li>
 * <li>{@code menuPath} - Abbreviated menu path defining where the script is
 * shown in the menu structure. Use greater than sign ({@code >}) as a
 * separator.</li>
 * <li>{@code menuRoot} - String identifier naming the menu to which this script
 * belongs.</li>
 * <li>{@code iconPath} - Path to the plugin's icon (e.g., shown in the menu
 * structure).</li>
 * <li>{@code priority} - Priority of the script. Larger values are higher
 * priority. Value can be written as a {@code double} constant, or as one of the
 * following convenient shorthands: {@code first}, {@code extremely-high},
 * {@code very-high}, {@code high}, {@code normal}, {@code low},
 * {@code very-low}, {@code extremely-low}, {@code last}.</li>
 * <li>{@code headless} - Provides a "hint" as to whether the script would
 * behave correctly in a headless context. Do <em>not</em> specify
 * {@code headless = true} unless the script refrains from using any UI-specific
 * features (e.g., AWT or Swing calls).</li>
 * </ul>
 * <p>
 * Any other key-value pairs encountered are stored as properties via the
 * {@link ModuleInfo#set(String, String)} method.
 * </p>
 * <p>
 * See also the @{@link Plugin} annotation, which mostly lines up with this list
 * of attributes.
 * </p>
 * <p>
 * Here are a few examples:
 * </p>
 * <ul>
 * <li>{@code #@script(name = "extra-functions")}</li>
 * <li>{@code #@script(headless = true)}</li>
 * <li>{@code #@script(menuPath = "Image > Import > Text...")}</li>
 * </ul>
 * 
 * @author Curtis Rueden
 */
@Plugin(type = ScriptProcessor.class, priority=Priority.HIGH)
public class ScriptDirectiveScriptProcessor extends DirectiveScriptProcessor {

	public ScriptDirectiveScriptProcessor() {
		super(directive -> "script".equals(directive));
	}

	@Parameter
	private LogService log;
	
	@Parameter
	private ModuleService moduleService;

	// -- Internal DirectiveScriptProcessor methods --

	@Override
	protected String process(final String directive,
		final Map<String, Object> attrs, final String theRest)
	{
		for (final String k : attrs.keySet()) {
			assignAttribute(k == null ? "name" : k, attrs.get(k));
		}
		moduleService.addModule(info()); // TODO how to handle duplicate names?
		return "";
	}

	// -- Helper methods --

	private <T> void assignAttribute(final String k, final Object v) {
		if (is(k, "name")) info().setName(as(v, String.class));
		else if (is(k, "label")) info().setLabel(as(v, String.class));
		else if (is(k, "description")) info().setDescription(as(v, String.class));
		else if (is(k, "menuPath")) {
			info().setMenuPath(new MenuPath(as(v, String.class)));
		}
		else if (is(k, "menuRoot")) info().setMenuRoot(as(v, String.class));
		else if (is(k, "iconPath")) info().setIconPath(as(v, String.class));
		else if (is(k, "priority")) {
			final Double priority = priority(v);
			if (priority != null) info().setPriority(priority);
		}
		else if (is(k, "headless") && as(v, boolean.class)) {
			// NB: There is no ModuleInfo#setHeadless(boolean).
			// So we add a "headless" property; see ScriptInfo#canRunHeadless().
			info().set("headless", "true");
		}
		else info().set(k, v.toString());
	}

	private Double priority(final Object p) {
		final Double pDouble = as(p, Double.class);
		if (pDouble != null) return pDouble;

		final String pString = as(p, String.class);
		if (pString == null) return null;

		final String lString = pString.toLowerCase();
		if (lString.matches("first")) return Priority.FIRST;
		if (lString.matches("extremely[ _-]?high")) return Priority.EXTREMELY_HIGH;
		if (lString.matches("very[ _-]?high")) return Priority.VERY_HIGH;
		if (lString.matches("high")) return Priority.HIGH;
		if (lString.matches("normal")) return Priority.NORMAL;
		if (lString.matches("low")) return Priority.LOW;
		if (lString.matches("very[ _-]?low")) return Priority.VERY_LOW;
		if (lString.matches("extremely[ _-]?low")) return Priority.EXTREMELY_LOW;
		if (lString.matches("last")) return Priority.LAST;
		return null;
	}
}
