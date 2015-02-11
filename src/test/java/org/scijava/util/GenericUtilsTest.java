/*
 * #%L
 * SciJava Common shared library for SciJava software.
 * %%
 * Copyright (C) 2009 - 2015 Board of Regents of the University of
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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;

import org.junit.Test;

/**
 * Tests {@link GenericUtils}.
 * 
 * @author Mark Hiner
 * @author Curtis Rueden
 */
public class GenericUtilsTest {

	/** Tests {@link GenericUtils#getClass(Type)}. */
	@Test
	public void testGetClass() {
		@SuppressWarnings("unused")
		class Struct {

			private int[] intArray;
			private double d;
			private String[][] strings;
			private Void v;
			private List<String> list;
			private HashMap<Integer, Float> map;
		}
		assertSame(int[].class, getClass(Struct.class, "intArray"));
		assertSame(double.class, getClass(Struct.class, "d"));
		assertSame(String[][].class, getClass(Struct.class, "strings"));
		assertSame(Void.class, getClass(Struct.class, "v"));
		assertSame(List.class, getClass(Struct.class, "list"));
		assertSame(HashMap.class, getClass(Struct.class, "map"));
	}

	/** Tests {@link GenericUtils#getComponentClass(Type)}. */
	@Test
	public void testGetComponentClass() {
		@SuppressWarnings("unused")
		class Struct {

			private int[] intArray;
			private double d;
			private String[][] strings;
			private Void v;
			private List<String>[] list;
			private HashMap<Integer, Float> map;
		}
		assertSame(int.class, getComponentClass(Struct.class, "intArray"));
		assertNull(getComponentClass(Struct.class, "d"));
		assertSame(String[].class, getComponentClass(Struct.class, "strings"));
		assertSame(null, getComponentClass(Struct.class, "v"));
		assertSame(List.class, getComponentClass(Struct.class, "list"));
		assertSame(null, getComponentClass(Struct.class, "map"));
	}

	/**
	 * Tests {@link GenericUtils#getFieldClasses(java.lang.reflect.Field, Class)}.
	 */
	@Test
	public void testGetFieldClasses() {
		final Field field = ClassUtils.getField(Thing.class, "thing");

		// T
		final Type tType = GenericUtils.getFieldType(field, Thing.class);
		assertEquals("capture of ?", tType.toString());

		// N extends Number
		final Type nType = GenericUtils.getFieldType(field, NumberThing.class);
		assertEquals("capture of ?", nType.toString());

		// Integer
		final Type iType = GenericUtils.getFieldType(field, IntegerThing.class);
		assertSame(Integer.class, iType);
	}

	/** Tests {@link GenericUtils#getFieldClasses(Field, Class)}. */
	@Test
	public void testGetGenericType() {
		final Field field = ClassUtils.getField(Thing.class, "thing");

		// Object
		assertAllTheSame(GenericUtils.getFieldClasses(field, Thing.class),
			Object.class);

		// N extends Number
		assertAllTheSame(GenericUtils.getFieldClasses(field, NumberThing.class),
			Number.class);

		// Integer
		assertAllTheSame(GenericUtils.getFieldClasses(field, IntegerThing.class),
			Integer.class);

		// Serializable & Cloneable
		assertAllTheSame(GenericUtils.getFieldClasses(field, ComplexThing.class),
			Serializable.class, Cloneable.class);
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

	// -- Helper methods --

	/** Convenience method to get the {@link Type} of a field. */
	private Type type(final Class<?> c, final String fieldName) {
		return ClassUtils.getField(c, fieldName).getGenericType();
	}

	/**
	 * Convenience method to call {@link GenericUtils#getClass(Type)} on a field.
	 */
	private Class<?> getClass(final Class<?> c, final String fieldName) {
		return GenericUtils.getClass(type(c, fieldName));
	}

	/**
	 * Convenience method to call {@link GenericUtils#getComponentClass(Type)} on
	 * a field.
	 */
	private Class<?> getComponentClass(final Class<?> c, final String fieldName) {
		return GenericUtils.getComponentClass(type(c, fieldName));
	}

	private <T> void assertAllTheSame(final List<T> list, final T... values) {
		assertEquals(list.size(), values.length);
		for (int i = 0; i < values.length; i++) {
			assertSame(list.get(i), values[i]);
		}
	}

}
