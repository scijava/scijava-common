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

package org.scijava.main.run;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests {@link MainCodeRunner}.
 * 
 * @author Curtis Rueden
 */
public class MainCodeRunnerTest {

	private MainCodeRunner runner;

	@Before
	public void setUp() {
		runner = new MainCodeRunner();
	}

	@Test
	public void testRunList() throws InvocationTargetException {
		runner.run(Counter.class);
		assertEquals(Counter.counter, 0);
		runner.run(Counter.class, "a");
		assertEquals(Counter.counter, 1);
		runner.run(Counter.class, "b", "c");
		assertEquals(Counter.counter, 3);
		runner.run(Counter.class, "d", "e", "f");
		assertEquals(Counter.counter, 6);
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testRunMap() throws InvocationTargetException {
		runner.run(Counter.class, new HashMap<String, Object>());
	}

	@Test
	public void testSupports() {
		assertTrue(runner.supports(Counter.class));
		assertTrue(runner.supports(Counter.class.getName()));

		assertFalse(runner.supports(getClass()));
		assertFalse(runner.supports("Not an actual class"));
		assertFalse(runner.supports(0));
	}

	// -- Helper classes --

	public static class Counter {

		public static int counter;

		public static void main(final String[] args) {
			counter += args.length;
		}
	}
}
