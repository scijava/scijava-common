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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.Iterator;

import org.junit.Test;

/**
 * Tests the {@link LastRecentlyUsed} data structure.
 * 
 * @author Johannes Schindelin
 */
public class LastRecentlyUsedTest {

	@Test
	public void test() {
		int count = 3;
		final LastRecentlyUsed<String> lru = new LastRecentlyUsed<>(count);

		for (int i = 1; i <= count; i++) {
			lru.add("" + i);
		}

		int position = -1;
		for (int i = 1; i <= count; i++) {
			position = lru.next(position);
			assertEquals("" + i, lru.get(position));
		}
		position = lru.next(position);
		assertEquals(-1, position);

		for (int i = count; i >= 1; i--) {
			position = lru.previous(position);
			assertEquals("" + i, lru.get(position));
		}
		position = lru.previous(position);
		assertEquals(-1, position);
	}

	@Test
	public void testRemove() {
		final LastRecentlyUsed<String> lru = new LastRecentlyUsed<>(3);
		lru.add("a");
		lru.add("b");
		lru.add("c");

		lru.remove("b");

		Iterator<String> iter = lru.iterator();
		assertEquals("c", iter.next());
		assertEquals("a", iter.next());
		assertFalse(iter.hasNext());

		lru.remove("a");

		iter = lru.iterator();
		assertEquals("c", iter.next());
		assertFalse(iter.hasNext());

		lru.remove("a");

		iter = lru.iterator();
		assertEquals("c", iter.next());
		assertFalse(iter.hasNext());

		lru.remove("c");

		iter = lru.iterator();
		assertFalse(iter.hasNext());
	}
}
