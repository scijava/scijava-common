/*
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

package org.scijava.convert;

import static org.junit.Assert.assertArrayEquals;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.scijava.Context;
import org.scijava.parse.ParseService;

/**
 * Tests {@link StringToArrayConverter}.
 *
 * @author Gabriel Selzer
 */
public class StringToArrayConverterTest {

	private final StringToArrayConverter converter = new StringToArrayConverter();
	private ConvertService convertService;
	private Context context;

	@Before
	public void setUp() {
		context = new Context(ParseService.class, ConvertService.class);
		context.inject(converter);
		convertService = context.getService(ConvertService.class);
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
		// Array types for array conversions
		List<Class<?>> classes = Arrays.asList( //
			byte[].class, //
			Byte[].class, //
			short[].class, //
			Short[].class, //
			int[].class, //
			Integer[].class, //
			long[].class, //
			Long[].class, //
			float[].class, //
			Float[].class, //
			double[].class, //
			Double[].class //
		);
		// String input
		String s = "{0, 1, 2}";
		for (Class<?> arrayClass : classes) {
			// Ensure our Converter can do the conversion
			Assert.assertTrue(converter.canConvert(s, arrayClass));
			// Do the conversion
			Object converted = converter.convert(s, arrayClass);
			// Ensure the output is the expected type
			Assert.assertEquals(arrayClass, converted.getClass());
			Class<?> c = arrayClass.getComponentType();
			for (int i = 0; i < 3; i++) {
				// Ensure element correctness
				Object expected = convertService.convert(i, c);
				Assert.assertEquals(expected, Array.get(converted, i));
			}
		}
	}

	/**
	 * Tests the ability of {@link StringToArrayConverter} in converting
	 * 2-dimensional arrays
	 */
	@Test
	public void test2DArrayConversion() {
		String s = "{{0, 1}, {2, 3}}";
		Assert.assertTrue(converter.canConvert(s, byte[][].class));
		byte[][] actual = converter.convert(s, byte[][].class);
		Assert.assertEquals(0, actual[0][0]);
		Assert.assertEquals(1, actual[0][1]);
		Assert.assertEquals(2, actual[1][0]);
		Assert.assertEquals(3, actual[1][1]);
	}

	/**
	 * Tests the ability of {@link StringToArrayConverter} in converting
	 * 3-dimensional arrays
	 */
	@Test
	public void test3DArrayConversion() {
		String s = "{{{0, 1}, {1, 2}},{{1, 2}, {2, 3}}}";
		Assert.assertTrue(converter.canConvert(s, byte[][][].class));
		byte[][][] actual = converter.convert(s, byte[][][].class);

		for (int i = 0; i < 2; i++)
			for (int j = 0; j < 2; j++)
				for (int k = 0; k < 2; k++)
					Assert.assertEquals(i + j + k, actual[i][j][k]);
	}

	/**
	 * Tests the ability of {@link StringToArrayConverter} in converting empty
	 * arrays
	 */
	@Test
	public void testEmptyArrayConversion() {
		String s = "{}";
		Assert.assertTrue(converter.canConvert(s, byte[].class));
		byte[] actual = converter.convert(s, byte[].class);
		Assert.assertEquals(0, actual.length);
	}

	/**
	 * Tests the special case of {@link String}s
	 */
	@Test
	public void testStringArrayConversion() {
		String[] expected = new String[] { //
			"{foo", "bar}", //
			"ha\nha", //
			"foo,bar", //
			"lol\"lol", //
			"foo\\\"bar" //
		};
		String converted = convertService.convert(expected, String.class);
		Assert.assertEquals("{\"{foo\", \"bar}\", " + //
			"\"ha\nha\", " + //
			"\"foo,bar\", " + //
			"\"lol\\\"lol\", " + //
			"\"foo\\\\\\\"bar\"}", converted);
		String[] actual = convertService.convert(converted, String[].class);
		Assert.assertArrayEquals(expected, actual);
	}

	/**
	 * Tests the special case of {@link Character}s
	 */
	@Test
	public void testCharacterArrayConversion() {
		Character[] expected = new Character[] { //
			's', //
			'\n', //
			',', //
			'{', //
			'}' //
		};
		String converted = convertService.convert(expected, String.class);
		Assert.assertEquals("{\"s\", \"\n\", \",\", \"{\", \"}\"}", converted);
		Character[] actual = convertService.convert(converted, Character[].class);
		Assert.assertArrayEquals(expected, actual);
	}

	@Test
	public void testStringToDoubleArraySingleValue() {
		assertArrayEquals(new double[] { 5 }, converter.convert("5",
			double[].class), 0);
		assertArrayEquals(new Double[] { 6d }, converter.convert("6",
			Double[].class));
		assertArrayEquals(new double[][] { { 7 } }, converter.convert("7",
			double[][].class));
		assertArrayEquals(new Double[][] { { 8d } }, converter.convert("8",
			Double[][].class));

		assertArrayEquals(new double[] { 0 }, converter.convert("spinach",
			double[].class), 0);
		assertArrayEquals(new Double[] { null }, converter.convert("kale",
			Double[].class));
		assertArrayEquals(new double[][] { { 0 } }, converter.convert("broccoli",
			double[][].class));
		assertArrayEquals(new Double[][] { { null } }, converter.convert("lettuce",
			Double[][].class));
	}

	@Test
	public void testStringToDoubleArray1D() {
		// all numbers
		assertArrayEquals(new double[] { 0, 1, 2, 3 }, converter.convert(
			"{0, 1, 2, 3}", double[].class), 0);
		assertArrayEquals(new Double[] { 7d, 11d }, converter.convert("{7, 11}",
			Double[].class));
		assertArrayEquals(new Double[] { 0d, 1d, 2d, 3d }, converter.convert(
			"{0, 1, 2, 3}", Double[].class));

		// mixed numbers/non-numbers
		assertArrayEquals(new double[] { 0, 1, 0, 3 }, converter.convert(
			"{0, 1, kumquat, 3}", double[].class), 0);
		assertArrayEquals(new Double[] { 4d, null, 5d }, converter.convert(
			"{4, eggplant, 5}", Double[].class));

		// all non-numbers
		assertArrayEquals(new double[] { 0, 0, 0 }, converter.convert(
			"{uno, dos, tres}", double[].class), 0);
		assertArrayEquals(new Double[] { null, null, null, null }, converter
			.convert("{cuatro, cinco, seis, siete}", Double[].class));
	}

	@Test
	public void testStringToDoubleArray2D() {
		// all numbers
		assertArrayEquals(new double[][] { { 0, 1 }, { 2, 3, 4 } }, converter
			.convert("{{0, 1}, {2, 3, 4}}", double[][].class));
		assertArrayEquals(new Double[][] { { 7d, 11d }, { 13d, 17d, 19d } },
			converter.convert("{{7, 11}, {13, 17, 19}}", Double[][].class));
		assertArrayEquals(new Double[][] { { 0d, 1d }, { 2d, 3d } }, converter
			.convert("{{0, 1}, {2, 3}}", Double[][].class));

		// mixed numbers/non-numbers
		assertArrayEquals(new double[][] { { 0, 1 }, { 0, 3 } }, converter.convert(
			"{{0, 1}, {kumquat, 3}}", double[][].class));
		assertArrayEquals(new Double[][] { { 4d }, { null, 5d }, { null } },
			converter.convert("{{4}, {eggplant, 5}, {squash}}", Double[][].class));

		// all non-numbers
		assertArrayEquals(new double[][] { { 0 }, { 0, 0 } }, converter.convert(
			"{{uno}, {dos, tres}}", double[][].class));
		assertArrayEquals(new Double[][] { { null, null }, { null, null } },
			converter.convert("{{cuatro, cinco}, {seis, siete}}", Double[][].class));
	}

	@Test
	public void testStringToDoubleArray3D() {
		final Random r = new Random(0xDA7ABA5E);
		final double[][][] ds = new double[r.nextInt(10)][][];
		for (int i = 0; i < ds.length; i++) {
			ds[i] = new double[r.nextInt(10)][];
			for (int j = 0; j < ds[i].length; j++) {
				ds[i][j] = new double[r.nextInt(10)];
				for (int k = 0; k < ds[i][j].length; k++) {
					ds[i][j][k] = 10000 * r.nextDouble();
				}
			}
		}
		String ss = Arrays.deepToString(ds).replace('[', '{').replace(']', '}');
		assertArrayEquals(ds, converter.convert(ss, double[][][].class));
	}
}
