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
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.scijava.AbstractContextual;
import org.scijava.Context;
import org.scijava.MenuPath;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;
import org.scijava.util.FileUtils;

/**
 * Discovers scripts.
 * <p>
 * To accomplish this, we crawl the directories specified by
 * {@link ScriptService#getScriptDirectories()}. By default, those directories
 * include the {@code scripts} and {@code plugins/Scripts} folders off the
 * SciJava application's base directory.
 * </p>
 * 
 * @author Johannes Schindelin
 * @author Curtis Rueden
 */
public class ScriptFinder extends AbstractContextual {

	private static final String SCRIPT_ICON = "/icons/script_code.png";

	@Parameter
	private ScriptService scriptService;

	@Parameter
	private LogService log;

	private final String pathPrefix;

	/**
	 * Creates a new script finder.
	 * 
	 * @param context The SciJava application context housing needed services.
	 */
	public ScriptFinder(final Context context) {
		this(context, ScriptService.SCRIPTS_RESOURCE_DIR);
	}

	/**
	 * Creates a new script finder.
	 * 
	 * @param context The SciJava application context housing needed services.
	 * @param pathPrefix the path prefix beneath which to scan classpath
	 *          resources, or null to skip classpath scanning.
	 */
	public ScriptFinder(final Context context, final String pathPrefix) {
		setContext(context);
		this.pathPrefix = pathPrefix;
	}

	// -- ScriptFinder methods --

	/**
	 * Discovers the scripts.
	 * 
	 * @param scripts The collection to which the discovered scripts are added.
	 */
	public void findScripts(final List<ScriptInfo> scripts) {
		final List<File> directories = scriptService.getScriptDirectories();

		final Set<URL> urls = new HashSet<>();
		int scriptCount = 0;

		scriptCount += scanResources(scripts, urls);

		// NB: We use a separate call to findResources for each directory so that
		// we can distinguish which URLs came from each directory, because each
		// directory may have a different menu prefix.
		for (final File dir : directories) {
			scriptCount += scanDirectory(scripts, urls, dir);
		}

		log.debug("Found " + scriptCount + " scripts");
	}

	// -- Helper methods --

	/** Scans classpath resources for scripts (e.g., inside JAR files). */
	private int scanResources(final List<ScriptInfo> scripts, final Set<URL> urls) {
		if (pathPrefix == null) return 0;

		// NB: We leave the baseDirectory argument null, because scripts on disk
		// will be picked up in the subsequent logic, which handles multiple
		// script directories rather than being limited to a single one.
		final Map<String, URL> scriptMap = //
			FileUtils.findResources(null, pathPrefix, null);

		return createInfos(scripts, urls, scriptMap, null);
	}

	/** Scans a directory for scripts. */
	private int scanDirectory(final List<ScriptInfo> scripts, final Set<URL> urls,
		final File dir)
	{
		if (!dir.exists()) {
			final String path = dir.getAbsolutePath();
			log.debug("Ignoring non-existent scripts directory: " + path);
			return 0;
		}
		final MenuPath menuPrefix = scriptService.getMenuPrefix(dir);

		try {
			final Set<URL> dirURL = Collections.singleton(dir.toURI().toURL());
			final Map<String, URL> scriptMap = //
				FileUtils.findResources(null, dirURL);

			return createInfos(scripts, urls, scriptMap, menuPrefix);
		}
		catch (final MalformedURLException exc) {
			log.error("Invalid script directory: " + dir, exc);
			return 0;
		}
	}

	private int createInfos(final List<ScriptInfo> scripts, final Set<URL> urls,
		final Map<String, URL> scriptMap, final MenuPath menuPrefix)
	{
		int scriptCount = 0;
		for (final String path : scriptMap.keySet()) {
			if (!scriptService.canHandleFile(path)) {
				log.debug("Ignoring unsupported script: " + path);
				continue;
			}

			final int dot = path.lastIndexOf('.');
			final String basePath = dot <= 0 ? path : path.substring(0, dot);
			final String friendlyPath = basePath.replace('_', ' ');

			final MenuPath menuPath = new MenuPath(menuPrefix);
			menuPath.addAll(new MenuPath(friendlyPath, "/", false));

			// E.g.:
			// path = "File/Import/Movie_File....groovy"
			// basePath = "File/Import/Movie_File..."
			// friendlyPath = "File/Import/Movie File..."
			// menuPath = File > Import > Movie File...

			// NB: Ignore base-level scripts (not nested in any menu).
			if (menuPath.size() == 1) continue;

			final URL url = scriptMap.get(path);

			// NB: Skip scripts whose URLs have already been added.
			if (urls.contains(url)) continue;
			urls.add(url);

			try {
				final ScriptInfo info = new ScriptInfo(getContext(), url, path);

				info.setMenuPath(menuPath);

				// flag script with special icon
				menuPath.getLeaf().setIconPath(SCRIPT_ICON);

				scripts.add(info);
				scriptCount++;
			}
			catch (final IOException exc) {
				log.error("Invalid script URL: " + url, exc);
			}
		}
		return scriptCount;
	}

	// -- Deprecated methods --

	/** @deprecated Use {@link #ScriptFinder(Context)} instead. */
	@Deprecated
	public ScriptFinder(final ScriptService scriptService) {
		this(scriptService.context());
	}

}
