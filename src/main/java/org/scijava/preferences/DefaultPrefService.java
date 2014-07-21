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

package org.scijava.preferences;

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
import org.scijava.util.Prefs;

/**
 * Default {@link PrefService} implementation. Uses the {@link Prefs} utility
 * class to persist preferences to disk.
 *
 * @author Mark Hiner
 */
@Plugin(type = Service.class)
public class DefaultPrefService extends AbstractPrefService {

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

	/** Clears everything. */
	@Override
	public void clearAll() {
		try {
			final String[] childNames = Preferences.userRoot().childrenNames();
			for (final String name : childNames)
				Preferences.userRoot().node(name).removeNode();
		}
		catch (final BackingStoreException e) {
			// do nothing
		}
	}

	/** Clears the node. */
	@Override
	public void clear(final String key) {
		clear(prefs(null), key);
	}

	@Override
	public void clear(final Preferences preferences, final String key) {
		try {
			if (preferences.nodeExists(key)) {
				preferences.node(key).clear();
			}
		}
		catch (final BackingStoreException bse) {
			bse.printStackTrace();
		}
	}

	/** Removes the node. */
	@Override
	public void remove(final Preferences preferences, final String key) {
		try {
			if (preferences.nodeExists(key)) {
				preferences.node(key).removeNode();
			}
		}
		catch (final BackingStoreException bse) {
			bse.printStackTrace();
		}
	}

	/** Puts a list into the preferences. */
	@Override
	public void putMap(final Map<String, String> map, final String key) {
		putMap(prefs(null), map, key);
	}

	@Override
	public void putMap(final Preferences preferences,
		final Map<String, String> map, final String key)
	{
		putMap(preferences.node(key), map);
	}

	/** Puts a list into the preferences. */
	@Override
	public void putMap(final Preferences preferences,
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

	/** Gets a Map from the preferences. */
	@Override
	public Map<String, String> getMap(final String key) {
		return getMap(prefs(null), key);
	}

	@Override
	public Map<String, String> getMap(final Preferences preferences,
		final String key)
	{
		return getMap(preferences.node(key));
	}

	/** Gets a Map from the preferences. */
	@Override
	public Map<String, String> getMap(final Preferences preferences) {
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

	/** Puts a list into the preferences. */
	@Override
	public void putList(final List<String> list, final String key) {
		putList(prefs(null), list, key);
	}

	@Override
	public void putList(final Preferences preferences, final List<String> list,
		final String key)
	{
		putList(preferences.node(key), list);
	}

	/** Puts a list into the preferences. */
	@Override
	public void putList(final Preferences preferences, final List<String> list) {
		if (preferences == null) {
			throw new IllegalArgumentException("Preferences not set.");
		}
		for (int index = 0; list != null && index < list.size(); index++) {
			final Object value = list.get(index);
			preferences.put("" + index, value == null ? null : value.toString());
		}
	}

	/** Gets a List from the preferences. */
	@Override
	public List<String> getList(final String key) {
		return getList(prefs(null), key);
	}

	@Override
	public List<String> getList(final Preferences preferences, final String key) {
		return getList(preferences.node(key));
	}

	/**
	 * Gets a List from the preferences. Returns an empty list if nothing in
	 * prefs.
	 */
	@Override
	public List<String> getList(final Preferences preferences) {
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

	// -- Helper methods --

	private Preferences prefs(final Class<?> c) {
		return Preferences.userNodeForPackage(c == null ? Prefs.class : c);
	}

	private String key(final Class<?> c, final String name) {
		return c == null ? name : c.getSimpleName() + "." + name;
	}
}
