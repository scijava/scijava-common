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

package org.scijava.main;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.scijava.Context;
import org.scijava.console.ConsoleService;

/**
 * Tests {@link MainService}.
 *
 * @author Curtis Rueden
 */
public class MainServiceTest {

	private MainService mainService;

	@Before
	public void setUp() {
		MathMain.resultCount = 0;
		final Context context = new Context();
		mainService = context.service(MainService.class);
	}

	@After
	public void tearDown() {
		mainService.context().dispose();
	}

	/**
	 * Tests {@link MainService#execMains()},
	 * {@link MainService#addMain(String, String...)} and
	 * {@link MainService#getMains()}.
	 */
	@Test
	public void testMainService() {
		final int mainCount0 = mainService.execMains();
		assertEquals(0, mainCount0);

		mainService.addMain(MathMain.class.getName(), "12.3", "/", "4.56");

		final int mainCount1 = mainService.execMains();
		assertEquals(1, mainCount1);
		assertEquals(System.getProperty(key(0)), "2.697368421052632");
	}

	/** Tests usage of {@link MainService} via {@code --main} CLI arguments. */
	@Test
	public void testConsoleArgs() {
		assertEquals(0, mainService.getMains().length);

		final ConsoleService consoleService = mainService.context().service(
			ConsoleService.class);
		consoleService.processArgs("-Dmain.test.foo=bar", //
			"--main", "org.scijava.main.MainServiceTest$MathMain", "5", "+", "6", //
			"--", "-Dmain.test.whiz=bang", //
			"--main", "org.scijava.main.MainServiceTest$MathMain", "7", "-", "4");

		final MainService.Main[] m = mainService.getMains();
		assertEquals(2, m.length);
		assertEquals("org.scijava.main.MainServiceTest$MathMain", m[0].className());
		assertArrayEquals(new String[] {"5", "+", "6"}, m[0].args());
		assertEquals("org.scijava.main.MainServiceTest$MathMain", m[1].className());
		assertArrayEquals(new String[] {"7", "-", "4"}, m[1].args());

		final int mainCount = mainService.execMains();
		assertEquals(2, mainCount);
		assertEquals(System.getProperty(key(0)), "11.0");
		assertEquals(System.getProperty(key(1)), "3.0");

		assertEquals(System.getProperty("main.test.foo"), "bar");
		assertEquals(System.getProperty("main.test.whiz"), "bang");
	}

	// -- Helper methods --

	private static String key(final int index) {
		return MathMain.class.getName() + ":" + index;
	}

	// -- Helper classes --

	private static class MathMain {
		private static int resultCount = 0;
		@SuppressWarnings("unused")
		public static void main(final String[] args) {
			if (args.length != 3) {
				throw new IllegalArgumentException("Invalid args: " + args);
			}

			// compute a result from the arguments
			final double operand1 = Double.parseDouble(args[0]);
			final String operator = args[1];
			final double operand2 = Double.parseDouble(args[2]);
			final double result;
			if (operator.equals("+")) result = operand1 + operand2;
			else if (operator.equals("-")) result = operand1 - operand2;
			else if (operator.equals("*")) result = operand1 * operand2;
			else if (operator.equals("/")) result = operand1 / operand2;
			else throw new IllegalArgumentException("Unknown operator: " + operator);

			// save the result to a system property, for later checking
			final String key = MathMain.class.getName() + ":" + resultCount++;
			final String value = "" + result;
			System.setProperty(key, value);
		}
	}
}
