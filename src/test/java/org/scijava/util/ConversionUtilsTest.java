/*
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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
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

	/**
	 * Tests populating a primitive array.
	 */
	@Test
	public void testPrimitiveArray() {
		class Struct {

			private int[] intArray;
		}
		final Struct struct = new Struct();

		final List<Integer> intVals = Arrays.asList(4, 3, 7);
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
		final List<Double> doubleVals = Arrays.asList(1.0, 2.0, 3.0);
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
		final List<String> stringVals = Arrays.asList("ok", "still ok");
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
	public void testBadPrimitiveArray() {
		class Struct {

			@SuppressWarnings("unused")
			private int[] intArray;
		}
		final Struct struct = new Struct();

		setFieldValue(struct, "intArray", "not an int array");
		assertEquals(null, struct.intArray);
	}

	/**
	 * Tests setting incompatible object values, for both array types of objects
	 * and a collection.
	 */
	@Test
	public void testIncompatibleCollections() {
		class Struct {

			private Double[] doubleArray;
			private List<Number> numberList;
			private Set<Integer[]> setOfIntegerArrays;
		}
		final Struct struct = new Struct();

		// NB: DefaultConverter converts non-collection/array objects to
		// collection/array objects, even if some or all of the constituent elements
		// cannot be converted to the array/collection component/element type.

		// Test object to incompatible array type
		setFieldValue(struct, "doubleArray", "not a double array");
		assertArrayEquals(new Double[] {null}, struct.doubleArray);

		// Test object to incompatible List type
		setFieldValue(struct, "numberList", "not actually a list of numbers");
		List<Number> expectedList = Arrays.asList((Number) null);
		assertEquals(expectedList, struct.numberList);

		// Test object to incompatible Set type
		setFieldValue(struct, "setOfIntegerArrays", //
			"definitely not a set of Integer[]");
		assertNotNull(struct.setOfIntegerArrays);
		assertEquals(1, struct.setOfIntegerArrays.size());
		Integer[] singleton = struct.setOfIntegerArrays.iterator().next();
		assertNotNull(singleton);
		assertEquals(1, singleton.length);
		assertNull(singleton[0]);
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

		setFieldValue(struct, "listWrapper", Arrays.asList(4, 8, 2));
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
		ClassUtils.setValue(Types.field(o.getClass(), fieldName), o, value);
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

}
