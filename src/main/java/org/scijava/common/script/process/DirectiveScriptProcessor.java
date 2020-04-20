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

package org.scijava.common.script.process;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.scijava.common.convert.ConvertService;
import org.scijava.common.parse.ParseService;
import org.scijava.common.plugin.Parameter;
import org.scijava.common.script.ScriptInfo;

/**
 * Abstract base class for {@link ScriptProcessor} plugins that parse lines
 * of the form {@code #@directive(...) ...}.
 * 
 * @author Curtis Rueden
 */
public abstract class DirectiveScriptProcessor implements ScriptProcessor {

	private final Pattern p = //
		Pattern.compile("^#@(\\w*)\\s*(\\((.*)\\))?\\s*(.*)$");

	@Parameter
	private ConvertService convertService;

	@Parameter
	private ParseService parser;

	private ScriptInfo info;

	private Predicate<String> directivesToMatch;

	public DirectiveScriptProcessor(final Predicate<String> directivesToMatch) {
		this.directivesToMatch = directivesToMatch;
	}

	// -- ScriptProcessor methods --

	@Override
	public void begin(final ScriptInfo scriptInfo) {
		info = scriptInfo;
	}

	@Override
	public String process(final String line) {
		// as quickly as possible, verify that this line is a directive
		if (!line.startsWith("#@")) return line;

		// parse the directive, and ensure it is well-formed
		final Matcher m = p.matcher(line);
		if (!m.matches()) return line;

		// ensure directive is relevant
		final String directive = m.group(1);
		if (!directivesToMatch.test(directive)) return line;

		// parse attributes (inner match without parentheses)
		final String attrString = m.group(3);
		final Map<String, Object> attrs = attrString == null ? //
			Collections.emptyMap() : parser.parse(attrString, false).asMap();

		// retain the rest of the string
		final String theRest = m.group(4);

		return process(directive, attrs, theRest);
	}

	// -- Internal methods --

	/** Processes the given directive. */
	protected abstract String process(final String directive,
		final Map<String, Object> attrs, final String theRest);

	/** Gets the active {@link ScriptInfo} instance. */
	protected ScriptInfo info() {
		return info;
	}

	/** Checks whether some key matches the desired value, ignoring case. */
	protected boolean is(final String key, final String desired) {
		return desired.equalsIgnoreCase(key);
	}

	/** Coerces some object into another object of the given type. */
	protected <T> T as(final Object v, final Class<T> type) {
		final T converted = convertService.convert(v, type);
		if (converted != null) return converted;
		// NB: Attempt to convert via string.
		// This is useful in cases where a weird type of object came back
		// (e.g., org.scijava.parse.eval.Unresolved), but which happens to have a
		// nice string representation which ultimately is expressible as the type.
		return convertService.convert(v.toString(), type);
	}

	/** Coerces some object into a list of objects of the given type. */
	protected <T> List<T> asList(final Object v, final Class<T> type) {
		final ArrayList<T> result = new ArrayList<>();
		final List<?> list = as(v, List.class);
		for (final Object item : list) {
			result.add(as(item, type));
		}
		return result;
	}
}
