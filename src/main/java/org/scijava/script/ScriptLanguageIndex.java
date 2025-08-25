/*
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

package org.scijava.script;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.script.ScriptEngineFactory;

import org.scijava.log.LogService;
import org.scijava.util.FileUtils;

/**
 * Data structure for managing registered scripting languages.
 *
 * @author Johannes Schindelin
 * @author Curtis Rueden
 */
public class ScriptLanguageIndex extends ArrayList<ScriptLanguage> {

	private static final long serialVersionUID = 1L;

	private final Map<String, List<ScriptLanguage>> byExtension = new HashMap<>();

	private final Map<String, List<ScriptLanguage>> byName = new HashMap<>();

	private final LogService log;

	/**
	 * Instantiates an index of the available script languages.
	 *
	 * @param log the log service for errors and warnings
	 */
	public ScriptLanguageIndex(final LogService log) {
		this.log = log;
	}

	/**
	 * @deprecated Gently flag is no longer respected. Use
	 *             {@link #add(ScriptLanguage)}.
	 */
	@Deprecated
	boolean add(final ScriptEngineFactory factory, final boolean gently) {
		boolean result = false;

		final ScriptLanguage language = wrap(factory);

		// add language names
		result |= put(byName, language.getLanguageName(), language, gently);
		for (final String name : language.getNames()) {
			result |= put(byName, name, language, gently);
		}

		// add file extensions
		for (final String extension : language.getExtensions()) {
			if ("".equals(extension)) continue;
			result |= put(byExtension, extension, language, gently);
		}

		result |= super.add(language);
		return result;
	}

	public ScriptLanguage getByExtension(final String extension) {
		List<ScriptLanguage> langs = byExtension.get(extension);
		if (langs == null) return null;
		for (ScriptLanguage lang : langs) {
			if (lang.isActive()) return lang;
		}
		return null;
	}

	public ScriptLanguage getByName(final String name) {
		List<ScriptLanguage> langs = byName.get(name);
		if (langs == null) return null;
		for (ScriptLanguage lang : langs) {
			if (lang.isActive()) return lang;
		}
		return null;
	}

	public String[] getFileExtensions(final ScriptLanguage language) {
		final List<String> extensions = language.getExtensions();
		return extensions.toArray(new String[extensions.size()]);
	}

	public boolean canHandleFile(final File file) {
		final String extension = FileUtils.getExtension(file);
		if ("".equals(extension)) return false;
		return byExtension.containsKey(extension);
	}

	public boolean canHandleFile(final String fileName) {
		final String extension = FileUtils.getExtension(fileName);
		if ("".equals(extension)) return false;
		return byExtension.containsKey(extension);
	}

	// -- Collection methods --

	@Override
	public boolean add(final ScriptLanguage language) {
		return add(language, false);
	}

	// -- Helper methods --

	private boolean put(final Map<String, List<ScriptLanguage>> map,
		final String key, final ScriptLanguage value, final boolean gently)
	{
		List<ScriptLanguage> list = map.get(key);
		if (list == null) {
			list = new ArrayList<>();
			map.put(key, list);
		}
		return list.add(value);
	}

	/** Helper method of {@link #add(ScriptEngineFactory, boolean)}. */
	private ScriptLanguage wrap(final ScriptEngineFactory factory) {
		if (factory instanceof ScriptLanguage) return (ScriptLanguage) factory;
		return new AdaptedScriptLanguage(factory);
	}
}
