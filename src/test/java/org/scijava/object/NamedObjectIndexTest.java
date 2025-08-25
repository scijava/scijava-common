/*-
 * #%L
 * SciJava Common shared library for SciJava software.
 * %%
 * Copyright (C) 2009 - 2025 SciJava developers.
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

package org.scijava.object;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

public class NamedObjectIndexTest {

	@Test
	public void testNamedObjects() {
		NamedObjectIndex<String> index = new NamedObjectIndex<>(String.class);
		String obj1 = "obj1";
		String name1 = "name1";
		String obj2 = "obj1";
		String name2 = "name1";
		assertTrue(index.add(obj1, name1));
		assertTrue(index.add(obj2, String.class, name2, false));
		assertTrue(index.contains(obj1));
		assertTrue(index.contains(obj2));
		assertEquals(name1, index.getName(obj1));
		assertEquals(name2, index.getName(obj2));
		assertTrue(index.remove(obj1));
		assertTrue(index.remove(obj2));
		assertFalse(index.contains(obj1));
		assertFalse(index.contains(obj2));
	}

	@Test
	public void testNullNames() {
		NamedObjectIndex<String> index = new NamedObjectIndex<>(String.class);
		String obj1 = "object1";
		String name1 = null;
		String obj2 = "object2";
		String name2 = "";
		assertTrue(index.add(obj1, name1));
		assertTrue(index.add(obj2, name2));
		assertEquals(name1, index.getName(obj1));
		assertEquals(name2, index.getName(obj2));
	}
}
