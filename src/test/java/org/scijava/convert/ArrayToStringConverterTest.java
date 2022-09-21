/*
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

import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.scijava.Context;
import org.scijava.parse.ParseService;

/**
 * Tests {@link ArrayToStringConverter}.
 * 
 * @author Gabriel Selzer
 */
public class ArrayToStringConverterTest {

	private final ArrayToStringConverter converter = new ArrayToStringConverter();
	private Context context;

	@Before
	public void setUp() {
		context = new Context(ConvertService.class, ParseService.class);
		context.inject(converter);
	}

	@After
	public void tearDown() {
		context.dispose();
	}

	/**
	 * Tests the ability of {@link StringToArrayConverter} in converting to arrays
	 * of various types
	 */
	@Test
	public void testArrayConversion() {
		// Component types for array conversions
		List<Object> arrays = Arrays.asList( //
			new byte[] { 1, 2, 3 }, //
			new Byte[] { 1, 2, 3 }, //
			new short[] { 1, 2, 3 }, //
			new Short[] { 1, 2, 3 }, //
			new int[] { 1, 2, 3 }, //
			new Integer[] { 1, 2, 3 }, //
			new long[] { 1, 2, 3 }, //
			new Long[] { 1L, 2L, 3L }, //
			new float[] { 1, 2, 3 }, //
			new Float[] { 1F, 2F, 3F }, //
			new double[] { 1, 2, 3 }, //
			new Double[] { 1., 2., 3. } //
		);
		// String expectation
		String sInt = "{1, 2, 3}";
		String sFloat = "{1.0, 2.0, 3.0}";
		for (Object array : arrays) {
			// Ensure our Converter can do the conversion
			Assert.assertTrue(converter.canConvert(array, String.class));
			// Do the conversion
			String converted = converter.convert(array, String.class);
			// Ensure correctness
			Assert.assertTrue(converted.equals(sInt) || converted.equals(sFloat));
		}
	}

	/**
	 * Tests the ability of {@link ArrayToStringConverter} in converting
	 * 2-dimensional arrays
	 */
	@Test
	public void test2DArrayConversion() {
		byte[][] arr = new byte[][] { new byte[] { 0, 1 }, new byte[] { 2, 3 } };
		Assert.assertTrue(converter.canConvert(arr, String.class));
		String actual = converter.convert(arr, String.class);
		String expected = "{{0, 1}, {2, 3}}";
		Assert.assertEquals(expected, actual);
	}

	/**
	 * Tests the ability of {@link ArrayToStringConverter} in converting
	 * 3-dimensional arrays
	 */
	@Test
	public void test3DArrayConversion() {
		byte[][][] arr = new byte[2][2][2];
		for (int i = 0; i < 2; i++)
			for (int j = 0; j < 2; j++)
				for (int k = 0; k < 2; k++)
					arr[i][j][k] = (byte) (i + j + k);

		Assert.assertTrue(converter.canConvert(arr, String.class));
		String actual = converter.convert(arr, String.class);
		String expected = "{{{0, 1}, {1, 2}}, {{1, 2}, {2, 3}}}";
		Assert.assertEquals(expected, actual);
	}

	/**
	 * Tests the ability of {@link ArrayToStringConverter} in converting empty
	 * arrays
	 */
	@Test
	public void testEmptyArrayConversion() {
		byte[] arr = new byte[0];
		Assert.assertTrue(converter.canConvert(arr, String.class));
		String actual = converter.convert(arr, String.class);
		String expected = "{}";
		Assert.assertEquals(expected, actual);
	}

	/**
	 * Tests the ability of {@link ArrayToStringConverter} and
	 * {@link StringToArrayConverter} to perform a cyclic conversion.
	 */
	@Test
	public void testCyclicConversion() {
		byte[] expected = new byte[] {1, 2, 3};
		// Do the first conversion
		ArrayToStringConverter c1 = new ArrayToStringConverter();
		context.inject(c1);
		String converted = c1.convert(expected, String.class);
		// Convert back
		StringToArrayConverter c2 = new StringToArrayConverter();
		context.inject(c2);
		byte[] actual = c2.convert(converted, byte[].class);
		Assert.assertArrayEquals(expected, actual);
	}

}
