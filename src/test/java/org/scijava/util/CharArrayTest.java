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

package org.scijava.util;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;

import org.junit.Test;

/**
 * Tests {@link CharArray}.
 * 
 * @author Mark Hiner
 * @author Curtis Rueden
 * @author Johannes Schindelin
 */
public class CharArrayTest extends PrimitiveArrayTest {

	/** Tests {@link CharArray#CharArray()}. */
	@Test
	public void testConstructorNoArgs() {
		final CharArray array = new CharArray();
		assertEquals(0, array.size());
		assertEquals(0, array.copyArray().length);
	}

	/** Tests {@link CharArray#CharArray(int)}. */
	@Test
	public void testConstructorSize() {
		final int size = 24;
		final CharArray array = new CharArray(size);
		assertEquals(size, array.size());
		assertEquals(size, array.copyArray().length);
	}

	/** Tests {@link CharArray#CharArray(char[])}. */
	@Test
	public void testConstructorArray() {
		final char[] raw = { 3, 5, 8, 13, 21 };
		final CharArray array = new CharArray(raw);
		assertSame(raw, array.getArray());
		assertEquals(raw.length, array.size());
		for (int i = 0; i < raw.length; i++) {
			assertEquals("@" + i, raw[i], array.getValue(i));
		}
		assertArrayEquals(raw, array.copyArray());
	}

	/** Tests {@link CharArray#addValue(char)}. */
	@Test
	public void testAddValue() {
		final char[] raw = { 3, 5, 8, 13, 21 };
		final CharArray array = new CharArray(raw.clone());
		final char e6 = 1, e7 = 2;
		array.addValue(e6);
		array.addValue(e7);
		assertEquals(raw.length + 2, array.size());
		for (int i = 0; i < raw.length; i++) {
			assertEquals("@" + i, raw[i], array.getValue(i));
		}
		assertEquals(e6, array.getValue(5));
		assertEquals(e7, array.getValue(6));
	}

	/** Tests {@link CharArray#removeValue(char)}. */
	public void testRemoveValue() {
		final char[] raw = { 3, 5, 8, 13, 21 };
		final CharArray array = new CharArray(raw.clone());
		assertEquals(raw.length, array.size());
		array.removeValue(raw[0]);
		assertEquals(raw.length - 1, array.size());
		array.removeValue(raw[2]);
		assertEquals(raw.length - 2, array.size());
		array.removeValue(raw[4]);
		assertEquals(raw.length - 3, array.size());
		assertEquals(raw[1], array.getValue(0));
		assertEquals(raw[3], array.getValue(1));
	}

	/** Tests {@link CharArray#getValue(int)}. */
	public void testGetValue() {
		final char[] raw = { 3, 5, 8, 13, 21 };
		final CharArray array = new CharArray(raw.clone());
		for (int i = 0; i < raw.length; i++) {
			assertEquals("@" + i, raw[i], array.getValue(i));
		}
	}

	/** Tests {@link CharArray#setValue(int, char)}. */
	@Test
	public void testSetValue() {
		final char[] raw = { 3, 5, 8, 13, 21 };
		final CharArray array = new CharArray(raw.clone());
		final char e0 = 7, e2 = 1, e4 = 2;
		array.setValue(0, e0);
		array.setValue(2, e2);
		array.setValue(4, e4);
		assertEquals(raw.length, array.size());
		assertEquals(e0, array.getValue(0));
		assertEquals(raw[1], array.getValue(1));
		assertEquals(e2, array.getValue(2));
		assertEquals(raw[3], array.getValue(3));
		assertEquals(e4, array.getValue(4));
	}

	/** Tests {@link CharArray#addValue(int, char)}. */
	@Test
	public void testAddValueIndex() {
		final char[] raw = { 3, 5, 8, 13, 21 };
		final CharArray array = new CharArray(raw.clone());
		final char e0 = 7, e4 = 1, e7 = 2;
		array.addValue(0, e0);
		array.addValue(4, e4);
		array.addValue(7, e7);
		assertEquals(raw.length + 3, array.size());
		assertEquals(e0, array.getValue(0));
		assertEquals(raw[0], array.getValue(1));
		assertEquals(raw[1], array.getValue(2));
		assertEquals(raw[2], array.getValue(3));
		assertEquals(e4, array.getValue(4));
		assertEquals(raw[3], array.getValue(5));
		assertEquals(raw[4], array.getValue(6));
		assertEquals(e7, array.getValue(7));
	}

	/** Tests {@link CharArray#remove(int)}. */
	public void testRemoveIndex() {
		final char[] raw = { 3, 5, 8, 13, 21 };
		final CharArray array = new CharArray(raw.clone());
		assertEquals(raw.length, array.size());
		array.remove(0);
		assertEquals(raw.length - 1, array.size());
		array.remove(2);
		assertEquals(raw.length - 2, array.size());
		array.remove(4);
		assertEquals(raw.length - 3, array.size());
		assertEquals(raw[1], array.getValue(0));
		assertEquals(raw[3], array.getValue(1));
	}

	/** Tests {@link CharArray#indexOf(char)}. */
	@Test
	public void testIndexOf() {
		final char[] raw = { 3, 5, 8, 13, 21 };
		final CharArray array = new CharArray(raw.clone());
		for (int i = 0; i < raw.length; i++) {
			assertEquals("@" + i, i, array.indexOf(raw[i]));
		}
		assertEquals(-1, array.indexOf(-1));
		assertEquals(-1, array.indexOf(0));
		assertEquals(-1, array.indexOf(1));
		assertEquals(-1, array.indexOf(Integer.MAX_VALUE));
		assertEquals(-1, array.indexOf(Integer.MIN_VALUE));
	}

	/** Tests {@link CharArray#lastIndexOf(char)}. */
	@Test
	public void testLastIndexOf() {
		final char[] raw = { 3, 5, 8, 13, 21 };
		final CharArray array = new CharArray(raw.clone());
		for (int i = 0; i < raw.length; i++) {
			assertEquals("@" + i, i, array.lastIndexOf(raw[i]));
		}
		assertEquals(-1, array.lastIndexOf(-1));
		assertEquals(-1, array.lastIndexOf(0));
		assertEquals(-1, array.lastIndexOf(1));
		assertEquals(-1, array.lastIndexOf(Integer.MAX_VALUE));
		assertEquals(-1, array.lastIndexOf(Integer.MIN_VALUE));
	}

	/** Tests {@link CharArray#contains(char)}. */
	@Test
	public void testContains() {
		final char[] raw = { 3, 5, 8, 13, 21 };
		final CharArray array = new CharArray(raw.clone());
		for (int i = 0; i < raw.length; i++) {
			assertTrue("@" + i, array.contains(raw[i]));
		}
		assertFalse(array.contains(-1));
		assertFalse(array.contains(0));
		assertFalse(array.contains(1));
		assertFalse(array.contains(Integer.MAX_VALUE));
		assertFalse(array.contains(Integer.MIN_VALUE));
	}

	/**
	 * Tests {@link CharArray#getArray()} and
	 * {@link CharArray#setArray(char[])}.
	 */
	@Test
	public void testSetArray() {
		final CharArray array = new CharArray();
		final char[] raw = { 1, 2, 3, 5, 8, 13, 21 };
		array.setArray(raw);
		assertSame(raw, array.getArray());
	}

	/** Tests {@link CharArray#insert(int, int)}. */
	@Test
	public void testInsert() {
		final char[] raw = { 3, 5, 8, 13, 21 };
		testInsert(new CharArray(raw));
	}

	/** Tests {@link CharArray#delete(int, int)}. */
	@Test
	public void testDelete() {
		final char[] raw = { 3, 5, 8, 13, 21 };
		testDelete(new CharArray(raw));
	}

	/** Tests {@link CharArray#get(int)}. */
	@Test
	public void testGet() {
		final char[] raw = { 3, 5, 8, 13, 21 };
		final CharArray array = new CharArray(raw.clone());
		for (int i = 0; i < raw.length; i++) {
			assertEquals("@" + i, raw[i], array.get(i).charValue());
		}
	}

	/** Tests {@link CharArray#set(int, Character)}. */
	@Test
	public void testSet() {
		final char[] raw = { 3, 5, 8, 13, 21 };
		final CharArray array = new CharArray(raw.clone());
		final Character e0 = 7, e2 = 1, e4 = 2;
		array.set(0, e0);
		array.set(2, e2);
		array.set(4, e4);
		assertEquals(raw.length, array.size());
		assertEquals(e0, array.get(0));
		assertEquals(raw[1], array.getValue(1));
		assertEquals(e2, array.get(2));
		assertEquals(raw[3], array.getValue(3));
		assertEquals(e4, array.get(4));
	}

	/** Tests {@link CharArray#add(int, Character)}. */
	@Test
	public void testAdd() {
		final char[] raw = { 3, 5, 8, 13, 21 };
		final CharArray array = new CharArray(raw.clone());
		final Character e6 = 1, e7 = 2;
		array.add(e6);
		array.add(e7);
		assertEquals(raw.length + 2, array.size());
		for (int i = 0; i < raw.length; i++) {
			assertEquals("@" + i, raw[i], array.getValue(i));
		}
		assertEquals(e6, array.get(5));
		assertEquals(e7, array.get(6));
	}

	/** Tests {@link CharArray#indexOf(Object)}. */
	@Test
	public void testIndexOfBoxed() {
		final char[] raw = { 3, 5, 8, 13, 21 };
		final CharArray array = new CharArray(raw.clone());
		for (int i = 0; i < raw.length; i++) {
			assertEquals("@" + i, i, array.indexOf(new Character(raw[i])));
		}
		assertEquals(-1, array.indexOf(new Character('0')));
		assertEquals(-1, array.indexOf(new Character(Character.MAX_VALUE)));
		assertEquals(-1, array.indexOf(new Character(Character.MIN_VALUE)));
		assertEquals(-1, array.indexOf(null));
		assertEquals(-1, array.indexOf("Not a char"));
	}

	/** Tests {@link CharArray#lastIndexOf(Object)}. */
	@Test
	public void testLastIndexOfBoxed() {
		final char[] raw = { 3, 5, 8, 13, 21 };
		final CharArray array = new CharArray(raw.clone());
		for (int i = 0; i < raw.length; i++) {
			assertEquals("@" + i, i, array.lastIndexOf(new Character(raw[i])));
		}
		assertEquals(-1, array.lastIndexOf(new Character('0')));
		assertEquals(-1, array.lastIndexOf(new Character(Character.MAX_VALUE)));
		assertEquals(-1, array.lastIndexOf(new Character(Character.MIN_VALUE)));
		assertEquals(-1, array.lastIndexOf(null));
		assertEquals(-1, array.lastIndexOf("Not a char"));
	}

	/** Tests {@link CharArray#contains(Object)}. */
	@Test
	public void testContainsBoxed() {
		final char[] raw = { 3, 5, 8, 13, 21 };
		final CharArray array = new CharArray(raw.clone());
		for (int i = 0; i < raw.length; i++) {
			assertTrue("@" + i, array.contains(new Character(raw[i])));
		}
		assertFalse(array.contains(new Character('0')));
		assertFalse(array.contains(new Character(Character.MAX_VALUE)));
		assertFalse(array.contains(new Character(Character.MIN_VALUE)));
		assertFalse(array.contains(null));
		assertFalse(array.contains("Not a char"));
	}

	/** Tests {@link CharArray#remove(Object)}. */
	@Test
	public void testRemove() {
		final char[] raw = { 3, 5, 8, 13, 21 };
		final CharArray array = new CharArray(raw.clone());
		assertEquals(raw.length, array.size());
		array.remove(new Character(raw[0]));
		assertEquals(raw.length - 1, array.size());
		array.remove(new Character(raw[2]));
		assertEquals(raw.length - 2, array.size());
		array.remove(new Character(raw[4]));
		assertEquals(raw.length - 3, array.size());
		assertEquals(raw[1], array.getValue(0));
		assertEquals(raw[3], array.getValue(1));
	}

	/** Tests {@link CharArray#containsAll(Collection)}. */
	@Test
	public void testContainsAll() {
		final char[] raw = { 3, 5, 8, 13, 21 };
		final CharArray array = new CharArray(raw.clone());

		final ArrayList<Character> list = new ArrayList<>();
		assertTrue(array.containsAll(list));
		list.add((char) 13);
		assertTrue(array.containsAll(list));
		list.add((char) 1);
		assertFalse(array.containsAll(list));

		final CharArray yes = new CharArray(new char[] { 3, 8, 21 });
		assertTrue(array.containsAll(yes));

		final CharArray no = new CharArray(new char[] { 5, 13, 1 });
		assertFalse(array.containsAll(no));
	}

	/** Tests {@link CharArray#addAll(int, Collection)}. */
	@Test
	public void testAddAll() {
		final char[] raw = { 3, 5, 8, 13, 21 };
		final CharArray array = new CharArray(raw.clone());
		final char[] add = { 1, 7 };
		final CharArray toAdd = new CharArray(add.clone());
		final int index = 3;
		array.addAll(index, toAdd);
		for (int i = 0; i < index; i++) {
			assertEquals(raw[i], array.getValue(i));
		}
		for (int i = index; i < index + add.length; i++) {
			assertEquals(add[i - index], array.getValue(i));
		}
		for (int i = index + add.length; i < raw.length + add.length; i++) {
			assertEquals(raw[i - add.length], array.getValue(i));
		}
	}

	/** Tests {@link CharArray#removeAll}. */
	@Test
	public void testRemoveAll() {
		final char[] raw = { 3, 5, 8, 13, 21 };
		final CharArray array = new CharArray(raw.clone());
		final CharArray toRemove = new CharArray(new char[] { 3, 8, 21 });
		assertEquals(raw.length, array.size());
		array.removeAll(toRemove);
		assertEquals(raw.length - 3, array.size());
		assertEquals(raw[1], array.getValue(0));
		assertEquals(raw[3], array.getValue(1));
	}

}
