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

package org.scijava.prefs;

import java.util.List;
import java.util.Map;

import org.scijava.service.SciJavaService;
import org.scijava.service.Service;

/**
 * {@link Service} for storing and retrieving arbitrary preferences.
 *
 * @author Mark Hiner
 */
public interface PrefService extends SciJavaService {

	default String get(final Class<?> c, final String name) {
		return get(c, name, null);
	}

	String get(Class<?> c, String name, String defaultValue);

	boolean getBoolean(Class<?> c, String name, boolean defaultValue);

	double getDouble(Class<?> c, String name, double defaultValue);

	float getFloat(Class<?> c, String name, float defaultValue);

	int getInt(Class<?> c, String name, int defaultValue);

	long getLong(Class<?> c, String name, long defaultValue);

	Map<String, String> getMap(Class<?> c, String name);

	List<String> getList(Class<?> c, String name);

	void put(Class<?> c, String name, String value);

	void put(Class<?> c, String name, boolean value);

	void put(Class<?> c, String name, double value);

	void put(Class<?> c, String name, float value);

	void put(Class<?> c, String name, int value);

	void put(Class<?> c, String name, long value);

	void put(Class<?> c, String name, Map<String, String> value);

	void put(Class<?> c, String name, Iterable<String> list);

	void clear(Class<?> c);

	/** Clears everything. */
	void clearAll();

	/** Removes the node. */
	void remove(Class<?> c, String name);

	// -- Deprecated methods --

	/**
	 * Puts a Map into the preferences, indexed under the specified class.
	 */
	@Deprecated
	default void putMap(final Class<?> c, final Map<String, String> map,
		final String name)
	{
		put(c, name, map);
	}

	/**
	 * Puts a Map into the preferences, indexed under the given class.
	 */
	@Deprecated
	void putMap(Class<?> c, Map<String, String> map);

	/** Gets a Map from the preferences. */
	@Deprecated
	Map<String, String> getMap(Class<?> c);

	/**
	 * Puts a list into the preferences, indexed under the specified class.
	 */
	@Deprecated
	default void putList(final Class<?> c, final List<String> list,
		final String name)
	{
		put(c, name, list);
	}

	/** Puts a list into the preferences. */
	@Deprecated
	void putList(Class<?> c, List<String> list);

	/**
	 * Gets a List from the preferences. Returns an empty list if nothing in
	 * prefs.
	 */
	@Deprecated
	List<String> getList(Class<?> c);

	/**
	 * Puts an iterable into the preferences.
	 */
	@Deprecated
	default void putIterable(final Class<?> c, final Iterable<String> iterable,
		final String name)
	{
		put(c, name, iterable);
	}

	/**
	 * Gets an iterable from the preferences.
	 */
	@Deprecated
	Iterable<String> getIterable(Class<?> c, String name);

	@Deprecated
	default String get(final String name) {
		return get((Class<?>) null, name);
	}

	@Deprecated
	default String get(final String name, final String defaultValue) {
		return get(null, name, defaultValue);
	}

	@Deprecated
	default boolean getBoolean(final String name, final boolean defaultValue) {
		return getBoolean(null, name, defaultValue);
	}

	@Deprecated
	default double getDouble(final String name, final double defaultValue) {
		return getDouble(null, name, defaultValue);
	}

	@Deprecated
	default float getFloat(final String name, final float defaultValue) {
		return getFloat(null, name, defaultValue);
	}

	@Deprecated
	default int getInt(final String name, final int defaultValue) {
		return getInt(null, name, defaultValue);
	}

	@Deprecated
	default long getLong(final String name, final long defaultValue) {
		return getLong(null, name, defaultValue);
	}

	@Deprecated
	default void put(final String name, final String value) {
		put(null, name, value);
	}

	@Deprecated
	default void put(final String name, final boolean value) {
		put(null, name, value);
	}

	@Deprecated
	default void put(final String name, final double value) {
		put(null, name, value);
	}

	@Deprecated
	default void put(final String name, final float value) {
		put(null, name, value);
	}

	@Deprecated
	default void put(final String name, final int value) {
		put(null, name, value);
	}

	@Deprecated
	default void put(final String name, final long value) {
		put(null, name, value);
	}

	@Deprecated
	default void clear(final String key) {
		clear((Class<?>) null, key);
	}

	@Deprecated
	void clear(String absolutePath, String key);

	@Deprecated
	void remove(String absolutePath, String key);

	@Deprecated
	void putMap(Map<String, String> map, String key);

	@Deprecated
	void putMap(String absolutePath, Map<String, String> map);

	@Deprecated
	void putMap(String absolutePath, Map<String, String> map, String key);

	@Deprecated
	Map<String, String> getMap(String key);

	@Deprecated
	Map<String, String> getMap(String absolutePath, String key);

	@Deprecated
	void putList(List<String> list, String key);

	@Deprecated
	void putList(String absolutePath, List<String> list, String key);

	@Deprecated
	void putList(String absolutePath, List<String> list);

	@Deprecated
	List<String> getList(String key);

	@Deprecated
	List<String> getList(String absolutePath, String key);

	@Deprecated
	void putIterable(Iterable<String> iterable, String key);

	@Deprecated
	Iterable<String> getIterable(String key);

	@Deprecated
	void clear(Class<?> c, String name);
}
