/*
 * #%L
 * SciJava Common shared library for SciJava software.
 * %%
 * Copyright (C) 2009 - 2017 Board of Regents of the University of
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

package org.scijava.script.process;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.script.ScriptException;

import org.scijava.ItemIO;
import org.scijava.ItemVisibility;
import org.scijava.command.Command;
import org.scijava.convert.ConvertService;
import org.scijava.log.LogService;
import org.scijava.module.DefaultMutableModuleItem;
import org.scijava.parse.ParseService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.script.ScriptInfo;
import org.scijava.script.ScriptModule;
import org.scijava.script.ScriptService;

/**
 * A {@link ScriptProcessor} which parses the script's input and output
 * parameters from the script header.
 * <p>
 * SciJava's scripting framework supports specifying @{@link Parameter}-style
 * inputs and outputs in a preamble. The format is a simplified version of the
 * Java @{@link Parameter} annotation syntax. The following syntaxes are
 * supported:
 * </p>
 * <ul>
 * <li>{@code #@<type> <varName>}</li>
 * <li>{@code #@<type>(<attr1>=<value1>, ..., <attrN>=<valueN>) <varName>}</li>
 * <li>{@code #@<IOType> <type> <varName>}</li>
 * <li>{@code #@<IOType>(<attr1>=<value1>, ..., <attrN>=<valueN>) <type>
 * <varName>}</li>
 * </ul>
 * <p>
 * Where:
 * </p>
 * <ul>
 * <li>{@code #@} - signals a special script processing instruction, so that the
 * parameter line is ignored by the script engine itself.</li>
 * <li>{@code <IOType>} - one of {@code INPUT}, {@code OUTPUT}, or {@code BOTH}.
 * </li>
 * <li>{@code <varName>} - the name of the input or output variable.</li>
 * <li>{@code <type>} - the Java {@link Class} of the variable.</li>
 * <li>{@code <attr*>} - an attribute key.</li>
 * <li>{@code <value*>} - an attribute value.</li>
 * </ul>
 * <p>
 * See the @{@link Parameter} annotation for a list of valid attributes.
 * </p>
 * <p>
 * Here are a few examples:
 * </p>
 * <ul>
 * <li>{@code #@Dataset dataset}</li>
 * <li>{@code #@double(type=OUTPUT) result}</li>
 * <li>{@code #@BOTH ImageDisplay display}</li>
 * <li>{@code #@INPUT(persist=false, visibility=INVISIBLE) boolean verbose}
 * </li>
 * </ul>
 * <p>
 * Parameters will be parsed and filled just like @{@link Parameter}-annotated
 * fields in {@link Command}s.
 * </p>
 * 
 * @author Curtis Rueden
 */
@Plugin(type = ScriptProcessor.class)
public class ParameterScriptProcessor implements ScriptProcessor {

	@Parameter
	private ScriptService scriptService;

	@Parameter
	private ConvertService convertService;

	@Parameter
	private ParseService parser;

	@Parameter
	private LogService log;

	private ScriptInfo info;
	private boolean header = true;

	// -- ScriptProcessor methods --

	@Override
	public void begin(final ScriptInfo scriptInfo) {
		info = scriptInfo;
		info.setReturnValueAppended(true);
	}

	@Override
	public String process(final String line) {
		// parse new-style parameters starting with @# anywhere in the script.
		if (line.matches("^#@.*")) {
			final int at = line.indexOf('@');
			return process(line, line.substring(at + 1));
		}

		// parse old-style parameters in the initial script header
		if (header) {
			// NB: Check if line contains an '@' with no prior alphameric
			// characters. This assumes that only non-alphanumeric characters can
			// be used as comment line markers.
			if (line.matches("^[^\\w]*@.*")) {
				final int at = line.indexOf('@');
				return process(line, line.substring(at + 1));
			}
			else if (line.matches(".*\\w.*")) header = false;
		}

		return line;
	}

	@Override
	public void end() {
		if (info.isReturnValueAppended()) {
			// add an output for the value returned by the script itself
			final HashMap<String, Object> attrs = new HashMap<>();
			attrs.put("type", "OUTPUT");
			addItem(ScriptModule.RETURN_VALUE, Object.class, attrs, false);
		}
	}

	// -- Helper methods --

	private String process(final String line, final String param) {
		if (parseParam(param)) return "";
		log.warn("Ignoring invalid parameter: " + param);
		return line;
	}

	private boolean parseParam(final String param) {
		final int lParen = param.indexOf("(");
		final int rParen = param.lastIndexOf(")");
		if (rParen < lParen) return false;
		if (lParen < 0) return parseParam(param, parseAttrs("()"));
		final String cutParam =
			param.substring(0, lParen) + param.substring(rParen + 1);
		final String attrs = param.substring(lParen + 1, rParen);
		return parseParam(cutParam, parseAttrs(attrs));
	}

	private boolean parseParam(final String param,
		final Map<String, Object> attrs)
	{
		final String[] tokens = param.trim().split("[ \t\n]+");
		if (tokens.length < 1) return false;
		final String typeName, varName;
		final String maybeIOType = tokens[0].toUpperCase();
		if (isIOType(maybeIOType)) {
			// assume syntax: <IOType> <type> <varName>
			if (tokens.length < 3) return false;
			attrs.put("type", maybeIOType);
			typeName = tokens[1];
			varName = tokens[2];
		}
		else {
			// assume syntax: <type> <varName>
			if (tokens.length < 2) return false;
			typeName = tokens[0];
			varName = tokens[1];
		}
		try {
			final Class<?> type = scriptService.lookupClass(typeName);
			addItem(varName, type, attrs, true);
		}
		catch (final ScriptException exc) {
			log.warn("Invalid class: " + typeName, exc);
			return false;
		}

		if (ScriptModule.RETURN_VALUE.equals(varName)) {
			// NB: The return value variable is declared as an explicit parameter.
			// So we should not append the return value as an extra output.
			info.setReturnValueAppended(false);
		}

		return true;
	}

	/** Parses a comma-delimited list of {@code key=value} pairs into a map. */
	private Map<String, Object> parseAttrs(final String attrs) {
		return parser.parse(attrs, false).asMap();
	}

	private boolean isIOType(final String token) {
		return convertService.convert(token.toUpperCase(), ItemIO.class) != null;
	}

	private <T> void addItem(final String name, final Class<T> type,
		final Map<String, Object> attrs, final boolean explicit)
	{
		final DefaultMutableModuleItem<T> item =
			new DefaultMutableModuleItem<>(info, name, type);
		for (final String key : attrs.keySet()) {
			final Object value = attrs.get(key);
			assignAttribute(item, key, value);
		}
		if (item.isInput()) info.registerInput(item);
		if (item.isOutput()) {
			info.registerOutput(item);
			// NB: Only append the return value as an extra
			// output when no explicit outputs are declared.
			if (explicit) info.setReturnValueAppended(false);
		}
	}

	private <T> void assignAttribute(final DefaultMutableModuleItem<T> item,
		final String k, final Object v)
	{
		// CTR: There must be an easier way to do this.
		// Just compile the thing using javac? Or parse via javascript, maybe?
		if (is(k, "callback")) item.setCallback(as(v, String.class));
		else if (is(k, "choices")) item.setChoices(asList(v, item.getType()));
		else if (is(k, "columns")) item.setColumnCount(as(v, int.class));
		else if (is(k, "description")) item.setDescription(as(v, String.class));
		else if (is(k, "initializer")) item.setInitializer(as(v, String.class));
		else if (is(k, "validater")) item.setValidater(as(v, String.class));
		else if (is(k, "type")) item.setIOType(as(v, ItemIO.class));
		else if (is(k, "label")) item.setLabel(as(v, String.class));
		else if (is(k, "max")) item.setMaximumValue(as(v, item.getType()));
		else if (is(k, "min")) item.setMinimumValue(as(v, item.getType()));
		else if (is(k, "name")) item.setName(as(v, String.class));
		else if (is(k, "persist")) item.setPersisted(as(v, boolean.class));
		else if (is(k, "persistKey")) item.setPersistKey(as(v, String.class));
		else if (is(k, "required")) item.setRequired(as(v, boolean.class));
		else if (is(k, "softMax")) item.setSoftMaximum(as(v, item.getType()));
		else if (is(k, "softMin")) item.setSoftMinimum(as(v, item.getType()));
		else if (is(k, "stepSize")) item.setStepSize(as(v, double.class));
		else if (is(k, "style")) item.setWidgetStyle(as(v, String.class));
		else if (is(k, "visibility")) item.setVisibility(as(v, ItemVisibility.class));
		else if (is(k, "value")) item.setDefaultValue(as(v, item.getType()));
		else item.set(k, v.toString());
	}

	/** Super terse comparison helper method. */
	private boolean is(final String key, final String desired) {
		return desired.equalsIgnoreCase(key);
	}

	/** Super terse conversion helper method. */
	private <T> T as(final Object v, final Class<T> type) {
		final T converted = convertService.convert(v, type);
		if (converted != null) return converted;
		// NB: Attempt to convert via string.
		// This is useful in cases where a weird type of object came back
		// (e.g., org.scijava.parse.eval.Unresolved), but which happens to have a
		// nice string representation which ultimately is expressible as the type.
		return convertService.convert(v.toString(), type);
	}

	private <T> List<T> asList(final Object v, final Class<T> type) {
		final ArrayList<T> result = new ArrayList<>();
		final List<?> list = as(v, List.class);
		for (final Object item : list) {
			result.add(as(item, type));
		}
		return result;
	}

}
