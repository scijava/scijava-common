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

package org.scijava.prefs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.scijava.Context;

/**
 * Tests {@link PrefService}.
 * 
 * @author Curtis Rueden
 */
public class PrefServiceTest {

	private PrefService prefService;

	@Before
	public void setUp() {
		final Context context = new Context(PrefService.class);
		prefService = context.getService(PrefService.class);
	}

	@After
	public void tearDown() {
		prefService.clear(getClass());
		prefService.getContext().dispose();
	}

	/**
	 * Tests {@link PrefService#put(Class, String, String)} and
	 * {@link PrefService#get(Class, String)}.
	 */
	@Test
	public void testString() {
		assertNull(prefService.get(getClass(), "animal"));
		prefService.put(getClass(), "animal", "kraken");
		assertEquals("kraken", prefService.get(getClass(), "animal"));
	}

	/**
	 * Tests {@link PrefService#put(Class, String, boolean)} and
	 * {@link PrefService#getBoolean(Class, String, boolean)}.
	 */
	@Test
	public void testBoolean() {
		assertFalse(prefService.getBoolean(getClass(), "awesome", false));
		prefService.put(getClass(), "awesome", true);
		assertTrue(prefService.getBoolean(getClass(), "awesome", false));
	}

	/**
	 * Tests {@link PrefService#put(Class, String, double)} and
	 * {@link PrefService#getDouble(Class, String, double)}.
	 */
	@Test
	public void testDouble() {
		final String key = "real";
		final double dv = 0, value = 123.456;
		assertEquals(dv, prefService.getDouble(getClass(), key, dv), 0);
		prefService.put(getClass(), key, value);
		assertEquals(value, prefService.getDouble(getClass(), key, dv), 0);
	}

	/**
	 * Tests {@link PrefService#put(Class, String, float)} and
	 * {@link PrefService#getFloat(Class, String, float)}.
	 */
	@Test
	public void testFloat() {
		final String key = "real";
		final float dv = 0, value = 654.321f;
		assertEquals(dv, prefService.getFloat(getClass(), key, dv), 0);
		prefService.put(getClass(), key, value);
		assertEquals(value, prefService.getFloat(getClass(), key, dv), 0);
	}

	/**
	 * Tests {@link PrefService#put(Class, String, int)} and
	 * {@link PrefService#getInt(Class, String, int)}.
	 */
	@Test
	public void testInt() {
		final String key = "integer";
		final int dv = 0, value = 1234;
		assertEquals(dv, prefService.getInt(getClass(), key, dv));
		prefService.put(getClass(), key, value);
		assertEquals(value, prefService.getInt(getClass(), key, dv));
	}

	/**
	 * Tests {@link PrefService#put(Class, String, long)} and
	 * {@link PrefService#getLong(Class, String, long)}.
	 */
	@Test
	public void testLong() {
		final long dv = 0L, value = 9999999999999L;
		assertEquals(dv, prefService.getLong(getClass(), "power level", dv));
		prefService.put(getClass(), "power level", value);
		assertTrue(prefService.getLong(getClass(), "power level", dv) > 9000);
	}

	/**
	 * Tests {@link PrefService#putMap(Class, Map, String)} and
	 * {@link PrefService#getMap(Class, String)}.
	 */
	@Test
	public void testMap() {
		final Map<String, String> map = new HashMap<>();
		map.put("0", "A");
		map.put("1", "B");
		map.put("2", "C");
		map.put("3", "D");
		map.put("5", "f");
		final String mapKey = "MapKey";
		prefService.putMap(getClass(), map, mapKey);
		final Map<String, String> result = prefService.getMap(getClass(), mapKey);
		assertEquals(map, result);
	}

	/**
	 * Tests {@link PrefService#putList(Class, List, String)} and
	 * {@link PrefService#getList(Class, String)}.
	 */
	@Test
	public void testList() {
		final String recentFilesKey = "RecentFiles";
		final List<String> recentFiles = new ArrayList<>();
		recentFiles.add("some/path1");
		recentFiles.add("some/path2");
		recentFiles.add("some/path3");
		prefService.putList(getClass(), recentFiles, recentFilesKey);
		final List<String> result = prefService.getList(getClass(), recentFilesKey);
		assertEquals(recentFiles, result);
	}

	/**
	 * The Java Preferences API does not support keys longer than 80 characters.
	 * Let's test that our service does not fall victim to this limitation.
	 */
	@Test
	public void testLongKeys() {
		final String longKey = "" + //
			"abcdefghijklmnopqrstuvwxyz" + //
			"ABCDEFGHIJKLMNOPQRSTUVWXYZ" + //
			"0123456789" + //
			"9876543210" + //
			"ZYXWVUTSRQPONMLKJIHGFEDCBA" + //
			"zyxwvutsrqponmlkjihgfedcba";
		final String lyrics =
			"Now I know my ABC's. Next time won't you sing with me?";
		prefService.put(longKey, lyrics);
		final String recovered = prefService.get(longKey);
		assertEquals(lyrics, recovered);
	}

}
