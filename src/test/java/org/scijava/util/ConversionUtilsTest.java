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

package org.scijava.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.junit.Test;

/**
 * Tests {@link ConversionUtils}.
 * 
 * @author Mark Hiner
 * @author Curtis Rueden
 */
public class ConversionUtilsTest {

	/** Tests {@link ConversionUtils#canCast(Class, Class)}. */
	@Test
	public void testCanCast() {
		// check casting to superclass
		assertTrue(ConversionUtils.canCast(String.class, Object.class));

		// check casting to interface
		assertTrue(ConversionUtils.canCast(ArrayList.class, Collection.class));

		// casting numeric primitives is not supported
		assertFalse(ConversionUtils.canCast(double.class, float.class));
		assertFalse(ConversionUtils.canCast(float.class, double.class));

		// boxing is not reported to work
		// TODO: Consider changing this behavior.
		assertFalse(ConversionUtils.canCast(int.class, Number.class));

		// casting from null always works
		final Class<?> nullClass = null;
		assertTrue(ConversionUtils.canCast(nullClass, Object.class));
		assertTrue(ConversionUtils.canCast(nullClass, int[].class));
		final Object nullObject = null;
		assertTrue(ConversionUtils.canCast(nullObject, Object.class));
		assertTrue(ConversionUtils.canCast(nullObject, int[].class));

		// casting to null is not allowed
		assertFalse(ConversionUtils.canCast(nullClass, null));
		assertFalse(ConversionUtils.canCast(Object.class, null));
		assertFalse(ConversionUtils.canCast(nullObject, null));
		assertFalse(ConversionUtils.canCast(new Object(), null));
	}

	/** Tests {@link ConversionUtils#cast(Object, Class)}. */
	@Test
	public void testCast() {
		// check casting to superclass
		final String string = "Hello";
		final Object stringToObject = ConversionUtils.cast(string, Object.class);
		assertSame(string, stringToObject);

		// check casting to interface
		final ArrayList<?> arrayList = new ArrayList<>();
		final Collection<?> arrayListToCollection =
			ConversionUtils.cast(arrayList, Collection.class);
		assertSame(arrayList, arrayListToCollection);

		// casting numeric primitives is not supported
		final Float doubleToFloat = ConversionUtils.cast(5.1, float.class);
		assertNull(doubleToFloat);
		final Double floatToDouble = ConversionUtils.cast(5.1f, double.class);
		assertNull(floatToDouble);

		// boxing works though
		final Number intToNumber = ConversionUtils.cast(5, Number.class);
		assertSame(Integer.class, intToNumber.getClass());
		assertEquals(5, intToNumber.intValue());
	}

	/** Tests {@link ConversionUtils#convertToEnum(String, Class)}. */
	@Test
	public void testConvertToEnum() {
		final Words foo = ConversionUtils.convertToEnum("FOO", Words.class);
		assertSame(Words.FOO, foo);
		final Words bar = ConversionUtils.convertToEnum("BAR", Words.class);
		assertSame(Words.BAR, bar);
		final Words fubar = ConversionUtils.convertToEnum("FUBAR", Words.class);
		assertSame(Words.FUBAR, fubar);
		final Words noConstant = ConversionUtils.convertToEnum("NONE", Words.class);
		assertNull(noConstant);
		final String notAnEnum =
			ConversionUtils.convertToEnum("HOOYAH", String.class);
		assertNull(notAnEnum);
	}

	/** Tests {@link ConversionUtils#getNonprimitiveType(Class)}. */
	@Test
	public void testGetNonprimitiveType() {
		final Class<Boolean> booleanType =
			ConversionUtils.getNonprimitiveType(boolean.class);
		assertSame(Boolean.class, booleanType);

		final Class<Byte> byteType =
			ConversionUtils.getNonprimitiveType(byte.class);
		assertSame(Byte.class, byteType);

		final Class<Character> charType =
			ConversionUtils.getNonprimitiveType(char.class);
		assertSame(Character.class, charType);

		final Class<Double> doubleType =
			ConversionUtils.getNonprimitiveType(double.class);
		assertSame(Double.class, doubleType);

		final Class<Float> floatType =
			ConversionUtils.getNonprimitiveType(float.class);
		assertSame(Float.class, floatType);

		final Class<Integer> intType =
			ConversionUtils.getNonprimitiveType(int.class);
		assertSame(Integer.class, intType);

		final Class<Long> longType =
			ConversionUtils.getNonprimitiveType(long.class);
		assertSame(Long.class, longType);

		final Class<Short> shortType =
			ConversionUtils.getNonprimitiveType(short.class);
		assertSame(Short.class, shortType);

		final Class<Void> voidType =
			ConversionUtils.getNonprimitiveType(void.class);
		assertSame(Void.class, voidType);

		final Class<?>[] types = { //
			Boolean.class, Byte.class, Character.class, Double.class, //
				Float.class, Integer.class, Long.class, Short.class, //
				Void.class, //
				String.class, //
				Number.class, BigInteger.class, BigDecimal.class, //
				boolean[].class, byte[].class, char[].class, double[].class, //
				float[].class, int[].class, long[].class, short[].class, //
				Boolean[].class, Byte[].class, Character[].class, Double[].class, //
				Float[].class, Integer[].class, Long[].class, Short[].class, //
				Void[].class, //
				Object.class, Object[].class, String[].class, //
				Object[][].class, String[][].class, //
				Collection.class, //
				List.class, ArrayList.class, LinkedList.class, //
				Set.class, HashSet.class, //
				Map.class, HashMap.class, //
				Collection[].class, List[].class, Set[].class, Map[].class };
		for (final Class<?> c : types) {
			final Class<?> type = ConversionUtils.getNonprimitiveType(c);
			assertSame(c, type);
		}
	}

	/** Tests {@link ConversionUtils#getNullValue(Class)}. */
	@Test
	public void testGetNullValue() {
		final boolean booleanNull = ConversionUtils.getNullValue(boolean.class);
		assertFalse(booleanNull);

		final byte byteNull = ConversionUtils.getNullValue(byte.class);
		assertEquals(0, byteNull);

		final char charNull = ConversionUtils.getNullValue(char.class);
		assertEquals('\0', charNull);

		final double doubleNull = ConversionUtils.getNullValue(double.class);
		assertEquals(0.0, doubleNull, 0.0);

		final float floatNull = ConversionUtils.getNullValue(float.class);
		assertEquals(0f, floatNull, 0f);

		final int intNull = ConversionUtils.getNullValue(int.class);
		assertEquals(0, intNull);

		final long longNull = ConversionUtils.getNullValue(long.class);
		assertEquals(0, longNull);

		final short shortNull = ConversionUtils.getNullValue(short.class);
		assertEquals(0, shortNull);

		final Void voidNull = ConversionUtils.getNullValue(void.class);
		assertNull(voidNull);

		final Class<?>[] types = { //
			Boolean.class, Byte.class, Character.class, Double.class, //
				Float.class, Integer.class, Long.class, Short.class, //
				Void.class, //
				String.class, //
				Number.class, BigInteger.class, BigDecimal.class, //
				boolean[].class, byte[].class, char[].class, double[].class, //
				float[].class, int[].class, long[].class, short[].class, //
				Boolean[].class, Byte[].class, Character[].class, Double[].class, //
				Float[].class, Integer[].class, Long[].class, Short[].class, //
				Void[].class, //
				Object.class, Object[].class, String[].class, //
				Object[][].class, String[][].class, //
				Collection.class, //
				List.class, ArrayList.class, LinkedList.class, //
				Set.class, HashSet.class, //
				Map.class, HashMap.class, //
				Collection[].class, List[].class, Set[].class, Map[].class };
		for (final Class<?> c : types) {
			final Object nullValue = ConversionUtils.getNullValue(c);
			assertNull("Expected null for " + c.getName(), nullValue);
		}
	}

	/**
	 * Tests populating a primitive array.
	 */
	@Test
	public void testPrimitiveArray() {
		class Struct {

			private int[] intArray;
		}
		final Struct struct = new Struct();

		final List<Integer> intVals = getValueList(4, 3, 7);
		setFieldValue(struct, "intArray", intVals);

		for (int i = 0; i < struct.intArray.length; i++) {
			assertEquals(intVals.get(i).intValue(), struct.intArray[i]);
		}

		// Repeat, using a primitive array of values this time
		setFieldValue(struct, "intArray", new int[] { 8, 6, 14 });

		for (int i = 0; i < struct.intArray.length; i++) {
			assertEquals(intVals.get(i).intValue() * 2, struct.intArray[i]);
		}
	}

	/**
	 * Tests populating an array type of Objects.
	 */
	@Test
	public void testObjectArray() {
		class Struct {

			private Double[] doubleArray;
		}
		final Struct struct = new Struct();

		// Verify behavior setting an array of Objects (Doubles)
		final List<Double> doubleVals = getValueList(1.0, 2.0, 3.0);
		setFieldValue(struct, "doubleArray", doubleVals);

		for (int i = 0; i < struct.doubleArray.length; i++) {
			assertEquals(doubleVals.get(i), struct.doubleArray[i]);
		}
	}

	/**
	 * Tests populating a collection.
	 */
	@Test
	public void testCollection() {
		class Struct {

			private List<String> stringList;
		}
		final Struct struct = new Struct();

		// Verify behavior setting a List of Objects (Strings)
		final List<String> stringVals = getValueList("ok", "still ok");
		setFieldValue(struct, "stringList", stringVals);

		for (int i = 0; i < struct.stringList.size(); i++) {
			assertEquals(stringVals.get(i), struct.stringList.get(i));
		}

		// Repeat, using an array of Strings to populate a collection
		setFieldValue(struct, "stringList", stringVals.toArray());

		for (int i = 0; i < struct.stringList.size(); i++) {
			assertEquals(stringVals.get(i), struct.stringList.get(i));
		}
	}

	/**
	 * Tests conversion <em>from</em> a subclass of a collection.
	 */
	@Test
	public void testFromCollectionSubclass() {
		class RandomSet extends HashSet<Random> {
			// NB: No implementation needed.
		}
		class Struct {

			private List<String> stringList;
		}
		final Struct struct = new Struct();

		final RandomSet randomSet = new RandomSet();
		randomSet.add(new Random(567));
		randomSet.add(new Random(321));

		setFieldValue(struct, "stringList", randomSet);

		assertNotNull(struct.stringList);
		assertEquals(2, struct.stringList.size());
		for (final String s : struct.stringList) {
			assertTrue(s.matches("^java.util.Random@[0-9a-f]+$"));
		}
	}

	/**
	 * Tests conversion <em>to</em> a subclass of a collection.
	 */
	@Test
	public void testToCollectionSubclass() {
		class Struct {

			private ListExtension<Double> myDoubles;
			private StringListExtension myStrings;
		}
		final Struct struct = new Struct();

		final LongArray longArray = new LongArray();
		longArray.add(123456789012L);
		longArray.add(987654321098L);

		// Conversion to list of Doubles (with a generic parameter) succeeds.

		setFieldValue(struct, "myDoubles", longArray);

		assertNotNull(struct.myDoubles);
		assertEquals(2, struct.myDoubles.size());
		assertEquals(123456789012.0, struct.myDoubles.get(0), 0.0);
		assertEquals(987654321098.0, struct.myDoubles.get(1), 0.0);

		// Conversion to a list of strings (with no generic parameter) fails.

		setFieldValue(struct, "myStrings", longArray);

		assertNull(struct.myStrings);
	}

	/**
	 * Tests populating nested multi-element objects (collection of arrays).
	 */
	@Test
	public void testNestingMultiElements() {
		class Struct {

			private Set<char[]> nestedArray;
		}
		final Struct struct = new Struct();

		// Verify behavior setting a nesting of multi-elements (Set of Array)
		final Set<char[]> nestedSetValues = new HashSet<>();
		final char[] chars = { 'a', 'b', 'c' };
		nestedSetValues.add(chars);

		setFieldValue(struct, "nestedArray", nestedSetValues);

		for (final char[] charVals : struct.nestedArray) {
			for (int i = 0; i < chars.length; i++) {
				assertEquals(chars[i], charVals[i]);
			}
		}
	}

	/**
	 * Tests setting single multi-element values when the value itself is not a
	 * collection/array.
	 */
	@Test
	public void testSettingSingleElements() {
		class Struct {

			private Double[] doubleArray;
			private List<String> stringList;
		}
		final Struct struct = new Struct();

		// Verify behavior setting a single element of an array
		final double dVal = 6.3;
		setFieldValue(struct, "doubleArray", dVal);
		assertEquals(new Double(dVal), struct.doubleArray[0]);

		// Verify behavior setting a single element of a list
		final String sVal = "I am a ghost";
		setFieldValue(struct, "stringList", sVal);
		assertEquals(sVal, struct.stringList.get(0));
	}

	/**
	 * Tests setting an incompatible element value for a primitive array.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testBadPrimitiveArray() {
		class Struct {

			@SuppressWarnings("unused")
			private int[] intArray;
		}
		final Struct struct = new Struct();

		setFieldValue(struct, "intArray", "not an int array");
	}

	/**
	 * Tests setting incompatible object values, for both array types of objects
	 * and a collection.
	 */
	@Test
	public void testBadObjectElements() {
		class Struct {

			private Double[] doubleArray;
			private List<String> stringList;
			@SuppressWarnings("unused")
			private Set<char[]> nestedArray;
		}
		final Struct struct = new Struct();

		// Test abnormal behavior for an object array
		setFieldValue(struct, "doubleArray", "not a double array");
		assertEquals(null, struct.doubleArray[0]);

		// Test abnormal behavior for a list
		setFieldValue(struct, "nestedArray", "definitely not a set of char arrays");
		assertNull(struct.stringList);
	}

	/**
	 * Test behavior when setting a single element field with a collection and
	 * array.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testBadSingleton() {
		class Struct {

			@SuppressWarnings("unused")
			private int singleValue;
		}
		final Struct struct = new Struct();

		setFieldValue(struct, "singleValue", new int[] { 4, 8, 2 });
	}

	/**
	 * Test behavior when setting a single element field with a constructor that
	 * accepts a primitive array.
	 */
	@Test
	public void testLegitimateSingletonArray() {
		class Struct {

			private ArrayWrapper arrayWrapper;
		}
		final Struct struct = new Struct();

		setFieldValue(struct, "arrayWrapper", new int[] { 4, 8, 2 });
		assertNotNull(struct.arrayWrapper);
	}

	/**
	 * Test behavior when setting a single element field with a constructor that
	 * accepts collections.
	 */
	@Test
	public void testLegitimateSingletonCollection() {
		class Struct {

			private ListWrapper listWrapper;
		}
		final Struct struct = new Struct();

		setFieldValue(struct, "listWrapper", getValueList(4, 8, 2));
		assertNotNull(struct.listWrapper);
	}

// -- Helper Methods --

	/**
	 * Convenience method to automatically get a field from a field name and call
	 * {@link ClassUtils#setValue(java.lang.reflect.Field, Object, Object)}.
	 */
	private void setFieldValue(final Object o, final String fieldName,
		final Object value)
	{
		ClassUtils.setValue(ClassUtils.getField(o.getClass(), fieldName), o, value);
	}

	/**
	 * Convenience method to convert an array of values to a collection.
	 */
	private <T> List<T> getValueList(final T... values) {
		final List<T> list = new ArrayList<>();
		for (final T value : values)
			list.add(value);
		return list;
	}

	// -- Helper Classes --

	/**
	 * Helper class for testing conversion of one {@link ArrayList} subclass to
	 * another.
	 */
	public static class HisList extends ArrayList<String> {
		public HisList() {
			super();
		}
		public HisList(final Collection<? extends String> c) {
			super(c);
		}
	}

	/**
	 * Helper class for testing conversion of one {@link ArrayList} subclass to
	 * another.
	 */
	public static class HerList extends ArrayList<String> {
		public HerList(final Collection<? extends String> c) {
			super(c);
		}
	}

	/**
	 * Helper class for testing conversion of one {@link ArrayList} subclass to
	 * another.
	 */
	public static class ObjectList extends ArrayList<Object> {
		public ObjectList(final Collection<? extends Object> c) {
			super(c);
		}
	}

	/**
	 * Helper class for testing conversion of one {@link ArrayList} subclass to
	 * another.
	 */
	public static class NumberList extends ArrayList<Number> implements
		INumberList
	{
		public NumberList() {
			super();
		}
		public NumberList(final Collection<? extends Number> c) {
			super(c);
		}
	}

	/**
	 * Helper interface for testing conversion of an {@link ArrayList} subclass
	 * to one of its implementing interfaces.
	 */
	private static interface INumberList extends List<Number> {
		// NB: No implementation needed.
	}

	/**
	 * Dummy class with an array constructor to ensure that the logic to
	 * recursively convert arrays doesn't consume the array improperly when it
	 * should be used in the constructor of an object.
	 */
	public static class ArrayWrapper {

		@SuppressWarnings("unused")
		public ArrayWrapper(final int[] gonnaWrapThisArray) {
			// nothing to do
		}
	}

	/**
	 * Dummy class with a list constructor to ensure that the logic to recursively
	 * convert collections doesn't consume the list improperly when it should be
	 * used in the constructor of an object.
	 */
	public static class ListWrapper {

		@SuppressWarnings("unused")
		public ListWrapper(final List<?> gonnaWrapThisList) {
			// nothing to do
		}
	}

	/** Extension of {@link ArrayList} which retains a generic parameter. */
	public static class ListExtension<T> extends ArrayList<T> {
		// NB: No implementation needed.
	}

	/** Extension of {@link ArrayList} which resolves the generic parameter. */
	public static class StringListExtension extends ArrayList<String> {
		// NB: No implementation needed.
	}

	/** Enumeration for testing conversion to enum types. */
	public static enum Words {
		FOO, BAR, FUBAR
	}

}
