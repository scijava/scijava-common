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

package org.scijava.util;

import java.util.List;
import java.util.Map;
import java.util.prefs.Preferences;

import org.scijava.preferences.DefaultPrefService;
import org.scijava.preferences.PrefService;

/**
 * Simple utility class that stores and retrieves user preferences.
 * <p>
 * Some of this code was adapted from the <a href=
 * "http://www.java2s.com/Code/Java/Development-Class/Utilityclassforpreferences.htm"
 * >PrefsUtil class by Robin Sharp of Javelin Software.</a>.
 * </p>
 *
 * @author Curtis Rueden
 * @author Barry DeZonia
 * @author Grant Harris
 */
public final class Prefs {

	private static PrefService prefService;

	private static PrefService prefServiceNoContext;

	private static double servicePriority = Double.MIN_VALUE;

	private Prefs() {
		// prevent instantiation of utility class
	}

	// -- Global preferences --

	public static String get(final String name) {
		return service().get(name);
	}

	public static String get(final String name, final String defaultValue) {
		return service().get(name, defaultValue);
	}

	public static boolean
		getBoolean(final String name, final boolean defaultValue)
	{
		return service().getBoolean(name, defaultValue);
	}

	public static double getDouble(final String name, final double defaultValue) {
		return service().getDouble(name, defaultValue);
	}

	public static float getFloat(final String name, final float defaultValue) {
		return service().getFloat(name, defaultValue);
	}

	public static int getInt(final String name, final int defaultValue) {
		return service().getInt(name, defaultValue);
	}

	public static long getLong(final String name, final long defaultValue) {
		return service().getLong(name, defaultValue);
	}

	public static void put(final String name, final String value) {
		service().put(name, value);
	}

	public static void put(final String name, final boolean value) {
		service().put(name, value);
	}

	public static void put(final String name, final double value) {
		service().put(name, value);
	}

	public static void put(final String name, final float value) {
		service().put(name, value);
	}

	public static void put(final String name, final int value) {
		service().put(name, value);
	}

	public static void put(final String name, final long value) {
		service().put(name, value);
	}

	// -- Class-specific preferences --

	public static String get(final Class<?> c, final String name) {
		return service().get(c, name);
	}

	public static String get(final Class<?> c, final String name,
		final String defaultValue)
	{
		return service().get(c, name, defaultValue);
	}

	public static boolean getBoolean(final Class<?> c, final String name,
		final boolean defaultValue)
	{
		return service().getBoolean(c, name, defaultValue);
	}

	public static double getDouble(final Class<?> c, final String name,
		final double defaultValue)
	{
		return service().getDouble(c, name, defaultValue);
	}

	public static float getFloat(final Class<?> c, final String name,
		final float defaultValue)
	{
		return service().getFloat(c, name, defaultValue);
	}

	public static int getInt(final Class<?> c, final String name,
		final int defaultValue)
	{
		return service().getInt(c, name, defaultValue);
	}

	public static long getLong(final Class<?> c, final String name,
		final long defaultValue)
	{
		return service().getLong(c, name, defaultValue);
	}

	public static void
		put(final Class<?> c, final String name, final String value)
	{
		service().put(c, name, value);
	}

	public static void put(final Class<?> c, final String name,
		final boolean value)
	{
		service().put(c, name, value);
	}

	public static void
		put(final Class<?> c, final String name, final double value)
	{
		service().put(c, name, value);
	}

	public static void
		put(final Class<?> c, final String name, final float value)
	{
		service().put(c, name, value);
	}

	public static void put(final Class<?> c, final String name, final int value) {
		service().put(c, name, value);
	}

	public static void put(final Class<?> c, final String name, final long value)
	{
		service().put(c, name, value);
	}

	public static void clear(final Class<?> c) {
		service().clear(c);
	}

	// -- Other/unsorted --

	// TODO - Evaluate which of these methods are really needed, and which are
	// duplicate of similar functionality above.

	/** Clears everything. */
	public static void clearAll() {
		service().clearAll();
	}

	/** Clears the node. */
	public static void clear(final String key) {
		service().clear(key);
	}

	public static void clear(final Preferences preferences, final String key) {
		service().clear(preferences, key);
	}

	/** Removes the node. */
	public static void remove(final Preferences preferences, final String key) {
		service().remove(preferences, key);
	}

	/** Puts a list into the preferences. */
	public static void putMap(final Map<String, String> map, final String key) {
		service().putMap(map, key);
	}

	public static void putMap(final Preferences preferences,
		final Map<String, String> map, final String key)
	{
		service().putMap(preferences, map, key);
	}

	/** Puts a list into the preferences. */
	public static void putMap(final Preferences preferences,
		final Map<String, String> map)
	{
		service().putMap(preferences, map);
	}

	/** Gets a Map from the preferences. */
	public static Map<String, String> getMap(final String key) {
		return service().getMap(key);
	}

	public static Map<String, String> getMap(final Preferences preferences,
		final String key)
	{
		return service().getMap(preferences, key);
	}

	/** Gets a Map from the preferences. */
	public static Map<String, String> getMap(final Preferences preferences) {
		return service().getMap(preferences);
	}

	/** Puts a list into the preferences. */
	public static void putList(final List<String> list, final String key) {
		service().putList(list, key);
	}

	public static void putList(final Preferences preferences,
		final List<String> list, final String key)
	{
		service().putList(preferences, list, key);
	}

	/** Puts a list into the preferences. */
	public static void putList(final Preferences preferences,
		final List<String> list)
	{
		service().putList(preferences, list);
	}

	/** Gets a List from the preferences. */
	public static List<String> getList(final String key) {
		return service().getList(key);
	}

	public static List<String> getList(final Preferences preferences,
		final String key)
	{
		return service().getList(preferences, key);
	}

	/**
	 * Gets a List from the preferences. Returns an empty list if nothing in
	 * prefs.
	 */
	public static List<String> getList(final Preferences preferences) {
		return service().getList(preferences);
	}

	// -- PrefService setter --

	/**
	 * Sets the {@link PrefService}
	 */
	public static void setDelegateService(final PrefService prefService,
		final double priority)
	{
		if (Double.compare(priority, Prefs.servicePriority) > 0) {
			Prefs.prefService = prefService;
			Prefs.servicePriority = priority;
		}
	}

	// -- Helper methods --

	/**
	 * Gets the delegate {@link PrefService} to use for preference operations. If
	 * this service has not been explicitly set, then a {@link DefaultPrefService}
	 * will be used.
	 *
	 * @return The current {@link PrefService} to use for delegation.
	 */
	private static PrefService service() {
		if (prefService != null) return prefService;

		if (prefServiceNoContext == null) prefServiceNoContext =
			new DefaultPrefService();

		return prefServiceNoContext;
	}
}
