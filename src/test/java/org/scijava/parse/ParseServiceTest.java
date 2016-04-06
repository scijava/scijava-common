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

package org.scijava.parse;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.scijava.Context;

/**
 * Tests {@link ParseService}.
 *
 * @author Curtis Rueden
 */
public class ParseServiceTest {

	private ParseService parser;

	@Before
	public void setUp() {
		final Context context = new Context(ParseService.class);
		parser = context.service(ParseService.class);
	}

	@After
	public void tearDown() {
		parser.getContext().dispose();
	}

	/** Tests {@link ParseService#parse(String)}. */
	@Test
	public void testEmpty() {
		final Items items = parser.parse("");
		assertTrue(items.isEmpty());
		assertTrue(items.isList());
		assertTrue(items.isMap());
		assertMapCorrect(items);
	}

	@Test
	public void testList() {
		final Items items = parser.parse("1,2,3,4,5");
		assertEquals(5, items.size());
		assertTrue(items.isList());
		assertFalse(items.isMap());
		assertSame(1, items.get(0).value());
		assertSame(2, items.get(1).value());
		assertSame(3, items.get(2).value());
		assertSame(4, items.get(3).value());
		assertSame(5, items.get(4).value());
		assertNull(items.get(0).name());
		assertNull(items.get(1).name());
		assertNull(items.get(2).name());
		assertNull(items.get(3).name());
		assertNull(items.get(4).name());
	}

	@Test
	public void testMap() {
		final Items items = parser.parse(
			"foo='bar', animal='Quick brown fox', colors={'red', 'green', 'blue'}");
		assertEquals(3, items.size());
		assertFalse(items.isList());
		assertTrue(items.isMap());
		assertEquals("foo", items.get(0).name());
		assertEquals("bar", items.get(0).value());
		assertEquals("animal", items.get(1).name());
		assertEquals("Quick brown fox", items.get(1).value());
		assertEquals("colors", items.get(2).name());
		final Object colors = items.get(2).value();
		assertTrue(colors instanceof List);
		final List<?> colorsList = (List<?>) colors;
		assertEquals(3, colorsList.size());
		assertEquals("red", colorsList.get(0));
		assertEquals("green", colorsList.get(1));
		assertEquals("blue", colorsList.get(2));

		assertMapCorrect(items);
	}

	// -- Helper methods --

	private void assertMapCorrect(final Items items) {
		final Map<String, Object> map = items.asMap();
		assertEquals(items.size(), map.size());

		// test that map contents match
		for (final Item item : items) {
			assertSame(item.value(), map.get(item.name()));
		}

		// test that map iteration order is the same
		int index = 0;
		for (final Object value : map.values()) {
			assertSame("" + index + ":", items.get(index++).value(), value);
		}
	}

}
