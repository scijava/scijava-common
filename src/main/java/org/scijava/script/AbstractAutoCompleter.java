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

package org.scijava.script;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;

/**
 * Abstract base class for {@link AutoCompleter} implementations.
 * 
 * @author Hadrien Mary
 */
public abstract class AbstractAutoCompleter implements AutoCompleter {

	@SuppressWarnings("unused")
	private ScriptLanguage scriptLanguage;

	public AbstractAutoCompleter(final ScriptLanguage scriptLanguage) {
		this.scriptLanguage = scriptLanguage;
	}

	@Override
	public AutoCompletionResult autocomplete(final String code, final int index,
		final ScriptEngine engine)
	{

		final List<String> matches = new ArrayList<>();
		final int startIndex = 0;

		if (code.endsWith(".")) {
			// Autocompletion with all the attributes of the object
			matches.addAll(engineAttributesCompleter(code, index, engine));

		}
		else if (code.contains(".")) {
			final List<String> codeList = Arrays.asList(code.split("\\."));
			final String objectString = codeList.get(codeList.size() - 2);
			final String fieldBeginWith = codeList.get(codeList.size() - 1);
			matches.addAll(engineAttributesCompleter(objectString + ".",
				fieldBeginWith, index, engine));

		}
		else {
			// Autocompletion with variables in the engine scope
			matches.addAll(engineVariablesCompleter(code, index, engine));
		}

		// Remove duplicates
		matches.stream().distinct().collect(Collectors.toList());

		// Sort alphabetically, ignoring case
		Collections.sort(matches, new Comparator<Object>() {

			@Override
			public int compare(final Object o1, final Object o2) {
				final String s1 = (String) o1;
				final String s2 = (String) o2;
				return s1.toLowerCase().compareTo(s2.toLowerCase());
			}
		});

		// Return results. For now we ignore index and startIndex.
		return new AutoCompletionResult(matches, startIndex);
	}

	private List<String> engineVariablesCompleter(final String code,
		@SuppressWarnings("unused") final int index, final ScriptEngine engine)
	{
		final List<String> matches = new ArrayList<>();

		final Bindings bindings = engine.getBindings(ScriptContext.ENGINE_SCOPE);

		for (final String key : bindings.keySet()) {
			if (key.toLowerCase().startsWith(code.toLowerCase())) {
				matches.add(key);
			}
		}
		return matches;

	}

	private List<String> engineAttributesCompleter(final String objectString,
		final int index, final ScriptEngine engine)
	{
		return engineAttributesCompleter(objectString, "", index, engine);
	}

	private List<String> engineAttributesCompleter(final String objectString,
		final String fieldBeginWith, @SuppressWarnings("unused") final int index,
		final ScriptEngine engine)
	{
		final List<String> matches = new ArrayList<>();

		final Bindings bindings = engine.getBindings(ScriptContext.ENGINE_SCOPE);

		for (final String key : bindings.keySet()) {
			if (objectString.endsWith(key + ".")) {
				final Object obj = bindings.get(key);
				for (final Field field : obj.getClass().getDeclaredFields()) {
					if (field.getName().toLowerCase().startsWith(fieldBeginWith
						.toLowerCase()))
					{
						matches.add(objectString + field.getName());
					}
				}
			}
		}

		return matches;
	}
}
