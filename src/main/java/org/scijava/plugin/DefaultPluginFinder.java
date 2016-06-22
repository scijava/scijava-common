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

package org.scijava.plugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.scijava.annotations.Index;
import org.scijava.annotations.IndexItem;

/**
 * Default SciJava plugin discovery mechanism.
 * <p>
 * It works by scanning the classpath for {@link Plugin} annotations
 * previously indexed by scijava-common itself.
 * </p>
 * 
 * @author Curtis Rueden
 */
public class DefaultPluginFinder implements PluginFinder {

	/** Class loader to use when querying the annotation indexes. */
	private final ClassLoader customClassLoader;

	private final PluginBlacklist blacklist;

	// -- Constructors --

	public DefaultPluginFinder() {
		this(null);
	}

	public DefaultPluginFinder(final ClassLoader classLoader) {
		customClassLoader = classLoader;
		blacklist = new SysPropBlacklist();
	}

	// -- PluginFinder methods --

	@Override
	public HashMap<String, Throwable> findPlugins(
		final List<PluginInfo<?>> plugins)
	{
		final HashMap<String, Throwable> exceptions =
			new HashMap<>();

		// load the annotation indexes
		final ClassLoader classLoader = getClassLoader();
		final Index<Plugin> annotationIndex =
			Index.load(Plugin.class, classLoader);

		// create a PluginInfo object for each item in the index
		for (final IndexItem<Plugin> item : annotationIndex) {
			if (blacklist.contains(item.className())) continue;
			try {
				final PluginInfo<?> info = createInfo(item, classLoader);
				plugins.add(info);
			}
			catch (final Throwable t) {
				exceptions.put(item.className(), t);
			}
		}

		return exceptions;
	}

	// -- Helper methods --

	private PluginInfo<SciJavaPlugin> createInfo(
		final IndexItem<Plugin> item, final ClassLoader classLoader)
	{
		final String className = item.className();
		final Plugin plugin = item.annotation();

		@SuppressWarnings("unchecked")
		final Class<SciJavaPlugin> pluginType =
			(Class<SciJavaPlugin>) plugin.type();

		return new PluginInfo<>(className, pluginType, plugin, classLoader);
	}

	private ClassLoader getClassLoader() {
		if (customClassLoader != null) return customClassLoader;
		return Thread.currentThread().getContextClassLoader();
	}

	// -- Helper classes --

	private interface PluginBlacklist {
		boolean contains(String className);
	}

	/**
	 * A blacklist defined by the {@code scijava.plugin.blacklist} system
	 * property, formatted as a colon-separated list of regexes.
	 * <p>
	 * If a plugin class matches any of the regexes, it is excluded from the
	 * plugin index.
	 * </p>
	 */
	private class SysPropBlacklist implements PluginBlacklist {
		private final List<Pattern> patterns;

		public SysPropBlacklist() {
			final String sysProp = System.getProperty("scijava.plugin.blacklist");
			final String[] regexes = //
				sysProp == null ? new String[0] : sysProp.split(":");
			patterns = new ArrayList<>(regexes.length);
			for (final String regex : regexes) {
				try {
					patterns.add(Pattern.compile(regex));
				}
				catch (final PatternSyntaxException exc) {
					// NB: Ignore this malformed pattern.
				}
			}
		}

		// -- PluginBlacklist methods --

		@Override
		public boolean contains(final String className) {
			for (final Pattern pattern : patterns) {
				if (pattern.matcher(className).matches()) return true;
			}
			return false;
		}
	}

}
