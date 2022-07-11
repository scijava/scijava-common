/*-
 * #%L
 * SciJava Common shared library for SciJava software.
 * %%
 * Copyright (C) 2009 - 2022 SciJava developers.
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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests {@link StringToNumberConverter}
 *
 * @author Gabriel Selzer
 */
public class StringToNumberConverterTest {

	Converter<String, Number> conv;

	@Before
	public void setUp() {
		conv = new StringToNumberConverter();
	}

	@Test
	public void stringToByteTest() {
		String s = "0";
		Assert.assertTrue(conv.canConvert(s, Byte.class));
		Assert.assertEquals(new Byte((byte) 0), conv.convert(s, Byte.class));
	}

	@Test
	public void stringToPrimitiveByteTest() {
		String s = "0";
		Assert.assertTrue(conv.canConvert(s, byte.class));
		Assert.assertEquals(0, (int) conv.convert(s, byte.class));
	}

	@Test
	public void stringToShortTest() {
		String s = "0";
		Assert.assertTrue(conv.canConvert(s, Short.class));
		Assert.assertEquals(new Short((short) 0), conv.convert(s, Short.class));
	}

	@Test
	public void stringToPrimitiveShortTest() {
		String s = "0";
		Assert.assertTrue(conv.canConvert(s, short.class));
		Assert.assertEquals(0, (int) conv.convert(s, short.class));
	}

	@Test
	public void stringToIntegerTest() {
		String s = "0";
		Assert.assertTrue(conv.canConvert(s, Integer.class));
		Assert.assertEquals(new Integer(0), conv.convert(s, Integer.class));
	}

	@Test
	public void stringToPrimitiveIntegerTest() {
		String s = "0";
		Assert.assertTrue(conv.canConvert(s, int.class));
		Assert.assertEquals(0, (int) conv.convert(s, int.class));
	}

	@Test
	public void stringToLongTest() {
		String s = "0";
		Assert.assertTrue(conv.canConvert(s, Long.class));
		Assert.assertEquals(new Long(0), conv.convert(s, Long.class));
	}

	@Test
	public void stringToPrimitiveLongTest() {
		String s = "0";
		Assert.assertTrue(conv.canConvert(s, long.class));
		Assert.assertEquals(0L, (long) conv.convert(s, long.class));
	}

	@Test
	public void stringToFloatTest() {
		String s = "0";
		Assert.assertTrue(conv.canConvert(s, Float.class));
		Assert.assertEquals(new Float(0), conv.convert(s, Float.class));
	}

	@Test
	public void stringToPrimitiveFloat() {
		String s = "0";
		Assert.assertTrue(conv.canConvert(s, float.class));
		Assert.assertEquals(0f, conv.convert(s, float.class), 1e-6);
	}

	@Test
	public void stringToDoubleTest() {
		String s = "0";
		Assert.assertTrue(conv.canConvert(s, Double.class));
		Assert.assertEquals(new Double(0), conv.convert(s, Double.class));
	}

	@Test
	public void stringToPrimitiveDouble() {
		String s = "0";
		Assert.assertTrue(conv.canConvert(s, double.class));
		Assert.assertEquals(0d, conv.convert(s, double.class), 1e-6);
	}

	@Test
	public void stringToNumberTest() {
		String s = "0";
		Assert.assertTrue(conv.canConvert(s, Number.class));
		Assert.assertEquals(0d, conv.convert(s, Number.class));
	}

	@Test
	public void invalidStringToNumberTest() {
		String s = "invalid";
		Assert.assertFalse(conv.canConvert(s, Number.class));
		Assert.assertThrows(IllegalArgumentException.class, () -> conv.convert(s,
			Number.class));
	}
}
