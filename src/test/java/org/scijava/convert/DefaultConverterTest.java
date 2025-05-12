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
package org.scijava.convert;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests {@link DefaultConverter}.
 * 
 * @author Curtis Rueden
 * */
public class DefaultConverterTest {
	private DefaultConverter converter;

	@Before
	public void setUp() {
		converter = new DefaultConverter();
	}

	@Test
	public void testObjectToObjectArray() {
		Object o = new Object();
		assertTrue(converter.canConvert(o, Object[].class));
		Object[] result = converter.convert(o, Object[].class);
		assertNotNull(result);
		assertEquals(1, result.length);
		assertSame(o, result[0]);
	}

	@Test
	public void testIntToObjectArray() {
		int v = 1;
		assertTrue(converter.canConvert(v, Object[].class));
		Object[] result = converter.convert(v, Object[].class);
		assertNotNull(result);
		assertEquals(1, result.length);
		assertSame(v, result[0]);
	}

	@Test
	public void testIntToPrimitiveIntArray() {
		int v = 2;
		assertTrue(converter.supports(new ConversionRequest(v, int[].class)));
		assertTrue(converter.canConvert(v, int[].class));
		int[] result = converter.convert(v, int[].class);
		assertNotNull(result);
		assertEquals(1, result.length);
		assertSame(v, result[0]);
	}

	@Test
	public void testIntToBoxedIntegerArray() {
		int v = 3;
		assertTrue(converter.canConvert(v, Integer[].class));
		Integer[] result = converter.convert(v, Integer[].class);
		assertNotNull(result);
		assertEquals(1, result.length);
		assertSame(v, result[0]);
	}

	@Test
	public void testByteToPrimitiveDoubleArray() {
		byte v = 4;
		assertTrue(converter.canConvert(v, double[].class));
		double[] result = converter.convert(v, double[].class);
		assertNotNull(result);
		assertEquals(1, result.length);
		assertEquals(v, result[0], 0.0);
	}

	@Test
	public void testByteToBoxedDoubleArray() {
		byte v = 4;
		assertTrue(converter.canConvert(v, Double[].class));
		Double[] result = converter.convert(v, Double[].class);
		assertNotNull(result);
		assertEquals(1, result.length);
		assertEquals(v, result[0], 0.0);
	}

	@Test
	public void testStringToObjectArray() {
		String s = "Pumpernickel";
		assertTrue(converter.canConvert(s, Object[].class));
		Object[] result = converter.convert(s, Object[].class);
		assertNotNull(result);
		assertEquals(1, result.length);
		assertSame(s, result[0]);
	}

	@Test
	public void testStringToStringArray() {
		String s = "smorgasbord";
		assertTrue(converter.canConvert(s, String[].class));
		String[] result = converter.convert(s, String[].class);
		assertNotNull(result);
		assertEquals(1, result.length);
		assertSame(s, result[0]);
	}

	@Test
	public void testObjectToCollection() throws NoSuchFieldException {
		@SuppressWarnings("unused")
		class Struct {
			private Collection<Object> collectionOfObjects;
			private List<Object> listOfObjects;
			private List<String> listOfStrings;
			private List<Double> listOfDoubles;
			private Set<Object> setOfObjects;
		}

		Type collectionOfObjectsType = Struct.class.getDeclaredField("collectionOfObjects").getGenericType();
		Type listOfObjectsType = Struct.class.getDeclaredField("listOfObjects").getGenericType();
		Type listOfStringsType = Struct.class.getDeclaredField("listOfStrings").getGenericType();
		Type listOfDoublesType = Struct.class.getDeclaredField("listOfDoubles").getGenericType();
		Type setOfObjectsType = Struct.class.getDeclaredField("setOfObjects").getGenericType();

		Object o = new Object();

		assertTrue(converter.canConvert(o, Collection.class));
		assertTrue(converter.canConvert(o, List.class));
		assertTrue(converter.canConvert(o, Set.class));

		assertCollection(o, o, converter.convert(o, Collection.class), Collection.class);
		assertCollection(o, o, converter.convert(o, List.class), List.class);
		assertCollection(o, o, converter.convert(o, Set.class), Set.class);

		assertTrue(converter.canConvert(o, collectionOfObjectsType));
		assertTrue(converter.canConvert(o, listOfObjectsType));
		assertTrue(converter.canConvert(o, listOfStringsType));
		assertTrue(converter.canConvert(o, listOfDoublesType));
		assertTrue(converter.canConvert(o, setOfObjectsType));

		assertCollection(o, o, converter.convert(o, collectionOfObjectsType), Collection.class);
		assertCollection(o, o, converter.convert(o, listOfObjectsType), List.class);
		assertCollection(o, o.toString(), converter.convert(o, listOfStringsType), List.class);
		assertCollection(o, null, converter.convert(o, listOfDoublesType), List.class);
		assertCollection(o, o, converter.convert(o, setOfObjectsType), Set.class);

		String s = "Thingamawhatsit";
		assertTrue(converter.canConvert(s, collectionOfObjectsType));
		assertTrue(converter.canConvert(s, listOfObjectsType));
		assertTrue(converter.canConvert(s, listOfStringsType));
		assertTrue(converter.canConvert(s, listOfDoublesType));
		assertTrue(converter.canConvert(s, setOfObjectsType));

		assertCollection(s, s, converter.convert(s, collectionOfObjectsType), Collection.class);
		assertCollection(s, s, converter.convert(s, listOfObjectsType), List.class);
		assertCollection(s, s, converter.convert(s, listOfStringsType), List.class);
		assertCollection(s, null, converter.convert(s, listOfDoublesType), List.class);
		assertCollection(s, s, converter.convert(s, setOfObjectsType), Set.class);

		// TODO: Test more things, covering the equivalent of all *To*Array above.
	}

	@Test
	public void testNumberToNumber() {
		double d = -5.6;
		assertTrue(converter.canConvert(d, int.class));
		assertEquals(-5, converter.convert(d, int.class), 0.0);
		// TODO: Test many more combinations of numeric types.
	}

	@Test
	public void testObjectToString() {
		Object friendly = new Object() {
			@Override
			public String toString() { return "Hello"; }
		};
		assertTrue(converter.canConvert(friendly, String.class));
		assertEquals("Hello", converter.convert(friendly, String.class));
	}

	@Test
	public void testStringToCharacter() {
		String plan = "Step 0: there is no plan";

		assertTrue(converter.canConvert(plan, char.class));
		assertEquals('S', (char) converter.convert(plan, char.class));

		assertTrue(converter.canConvert(plan, Character.class));
		assertEquals(new Character('S'), converter.convert(plan, Character.class));
	}

	@Test
	public void testStringToCharacterArray() {
		String plan = "Step 0: there is no plan";

		assertTrue(converter.canConvert(plan, char[].class));
		final char[] converted = converter.convert(plan, char[].class);
		assertArrayEquals(plan.toCharArray(), converted);

		// NB: Conversion to Character[] does not work the same way.
	}

	private enum Gem {
		RUBY, DIAMOND, EMERALD;
	}

	@Test
	public void testStringToEnum() {
		assertTrue(converter.canConvert("RUBY", Gem.class));
		assertTrue(converter.canConvert("DIAMOND", Gem.class));
		assertTrue(converter.canConvert("EMERALD", Gem.class));
		assertTrue(converter.canConvert("QUARTZ", Gem.class));
		assertEquals(Gem.RUBY, converter.convert("RUBY", Gem.class));
		assertEquals(Gem.DIAMOND, converter.convert("DIAMOND", Gem.class));
		assertEquals(Gem.EMERALD, converter.convert("EMERALD", Gem.class));
		assertNull(converter.convert("QUARTZ", Gem.class));
	}

	public static class StringWrapper {
		public String s;
		public StringWrapper(String s) { this.s = s; }
	}

	@Test
	public void testConstructorConversion() {
		String s = "Juggernaut";
		assertTrue(converter.canConvert(s, StringWrapper.class));
		assertFalse(converter.canConvert(7, StringWrapper.class));
		StringWrapper sw = converter.convert(s, StringWrapper.class);
		assertNotNull(sw);
		assertSame(s, sw.s);
	}

	// -- Helper methods --

	private static void assertCollection(Object o, Object expected,
		Object collection, Class<?> collectionClass)
	{
		assertNotNull(collection);
		assertTrue(collectionClass.isInstance(collection));
		assertEquals(1, ((Collection<?>) collection).size());
		Object actual = ((Collection<?>) collection).iterator().next();
		if (o == expected) assertSame(expected, actual);
		else assertEquals(expected, actual); // o was converted to element type
	}
}
