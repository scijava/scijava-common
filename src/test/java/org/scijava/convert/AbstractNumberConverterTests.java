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

package org.scijava.convert;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.scijava.util.ConversionUtils;

/**
 * Tests converter plugins that convert from primitive numeric types to other
 * primitive numeric types.
 * 
 * @author Alison Walter
 */
public abstract class AbstractNumberConverterTests {

	protected NumberToNumberConverter<?, ?> converter = getConverter();
	protected Class<?> srcType = converter.getInputType();
	protected Class<?> destType = converter.getOutputType();

	public abstract Number getSrc();

	public abstract NumberToNumberConverter<?, ?> getConverter();

	public abstract Number getExpectedValue();

	public abstract Number getInvalidInput();

	public abstract Class<?> getInvalidOutput();

	/**
	 * Test case for the wrapper classes
	 */
	@Test
	public void testWrapper() {
		final Number src = getSrc();
		final Number expect = getExpectedValue();
		assertTrue(destType.isInstance(converter.convert(src, destType)));
		assertEquals(expect, converter.convert(src, destType));
	}

	/**
	 * Test case for primitive values
	 */
	@Test
	public void testPrimitive() {
		final Number src = getSrc();
		final Number expect = getExpectedValue();
		assertEquals(expect, converter.convert(src, ConversionUtils
			.getPrimitiveType(destType)));
	}

	/**
	 * Test case for null input
	 */
	@Test
	public void nullInput() {
		iae("Null input", null, null);
	}

	/**
	 * Test case for invalid input class
	 */
	@Test
	public void incorrectInputType() {
		final Number input = getInvalidInput();
		final String message =
			"Expected input of type " + srcType.getSimpleName() + ", but got " +
				input.getClass().getSimpleName();
		iae(message, input, destType);
	}

	/**
	 * Test case for invalid output class
	 */
	@Test
	public void incorrectOutputType() {
		final Class<?> output = getInvalidOutput();
		final Number src = getSrc();
		final String message =
			"Expected output class of " + destType.getSimpleName() + ", but got " +
				output.getSimpleName();
		iae(message, src, output);
	}

	@Rule
	public ExpectedException exception = ExpectedException.none();

	// helper methods
	protected void
		iae(final String message, final Number src, final Class<?> dest)
	{
		exception(IllegalArgumentException.class, message, src, dest);
	}

	protected void exception(final Class<? extends Throwable> excType,
		final String message, final Number src, final Class<?> dest)
	{
		exception.expect(excType);
		exception.expectMessage(message);
		converter.convert(src, dest);
	}
}
