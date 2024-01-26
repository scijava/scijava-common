/*
 * #%L
 * SciJava Common shared library for SciJava software.
 * %%
 * Copyright (C) 2009 - 2024 SciJava developers.
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

import org.junit.Test;

/**
 * Tests {@link LongArray}.
 * 
 * @author Curtis Rueden
 * @author Johannes Schindelin
 */
public class LongArrayTest extends PrimitiveArrayTest {

	/** Tests {@link LongArray#LongArray()}. */
	@Test
	public void testConstructorNoArgs() {
		final LongArray array = new LongArray();
		assertEquals(0, array.size());
		assertEquals(0, array.copyArray().length);
	}

	/** Tests {@link LongArray#LongArray(int)}. */
	@Test
	public void testConstructorSize() {
		final int size = 24;
		final LongArray array = new LongArray(size);
		assertEquals(size, array.size());
		assertEquals(size, array.copyArray().length);
	}

	/** Tests {@link LongArray#LongArray(long[])}. */
	@Test
	public void testConstructorArray() {
		final long[] raw = { 3, 5, 8, 13, 21 };
		final LongArray array = new LongArray(raw);
		assertSame(raw, array.getArray());
		assertEquals(raw.length, array.size());
		for (int i = 0; i < raw.length; i++) {
			assertEquals("@" + i, raw[i], array.getValue(i));
		}
		assertArrayEquals(raw, array.copyArray());
	}

	/** Tests {@link LongArray#addValue(long)}. */
	@Test
	public void testAddValue() {
		final long[] raw = { 3, 5, 8, 13, 21 };
		final LongArray array = new LongArray(raw.clone());
		final long e6 = 1, e7 = 2;
		array.addValue(e6);
		array.addValue(e7);
		assertEquals(raw.length + 2, array.size());
		for (int i = 0; i < raw.length; i++) {
			assertEquals("@" + i, raw[i], array.getValue(i));
		}
		assertEquals(e6, array.getValue(5));
		assertEquals(e7, array.getValue(6));
	}

	/** Tests {@link LongArray#removeValue(long)}. */
	public void testRemoveValue() {
		final long[] raw = { 3, 5, 8, 13, 21 };
		final LongArray array = new LongArray(raw.clone());
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

	/** Tests {@link LongArray#getValue(int)}. */
	public void testGetValue() {
		final long[] raw = { 3, 5, 8, 13, 21 };
		final LongArray array = new LongArray(raw.clone());
		for (int i = 0; i < raw.length; i++) {
			assertEquals("@" + i, raw[i], array.getValue(i));
		}
	}

	/** Tests {@link LongArray#setValue(int, long)}. */
	@Test
	public void testSetValue() {
		final long[] raw = { 3, 5, 8, 13, 21 };
		final LongArray array = new LongArray(raw.clone());
		final long e0 = 7, e2 = 1, e4 = 2;
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

	/** Tests {@link LongArray#addValue(int, long)}. */
	@Test
	public void testAddValueIndex() {
		final long[] raw = { 3, 5, 8, 13, 21 };
		final LongArray array = new LongArray(raw.clone());
		final long e0 = 7, e4 = 1, e7 = 2;
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

	/** Tests {@link LongArray#remove(int)}. */
	public void testRemoveIndex() {
		final long[] raw = { 3, 5, 8, 13, 21 };
		final LongArray array = new LongArray(raw.clone());
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

	/** Tests {@link LongArray#indexOf(long)}. */
	@Test
	public void testIndexOf() {
		final long[] raw = { 3, 5, 8, 13, 21 };
		final LongArray array = new LongArray(raw.clone());
		for (int i = 0; i < raw.length; i++) {
			assertEquals("@" + i, i, array.indexOf(raw[i]));
		}
		assertEquals(-1, array.indexOf(-1));
		assertEquals(-1, array.indexOf(0));
		assertEquals(-1, array.indexOf(1));
		assertEquals(-1, array.indexOf(Long.MAX_VALUE));
		assertEquals(-1, array.indexOf(Long.MIN_VALUE));
	}

	/** Tests {@link LongArray#lastIndexOf(long)}. */
	@Test
	public void testLastIndexOf() {
		final long[] raw = { 3, 5, 8, 13, 21 };
		final LongArray array = new LongArray(raw.clone());
		for (int i = 0; i < raw.length; i++) {
			assertEquals("@" + i, i, array.lastIndexOf(raw[i]));
		}
		assertEquals(-1, array.lastIndexOf(-1));
		assertEquals(-1, array.lastIndexOf(0));
		assertEquals(-1, array.lastIndexOf(1));
		assertEquals(-1, array.lastIndexOf(Long.MAX_VALUE));
		assertEquals(-1, array.lastIndexOf(Long.MIN_VALUE));
	}

	/** Tests {@link LongArray#contains(long)}. */
	@Test
	public void testContains() {
		final long[] raw = { 3, 5, 8, 13, 21 };
		final LongArray array = new LongArray(raw.clone());
		for (int i = 0; i < raw.length; i++) {
			assertTrue("@" + i, array.contains(raw[i]));
		}
		assertFalse(array.contains(-1));
		assertFalse(array.contains(0));
		assertFalse(array.contains(1));
		assertFalse(array.contains(Long.MAX_VALUE));
		assertFalse(array.contains(Long.MIN_VALUE));
	}

	/**
	 * Tests {@link LongArray#getArray()} and
	 * {@link LongArray#setArray(long[])}.
	 */
	@Test
	public void testSetArray() {
		final LongArray array = new LongArray();
		final long[] raw = { 1, 2, 3, 5, 8, 13, 21 };
		array.setArray(raw);
		assertSame(raw, array.getArray());
	}

	/** Tests {@link LongArray#insert(int, int)}. */
	@Test
	public void testInsert() {
		final long[] raw = { 3, 5, 8, 13, 21 };
		testInsert(new LongArray(raw));
	}

	/** Tests {@link LongArray#delete(int, int)}. */
	@Test
	public void testDelete() {
		final long[] raw = { 3, 5, 8, 13, 21 };
		testDelete(new LongArray(raw));
	}

	/** Tests {@link LongArray#get(int)}. */
	@Test
	public void testGet() {
		final long[] raw = { 3, 5, 8, 13, 21 };
		final LongArray array = new LongArray(raw.clone());
		for (int i = 0; i < raw.length; i++) {
			assertEquals("@" + i, raw[i], array.get(i).longValue());
		}
	}

	/** Tests {@link LongArray#set(int, Long)}. */
	@Test
	public void testSet() {
		final long[] raw = { 3, 5, 8, 13, 21 };
		final LongArray array = new LongArray(raw.clone());
		final Long e0 = 7L, e2 = 1L, e4 = 2L;
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

	/** Tests {@link LongArray#add(int, Long)}. */
	@Test
	public void testAdd() {
		final long[] raw = { 3, 5, 8, 13, 21 };
		final LongArray array = new LongArray(raw.clone());
		final Long e6 = 1L, e7 = 2L;
		array.add(e6);
		array.add(e7);
		assertEquals(raw.length + 2, array.size());
		for (int i = 0; i < raw.length; i++) {
			assertEquals("@" + i, raw[i], array.getValue(i));
		}
		assertEquals(e6, array.get(5));
		assertEquals(e7, array.get(6));
	}

	/** Tests {@link LongArray#indexOf(Object)}. */
	@Test
	public void testIndexOfBoxed() {
		final long[] raw = { 3, 5, 8, 13, 21 };
		final LongArray array = new LongArray(raw.clone());
		for (int i = 0; i < raw.length; i++) {
			assertEquals("@" + i, i, array.indexOf(new Long(raw[i])));
		}
		assertEquals(-1, array.indexOf(new Long(-1)));
		assertEquals(-1, array.indexOf(new Long(0)));
		assertEquals(-1, array.indexOf(new Long(1)));
		assertEquals(-1, array.indexOf(new Long(Long.MAX_VALUE)));
		assertEquals(-1, array.indexOf(new Long(Long.MIN_VALUE)));
		assertEquals(-1, array.indexOf(null));
		assertEquals(-1, array.indexOf("Not a long"));
	}

	/** Tests {@link LongArray#lastIndexOf(Object)}. */
	@Test
	public void testLastIndexOfBoxed() {
		final long[] raw = { 3, 5, 8, 13, 21 };
		final LongArray array = new LongArray(raw.clone());
		for (int i = 0; i < raw.length; i++) {
			assertEquals("@" + i, i, array.lastIndexOf(new Long(raw[i])));
		}
		assertEquals(-1, array.lastIndexOf(new Long(-1)));
		assertEquals(-1, array.lastIndexOf(new Long(0)));
		assertEquals(-1, array.lastIndexOf(new Long(1)));
		assertEquals(-1, array.lastIndexOf(new Long(Long.MAX_VALUE)));
		assertEquals(-1, array.lastIndexOf(new Long(Long.MIN_VALUE)));
		assertEquals(-1, array.lastIndexOf(null));
		assertEquals(-1, array.lastIndexOf("Not a long"));
	}

	/** Tests {@link LongArray#contains(Object)}. */
	@Test
	public void testContainsBoxed() {
		final long[] raw = { 3, 5, 8, 13, 21 };
		final LongArray array = new LongArray(raw.clone());
		for (int i = 0; i < raw.length; i++) {
			assertTrue("@" + i, array.contains(new Long(raw[i])));
		}
		assertFalse(array.contains(new Long(-1)));
		assertFalse(array.contains(new Long(0)));
		assertFalse(array.contains(new Long(1)));
		assertFalse(array.contains(new Long(Long.MAX_VALUE)));
		assertFalse(array.contains(new Long(Long.MIN_VALUE)));
		assertFalse(array.contains(null));
		assertFalse(array.contains("Not a long"));
	}

	/** Tests {@link LongArray#remove(Object)}. */
	@Test
	public void testRemove() {
		final long[] raw = { 3, 5, 8, 13, 21 };
		final LongArray array = new LongArray(raw.clone());
		assertEquals(raw.length, array.size());
		array.remove(new Long(raw[0]));
		assertEquals(raw.length - 1, array.size());
		array.remove(new Long(raw[2]));
		assertEquals(raw.length - 2, array.size());
		array.remove(new Long(raw[4]));
		assertEquals(raw.length - 3, array.size());
		assertEquals(raw[1], array.getValue(0));
		assertEquals(raw[3], array.getValue(1));
	}

	/** Tests {@link LongArray#containsAll}. */
	@Test
	public void testContainsAll() {
		final long[] raw = { 3, 5, 8, 13, 21 };
		final LongArray array = new LongArray(raw.clone());

		final ArrayList<Long> list = new ArrayList<>();
		assertTrue(array.containsAll(list));
		list.add(13L);
		assertTrue(array.containsAll(list));
		list.add(1L);
		assertFalse(array.containsAll(list));

		final LongArray yes = new LongArray(new long[] { 3, 8, 21 });
		assertTrue(array.containsAll(yes));

		final LongArray no = new LongArray(new long[] { 5, 13, 1 });
		assertFalse(array.containsAll(no));
	}

	/** Tests {@link LongArray#addAll(int, java.util.Collection)}. */
	@Test
	public void testAddAll() {
		final long[] raw = { 3, 5, 8, 13, 21 };
		final LongArray array = new LongArray(raw.clone());
		final long[] add = { 1, 7 };
		final LongArray toAdd = new LongArray(add.clone());
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

	/** Tests {@link LongArray#removeAll}. */
	@Test
	public void testRemoveAll() {
		final long[] raw = { 3, 5, 8, 13, 21 };
		final LongArray array = new LongArray(raw.clone());
		final LongArray toRemove = new LongArray(new long[] { 3, 8, 21 });
		assertEquals(raw.length, array.size());
		array.removeAll(toRemove);
		assertEquals(raw.length - 3, array.size());
		assertEquals(raw[1], array.getValue(0));
		assertEquals(raw[3], array.getValue(1));
	}

}
