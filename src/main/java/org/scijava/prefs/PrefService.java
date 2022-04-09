/*
 * #%L
 * SciJava Common shared library for SciJava software.
 * %%
 * Copyright (C) 2009 - 2022 SciJava developers.
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

	/**
	 * Gets a persisted key as a {@link String}.
	 * 
	 * @param c The class with which the key is associated.
	 * @param name The key to retrieve.
	 * @return The value of the key as a {@link String}, or null if the key is not
	 *         present.
	 */
	default String get(final Class<?> c, final String name) {
		return get(c, name, null);
	}

	/**
	 * Gets a persisted key as a {@link String}.
	 * 
	 * @param c The class with which the key is associated.
	 * @param name The key to retrieve.
	 * @param defaultValue The value to return if the key is not present.
	 * @return The value of the key as a {@link String}.
	 */
	String get(Class<?> c, String name, String defaultValue);

	/**
	 * Gets a persisted key as a {@code boolean}.
	 * 
	 * @param c The class with which the key is associated.
	 * @param name The key to retrieve.
	 * @param defaultValue The value to return if the key is not present.
	 * @return The value of the key as a {@code boolean}.
	 */
	boolean getBoolean(Class<?> c, String name, boolean defaultValue);

	/**
	 * Gets a persisted key as a {@code double}.
	 * 
	 * @param c The class with which the key is associated.
	 * @param name The key to retrieve.
	 * @param defaultValue The value to return if the key is not present.
	 * @return The value of the key as a {@code double}.
	 */
	double getDouble(Class<?> c, String name, double defaultValue);

	/**
	 * Gets a persisted key as a {@code float}.
	 * 
	 * @param c The class with which the key is associated.
	 * @param name The key to retrieve.
	 * @param defaultValue The value to return if the key is not present.
	 * @return The value of the key as a {@code float}.
	 */
	float getFloat(Class<?> c, String name, float defaultValue);

	/**
	 * Gets a persisted key as an {@code int}.
	 * 
	 * @param c The class with which the key is associated.
	 * @param name The key to retrieve.
	 * @param defaultValue The value to return if the key is not present.
	 * @return The value of the key as an {@code int}.
	 */
	int getInt(Class<?> c, String name, int defaultValue);

	/**
	 * Gets a persisted key as a {@code long}.
	 * 
	 * @param c The class with which the key is associated.
	 * @param name The key to retrieve.
	 * @param defaultValue The value to return if the key is not present.
	 * @return The value of the key as an {@code long}.
	 */
	long getLong(Class<?> c, String name, long defaultValue);

	/**
	 * Gets a persisted key as a {@code Map}.
	 * 
	 * @param c The class with which the key is associated.
	 * @param name The key to retrieve.
	 * @return The value of the key as an {@code Map}, or null if the key is not
	 *         present.
	 */
	Map<String, String> getMap(Class<?> c, String name);

	/**
	 * Gets a persisted key as a {@code List}.
	 * 
	 * @param c The class with which the key is associated.
	 * @param name The key to retrieve.
	 * @return The value of the key as an {@code List}, or null if the key is not
	 *         present.
	 */
	List<String> getList(Class<?> c, String name);

	/**
	 * Saves a key/value pair in persistent storage.
	 * 
	 * @param c The class with which the key/value pair is associated.
	 * @param name The key where the value should be stored.
	 * @param value The value to store.
	 * @see #get(Class, String)
	 * @see #get(Class, String, String)
	 */
	void put(Class<?> c, String name, String value);

	/**
	 * Saves a key/value pair in persistent storage.
	 * 
	 * @param c The class with which the key/value pair is associated.
	 * @param name The key where the value should be stored.
	 * @param value The value to store.
	 * @see #getBoolean(Class, String, boolean)
	 */
	void put(Class<?> c, String name, boolean value);

	/**
	 * Saves a key/value pair in persistent storage.
	 * 
	 * @param c The class with which the key/value pair is associated.
	 * @param name The key where the value should be stored.
	 * @param value The value to store.
	 * @see #getDouble(Class, String, double)
	 */
	void put(Class<?> c, String name, double value);

	/**
	 * Saves a key/value pair in persistent storage.
	 * 
	 * @param c The class with which the key/value pair is associated.
	 * @param name The key where the value should be stored.
	 * @param value The value to store.
	 * @see #getFloat(Class, String, float)
	 */
	void put(Class<?> c, String name, float value);

	/**
	 * Saves a key/value pair in persistent storage.
	 * 
	 * @param c The class with which the key/value pair is associated.
	 * @param name The key where the value should be stored.
	 * @param value The value to store.
	 * @see #getInt(Class, String, int)
	 */
	void put(Class<?> c, String name, int value);

	/**
	 * Saves a key/value pair in persistent storage.
	 * 
	 * @param c The class with which the key/value pair is associated.
	 * @param name The key where the value should be stored.
	 * @param value The value to store.
	 * @see #getLong(Class, String, long)
	 */
	void put(Class<?> c, String name, long value);

	/**
	 * Saves a key/value pair in persistent storage.
	 * 
	 * @param c The class with which the key/value pair is associated.
	 * @param name The key where the value should be stored.
	 * @param value The value to store.
	 * @see #getMap(Class, String)
	 */
	void put(Class<?> c, String name, Map<String, String> value);

	/**
	 * Saves a key/value pair in persistent storage.
	 * 
	 * @param c The class with which the key/value pair is associated.
	 * @param name The key where the value should be stored.
	 * @param value The value to store.
	 * @see #getList(Class, String)
	 */
	void put(Class<?> c, String name, Iterable<String> value);

	/**
	 * Deletes a key from persistent storage.
	 * 
	 * @param c The class with which the key is associated.
	 * @param name The key to remove.
	 */
	void remove(Class<?> c, String name);

	/**
	 * Deletes all of the given {@link Class}'s keys from persistent storage.
	 * @param c The class whose keys should be removed.
	 */
	void clear(Class<?> c);

	/** Deletes all information from the data store. Use with care! */
	void clearAll();

	// -- Deprecated methods --

	/** @deprecated Use {@link #put(Class, String, Map)}. */
	@Deprecated
	default void putMap(final Class<?> c, final Map<String, String> map,
		final String name)
	{
		put(c, name, map);
	}

	/** @deprecated Use {@link #put(Class, String, Map)}. */
	@Deprecated
	void putMap(Class<?> c, Map<String, String> map);

	/** @deprecated Use {@link #getMap(Class, String)}. */
	@Deprecated
	Map<String, String> getMap(Class<?> c);

	/** @deprecated Use {@link #put(Class, String, Iterable)}. */
	@Deprecated
	default void putList(final Class<?> c, final List<String> list,
		final String name)
	{
		put(c, name, list);
	}

	/** @deprecated Use {@link #put(Class, String, Iterable)}. */
	@Deprecated
	void putList(Class<?> c, List<String> list);

	/** @deprecated Use {@link #getList(Class, String)}. */
	@Deprecated
	List<String> getList(Class<?> c);

	/** @deprecated Use {@link #put(Class, String, Iterable)}. */
	@Deprecated
	default void putIterable(final Class<?> c, final Iterable<String> iterable,
		final String name)
	{
		put(c, name, iterable);
	}

	/** @deprecated Use {@link #getList(Class, String)}. */
	@Deprecated
	Iterable<String> getIterable(Class<?> c, String name);

	/** @deprecated Use {@link #get(Class, String)}. */
	@Deprecated
	default String get(final String name) {
		return get((Class<?>) null, name);
	}

	/** @deprecated Use {@link #get(Class, String, String)}. */
	@Deprecated
	default String get(final String name, final String defaultValue) {
		return get(null, name, defaultValue);
	}

	/** @deprecated Use {@link #getBoolean(Class, String, boolean)}. */
	@Deprecated
	default boolean getBoolean(final String name, final boolean defaultValue) {
		return getBoolean(null, name, defaultValue);
	}

	/** @deprecated Use {@link #getDouble(Class, String, double)}. */
	@Deprecated
	default double getDouble(final String name, final double defaultValue) {
		return getDouble(null, name, defaultValue);
	}

	/** @deprecated Use {@link #getFloat(Class, String, float)}. */
	@Deprecated
	default float getFloat(final String name, final float defaultValue) {
		return getFloat(null, name, defaultValue);
	}

	/** @deprecated Use {@link #getInt(Class, String, int)}. */
	@Deprecated
	default int getInt(final String name, final int defaultValue) {
		return getInt(null, name, defaultValue);
	}

	/** @deprecated Use {@link #getLong(Class, String, long)}. */
	@Deprecated
	default long getLong(final String name, final long defaultValue) {
		return getLong(null, name, defaultValue);
	}

	/** @deprecated Use {@link #put(Class, String, String)}. */
	@Deprecated
	default void put(final String name, final String value) {
		put(null, name, value);
	}

	/** @deprecated Use {@link #put(Class, String, boolean)}. */
	@Deprecated
	default void put(final String name, final boolean value) {
		put(null, name, value);
	}

	/** @deprecated Use {@link #put(Class, String, double)}. */
	@Deprecated
	default void put(final String name, final double value) {
		put(null, name, value);
	}

	/** @deprecated Use {@link #put(Class, String, float)}. */
	@Deprecated
	default void put(final String name, final float value) {
		put(null, name, value);
	}

	/** @deprecated Use {@link #put(Class, String, int)}. */
	@Deprecated
	default void put(final String name, final int value) {
		put(null, name, value);
	}

	/** @deprecated Use {@link #put(Class, String, long)}. */
	@Deprecated
	default void put(final String name, final long value) {
		put(null, name, value);
	}

	/**
	 * @deprecated Use {@link #remove(Class, String)} or {@link #clear(Class)}.
	 */
	@Deprecated
	default void clear(final String key) {
		clear((Class<?>) null, key);
	}

	/** @deprecated Use {@link #remove(Class, String)}. */
	@Deprecated
	void clear(String absolutePath, String key);

	/** @deprecated Use {@link #remove(Class, String)}. */
	@Deprecated
	void remove(String absolutePath, String key);

	/** @deprecated Use {@link #put(Class, String, Map)}. */
	@Deprecated
	default void putMap(final Map<String, String> map, final String key) {
		putMap((Class<?>) null, map, key);
	}

	/** @deprecated Use {@link #put(Class, String, Map)}. */
	@Deprecated
	void putMap(String absolutePath, Map<String, String> map);

	/** @deprecated Use {@link #put(Class, String, Map)}. */
	@Deprecated
	void putMap(String absolutePath, Map<String, String> map, String key);

	/** @deprecated Use {@link #getMap(Class, String)}. */
	@Deprecated
	default Map<String, String> getMap(final String key) {
		return getMap((Class<?>) null, key);
	}

	/** @deprecated Use {@link #getMap(Class, String)}. */
	@Deprecated
	Map<String, String> getMap(String absolutePath, String key);

	/** @deprecated Use {@link #put(Class, String, Iterable)}. */
	@Deprecated
	default void putList(final List<String> list, final String key) {
		putList((Class<?>) null, list, key);
	}

	/** @deprecated Use {@link #put(Class, String, Iterable)}. */
	@Deprecated
	void putList(String absolutePath, List<String> list, String key);

	/** @deprecated Use {@link #put(Class, String, Iterable)}. */
	@Deprecated
	void putList(String absolutePath, List<String> list);

	/** @deprecated Use {@link #getList(Class, String)}. */
	@Deprecated
	default List<String> getList(final String key) {
		return getList((Class<?>) null, key);
	}

	/** @deprecated Use {@link #getList(Class, String)}. */
	@Deprecated
	List<String> getList(String absolutePath, String key);

	/** @deprecated Use {@link #put(Class, String, Iterable)}. */
	@Deprecated
	default void putIterable(final Iterable<String> iterable, final String key) {
		putIterable((Class<?>) null, iterable, key);
	}

	/** @deprecated User {@link #getList(Class, String)}. */
	@Deprecated
	default Iterable<String> getIterable(final String key) {
		return getIterable((Class<?>) null, key);
	}

	/** @deprecated Use {@link #remove(Class, String)}. */
	@Deprecated
	void clear(Class<?> c, String name);
}
