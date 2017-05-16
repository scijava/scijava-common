/*
 * #%L
 * SciJava Common shared library for SciJava software.
 * %%
 * Copyright (C) 2009 - 2017 Board of Regents of the University of
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

package org.scijava.log;

import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.*;

/**
 * Tests {@link LogSource}
 */
public class LogSourceTest {

	@Test
	public void testRoot() {
		LogSource root = LogSource.newRoot();
		assertEquals(Collections.emptyList(), root.path());
		assertTrue(root.isRoot());
	}

	@Test
	public void testIsRoot() {
		LogSource source = LogSource.newRoot().subSource("sub");
		assertFalse(source.isRoot());
	}

	@Test
	public void testChildIsUnique() {
		String name = "foo";
		LogSource root = LogSource.newRoot();
		LogSource a = root.subSource(name);
		LogSource b = root.subSource(name);
		assertSame(a, b);
	}

	@Test
	public void testToString() {
		LogSource source = LogSource.newRoot().subSource("Hello").subSource("World");
		String result = source.toString();
		assertEquals("Hello:World", result);
	}

	@Test
	public void testRootToString() {
		LogSource source = LogSource.newRoot();
		String result = source.toString();
		assertEquals("", result);
	}

	@Test
	public void testName() {
		LogSource source = LogSource.newRoot().subSource("Hello").subSource("World");
		assertEquals("World", source.name());
	}

	@Test
	public void testParent() {
		LogSource root = LogSource.newRoot();
		LogSource source = root.subSource("sub");
		assertSame(root, source.parent());
	}
}