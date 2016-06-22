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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.scijava.Context;
import org.scijava.Priority;
import org.scijava.plugin.Plugin;
import org.scijava.util.BoolArray;
import org.scijava.util.CharArray;
import org.scijava.util.ClassUtils;
import org.scijava.util.DoubleArray;
import org.scijava.util.FloatArray;
import org.scijava.util.IntArray;
import org.scijava.util.LongArray;
import org.scijava.util.PrimitiveArray;
import org.scijava.util.ShortArray;

/**
 * Tests {@link ConvertService}.
 * 
 * @author Mark Hiner
 * @author Curtis Rueden
 */
public class ConvertServiceTest {

	private ConvertService convertService;

	@Before
	public void setUp() {
		final Context context = new Context(ConvertService.class);
		convertService = context.getService(ConvertService.class);
	}

	@After
	public void tearDown() {
		convertService.getContext().dispose();
	}

	@Test
	public void testNull() {
		// Test convertService.supports for null parameters
		assertTrue(convertService.supports((Object)null, Object.class));
		assertTrue(convertService.supports((Class<Object>)null, Object.class));
		assertTrue(convertService.supports(Object.class, (Class<Object>)null));
		assertTrue(convertService.supports(Object.class, (Type)null));
		assertTrue(convertService.supports(new Object(), (Class<Object>)null));
		assertTrue(convertService.supports(new Object(), (Type)null));
		assertTrue(convertService.supports(new ConversionRequest((Object)null, Object.class)));
		assertTrue(convertService.supports(new ConversionRequest((Class<Object>)null, Object.class)));
		assertTrue(convertService.supports(new ConversionRequest(Object.class, (Class<Object>)null)));
		assertTrue(convertService.supports(new ConversionRequest(Object.class, (Type)null)));
		assertTrue(convertService.supports(new ConversionRequest(new Object(), (Class<Object>)null)));
		assertTrue(convertService.supports(new ConversionRequest(new Object(), (Type)null)));

		// Test convertService.convert for null parameters
		assertNull(convertService.convert((Object)null, Object.class));
		assertNull(convertService.convert((Class<Object>)null, Object.class));
		assertNull(convertService.convert(Object.class, (Class<Object>)null));
		assertNull(convertService.convert(Object.class, (Type)null));
		assertNull(convertService.convert(new Object(), (Class<Object>)null));
		assertNull(convertService.convert(new Object(), (Type)null));
		assertNull(convertService.convert(new ConversionRequest((Object)null, Object.class)));
		assertNull(convertService.convert(new ConversionRequest((Class<Object>)null, Object.class)));
		assertNull(convertService.convert(new ConversionRequest(Object.class, (Class<Object>)null)));
		assertNull(convertService.convert(new ConversionRequest(Object.class, (Type)null)));
		assertNull(convertService.convert(new ConversionRequest(new Object(), (Class<Object>)null)));
		assertNull(convertService.convert(new ConversionRequest(new Object(), (Type)null)));
	}

	/**
	 * Test conversion between primitive types.
	 */
	@Test
	public void testPrimitives() {
		assertTrue(1d == convertService.convert(1, double.class));
		assertTrue(1d == convertService.convert(1l, double.class));
		assertTrue(1d == convertService.convert(1.0f, double.class));
		assertTrue(1d == convertService.convert((short)1, double.class));
		assertTrue(1d == convertService.convert(1.0, double.class));
	}

	/**
	 * Test conversion between primitive types, {@link PrimitiveArray}s and
	 * {@link List}s of {@link Number}s.
	 */
	@Test
	public void testArrays() {
		// Test that each primitive [] is compatible in either direciton with its
		// paired PrimitiveArray
		testIntechangeable(int[].class, IntArray.class);
		testIntechangeable(long[].class, LongArray.class);
		testIntechangeable(double[].class, DoubleArray.class);
		testIntechangeable(float[].class, FloatArray.class);
		testIntechangeable(short[].class, ShortArray.class);
		testIntechangeable(char[].class, CharArray.class);
		testIntechangeable(boolean[].class, BoolArray.class);

		// Test that primitive [] can not be convertied to mismatched PrimitiveArray
		assertFalse(convertService.supports(int[].class, LongArray.class));

		// Test that lists can be converted to any primitive []
		final List<Integer> list = new ArrayList<>();
		for (int i=0; i<100; i++) list.add((int) (10000 * Math.random()));

		assertTrue(convertService.supports(list, int[].class));
		assertTrue(convertService.supports(list, long[].class));
		assertTrue(convertService.supports(list, double[].class));
		assertTrue(convertService.supports(list, float[].class));
		assertTrue(convertService.supports(list, short[].class));
		assertTrue(convertService.supports(list, char[].class));
		assertTrue(convertService.supports(list, boolean[].class));

		// Verify PrimitiveArray conversion
		final int[] primitives = convertService.convert(list, int[].class);

		final IntArray intArray = convertService.convert(primitives, IntArray.class);

		// Should just unwrap the IntArray
		assertTrue(primitives == convertService.convert(intArray, int[].class));

		// Verify all our lists are the same

		for (int i=0; i<list.size(); i++) {
			assertTrue(list.get(i) == primitives[i]);
			assertTrue(list.get(i) == intArray.getValue(i));
		}
	}

	/** Tests {@link ConvertService#supports(Class, Class)}. */
	@Test
	public void testCanConvert() {
		// check "conversion" (i.e., casting) to superclass
		assertTrue(convertService.supports(String.class, Object.class));

		// check "conversion" (i.e., casting) to interface
		assertTrue(convertService.supports(ArrayList.class, Collection.class));

		// check conversion of numeric primitives
		assertTrue(convertService.supports(double.class, float.class));
		assertTrue(convertService.supports(float.class, double.class));

		// check that boxing works
		assertTrue(convertService.supports(int.class, Number.class));
		assertTrue(convertService.supports(Integer.class, double.class));

		// can convert anything to string
		assertTrue(convertService.supports(Object.class, String.class));

		// can convert string to char
		// TODO: Consider changing this behavior to allow conversion from anything.
		assertTrue(convertService.supports(String.class, char.class));
		assertTrue(convertService.supports(String.class, Character.class));

		// can convert string to enum
		assertTrue(convertService.supports(String.class, Words.class));

		// check conversion of various types w/ appropriate constructor
		assertTrue(convertService.supports(String.class, Double.class));
		assertTrue(convertService.supports(Collection.class, ArrayList.class));
		assertTrue(convertService.supports(HashSet.class, ArrayList.class));
		assertTrue(convertService.supports(long.class, Date.class));

		// check lack of conversion of various types w/o appropriate constructor
		assertFalse(convertService.supports(Collection.class, List.class));
		assertFalse(convertService.supports(int.class, Date.class));
	}

	/** Tests {@link ConvertService#convert(Object, Class)}. */
	public void testConvert() {
		// check "conversion" (i.e., casting) to superclass
		final String string = "Hello";
		final Object stringToObject = convertService.convert(string, Object.class);
		assertSame(string, stringToObject);

		// check "conversion" (i.e., casting) to interface
		final ArrayList<?> arrayList = new ArrayList<>();
		final Collection<?> arrayListToCollection =
			convertService.convert(arrayList, Collection.class);
		assertSame(arrayList, arrayListToCollection);

		// check conversion to enum values (testConvertToEnum is more thorough)
		final Words fubar = convertService.convert("FUBAR", Words.class);
		assertSame(Words.FUBAR, fubar);
		final Words noConstant = convertService.convert("NONE", Words.class);
		assertNull(noConstant);

		// check conversion of numeric primitives: double to float
		final double d = 5.1;
		final float doubleToFloat = convertService.convert(d, float.class);
		assertTrue((float) d == doubleToFloat);

		// check conversion of numeric primitives: float to double
		final float f = 6.2f;
		final double floatToDouble =
			convertService.convert(float.class, double.class);
		assertEquals(f, floatToDouble, 0.0);

		// boxing works
		final Number intToNumber = convertService.convert(5, Number.class);
		assertSame(Integer.class, intToNumber.getClass());
		assertEquals(5, intToNumber.intValue());

		// can convert anything to string
		final Object object = new Object();
		final String objectToString = convertService.convert(object, String.class);
		assertEquals(object.toString(), objectToString);

		// can convert string to char
		// TODO: Consider changing this behavior to allow conversion from anything.
		final String name = "Houdini";
		final char c = convertService.convert(name, char.class);
		assertTrue(name.charAt(0) == c);

		// check conversion via constructor: String to double
		final String ns = "8.7";
		final double stringToDouble = convertService.convert(ns, Double.class);
		assertEquals(8.7, stringToDouble, 0.0);

		// check conversion via constructor: HashSet to ArrayList
		final HashSet<String> set = new HashSet<>();
		set.add("Foo");
		set.add("Bar");
		@SuppressWarnings("unchecked")
		final ArrayList<String> setToArrayList =
			convertService.convert(set, ArrayList.class);
		assertEquals(2, setToArrayList.size());
		Collections.sort(setToArrayList);
		assertEquals("Bar", setToArrayList.get(0));
		assertEquals("Foo", setToArrayList.get(1));

		// check conversion via constructor: long to Date
		final Date date = new Date();
		final long datestamp = date.getTime();
		final Date longToDate = convertService.convert(datestamp, Date.class);
		assertEquals(date, longToDate);

		// check conversion failure: HashSet to List interface
		@SuppressWarnings("unchecked")
		final List<String> setToList = convertService.convert(set, List.class);
		assertNull(setToList);

		// check conversion failure: int to Date
		final int intStamp = (int) datestamp;
		final Date intToDate = convertService.convert(intStamp, Date.class);
		assertNull(intToDate);
	}

	/**
	 * Tests {@link ConvertService#convert(Object, Class)} in subclassing cases.
	 */
	@Test
	public void testConvertSubclass() {
		final HisList hisList = new HisList();
		hisList.add("Foo");
		hisList.add("Bar");

		// ArrayList<String> subclass to ArrayList<String> subclass
		final HerList herList = convertService.convert(hisList, HerList.class);
		assertEquals(2, herList.size());
		assertEquals("Foo", herList.get(0));
		assertEquals("Bar", herList.get(1));

		// ArrayList<String> subclass to ArrayList<Object> subclass
		final ObjectList objectList =
			convertService.convert(hisList, ObjectList.class);
		assertEquals(2, objectList.size());
		assertEquals("Foo", objectList.get(0));
		assertEquals("Bar", objectList.get(1));

		// ArrayList<Object> subclass to ArrayList<String> subclass
		final HisList objectToHisList =
			convertService.convert(objectList, HisList.class);
		assertEquals(2, objectToHisList.size());
		assertEquals("Foo", objectToHisList.get(0));
		assertEquals("Bar", objectToHisList.get(1));

		// ArrayList<String> subclass to ArrayList<Number> subclass
		// This surprisingly works due to type erasure... dangerous stuff.
		final NumberList hisToNumberList =
			convertService.convert(hisList, NumberList.class);
		assertEquals(2, hisToNumberList.size());
		assertEquals("Foo", hisToNumberList.get(0));
		assertEquals("Bar", hisToNumberList.get(1));
		try {
			final Number n0 = hisToNumberList.get(0);
			fail("expected ClassCastException but got: " + n0);
		}
		catch (final ClassCastException exc) {
			// NB: Exception expected.
		}
	}

	/**
	 * Tests that {@link ConvertService#convert(Object, Type)} prefers casting to
	 * conversion.
	 */
	@Test
	public void testConvertTypeCasting() {
		class Struct {
			private INumberList iNumberList;
			private List<String> list;
		}
		final Struct struct = new Struct();
		final NumberList numberList = new NumberList();
		numberList.add(5);

		// check casting to an INumberList (Type w/o generic parameter)
		setFieldValue(struct, "iNumberList", numberList);
		assertSame(numberList, struct.iNumberList);

		// check casting to a List<String> (Type w/ generic parameter)
		setFieldValue(struct, "list", numberList);
		assertSame(numberList, struct.list);
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

	/**
	 * Test that a {@link Converter} with the appropriate {@link
	 * Converter#populateInputCandidates(Collection)} implementation will populate
	 * candidate input lists with objects it can convert to a requested output
	 * type.
	 */
	@Test
	public void testGetCompatibleInputs() {
		final List<Object> compatibleInputs =
			new ArrayList<>(convertService.getCompatibleInputs(HisList.class));

		assertEquals(4, compatibleInputs.size());
		assertEquals(StringHisListConverter.S1, compatibleInputs.get(0));
		assertEquals(StringHisListConverter.S2, compatibleInputs.get(1));
		assertEquals(StringHisListConverter.S3, compatibleInputs.get(2));
		assertEquals(StringHisListConverter.S4, compatibleInputs.get(3));
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

	/**
	 * Dummy {@link Converter} implementation that provides two {@code String}
	 * input candidates for converting to {@link HisList}s. The actual conversion
	 * methods are not implemented and are unnecessary.
	 */
	@Plugin(type = Converter.class, priority = Priority.LAST_PRIORITY)
	public static class StringHisListConverter extends
		AbstractConverter<String, HisList>
	{

		// Sample strings

		private static final String S1 = "THIS_IS_A_TEST";
		private static final String S2 = "WHY_AM_I_HERE";
		private static final String S3 = "I_LIKE_TURTLES";
		private static final String S4 = "OVER_9000";

		// -- Converter methods --

		@Override
		public void populateInputCandidates(final Collection<Object> objects) {
			objects.add(S1);
			objects.add(S2);
			objects.add(S3);
			objects.add(S4);
		}

		@Override
		public Class<HisList> getOutputType() {
			return HisList.class;
		}

		@Override
		public Class<String> getInputType() {
			return String.class;
		}

		// -- Dummy conversion methods --

		@Override
		public boolean canConvert(Class<?> src, Class<?> dest) {
			return false;
		}

		@Override
		public <T> T convert(Object src, Class<T> dest) {
			return null;
		}
	}

	// -- Helper methods --

	/**
	 * Verify bi-direciotnal conversion is supported between the two classes
	 */
	private void testIntechangeable(final Class<?> c1, final Class<?> c2) {
		assertTrue(convertService.supports(c1, c2));
		assertTrue(convertService.supports(c2, c1));
	}
}
