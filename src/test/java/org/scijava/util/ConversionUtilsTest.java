/*
 * #%L
 * SciJava Common shared library for SciJava software.
 * %%
 * Copyright (C) 2009 - 2013 Board of Regents of the University of
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
 * 
 * The views and conclusions contained in the software and documentation are
 * those of the authors and should not be interpreted as representing official
 * policies, either expressed or implied, of any organization.
 * #L%
 */

package org.scijava.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
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
	public void testPrimitiveArray() throws SecurityException {
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
	public void testObjectArray() throws SecurityException {
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
	public void testCollection() throws SecurityException {
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
	 * Tests populating a subclass of a collection.
	 */
	@Test
	public void testCollectionSubclass() throws SecurityException {
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
			assertTrue(s.matches("^java.util.Random@[0-9a-f]{8}$"));
		}
	}

	/**
	 * Tests populating nested multi-element objects (collection of arrays).
	 */
	@Test
	public void testNestingMultiElements() throws SecurityException {
		class Struct {

			private Set<char[]> nestedArray;
		}
		final Struct struct = new Struct();

		// Verify behavior setting a nesting of multi-elements (Set of Array)
		final Set<char[]> nestedSetValues = new HashSet<char[]>();
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
	public void testSettingSingleElements() throws SecurityException {
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
	public void testBadPrimitiveArray() throws SecurityException {
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
	public void testBadObjectElements() throws SecurityException {
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
	 * 
	 * @throws NoSuchFieldException
	 * @throws SecurityException
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testBadSingleton() throws SecurityException, NoSuchFieldException
	{
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
	 * 
	 * @throws NoSuchFieldException
	 * @throws SecurityException
	 */
	@Test
	public void testLegitimateSingletonArray() throws SecurityException,
		NoSuchFieldException
	{
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
	 * 
	 * @throws NoSuchFieldException
	 * @throws SecurityException
	 */
	@Test
	public void testLegitimateSingletonCollection() throws SecurityException,
		NoSuchFieldException
	{
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
		final Object value) throws SecurityException
	{
		ClassUtils.setValue(ClassUtils.getField(o.getClass(), fieldName), o, value);
	}

	/**
	 * Convenience method to convert an array of values to a collection.
	 */
	private <T> List<T> getValueList(final T... values) {
		final List<T> list = new ArrayList<T>();
		for (final T value : values)
			list.add(value);
		return list;
	}

	// -- Helper Classes --

	/**
	 * Dummy class with an array constructor to ensure that the logic to
	 * recursively convert arrays doesn't consume the array improperly when it
	 * should be used in the constructor of an object.
	 */
	private static class ArrayWrapper {

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
	private static class ListWrapper {

		@SuppressWarnings("unused")
		public ListWrapper(final List<?> gonnaWrapThisList) {
			// nothing to do
		}
	}

}
