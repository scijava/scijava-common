/*
 * #%L
 * SciJava Common shared library for SciJava software.
 * %%
 * Copyright (C) 2009 - 2017 Board of Regents of the University of
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

import com.googlecode.gentyref.GenericTypeReflector;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;

/**
 * Useful methods for working with {@link Type} objects, particularly generic
 * types.
 * <p>
 * This class leans heavily on the excellent <a
 * href="https://code.google.com/p/gentyref/">gentyref</a> library, and exists
 * mainly to keep the gentyref dependency encapsulated within SciJava Common.
 * </p>
 * 
 * @author Curtis Rueden
 * @see ClassUtils For utility methods specific to {@link Class} objects.
 * @see ConversionUtils For utility methods that convert between {@link Type}s.
 */
public final class GenericUtils {

	private GenericUtils() {
		// prevent instantiation of utility class
	}

	/**
	 * Gets the sole raw class corresponding to the given type, or null if none.
	 */
	public static Class<?> getClass(final Type type) {
		if (type == null) return null;
		if (type instanceof Class) return (Class<?>) type;
		final List<Class<?>> c = getClasses(type);
		if (c == null || c.size() != 1) return null;
		return c.get(0);
	}

	/**
	 * Gets all raw classes corresponding to the given type.
	 * <p>
	 * For example, a type parameter {@code A extends Number & Iterable} will
	 * return both {@link Number} and {@link Iterable} as its raw classes.
	 * </p>
	 */
	public static List<Class<?>> getClasses(final Type type) {
		if (type == null) return null;
		return GenericTypeReflector.getUpperBoundClassAndInterfaces(type);
	}

	/**
	 * Gets the component type of the given array type, or null if not an array.
	 */
	public static Type getComponentType(final Type type) {
		return GenericTypeReflector.getArrayComponentType(type);
	}

	/**
	 * Gets the sole component class of the given array type, or null if none.
	 */
	public static Class<?> getComponentClass(final Type type) {
		return getClass(getComponentType(type));
	}

	/**
	 * Returns the "safe" generic type of the given field, as viewed from the
	 * given type. This may be narrower than what {@link Field#getGenericType()}
	 * returns, if the field is declared in a superclass, or {@code type} has a
	 * type parameter that is used in the type of the field.
	 * <p>
	 * For example, suppose we have the following three classes:
	 * </p>
	 * 
	 * <pre>
	 * public class Thing&lt;T&gt; {
	 * 	public T thing;
	 * }
	 * 
	 * public class NumberThing&lt;N extends Number&gt; extends Thing&lt;N&gt; { }
	 * 
	 * public class IntegerThing extends NumberThing&lt;Integer&gt; { }
	 * </pre>
	 * 
	 * Then this method operates as follows:
	 * 
	 * <pre>
	 * field = ClassUtils.getField(Thing.class, "thing");
	 * 
	 * field.getType(); // Object
	 * field.getGenericType(); // T
	 * 
	 * GenericUtils.getFieldType(field, Thing.class); // T
	 * GenericUtils.getFieldType(field, NumberThing.class); // N extends Number
	 * GenericUtils.getFieldType(field, IntegerThing.class); // Integer
	 * </pre>
	 */
	public static Type getFieldType(final Field field, final Class<?> type) {
		final Type wildType = GenericTypeReflector.addWildcardParameters(type);
		return GenericTypeReflector.getExactFieldType(field, wildType);
	}

	/**
	 * Returns the "safe" class(es) of the given field, as viewed from the
	 * specified type. This may be narrower than what {@link Field#getType()}
	 * returns, if the field is declared in a superclass, or {@code type} has a
	 * type parameter that is used in the type of the field.
	 * <p>
	 * For example, suppose we have the following three classes:
	 * </p>
	 * 
	 * <pre>
	 * 
	 * public class Thing&lt;T&gt; {
	 * 
	 * 	public T thing;
	 * }
	 * 
	 * public class NumberThing&lt;N extends Number&gt; extends Thing&lt;N&gt; {}
	 * 
	 * public class IntegerThing extends NumberThing&lt;Integer&gt; {}
	 * </pre>
	 * 
	 * Then this method operates as follows:
	 * 
	 * <pre>
	 * field = ClassUtils.getField(Thing.class, &quot;thing&quot;);
	 * 
	 * field.getType(); // Object
	 * 
	 * ClassUtils.getTypes(field, Thing.class).get(0); // Object
	 * ClassUtils.getTypes(field, NumberThing.class).get(0); // Number
	 * ClassUtils.getTypes(field, IntegerThing.class).get(0); // Integer
	 * </pre>
	 * <p>
	 * In cases of complex generics which take the intersection of multiple types
	 * using the {@code &} operator, there may be multiple types returned by this
	 * method. For example:
	 * </p>
	 * 
	 * <pre>
	 * public class ComplexThing&lt;T extends Serializable &amp; Cloneable&gt; extends Thing&lt;T&gt; {}
	 * 
	 * ClassUtils.getTypes(field, ComplexThing.class); // Serializable, Cloneable
	 * </pre>
	 * 
	 * @see #getFieldType(Field, Class)
	 * @see #getClasses(Type)
	 */
	public static List<Class<?>> getFieldClasses(final Field field,
		final Class<?> type)
	{
		final Type genericType = getFieldType(field, type);
		return getClasses(genericType);
	}

	/**
	 * As {@link #getFieldType(Field, Class)}, but with respect to the return
	 * type of the given {@link Method} rather than a {@link Field}.
	 */
	public static Type getMethodReturnType(final Method method,
		final Class<?> type)
	{
		final Type wildType = GenericTypeReflector.addWildcardParameters(type);
		return GenericTypeReflector.getExactReturnType(method, wildType);
	}

	/**
	 * As {@link #getFieldClasses(Field, Class)}, but with respect to the return
	 * type of the given {@link Method} rather than a {@link Field}.
	 * 
	 * @see #getMethodReturnType(Method, Class)
	 * @see #getClasses(Type)
	 */
	public static List<Class<?>>
		getMethodReturnClasses(final Method method, final Class<?> type)
	{
		final Type genericType = getMethodReturnType(method, type);
		return getClasses(genericType);
	}

	/**
	 * Gets the given type's {@code n}th type parameter of the specified class.
	 * <p>
	 * For example, with class {@code StringList implements List<String>},
	 * {@code getTypeParameter(StringList.class, Collection.class, 0)} returns
	 * {@code String}.
	 * </p>
	 */
	public static Type getTypeParameter(final Type type, final Class<?> c,
		final int paramNo)
	{
		return GenericTypeReflector.getTypeParameter(type,
			c.getTypeParameters()[paramNo]);
	}

}
