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

	String get(Class<?> c, String name);

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

	/**
	 * Clears the node indexed under the given class.
	 */
	void clear(Class<?> prefClass, String name);

	/** Removes the node. */
	void remove(Class<?> prefClass, String name);

	/**
	 * Puts a Map into the preferences, indexed under the specified class.
	 */
	@Deprecated
	void putMap(Class<?> prefClass, Map<String, String> map, String name);

	/**
	 * Puts a Map into the preferences, indexed under the given class.
	 */
	@Deprecated
	void putMap(Class<?> prefClass, Map<String, String> map);

	/** Gets a Map from the preferences. */
	@Deprecated
	Map<String, String> getMap(Class<?> prefClass);

	/**
	 * Puts a list into the preferences, indexed under the specified class.
	 */
	@Deprecated
	void putList(Class<?> prefClass, List<String> list, String name);

	/** Puts a list into the preferences. */
	@Deprecated
	void putList(Class<?> prefClass, List<String> list);

	/**
	 * Gets a List from the preferences. Returns an empty list if nothing in
	 * prefs.
	 */
	@Deprecated
	List<String> getList(Class<?> prefClass);

	/**
	 * Puts an iterable into the preferences.
	 */
	@Deprecated
	void putIterable(Class<?> prefClass, Iterable<String> iterable, String name);

	/**
	 * Gets an iterable from the preferences.
	 */
	@Deprecated
	Iterable<String> getIterable(Class<?> prefClass, String name);

	// -- Deprecated methods --

	@Deprecated
	String get(String name);

	@Deprecated
	String get(String name, String defaultValue);

	@Deprecated
	boolean getBoolean(String name, boolean defaultValue);

	@Deprecated
	double getDouble(String name, double defaultValue);

	@Deprecated
	float getFloat(String name, float defaultValue);

	@Deprecated
	int getInt(String name, int defaultValue);

	@Deprecated
	long getLong(String name, long defaultValue);

	@Deprecated
	void put(String name, String value);

	@Deprecated
	void put(String name, boolean value);

	@Deprecated
	void put(String name, double value);

	@Deprecated
	void put(String name, float value);

	@Deprecated
	void put(String name, int value);

	@Deprecated
	void put(String name, long value);

	@Deprecated
	void clear(String key);

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
}
