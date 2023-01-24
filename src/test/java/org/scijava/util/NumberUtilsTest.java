/*-
 * #%L
 * SciJava Common shared library for SciJava software.
 * %%
 * Copyright (C) 2009 - 2023 SciJava developers.
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

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Tests {@link NumberUtils} functionality
 *
 * @author Gabriel Selzer
 */
public class NumberUtilsTest {

	/** Tests {@link NumberUtils#getMinimumNumber(Class)} */
	@Test
	public void testGetMinimumNumber() {
		assertEquals(Byte.MIN_VALUE, NumberUtils.getMinimumNumber(Byte.class));
		assertEquals(Short.MIN_VALUE, NumberUtils.getMinimumNumber(Short.class));
		assertEquals(Integer.MIN_VALUE, NumberUtils.getMinimumNumber(
			Integer.class));
		assertEquals(Long.MIN_VALUE, NumberUtils.getMinimumNumber(Long.class));
		assertEquals(-Float.MAX_VALUE, NumberUtils.getMinimumNumber(Float.class));
		assertEquals(-Double.MAX_VALUE, NumberUtils.getMinimumNumber(Double.class));
		// Number's minimum value should be the smallest of all the above -> Double
		assertEquals(-Double.MAX_VALUE, NumberUtils.getMinimumNumber(Number.class));
	}

	/** Tests {@link NumberUtils#getMaximumNumber(Class)} */
	@Test
	public void testGetMaximumNumber() {
		assertEquals(Byte.MAX_VALUE, NumberUtils.getMaximumNumber(Byte.class));
		assertEquals(Short.MAX_VALUE, NumberUtils.getMaximumNumber(Short.class));
		assertEquals(Integer.MAX_VALUE, NumberUtils.getMaximumNumber(
			Integer.class));
		assertEquals(Long.MAX_VALUE, NumberUtils.getMaximumNumber(Long.class));
		assertEquals(Float.MAX_VALUE, NumberUtils.getMaximumNumber(Float.class));
		assertEquals(Double.MAX_VALUE, NumberUtils.getMaximumNumber(Double.class));
		// Number's maximum value should be the largest of all the above -> Double
		assertEquals(Double.MAX_VALUE, NumberUtils.getMaximumNumber(Number.class));
	}
}
