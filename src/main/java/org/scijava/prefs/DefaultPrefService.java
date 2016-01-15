/*
 * #%L
 * SciJava Common shared library for SciJava software.
 * %%
 * Copyright (C) 2009 - 2015 Board of Regents of the University of
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

package org.scijava.prefs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.service.Service;

/**
 * Default {@link PrefService} implementation, which persists preferences to
 * disk using the {@link java.util.prefs.Preferences} API.
 *
 * @author Mark Hiner
 * @author Curtis Rueden
 * @author Grant Harris
 */
@Plugin(type = Service.class)
public class DefaultPrefService extends AbstractPrefService {

	@Parameter(required = false)
	private LogService log;

	// -- Global preferences --

	@Override
	public String get(final String name) {
		return get((Class<?>) null, name);
	}

	@Override
	public String get(final String name, final String defaultValue) {
		return get(null, name, defaultValue);
	}

	@Override
	public boolean getBoolean(final String name, final boolean defaultValue) {
		return getBoolean(null, name, defaultValue);
	}

	@Override
	public double getDouble(final String name, final double defaultValue) {
		return getDouble(null, name, defaultValue);
	}

	@Override
	public float getFloat(final String name, final float defaultValue) {
		return getFloat(null, name, defaultValue);
	}

	@Override
	public int getInt(final String name, final int defaultValue) {
		return getInt(null, name, defaultValue);
	}

	@Override
	public long getLong(final String name, final long defaultValue) {
		return getLong(null, name, defaultValue);
	}

	@Override
	public void put(final String name, final String value) {
		put(null, name, value);
	}

	@Override
	public void put(final String name, final boolean value) {
		put(null, name, value);
	}

	@Override
	public void put(final String name, final double value) {
		put(null, name, value);
	}

	@Override
	public void put(final String name, final float value) {
		put(null, name, value);
	}

	@Override
	public void put(final String name, final int value) {
		put(null, name, value);
	}

	@Override
	public void put(final String name, final long value) {
		put(null, name, value);
	}

	// -- Class-specific preferences --

	@Override
	public String get(final Class<?> c, final String name) {
		return get(c, name, null);
	}

	@Override
	public String get(final Class<?> c, final String name,
		final String defaultValue)
	{
		return prefs(c).get(key(c, name), defaultValue);
	}

	@Override
	public boolean getBoolean(final Class<?> c, final String name,
		final boolean defaultValue)
	{
		return prefs(c).getBoolean(key(c, name), defaultValue);
	}

	@Override
	public double getDouble(final Class<?> c, final String name,
		final double defaultValue)
	{
		return prefs(c).getDouble(key(c, name), defaultValue);
	}

	@Override
	public float getFloat(final Class<?> c, final String name,
		final float defaultValue)
	{
		return prefs(c).getFloat(key(c, name), defaultValue);
	}

	@Override
	public int
		getInt(final Class<?> c, final String name, final int defaultValue)
	{
		return prefs(c).getInt(key(c, name), defaultValue);
	}

	@Override
	public long getLong(final Class<?> c, final String name,
		final long defaultValue)
	{
		return prefs(c).getLong(key(c, name), defaultValue);
	}

	@Override
	public void put(final Class<?> c, final String name, final String value) {
		prefs(c).put(key(c, name), value);
	}

	@Override
	public void put(final Class<?> c, final String name, final boolean value) {
		prefs(c).putBoolean(key(c, name), value);
	}

	@Override
	public void put(final Class<?> c, final String name, final double value) {
		prefs(c).putDouble(key(c, name), value);
	}

	@Override
	public void put(final Class<?> c, final String name, final float value) {
		prefs(c).putFloat(key(c, name), value);
	}

	@Override
	public void put(final Class<?> c, final String name, final int value) {
		prefs(c).putInt(key(c, name), value);
	}

	@Override
	public void put(final Class<?> c, final String name, final long value) {
		prefs(c).putLong(key(c, name), value);
	}

	@Override
	public void clear(final Class<?> c) {
		prefs(c).clear();
	}

	// -- Other/unsorted --

	// TODO - Evaluate which of these methods are really needed, and which are
	// duplicate of similar functionality above.

	@Override
	public void clearAll() {
		for (final String name : allPrefs())
			prefs(name).removeNode();
	}

	@Override
	public void clear(final String key) {
		clear((Class<?>) null, key);
	}

	@Override
	public void clear(final Class<?> prefClass, final String key) {
		prefs(prefClass).clear(key);
	}

	@Override
	public void clear(final String absolutePath, final String key) {
		prefs(absolutePath).clear(key);
	}

	@Override
	public void remove(final Class<?> prefClass, final String key) {
		prefs(prefClass).remove(key);
	}

	@Override
	public void remove(final String absolutePath, final String key) {
		prefs(absolutePath).remove(key);
	}

	@Override
	public void putMap(final Map<String, String> map, final String key) {
		putMap((Class<?>) null, map, key);
	}

	@Override
	public void putMap(final Class<?> prefClass, final Map<String, String> map,
		final String key)
	{
		prefs(prefClass).node(key).putMap(map);
	}

	@Override
	public void putMap(final String absolutePath, final Map<String, String> map,
		final String key)
	{
		prefs(absolutePath).node(key).putMap(map);
	}

	@Override
	public void putMap(final Class<?> prefClass, final Map<String, String> map) {
		prefs(prefClass).putMap(map);
	}

	@Override
	public void putMap(final String absolutePath, final Map<String, String> map) {
		prefs(absolutePath).putMap(map);
	}

	@Override
	public Map<String, String> getMap(final String key) {
		return getMap((Class<?>) null, key);
	}

	@Override
	public Map<String, String> getMap(final Class<?> prefClass, final String key)
	{
		return prefs(prefClass).node(key).getMap();
	}

	@Override
	public Map<String, String>
		getMap(final String absolutePath, final String key)
	{
		return prefs(absolutePath).node(key).getMap();
	}

	@Override
	public Map<String, String> getMap(final Class<?> prefClass) {
		return prefs(prefClass).getMap();
	}

	@Override
	public void putList(final List<String> list, final String key) {
		putList((Class<?>) null, list, key);
	}

	@Override
	public void putList(final Class<?> prefClass, final List<String> list,
		final String key)
	{
		prefs(prefClass).node(key).putList(list);
	}

	@Override
	public void putList(final String absolutePath, final List<String> list,
		final String key)
	{
		prefs(absolutePath).node(key).putList(list);
	}

	@Override
	public void putList(final Class<?> prefClass, final List<String> list) {
		prefs(prefClass).putList(list);
	}

	@Override
	public void putList(final String absolutePath, final List<String> list) {
		prefs(absolutePath).putList(list);
	}

	@Override
	public List<String> getList(final String key) {
		return getList((Class<?>) null, key);
	}

	@Override
	public List<String> getList(final Class<?> prefClass, final String key) {
		return prefs(prefClass).node(key).getList();
	}

	@Override
	public List<String> getList(final String absolutePath, final String key) {
		return prefs(absolutePath).node(key).getList();
	}

	@Override
	public List<String> getList(final Class<?> prefClass) {
		return prefs(prefClass).getList();
	}

	@Override
	public Iterable<String> getIterable(final String key) {
		return getIterable((Class<?>) null, key);
	}

	@Override
	public Iterable<String> getIterable(final Class<?> prefClass, final String key) {
		return prefs(prefClass).node(key).getIterable();
	}

	@Override
	public void putIterable(final Iterable<String> iterable, final String key) {
		putIterable((Class<?>) null, iterable, key);
	}

	@Override
	public void putIterable(final Class<?> prefClass, final Iterable<String> iterable, final String key) {
		prefs(prefClass).node(key).node(key).putIterable(iterable);
	}

	// -- Helper methods --

	private static String key(final Class<?> c, final String name) {
		return c == null ? name : c.getSimpleName() + "." + name;
	}

	private SmartPrefs prefs(final Class<?> c) {
		return new SmartPrefs(java.util.prefs.Preferences.userNodeForPackage(
			c == null ? PrefService.class : c), log);
	}

	private SmartPrefs prefs(final String absolutePath) {
		return new SmartPrefs(java.util.prefs.Preferences.userRoot().node(
			absolutePath), log);
	}

	private String[] allPrefs() {
		try {
			return java.util.prefs.Preferences.userRoot().childrenNames();
		}
		catch (java.util.prefs.BackingStoreException exc) {
			log.error(exc);
			return new String[0];
		}
	}

	// -- Helper classes --

	/**
	 * Smart wrapper around {@link java.util.prefs.Preferences} which
	 * encapsulates, improves and enhances its behavior.
	 */
	private static class SmartPrefs {

		private final java.util.prefs.Preferences p;
		private final LogService log;

		public SmartPrefs(final java.util.prefs.Preferences p,
			final LogService log)
		{
			this.p = p;
			this.log = log;
		}

		// -- SmartPrefs methods --

		public void clear(final String key) {
			if (nodeExists(key)) node(key).clear();
		}

		public void remove(final String key) {
			if (nodeExists(key)) node(key).removeNode();
		}

		public void putMap(final Map<String, String> map) {
			final Iterator<Entry<String, String>> iter = map.entrySet().iterator();
			while (iter.hasNext()) {
				final Entry<String, String> entry = iter.next();
				final String key = entry.getKey().toString();
				final Object value = entry.getValue();
				put(key, value);
			}

		}

		public Map<String, String> getMap() {
			final Map<String, String> map = new HashMap<String, String>();
			final String[] keys = keys();
			for (int index = 0; index < keys.length; index++) {
				map.put(keys[index], get(keys[index]));
			}
			return map;
		}

		public void putList(final List<String> list) {
			for (int index = 0; list != null && index < list.size(); index++) {
				final Object value = list.get(index);
				put("" + index, value);
			}
		}

		public List<String> getList() {
			final List<String> list = new ArrayList<String>();
			for (int index = 0; index < 1000; index++) {
				final String value = get("" + index);
				if (value == null) {
					break;
				}
				list.add(value);
			}
			return list;
		}

		public void putIterable(final Iterable<String> iterable) {
			int index = 0;
			for (final String value : iterable) {
				put("" + index++, value);
			}
		}

		public Iterable<String> getIterable() {
			return new Iterable<String>() {
				@Override
				public Iterator<String> iterator() {
					return new Iterator<String>() {
						private String value;
						private int index;
						{
							findNext();
						}

						@Override
						public String next() {
							final String result = value;
							findNext();
							return result;
						}

						@Override
						public boolean hasNext() {
							return value != null;
						}

						@Override
						public void remove() {
							throw new UnsupportedOperationException();
						}

						private void findNext() {
							if (index < 0) return;
							value = get("" + index);
							index = value == null ? -1 : index + 1;
						}
					};
				}
			};
		}

		// -- Adapted Preferences methods --

		/** @see java.util.prefs.Preferences#put */
		public void put(final String key, final Object value) {
			p.put(key, value == null ? null : value.toString());
		}

		/** @see java.util.prefs.Preferences#get(String, String) */
		public String get(final String key) {
			return get(key, null);
		}

		/** @see java.util.prefs.Preferences#get(String, String) */
		public String get(final String key, final String def) {
			return p.get(key, def);
		}

		/** @see java.util.prefs.Preferences#clear() */
		public void clear() {
			try {
				p.clear();
			}
			catch (java.util.prefs.BackingStoreException exc) {
				log.error(exc);
			}
		}

		/** @see java.util.prefs.Preferences#putInt(String, int) */
		public void putInt(final String key, final int value) {
			p.putInt(key, value);
		}

		/** @see java.util.prefs.Preferences#getInt(String, int) */
		public int getInt(final String key, final int def) {
			return p.getInt(key, def);
		}

		/** @see java.util.prefs.Preferences#putLong(String, long) */
		public void putLong(final String key, final long value) {
			p.putLong(key, value);
		}

		/** @see java.util.prefs.Preferences#getLong(String, long) */
		public long getLong(final String key, final long def) {
			return p.getLong(key, def);
		}

		/** @see java.util.prefs.Preferences#putBoolean(String, boolean) */
		public void putBoolean(final String key, final boolean value) {
			p.putBoolean(key, value);
		}

		/** @see java.util.prefs.Preferences#getFloat(String, float) */
		public boolean getBoolean(final String key, final boolean def) {
			return p.getBoolean(key, def);
		}

		/** @see java.util.prefs.Preferences#putFloat(String, float) */
		public void putFloat(final String key, final float value) {
			p.putFloat(key, value);
		}

		/** @see java.util.prefs.Preferences#getFloat(String, float) */
		public float getFloat(final String key, final float def) {
			return p.getFloat(key, def);
		}

		/** @see java.util.prefs.Preferences#putDouble(String, double) */
		public void putDouble(final String key, final double value) {
			p.putDouble(key, value);
		}

		/** @see java.util.prefs.Preferences#getDouble(String, double) */
		public double getDouble(final String key, final double def) {
			return p.getDouble(key, def);
		}

		/** @see java.util.prefs.Preferences#keys() */
		public String[] keys() {
			try {
				return p.keys();
			}
			catch (final java.util.prefs.BackingStoreException exc) {
				log.error(exc);
				return new String[0];
			}
		}

		/** @see java.util.prefs.Preferences#node(String) */
		public SmartPrefs node(final String pathName) {
			return new SmartPrefs(p.node(pathName), log);
		}

		/** @see java.util.prefs.Preferences#nodeExists(String) */
		public boolean nodeExists(final String pathName) {
			try {
				return p.nodeExists(pathName);
			}
			catch (final java.util.prefs.BackingStoreException exc) {
				log.error(exc);
				return false;
			}
		}

		/** @see java.util.prefs.Preferences#removeNode() */
		public void removeNode() {
			try {
				p.removeNode();
			}
			catch (final java.util.prefs.BackingStoreException exc) {
				log.error(exc);
			}
		}

	}

}
