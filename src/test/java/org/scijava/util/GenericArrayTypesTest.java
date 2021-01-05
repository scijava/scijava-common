package org.scijava.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Type;
import java.util.List;

import org.junit.Test;

public class GenericArrayTypesTest {

	@Test
	public void testArrayFields() throws NoSuchFieldException, SecurityException {
		Field rawField = Types.field(ClassWithFields.class, "rawListArray");
		Field genericWildcardField = Types.field(ClassWithFields.class, "wildcardListArray");
		Field genericTypedField = Types.field(ClassWithFields.class, "integerListArray");

		Type rawFieldType = Types.fieldType(rawField, ClassWithFields.class);
		Type genericWildcardFieldType = Types.fieldType(genericWildcardField, ClassWithFields.class);
		Type genericTypedFieldType = Types.fieldType(genericTypedField, ClassWithFields.class);

		// raw type
		assertFalse(rawFieldType instanceof GenericArrayType);
		assertTrue(rawFieldType instanceof Class);

		// generic array types
		assertTrue(genericWildcardFieldType instanceof GenericArrayType);
		assertTrue(genericTypedFieldType instanceof GenericArrayType);

		assertEquals(rawField.getGenericType(), rawFieldType);
		assertEquals(genericWildcardField.getGenericType(), genericWildcardFieldType);
		assertEquals(genericTypedField.getGenericType(), genericTypedFieldType);

		assertSame(List[].class, Types.raw(rawFieldType));
		assertSame(List[].class, Types.raw(genericWildcardFieldType));
		assertSame(List[].class, Types.raw(genericTypedFieldType));
	}

	@SuppressWarnings({ "rawtypes", "unused" })
	private static class ClassWithFields {
		public List[] rawListArray;
		public List<?>[] wildcardListArray;
		public List<Integer>[] integerListArray;
	}
}
