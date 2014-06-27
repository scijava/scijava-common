/*
 * #%L
 * SciJava Common shared library for SciJava software.
 * %%
 * Copyright (C) 2009 - 2014 Board of Regents of the University of
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
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import org.scijava.plugin.Plugin;
import org.scijava.service.Service;

/**
 * Default {@link PrefService} implementation, which persists preferences to
 * disk using the Java {@link Preferences} API.
 *
 * @author Mark Hiner
 */
@Plugin(type = Service.class)
public class DefaultPrefService extends AbstractPrefService {

	// TODO - with the conversion from a static utility class to a service, we
	// have unfortunately lost some power to adapt behavior to individual data
	// types - either the whole service is superceded, or it's not. For example,
	// see the saveValue/loadValue of the ModuleItem class, where each item could
	// decide how it was saved and loaded.
	// Thus it would be nice to refactor this service to use the Handler pattern,
	// such that there would just be a few base put/get methods that delegated
	// to appropriate handlers. Then the handlers of a single type could be
	// provided and overridden.

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
		try {
			prefs(c).clear();
		}
		catch (final BackingStoreException e) {
			// do nothing
		}
	}

	// -- Other/unsorted --

	// TODO - Evaluate which of these methods are really needed, and which are
	// duplicate of similar functionality above.

	@Override
	public void clearAll() {
		try {
			for (final String name : allPrefs())
				prefs(name).removeNode();
		}
		catch (final BackingStoreException e) {
			// do nothing
		}
	}

	@Override
	public void clear(final String key) {
		clear((Class<?>) null, key);
	}

	@Override
	public void clear(final Class<?> prefClass, final String key) {
		final Preferences preferences = prefs(prefClass);
		clear(preferences, key);
	}

	@Override
	public void clear(final String absolutePath, final String key) {
		final Preferences preferences = prefs(absolutePath);
		clear(preferences, key);
	}

	@Override
	public void remove(final Class<?> prefClass, final String key) {
		final Preferences preferences = prefs(prefClass);
		remove(preferences, key);
	}

	@Override
	public void remove(final String absolutePath, final String key) {
		final Preferences preferences = prefs(absolutePath);
		remove(preferences, key);
	}

	@Override
	public void putMap(final Map<String, String> map, final String key) {
		putMap((Class<?>) null, map, key);
	}

	@Override
	public void putMap(final Class<?> prefClass, final Map<String, String> map,
		final String key)
	{
		final Preferences preferences = prefs(prefClass);
		putMap(preferences.node(key), map);
	}

	@Override
	public void putMap(final String absolutePath, final Map<String, String> map,
		final String key)
	{
		final Preferences preferences = prefs(absolutePath);
		putMap(preferences.node(key), map);
	}

	@Override
	public void putMap(final Class<?> prefClass, final Map<String, String> map) {
		final Preferences preferences = prefs(prefClass);
		putMap(preferences, map);
	}

	@Override
	public void putMap(final String absolutePath, final Map<String, String> map) {
		final Preferences preferences = prefs(absolutePath);
		putMap(preferences, map);
	}

	@Override
	public Map<String, String> getMap(final String key) {
		return getMap((Class<?>) null, key);
	}

	@Override
	public Map<String, String> getMap(final Class<?> prefClass, final String key)
	{
		final Preferences preferences = prefs(prefClass);
		return getMap(preferences.node(key));
	}

	@Override
	public Map<String, String>
		getMap(final String absolutePath, final String key)
	{
		final Preferences preferences = prefs(absolutePath);
		return getMap(preferences.node(key));
	}

	@Override
	public Map<String, String> getMap(final Class<?> prefClass) {
		final Preferences preferences = prefs(prefClass);
		return getMap(preferences);
	}

	@Override
	public void putList(final List<String> list, final String key) {
		putList((Class<?>) null, list, key);
	}

	@Override
	public void putList(final Class<?> prefClass, final List<String> list,
		final String key)
	{
		final Preferences preferences = prefs(prefClass);
		putList(preferences.node(key), list);
	}

	@Override
	public void putList(final String absolutePath, final List<String> list,
		final String key)
	{
		final Preferences preferences = prefs(absolutePath);
		putList(preferences.node(key), list);
	}

	@Override
	public void putList(final Class<?> prefClass, final List<String> list) {
		final Preferences preferences = prefs(prefClass);
		putList(preferences, list);
	}

	@Override
	public void putList(final String absolutePath, final List<String> list) {
		final Preferences preferences = prefs(absolutePath);
		putList(preferences, list);
	}

	@Override
	public List<String> getList(final String key) {
		return getList((Class<?>) null, key);
	}

	@Override
	public List<String> getList(final Class<?> prefClass, final String key) {
		final Preferences preferences = prefs(prefClass);
		return getList(preferences.node(key));
	}

	@Override
	public List<String> getList(final String absolutePath, final String key) {
		final Preferences preferences = prefs(absolutePath);
		return getList(preferences.node(key));
	}

	@Override
	public List<String> getList(final Class<?> prefClass) {
		final Preferences preferences = prefs(prefClass);
		return getList(preferences);
	}

	@Override
	public Iterable<String> getIterable(final String key) {
		return getIterable((Class<?>) null, key);
	}

	@Override
	public Iterable<String> getIterable(final Class<?> prefClass, final String key) {
		final Preferences preferences = prefs(prefClass);
		return getIterable(preferences.node(key));
	}

	@Override
	public void putIterable(final Iterable<String> iterable, final String key) {
		putIterable((Class<?>) null, iterable, key);
	}

	@Override
	public void putIterable(final Class<?> prefClass, final Iterable<String> iterable, final String key) {
		final Preferences preferences = prefs(prefClass);
		putIterable(preferences.node(key), iterable);
	}

	// -- Helper methods --

	private void clear(final Preferences preferences, final String key) {
		try {
			if (preferences.nodeExists(key)) {
				preferences.node(key).clear();
			}
		}
		catch (final BackingStoreException bse) {
			bse.printStackTrace();
		}
	}

	private void remove(final Preferences preferences, final String key) {
		try {
			if (preferences.nodeExists(key)) {
				preferences.node(key).removeNode();
			}
		}
		catch (final BackingStoreException bse) {
			bse.printStackTrace();
		}
	}

	private void putMap(final Preferences preferences,
		final Map<String, String> map)
	{
		if (preferences == null) {
			throw new IllegalArgumentException("Preferences not set.");
		}
		final Iterator<Entry<String, String>> iter = map.entrySet().iterator();
		while (iter.hasNext()) {
			final Entry<String, String> entry = iter.next();
			final Object value = entry.getValue();
			preferences.put(entry.getKey().toString(), value == null ? null : value
				.toString());
		}

	}

	private Map<String, String> getMap(final Preferences preferences) {
		if (preferences == null) {
			throw new IllegalArgumentException("Preferences not set.");
		}
		final Map<String, String> map = new HashMap<String, String>();
		try {
			final String[] keys = preferences.keys();
			for (int index = 0; index < keys.length; index++) {
				map.put(keys[index], preferences.get(keys[index], null));
			}
		}
		catch (final BackingStoreException bse) {
			bse.printStackTrace();
		}
		return map;
	}

	private void putList(final Preferences preferences, final List<String> list) {
		if (preferences == null) {
			throw new IllegalArgumentException("Preferences not set.");
		}
		for (int index = 0; list != null && index < list.size(); index++) {
			final Object value = list.get(index);
			preferences.put("" + index, value == null ? null : value.toString());
		}
	}

	private List<String> getList(final Preferences preferences) {
		if (preferences == null) {
			throw new IllegalArgumentException("Preferences not set.");
		}
		final List<String> list = new ArrayList<String>();
		for (int index = 0; index < 1000; index++) {
			final String value = preferences.get("" + index, null);
			if (value == null) {
				break;
			}
			list.add(value);
		}
		return list;
	}

	private void putIterable(final Preferences preferences,
		final Iterable<String> iterable)
	{
		if (preferences == null) {
			throw new IllegalArgumentException("Preferences not set.");
		}
		int index = 0;
		for (final String value : iterable) {
			preferences.put("" + index++, value == null ? null : value.toString());
		}
	}

	private Iterable<String> getIterable(final Preferences preferences)
	{
		if (preferences == null) {
			throw new IllegalArgumentException("Preferences not set.");
		}
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
						value = preferences.get("" + index, null);
						index = value == null ? -1 : index + 1;
					}
				};
			}
		};
	}

	private Preferences prefs(final Class<?> c) {
		return Preferences.userNodeForPackage(c == null ? PrefService.class : c);
	}

	private String[] allPrefs() throws BackingStoreException {
		return Preferences.userRoot().childrenNames();
	}

	private Preferences prefs(final String absolutePath) {
		return Preferences.userRoot().node(absolutePath);
	}

	private String key(final Class<?> c, final String name) {
		return c == null ? name : c.getSimpleName() + "." + name;
	}
}
