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

package org.scijava.script;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.script.ScriptEngineFactory;

import org.scijava.log.LogService;
import org.scijava.util.ClassUtils;
import org.scijava.util.FileUtils;

/**
 * Data structure for managing registered scripting languages.
 * 
 * @author Johannes Schindelin
 * @author Curtis Rueden
 */
public class ScriptLanguageIndex extends ArrayList<ScriptLanguage> {

	private static final long serialVersionUID = 1L;

	private final Map<String, ScriptLanguage> byExtension =
		new HashMap<>();

	private final Map<String, ScriptLanguage> byName =
		new HashMap<>();

	private final LogService log;

	@Deprecated
	public ScriptLanguageIndex() {
		this(null);
	}

	/**
	 * Instantiates an index of the available script languages.
	 * 
	 * @param log the log service for errors and warnings
	 */
	public ScriptLanguageIndex(final LogService log) {
		this.log = log;
	}

	public boolean add(final ScriptEngineFactory factory, final boolean gently) {
		boolean result = false;

		final ScriptLanguage language = wrap(factory);

		// add language names
		result |= put("name", byName, language.getLanguageName(), language, gently);
		for (final String name : language.getNames()) {
			result |= put("name", byName, name, language, gently);
		}

		// add file extensions
		for (final String extension : language.getExtensions()) {
			if ("".equals(extension)) continue;
			result |= put("extension", byExtension, extension, language, gently);
		}

		result |= super.add(language);
		return result;
	}

	public ScriptLanguage getByExtension(final String extension) {
		return byExtension.get(extension);
	}

	public ScriptLanguage getByName(final String name) {
		return byName.get(name);
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

	private boolean put(final String type, final Map<String, ScriptLanguage> map,
		final String key, final ScriptLanguage value, final boolean gently)
	{
		final ScriptLanguage existing = map.get(key);

		if (existing == value) {
			// Duplicate key/value pair; do not overwrite.
			if (log != null && log.isDebug()) {
				// In debug mode, warn about the duplicate (since it is atypical).
				log.debug(overwriteMessage(false, type, key, value, existing));
			}
			return false;
		}

		if (existing != null) {
			// Conflicting value; behavior depends on mode.
			if (gently) {
				// Do not overwrite the previous value.
				if (log != null && log.isWarn()) {
					log.warn(overwriteMessage(false, type, key, value, existing));
				}
				return false;
			}
			if (log != null && log.isDebug()) {
				// In debug mode, warn about overwriting.
				log.debug(overwriteMessage(true, type, key, value, existing));
			}
		}

		map.put(key, value);
		return true;
	}

	/** Helper method of {@link #add(ScriptEngineFactory, boolean)}. */
	private ScriptLanguage wrap(final ScriptEngineFactory factory) {
		if (factory instanceof ScriptLanguage) return (ScriptLanguage) factory;
		return new AdaptedScriptLanguage(factory);
	}

	/** Helper method of {@link #put}. */
	private String overwriteMessage(final boolean overwrite, final String type,
		final String key, final ScriptLanguage proposed,
		final ScriptLanguage existing)
	{
		return (overwrite ? "Overwriting " : "Not overwriting ") + //
			type + " '" + key + "':\n" + //
			"\tproposed = " + details(proposed) + "\n" +
			"\texisting = " + details(existing);
	}

	/** Helper method of {@link #overwriteMessage}. */
	private String details(final ScriptLanguage language) {
		final Class<?> c = language.getClass();
		return c.getName() + " [" + ClassUtils.getLocation(c);
	}
}
