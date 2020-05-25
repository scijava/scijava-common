/*
 * #%L
 * SciJava Common shared library for SciJava software.
 * %%
 * Copyright (C) 2009 - 2020 SciJava developers.
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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.scijava.test.TestUtils.createTemporaryDirectory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;

import org.junit.Test;
import org.scijava.util.FileUtils;
/**
 * Tests {@link Types}.
 * 
 * @author Curtis Rueden
 * @author Mark Hiner
 * @author Johannes Schindelin
 */
public class TypesTest {

	/** Tests {@link Types#load}. */
	@Test
	public void testLoad() {
		assertLoaded(boolean.class, "boolean");
		assertLoaded(byte.class, "byte");
		assertLoaded(char.class, "char");
		assertLoaded(double.class, "double");
		assertLoaded(float.class, "float");
		assertLoaded(int.class, "int");
		assertLoaded(long.class, "long");
		assertLoaded(short.class, "short");
		assertLoaded(void.class, "void");
		assertLoaded(String.class, "string");
		assertLoaded(Number.class, "java.lang.Number");
		assertLoaded(boolean[].class, "boolean[]");
		assertLoaded(byte[].class, "byte[]");
		assertLoaded(char[].class, "char[]");
		assertLoaded(double[].class, "double[]");
		assertLoaded(float[].class, "float[]");
		assertLoaded(int[].class, "int[]");
		assertLoaded(long[].class, "long[]");
		assertLoaded(short[].class, "short[]");
		assertLoaded(null, "void[]");
		assertLoaded(String[].class, "string[]");
		assertLoaded(Number[].class, "java.lang.Number[]");
		assertLoaded(boolean[][].class, "boolean[][]");
		assertLoaded(byte[][].class, "byte[][]");
		assertLoaded(char[][].class, "char[][]");
		assertLoaded(double[][].class, "double[][]");
		assertLoaded(float[][].class, "float[][]");
		assertLoaded(int[][].class, "int[][]");
		assertLoaded(long[][].class, "long[][]");
		assertLoaded(short[][].class, "short[][]");
		assertLoaded(null, "void[][]");
		assertLoaded(String[][].class, "string[][]");
		assertLoaded(Number[][].class, "java.lang.Number[][]");
		assertLoaded(boolean[].class, "[Z");
		assertLoaded(byte[].class, "[B");
		assertLoaded(char[].class, "[C");
		assertLoaded(double[].class, "[D");
		assertLoaded(float[].class, "[F");
		assertLoaded(int[].class, "[I");
		assertLoaded(long[].class, "[J");
		assertLoaded(short[].class, "[S");
		assertLoaded(null, "[V");
		assertLoaded(String[].class, "[Lstring;");
		assertLoaded(Number[].class, "[Ljava.lang.Number;");
		assertLoaded(boolean[][].class, "[[Z");
		assertLoaded(byte[][].class, "[[B");
		assertLoaded(char[][].class, "[[C");
		assertLoaded(double[][].class, "[[D");
		assertLoaded(float[][].class, "[[F");
		assertLoaded(int[][].class, "[[I");
		assertLoaded(long[][].class, "[[J");
		assertLoaded(short[][].class, "[[S");
		assertLoaded(null, "[[V");
		assertLoaded(String[][].class, "[[Lstring;");
		assertLoaded(Number[][].class, "[[Ljava.lang.Number;");
	}

	/** Tests {@link Types#load}. */
	@Test
	public void testLoadFailureQuiet() {
		// test quiet failure
		assertNull(Types.load("a.non.existent.class"));
	}

	/** Tests {@link Types#load}. */
	@Test(expected = IllegalArgumentException.class)
	public void testLoadFailureLoud() {
		Types.load("a.non.existent.class", false);
	}

	/** Tests {@link Types#location} with a class on the file system. */
	@Test
	public void testLocationUnpackedClass() throws IOException {
		final File tmpDir = createTemporaryDirectory("class-utils-test-");
		final String path = getClass().getName().replace('.', '/') + ".class";
		final File classFile = new File(tmpDir, path);
		assertTrue(classFile.getParentFile().exists() ||
			classFile.getParentFile().mkdirs());
		copy(getClass().getResource("/" + path).openStream(),
			new FileOutputStream(classFile), true);

		final ClassLoader classLoader =
			new URLClassLoader(new URL[] { tmpDir.toURI().toURL() }, null);
		final Class<?> c = Types.load(getClass().getName(), classLoader);
		final URL location = Types.location(c);
		assertEquals(tmpDir, FileUtils.urlToFile(location));
		FileUtils.deleteRecursively(tmpDir);
	}

	/** Tests {@link Types#location} with a class in a JAR file. */
	@Test
	public void testLocationClassInJar() throws IOException {
		final File tmpDir = createTemporaryDirectory("class-utils-test-");
		final File jar = new File(tmpDir, "test.jar");
		final JarOutputStream out = new JarOutputStream(new FileOutputStream(jar));
		final String path = getClass().getName().replace('.', '/') + ".class";
		out.putNextEntry(new ZipEntry(path));
		copy(getClass().getResource("/" + path).openStream(), out, true);

		final ClassLoader classLoader =
			new URLClassLoader(new URL[] { jar.toURI().toURL() }, null);
		final Class<?> c = Types.load(getClass().getName(), classLoader);
		final URL location = Types.location(c);
		assertEquals(jar, FileUtils.urlToFile(location));
		jar.deleteOnExit();
	}

	/** Tests quiet behavior of {@link Types#location(Class, boolean)}. */
	@Test
	public void testLocationFailureQuiet() {
		final Class<?> weirdClass = loadCustomClass();
		assertEquals("Hello", weirdClass.getName());
		assertNull(Types.location(weirdClass));
	}

	/** Tests exceptions from {@link Types#location(Class, boolean)}. */
	@Test(expected = IllegalArgumentException.class)
	public void testLocationFailureLoud() {
		final Class<?> weirdClass = loadCustomClass();
		assertEquals("Hello", weirdClass.getName());
		Types.location(weirdClass, false);
	}

	/** Tests {@link Types#name}. */
	public void testName() {
		@SuppressWarnings("unused")
		class Struct {

			private List<String> list;
		}
		assertEquals("boolean", Types.name(boolean.class));
		assertEquals("java.lang.String", Types.name(String.class));
		assertEquals("List<String>[]", Types.name(type(Struct.class, "list")));
	}

	/** Tests {@link Types#raw(Type)}. */
	@Test
	public void testRaw() {
		@SuppressWarnings("unused")
		class Struct {

			private int[] intArray;
			private double d;
			private String[][] strings;
			private Void v;
			private List<String> list;
			private HashMap<Integer, Float> map;
		}
		assertSame(int[].class, raw(Struct.class, "intArray"));
		assertSame(double.class, raw(Struct.class, "d"));
		assertSame(String[][].class, raw(Struct.class, "strings"));
		assertSame(Void.class, raw(Struct.class, "v"));
		assertSame(List.class, raw(Struct.class, "list"));
		assertSame(HashMap.class, raw(Struct.class, "map"));
	}

	/** Tests {@link Types#raws}. */
	@Test
	public void testRaws() {
		final Field field = Types.field(Thing.class, "thing");

		// Object
		assertAllTheSame(Types.raws(Types.fieldType(field, Thing.class)), Object.class);

		// N extends Number
		assertAllTheSame(Types.raws(Types.fieldType(field, NumberThing.class)),
			Number.class);

		// Integer
		assertAllTheSame(Types.raws(Types.fieldType(field, IntegerThing.class)),
			Integer.class);

		// Serializable & Cloneable
		assertAllTheSame(Types.raws(Types.fieldType(field, ComplexThing.class)),
			Serializable.class, Cloneable.class);
	}

	/** Tests {@link Types#box(Class)}. */
	@Test
	public void testBox() {
		final Class<Boolean> booleanType = Types.box(boolean.class);
		assertSame(Boolean.class, booleanType);

		final Class<Byte> byteType = Types.box(byte.class);
		assertSame(Byte.class, byteType);

		final Class<Character> charType = Types.box(char.class);
		assertSame(Character.class, charType);

		final Class<Double> doubleType = Types.box(double.class);
		assertSame(Double.class, doubleType);

		final Class<Float> floatType = Types.box(float.class);
		assertSame(Float.class, floatType);

		final Class<Integer> intType = Types.box(int.class);
		assertSame(Integer.class, intType);

		final Class<Long> longType = Types.box(long.class);
		assertSame(Long.class, longType);

		final Class<Short> shortType = Types.box(short.class);
		assertSame(Short.class, shortType);

		final Class<Void> voidType = Types.box(void.class);
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
			final Class<?> type = Types.box(c);
			assertSame(c, type);
		}
	}

	/** Tests {@link Types#unbox(Class)}. */
	@Test
	public void testUnbox() {
		// TODO
	}

	/** Tests {@link Types#nullValue(Class)}. */
	@Test
	public void testNullValue() {
		final boolean booleanNull = Types.nullValue(boolean.class);
		assertFalse(booleanNull);

		final byte byteNull = Types.nullValue(byte.class);
		assertEquals(0, byteNull);

		final char charNull = Types.nullValue(char.class);
		assertEquals('\0', charNull);

		final double doubleNull = Types.nullValue(double.class);
		assertEquals(0.0, doubleNull, 0.0);

		final float floatNull = Types.nullValue(float.class);
		assertEquals(0f, floatNull, 0f);

		final int intNull = Types.nullValue(int.class);
		assertEquals(0, intNull);

		final long longNull = Types.nullValue(long.class);
		assertEquals(0, longNull);

		final short shortNull = Types.nullValue(short.class);
		assertEquals(0, shortNull);

		final Void voidNull = Types.nullValue(void.class);
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
			final Object nullValue = Types.nullValue(c);
			assertNull("Expected null for " + c.getName(), nullValue);
		}
	}

	/** Tests {@link Types#field}. */
	@Test
	public void testField() {
		final Field field = Types.field(Thing.class, "thing");
		assertEquals("thing", field.getName());
		assertSame(Object.class, field.getType());
		assertTrue(field.getGenericType() instanceof TypeVariable);
		assertEquals("T", ((TypeVariable<?>) field.getGenericType()).getName());
	}

	/** Tests {@link Types#method}. */
	@Test
	public void testMethod() {
		final Method objectMethod = Types.method(Thing.class, "toString");
		assertSame(Object.class, objectMethod.getDeclaringClass());
		assertEquals("toString", objectMethod.getName());
		assertSame(String.class, objectMethod.getReturnType());
		assertEquals(0, objectMethod.getParameterTypes().length);

		final Method wordsMethod = //
			Types.method(Words.class, "valueOf", String.class);
		// NB: What is going on under the hood to make the Enum
		// subtype Words be the declaring class for the 'valueOf'
		// method? The compiler must internally override the valueOf
		// method for each enum type, to narrow the return type...
		assertSame(Words.class, wordsMethod.getDeclaringClass());
		assertEquals("valueOf", wordsMethod.getName());
		assertSame(Words.class, wordsMethod.getReturnType());
		assertEquals(1, wordsMethod.getParameterTypes().length);
		assertSame(String.class, wordsMethod.getParameterTypes()[0]);
	}

	/** Tests {@link Types#array}. */
	@Test
	public void testArray() {
		// 1-dimensional cases
		assertSame(boolean[].class, Types.array(boolean.class));
		assertSame(String[].class, Types.array(String.class));
		assertSame(Number[].class, Types.array(Number.class));
		assertSame(boolean[][].class, Types.array(boolean[].class));
		assertSame(String[][].class, Types.array(String[].class));
		assertSame(Number[][].class, Types.array(Number[].class));
		try {
			Types.array(void.class);
			fail("Unexpected success creating void[]");
		}
		catch (final IllegalArgumentException exc) { }

		// multidimensional cases
		assertSame(Number[][].class, Types.array(Number.class, 2));
		assertSame(boolean[][][].class, Types.array(boolean.class, 3));
		assertSame(String.class, Types.array(String.class, 0));
		try {
			Types.array(char.class, -1);
			fail("Unexpected success creating negative dimensional array");
		}
		catch (final IllegalArgumentException exc) { }
	}

	/** Tests {@link Types#component(Type)}. */
	@Test
	public void testComponent() {
		@SuppressWarnings("unused")
		class Struct {

			private int[] intArray;
			private double d;
			private String[][] strings;
			private Void v;
			private List<String>[] list;
			private HashMap<Integer, Float> map;
		}
		assertSame(int.class, componentType(Struct.class, "intArray"));
		assertNull(componentType(Struct.class, "d"));
		assertSame(String[].class, componentType(Struct.class, "strings"));
		assertSame(null, componentType(Struct.class, "v"));
		assertSame(List.class, componentType(Struct.class, "list"));
		assertSame(null, componentType(Struct.class, "map"));
	}

	/** Tests {@link Types#fieldType(Field, Class)}. */
	@Test
	public void testFieldType() {
		final Field field = Types.field(Thing.class, "thing");

		// T
		final Type tType = Types.fieldType(field, Thing.class);
		assertEquals("capture of ?", tType.toString());

		// N extends Number
		final Type nType = Types.fieldType(field, NumberThing.class);
		assertEquals("capture of ?", nType.toString());

		// Integer
		final Type iType = Types.fieldType(field, IntegerThing.class);
		assertSame(Integer.class, iType);
	}

	/** Tests {@link Types#param}. */
	@Test
	public void testParam() {
		class Struct {

			@SuppressWarnings("unused")
			private List<int[]> list;
		}
		final Type listType = type(Struct.class, "list");
		final Type paramType = Types.param(listType, List.class, 0);
		final Class<?> paramClass = Types.raw(paramType);
		assertSame(int[].class, paramClass);
	}

	/** Tests {@link Types#isAssignable(Type, Type)}. */
	@Test
	public void testIsAssignable() {
		// check casting to superclass
		assertTrue(Types.isAssignable(String.class, Object.class));

		// check casting to interface
		assertTrue(Types.isAssignable(ArrayList.class, Collection.class));

		// casting numeric primitives is not supported
		assertFalse(Types.isAssignable(double.class, float.class));
		assertFalse(Types.isAssignable(float.class, double.class));

		// boxing is not reported to work
		// TODO: Consider changing this behavior.
		assertFalse(Types.isAssignable(int.class, Number.class));

		// casting from null always works
		assertTrue(Types.isAssignable(null, Object.class));
		assertTrue(Types.isAssignable(null, int[].class));
	}

	/** Tests {@link Types#isAssignable(Type, Type)} from null to null. */
	@Test(expected = NullPointerException.class)
	public void testIsAssignableNullToNull() {
		Types.isAssignable(null, null);
	}

	/** Tests {@link Types#isAssignable(Type, Type)} from Class to null. */
	@Test(expected = NullPointerException.class)
	public void testIsAssignableClassToNull() {
		Types.isAssignable(Object.class, null);
	}

	/** Tests {@link Types#isAssignable(Type, Type)} with type variable. */
	@Test
	public <T extends Number> void testIsAssignableT() {
		final Type t = genericTestType("t");
		final Type listT = genericTestType("listT");
		final Type listNumber = genericTestType("listNumber");
		final Type listInteger = genericTestType("listInteger");
		final Type listExtendsNumber = genericTestType("listExtendsNumber");

		assertTrue(Types.isAssignable(t, t));
		assertTrue(Types.isAssignable(listT, listT));
		assertTrue(Types.isAssignable(listNumber, listNumber));
		assertTrue(Types.isAssignable(listInteger, listInteger));
		assertTrue(Types.isAssignable(listExtendsNumber, listExtendsNumber));

		assertTrue(Types.isAssignable(listT, listExtendsNumber));
		assertTrue(Types.isAssignable(listNumber, listExtendsNumber));
		assertTrue(Types.isAssignable(listInteger, listExtendsNumber));

		assertFalse(Types.isAssignable(listNumber, listT));
		assertFalse(Types.isAssignable(listInteger, listT));
		assertFalse(Types.isAssignable(listExtendsNumber, listT));
		assertFalse(Types.isAssignable(listExtendsNumber, listNumber));
	}

	/** Tests {@link Types#isInstance(Object, Class)}. */
	@Test
	public void testIsInstance() {
		// casting from null always works
		final Object nullObject = null;
		assertTrue(Types.isInstance(nullObject, Object.class));
		assertTrue(Types.isInstance(nullObject, int[].class));

		// casting to null is not allowed
		assertFalse(Types.isInstance(nullObject, null));
		assertFalse(Types.isInstance(new Object(), null));
	}

	/** Tests {@link Types#cast(Object, Class)}. */
	@Test
	public void testCast() {
		// check casting to superclass
		final String string = "Hello";
		final Object stringToObject = Types.cast(string, Object.class);
		assertSame(string, stringToObject);

		// check casting to interface
		final ArrayList<?> arrayList = new ArrayList<>();
		final Collection<?> arrayListToCollection = //
			Types.cast(arrayList, Collection.class);
		assertSame(arrayList, arrayListToCollection);

		// casting numeric primitives is not supported
		final Float doubleToFloat = Types.cast(5.1, float.class);
		assertNull(doubleToFloat);
		final Double floatToDouble = Types.cast(5.1f, double.class);
		assertNull(floatToDouble);

		// boxing works though
		final Number intToNumber = Types.cast(5, Number.class);
		assertSame(Integer.class, intToNumber.getClass());
		assertEquals(5, intToNumber.intValue());
	}

	/** Tests {@link Types#enumValue(String, Class)}. */
	@Test
	public void testEnumValue() {
		final Words foo = Types.enumValue("FOO", Words.class);
		assertSame(Words.FOO, foo);
		final Words bar = Types.enumValue("BAR", Words.class);
		assertSame(Words.BAR, bar);
		final Words fubar = Types.enumValue("FUBAR", Words.class);
		assertSame(Words.FUBAR, fubar);
	}

	/** Tests {@link Types#enumValue(String, Class)} for invalid value. */
	@Test(expected = IllegalArgumentException.class)
	public void testEnumValueNoConstant() {
		Types.enumValue("NONE", Words.class);
	}

	/** Tests {@link Types#enumValue(String, Class)} for non-enum class. */
	@Test(expected = IllegalArgumentException.class)
	public void testEnumValueNonEnum() {
		Types.enumValue("HOOYAH", String.class);
	}

	/** Tests {@link Types#parameterize(Class, Map)}. */
	@Test
	public void testParameterizeMap() {
		// TODO
	}

	/** Tests {@link Types#parameterize(Class, Type...)}. */
	@Test
	public void testParameterizeTypes() {
		// TODO
	}

	/** Tests {@link Types#parameterizeWithOwner(Type, Class, Type...)}. */
	@Test
	public void testParameterizeWithOwner() {
		// TODO
	}

	// -- Helper classes --

	private static class Thing<T> {

		@SuppressWarnings("unused")
		private T thing;
	}

	private static class NumberThing<N extends Number> extends Thing<N> {
		// NB: No implementation needed.
	}

	private static class IntegerThing extends NumberThing<Integer> {
		// NB: No implementation needed.
	}

	private static class ComplexThing<T extends Serializable & Cloneable> extends
		Thing<T>
	{
		// NB: No implementation needed.
	}

	/** Enumeration for testing conversion to enum types. */
	public static enum Words {
		FOO, BAR, FUBAR
	}

	private interface TestTypes<T extends Number> {
		T t();
		List<T> listT();
		List<Number> listNumber();
		List<Integer> listInteger();
		List<? extends Number> listExtendsNumber();
	}

	// -- Helper methods --

	/**
	 * Copies bytes from an {@link InputStream} to an {@link OutputStream}.
	 * 
	 * @param in the source
	 * @param out the sink
	 * @param closeOut whether to close the sink after we're done
	 * @throws IOException
	 */
	private void copy(final InputStream in, final OutputStream out,
		final boolean closeOut) throws IOException
	{
		final byte[] buffer = new byte[16384];
		for (;;) {
			final int count = in.read(buffer);
			if (count < 0) break;
			out.write(buffer, 0, count);
		}
		in.close();
		if (closeOut) out.close();
	}

	private Class<?> loadCustomClass() {
		// NB: The bytecode below was compiled from the following source:
		//
		//     public class Hello {}
		//
		final byte[] bytecode = { -54, -2, -70, -66, 0, 0, 0, 52, 0, 13, 10, 0, 3,
			0, 10, 7, 0, 11, 7, 0, 12, 1, 0, 6, 60, 105, 110, 105, 116, 62, 1, 0, 3,
			40, 41, 86, 1, 0, 4, 67, 111, 100, 101, 1, 0, 15, 76, 105, 110, 101, 78,
			117, 109, 98, 101, 114, 84, 97, 98, 108, 101, 1, 0, 10, 83, 111, 117, 114,
			99, 101, 70, 105, 108, 101, 1, 0, 10, 72, 101, 108, 108, 111, 46, 106, 97,
			118, 97, 12, 0, 4, 0, 5, 1, 0, 5, 72, 101, 108, 108, 111, 1, 0, 16, 106,
			97, 118, 97, 47, 108, 97, 110, 103, 47, 79, 98, 106, 101, 99, 116, 0, 33,
			0, 2, 0, 3, 0, 0, 0, 0, 0, 1, 0, 1, 0, 4, 0, 5, 0, 1, 0, 6, 0, 0, 0, 29,
			0, 1, 0, 1, 0, 0, 0, 5, 42, -73, 0, 1, -79, 0, 0, 0, 1, 0, 7, 0, 0, 0, 6,
			0, 1, 0, 0, 0, 1, 0, 1, 0, 8, 0, 0, 0, 2, 0, 9 };

		class BytesClassLoader extends ClassLoader {
			public Class<?> load(final String name, final byte[] b) {
				return defineClass(name, b, 0, b.length);
			}
		}
		return new BytesClassLoader().load("Hello", bytecode);
	}

	/** Convenience method to get the {@link Type} of a field. */
	private Type type(final Class<?> c, final String fieldName) {
		return Types.field(c, fieldName).getGenericType();
	}

	/** Convenience method to call {@link Types#raw} on a field. */
	private Class<?> raw(final Class<?> c, final String fieldName) {
		return Types.raw(type(c, fieldName));
	}

	/** Convenience method to call {@link Types#component} on a field. */
	private Class<?> componentType(final Class<?> c, final String fieldName) {
		return Types.raw(Types.component(type(c, fieldName)));
	}

	private void assertLoaded(final Class<?> c, final String name) {
		assertSame(c, Types.load(name));
	}

	private void assertAllTheSame(final List<?> list, final Object... values) {
		assertEquals(list.size(), values.length);
		for (int i = 0; i < values.length; i++) {
			assertSame(list.get(i), values[i]);
		}
	}

	private Type genericTestType(final String name) {
		return Types.method(TestTypes.class, name).getGenericReturnType();
	}
}
