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

	String get(String name);

	String get(String name, String defaultValue);

	boolean getBoolean(String name, boolean defaultValue);

	double getDouble(String name, double defaultValue);

	float getFloat(String name, float defaultValue);

	int getInt(String name, int defaultValue);

	long getLong(String name, long defaultValue);

	void put(String name, String value);

	void put(String name, boolean value);

	void put(String name, double value);

	void put(String name, float value);

	void put(String name, int value);

	void put(String name, long value);

	String get(Class<?> c, String name);

	String get(Class<?> c, String name, String defaultValue);

	boolean getBoolean(Class<?> c, String name, boolean defaultValue);

	double getDouble(Class<?> c, String name, double defaultValue);

	float getFloat(Class<?> c, String name, float defaultValue);

	int getInt(Class<?> c, String name, int defaultValue);

	long getLong(Class<?> c, String name, long defaultValue);

	void put(Class<?> c, String name, String value);

	void put(Class<?> c, String name, boolean value);

	void put(Class<?> c, String name, double value);

	void put(Class<?> c, String name, float value);

	void put(Class<?> c, String name, int value);

	void put(Class<?> c, String name, long value);

	void clear(Class<?> c);

	/** Clears everything. */
	void clearAll();

	/** Clears the node. */
	void clear(String key);

	/**
	 * Clears the node indexed under the given class.
	 */
	void clear(Class<?> prefClass, String key);

	/**
	 * Clears the ndoe indexed under the given path.
	 */
	void clear(String absolutePath, String key);

	/** Removes the node. */
	void remove(Class<?> prefClass, String key);

	void remove(String absolutePath, String key);

	/** Puts a Map into the preferences. */
	void putMap(Map<String, String> map, String key);

	/**
	 * Puts a Map into the preferences, indexed under the specified class.
	 */
	void putMap(Class<?> prefClass, Map<String, String> map, String key);

	/**
	 * Puts a Map into the preferences, indexed under the given path.
	 */
	void putMap(String absolutePath, Map<String, String> map);

	/**
	 * Puts a Map into the preferences, indexed under the given class.
	 */
	void putMap(Class<?> prefClass, Map<String, String> map);

	/**
	 * Puts a Map into the preferences, indexed under the given path and
	 * relative key path.
	 */
	void putMap(String absolutePath, Map<String, String> map, String key);

	/** Gets a Map from the preferences. */
	Map<String, String> getMap(String key);

	/**
	 * Gets a map from the preferences, indexed under the specified class.
	 */
	Map<String, String> getMap(Class<?> prefClass, String key);

	/** Gets a Map from the preferences. */
	Map<String, String> getMap(Class<?> prefClass);

	Map<String, String> getMap(String absolutePath, String key);

	/** Puts a list into the preferences. */
	void putList(List<String> list, String key);

	/**
	 * Puts a list into the preferences, indexed under the specified class.
	 */
	void putList(Class<?> prefClass, List<String> list, String key);

	/**
	 * Puts a list into the preferences, indexed under the specified path and
	 * relative key.
	 */
	void putList(String absolutePath, List<String> list, String key);

	/** Puts a list into the preferences. */
	void putList(Class<?> prefClass, List<String> list);

	/** Puts a list into the preferences, indexed under the specified path. */
	void putList(String absolutePath, List<String> list);

	/** Gets a List from the preferences. */
	List<String> getList(String key);

	/**
	 * Gets a List from the preferences, indexed under the specified path.
	 */
	List<String> getList(String absolutePath, String key);

	/**
	 * Gets a List from the preferences, indexed under the specified class.
	 */
	List<String> getList(Class<?> prefClass, String key);

	/**
	 * Gets a List from the preferences. Returns an empty list if nothing in
	 * prefs.
	 */
	List<String> getList(Class<?> prefClass);

	/**
	 * Puts an iterable into the preferences.
	 */
	void putIterable(Iterable<String> iterable, String key);

	/**
	 * Puts an iterable into the preferences.
	 */
	void putIterable(Class<?> prefClass, Iterable<String> iterable, String key);

	/**
	 * Gets an iterable from the preferences.
	 */
	Iterable<String> getIterable(String key);

	/**
	 * Gets an iterable from the preferences.
	 */
	Iterable<String> getIterable(Class<?> prefClass, String key);
}
