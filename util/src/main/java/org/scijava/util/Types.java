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

// Portions of this class were adapted from the
// org.apache.commons.lang3.reflect.TypeUtils and
// org.apache.commons.lang3.Validate classes of
// Apache Commons Lang 3.4, which is distributed
// under the Apache 2 license.
// See lines below starting with "BEGIN FORK OF APACHE COMMONS LANG".
//
// Portions of this class were adapted from the GenTyRef project
// by Wouter Coekaerts, which is distributed under the Apache 2 license.
// See lines below starting with "BEGIN FORK OF GENTYREF".
//
// See NOTICE.txt for further details on third-party licenses.

import java.io.Serializable;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.CodeSource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Utility class for working with generic types, fields and methods.
 * <p>
 * Logic and inspiration were drawn from the following excellent libraries:
 * <ul>
 * <li>Google Guava's {@code com.google.common.reflect} package.</li>
 * <li>Apache Commons Lang 3's {@code org.apache.commons.lang3.reflect} package.
 * </li>
 * <li><a href="https://github.com/coekarts/gentyref">GenTyRef</a> (Generic Type
 * Reflector), a library for runtime generic type introspection.</li>
 * </ul>
 * </p>
 *
 * @author Curtis Rueden
 */
public final class Types {

	private Types() {
		// NB: Prevent instantiation of utility class.
	}

	/**
	 * Loads the class with the given name, using the current thread's context
	 * class loader, or null if it cannot be loaded.
	 *
	 * @param name The name of the class to load.
	 * @return The loaded class, or null if the class could not be loaded.
	 * @see #load(String, ClassLoader, boolean)
	 */
	public static Class<?> load(final String name) {
		return load(name, null, true);
	}

	/**
	 * Loads the class with the given name, using the specified
	 * {@link ClassLoader}, or null if it cannot be loaded.
	 *
	 * @param name The name of the class to load.
	 * @param classLoader The class loader with which to load the class; if null,
	 *          the current thread's context class loader will be used.
	 * @return The loaded class, or null if the class could not be loaded.
	 * @see #load(String, ClassLoader, boolean)
	 */
	public static Class<?> load(final String name,
		final ClassLoader classLoader)
	{
		return load(name, classLoader, true);
	}

	/**
	 * Loads the class with the given name, using the current thread's context
	 * class loader.
	 *
	 * @param className the name of the class to load.
	 * @param quietly Whether to return {@code null} (rather than throwing
	 *          {@link IllegalArgumentException}) if something goes wrong loading
	 *          the class.
	 * @return The loaded class, or {@code null} if the class could not be loaded
	 *         and the {@code quietly} flag is set.
	 * @see #load(String, ClassLoader, boolean)
	 * @throws IllegalArgumentException If the class cannot be loaded and the
	 *           {@code quietly} flag is not set.
	 */
	public static Class<?> load(final String className, final boolean quietly) {
		return load(className, null, quietly);
	}

	/**
	 * Loads the class with the given name, using the specified
	 * {@link ClassLoader}, or null if it cannot be loaded.
	 * <p>
	 * This method is capable of parsing several different class name syntaxes. In
	 * particular, array classes (including primitives) represented using either
	 * square brackets or internal Java array name syntax are supported. Examples:
	 * </p>
	 * <ul>
	 * <li>{@code boolean} is loaded as {@code boolean.class}</li>
	 * <li>{@code Z} is loaded as {@code boolean.class}</li>
	 * <li>{@code double[]} is loaded as {@code double[].class}</li>
	 * <li>{@code string[]} is loaded as {@code java.lang.String.class}</li>
	 * <li>{@code [F} is loaded as {@code float[].class}</li>
	 * </ul>
	 *
	 * @param name The name of the class to load.
	 * @param classLoader The class loader with which to load the class; if null,
	 *          the current thread's context class loader will be used.
	 * @param quietly Whether to return {@code null} (rather than throwing
	 *          {@link IllegalArgumentException}) if something goes wrong loading
	 *          the class
	 * @return The loaded class, or {@code null} if the class could not be loaded
	 *         and the {@code quietly} flag is set.
	 * @throws IllegalArgumentException If the class cannot be loaded and the
	 *           {@code quietly} flag is not set.
	 */
	public static Class<?> load(final String name, final ClassLoader classLoader,
		final boolean quietly)
	{
		// handle primitive types
		if (name.equals("Z") || name.equals("boolean")) return boolean.class;
		if (name.equals("B") || name.equals("byte")) return byte.class;
		if (name.equals("C") || name.equals("char")) return char.class;
		if (name.equals("D") || name.equals("double")) return double.class;
		if (name.equals("F") || name.equals("float")) return float.class;
		if (name.equals("I") || name.equals("int")) return int.class;
		if (name.equals("J") || name.equals("long")) return long.class;
		if (name.equals("S") || name.equals("short")) return short.class;
		if (name.equals("V") || name.equals("void")) return void.class;

		// handle built-in class shortcuts
		final String className;
		if (name.equals("string")) className = "java.lang.String";
		else className = name;

		// handle source style arrays (e.g.: "java.lang.String[]")
		if (name.endsWith("[]")) {
			final String elementClassName = name.substring(0, name.length() - 2);
			return arrayOrNull(load(elementClassName, classLoader));
		}

		// handle non-primitive internal arrays (e.g.: "[Ljava.lang.String;")
		if (name.startsWith("[L") && name.endsWith(";")) {
			final String elementClassName = name.substring(2, name.length() - 1);
			return arrayOrNull(load(elementClassName, classLoader));
		}

		// handle other internal arrays (e.g.: "[I", "[[I", "[[Ljava.lang.String;")
		if (name.startsWith("[")) {
			final String elementClassName = name.substring(1);
			return arrayOrNull(load(elementClassName, classLoader));
		}

		// load the class!
		try {
			final ClassLoader cl = classLoader == null ? Thread.currentThread()
				.getContextClassLoader() : classLoader;
			return cl.loadClass(className);
		}
		catch (final Throwable t) {
			// NB: Do not allow any failure to load the class to crash us.
			// Not ClassNotFoundException.
			// Not NoClassDefFoundError.
			// Not UnsupportedClassVersionError!
			if (quietly) return null;
			throw iae(t, "Cannot load class: " + className);
		}
	}

	/**
	 * Gets the base location of the given class.
	 *
	 * @param c The class whose location is desired.
	 * @return URL pointing to the class, or null if the location could not be
	 *         determined.
	 * @see #location(Class, boolean)
	 */
	public static URL location(final Class<?> c) {
		return location(c, true);
	}

	/**
	 * Gets the base location of the given class.
	 * <p>
	 * If the class is directly on the file system (e.g.,
	 * "/path/to/my/package/MyClass.class") then it will return the base directory
	 * (e.g., "file:/path/to").
	 * </p>
	 * <p>
	 * If the class is within a JAR file (e.g.,
	 * "/path/to/my-jar.jar!/my/package/MyClass.class") then it will return the
	 * path to the JAR (e.g., "file:/path/to/my-jar.jar").
	 * </p>
	 *
	 * @param c The class whose location is desired.
	 * @param quietly Whether to return {@code null} (rather than throwing
	 *          {@link IllegalArgumentException}) if something goes wrong
	 *          determining the location.
	 * @return URL pointing to the class, or null if the location could not be
	 *         determined and the {@code quietly} flag is set.
	 * @throws IllegalArgumentException If the location cannot be determined and
	 *           the {@code quietly} flag is not set.
	 */
	public static URL location(final Class<?> c, final boolean quietly) {
		Exception cause = null;
		String why = null;

		// try the easy way first
		try {
			final CodeSource codeSource = c.getProtectionDomain().getCodeSource();
			if (codeSource != null) {
				final URL location = codeSource.getLocation();
				if (location != null) return location;
				why = "null code source location";
			}
			else why = "null code source";
		}
		catch (final SecurityException exc) {
			// NB: Cannot access protection domain.
			cause = exc;
			why = "cannot access protection domain";
		}

		// NB: The easy way failed, so we try the hard way. We ask for the class
		// itself as a resource, then strip the class's path from the URL string,
		// leaving the base path.

		// get the class's raw resource path
		final URL classResource = c.getResource(c.getSimpleName() + ".class");
		if (classResource == null) {
			// cannot find class resource
			if (quietly) return null;
			throw iae(cause, "No class resource for class: " + name(c), why);
		}

		final String url = classResource.toString();
		final String suffix = c.getCanonicalName().replace('.', '/') + ".class";
		if (!url.endsWith(suffix)) {
			// weird URL
			if (quietly) return null;
			throw iae(cause, "Unsupported URL format: " + url, why);
		}

		// strip the class's path from the URL string
		final String base = url.substring(0, url.length() - suffix.length());

		String path = base;

		// remove the "jar:" prefix and "!/" suffix, if present
		if (path.startsWith("jar:")) path = path.substring(4, path.length() - 2);

		try {
			return new URL(path);
		}
		catch (final MalformedURLException e) {
			if (quietly) return null;
			throw iae(e, "Malformed URL", why);
		}
	}

	/**
	 * Gets a string representation of the given type.
	 *
	 * @param t Type whose name is desired.
	 * @return The name of the given type.
	 */
	public static String name(final Type t) {
		if (t instanceof Class) {
			final Class<?> c = (Class<?>) t;
			return c.isArray() ? (name(component(c)) + "[]") : c.getName();
		}
		return t.toString();
	}

	/**
	 * Gets the (first) raw class of the given type.
	 * <ul>
	 * <li>If the type is a {@code Class} itself, the type itself is returned.
	 * </li>
	 * <li>If the type is a {@link ParameterizedType}, the raw type of the
	 * parameterized type is returned.</li>
	 * <li>If the type is a {@link GenericArrayType}, the returned type is the
	 * corresponding array class. For example: {@code List<Integer>[] => List[]}.
	 * </li>
	 * <li>If the type is a type variable or wildcard type, the raw type of the
	 * first upper bound is returned. For example:
	 * {@code <X extends Foo & Bar> => Foo}.</li>
	 * </ul>
	 * <p>
	 * If you want <em>all</em> raw classes of the given type, use {@link #raws}.
	 * </p>
	 *
	 * @param type The type from which to discern the (first) raw class.
	 * @return The type's first raw class.
	 */
	public static Class<?> raw(final Type type) {
		if (type == null) return null;
		if (type instanceof Class) return (Class<?>) type;
		final List<Class<?>> c = raws(type);
		if (c == null || c.size() == 0) return null;
		return c.get(0);
	}

	/**
	 * Gets all raw classes corresponding to the given type.
	 * <p>
	 * For example, a type parameter {@code A extends Number & Iterable} will
	 * return both {@link Number} and {@link Iterable} as its raw classes.
	 * </p>
	 *
	 * @param type The type from which to discern the raw classes.
	 * @return List of the type's raw classes.
	 * @see #raw
	 */
	public static List<Class<?>> raws(final Type type) {
		if (type == null) return null;
		return GenericTypeReflector.getUpperBoundClassAndInterfaces(type);
	}

	public static boolean isBoolean(final Class<?> type) {
		return type == boolean.class || Boolean.class.isAssignableFrom(type);
	}

	public static boolean isByte(final Class<?> type) {
		return type == byte.class || Byte.class.isAssignableFrom(type);
	}

	public static boolean isCharacter(final Class<?> type) {
		return type == char.class || Character.class.isAssignableFrom(type);
	}

	public static boolean isDouble(final Class<?> type) {
		return type == double.class || Double.class.isAssignableFrom(type);
	}

	public static boolean isFloat(final Class<?> type) {
		return type == float.class || Float.class.isAssignableFrom(type);
	}

	public static boolean isInteger(final Class<?> type) {
		return type == int.class || Integer.class.isAssignableFrom(type);
	}

	public static boolean isLong(final Class<?> type) {
		return type == long.class || Long.class.isAssignableFrom(type);
	}

	public static boolean isShort(final Class<?> type) {
		return type == short.class || Short.class.isAssignableFrom(type);
	}

	public static boolean isNumber(final Class<?> type) {
		return Number.class.isAssignableFrom(type) || type == byte.class ||
			type == double.class || type == float.class || type == int.class ||
			type == long.class || type == short.class;
	}

	public static boolean isText(final Class<?> type) {
		return String.class.isAssignableFrom(type) || isCharacter(type);
	}

	/**
	 * Compares two {@link Class} objects using their fully qualified names.
	 * <p>
	 * Note: this method provides a natural ordering that may be inconsistent with
	 * equals. Specifically, two unequal classes may return 0 when compared in
	 * this fashion if they represent the same class loaded using two different
	 * {@link ClassLoader}s. Hence, if this method is used as a basis for
	 * implementing {@link Comparable#compareTo} or
	 * {@link java.util.Comparator#compare}, that implementation may want to
	 * impose logic beyond that of this method, for breaking ties, if a total
	 * ordering consistent with equals is always required.
	 * </p>
	 *
	 * @see org.scijava.Priority#compare(org.scijava.Prioritized,
	 *      org.scijava.Prioritized)
	 */
	public static int compare(final Class<?> c1, final Class<?> c2) {
		if (c1 == c2) return 0;
		final String name1 = c1 == null ? null : c1.getName();
		final String name2 = c2 == null ? null : c2.getName();
		return MiscUtils.compare(name1, name2);
	}

	/**
	 * Returns the non-primitive {@link Class} closest to the given type.
	 * <p>
	 * Specifically, the following type conversions are done:
	 * <ul>
	 * <li>boolean.class becomes Boolean.class</li>
	 * <li>byte.class becomes Byte.class</li>
	 * <li>char.class becomes Character.class</li>
	 * <li>double.class becomes Double.class</li>
	 * <li>float.class becomes Float.class</li>
	 * <li>int.class becomes Integer.class</li>
	 * <li>long.class becomes Long.class</li>
	 * <li>short.class becomes Short.class</li>
	 * <li>void.class becomes Void.class</li>
	 * </ul>
	 * All other types are unchanged.
	 * </p>
	 */
	public static <T> Class<T> box(final Class<T> type) {
		final Class<?> destType;
		if (type == boolean.class) destType = Boolean.class;
		else if (type == byte.class) destType = Byte.class;
		else if (type == char.class) destType = Character.class;
		else if (type == double.class) destType = Double.class;
		else if (type == float.class) destType = Float.class;
		else if (type == int.class) destType = Integer.class;
		else if (type == long.class) destType = Long.class;
		else if (type == short.class) destType = Short.class;
		else if (type == void.class) destType = Void.class;
		else destType = type;
		@SuppressWarnings("unchecked")
		final Class<T> result = (Class<T>) destType;
		return result;
	}

	/**
	 * Returns the primitive {@link Class} closest to the given type.
	 * <p>
	 * Specifically, the following type conversions are done:
	 * <ul>
	 * <li>Boolean.class becomes boolean.class</li>
	 * <li>Byte.class becomes byte.class</li>
	 * <li>Character.class becomes char.class</li>
	 * <li>Double.class becomes double.class</li>
	 * <li>Float.class becomes float.class</li>
	 * <li>Integer.class becomes int.class</li>
	 * <li>Long.class becomes long.class</li>
	 * <li>Short.class becomes short.class</li>
	 * <li>Void.class becomes void.class</li>
	 * </ul>
	 * All other types are unchanged.
	 * </p>
	 */
	public static <T> Class<T> unbox(final Class<T> type) {
		final Class<?> destType;
		if (type == Boolean.class) destType = boolean.class;
		else if (type == Byte.class) destType = byte.class;
		else if (type == Character.class) destType = char.class;
		else if (type == Double.class) destType = double.class;
		else if (type == Float.class) destType = float.class;
		else if (type == Integer.class) destType = int.class;
		else if (type == Long.class) destType = long.class;
		else if (type == Short.class) destType = short.class;
		else if (type == Void.class) destType = void.class;
		else destType = type;
		@SuppressWarnings("unchecked")
		final Class<T> result = (Class<T>) destType;
		return result;
	}

	/**
	 * Gets the "null" value for the given type. For non-primitives, this will
	 * actually be null. For primitives, it will be zero for numeric types, false
	 * for boolean, and the null character for char.
	 */
	public static <T> T nullValue(final Class<T> type) {
		final Object defaultValue;
		if (type == boolean.class) defaultValue = false;
		else if (type == byte.class) defaultValue = (byte) 0;
		else if (type == char.class) defaultValue = '\0';
		else if (type == double.class) defaultValue = 0d;
		else if (type == float.class) defaultValue = 0f;
		else if (type == int.class) defaultValue = 0;
		else if (type == long.class) defaultValue = 0L;
		else if (type == short.class) defaultValue = (short) 0;
		else defaultValue = null;
		@SuppressWarnings("unchecked")
		final T result = (T) defaultValue;
		return result;
	}

	/**
	 * Gets the field with the specified name, of the given class, or superclass
	 * thereof.
	 * <p>
	 * Unlike {@link Class#getField(String)}, this method will return fields of
	 * any visibility, not just {@code public}. And unlike
	 * {@link Class#getDeclaredField(String)}, it will do so recursively,
	 * returning the first field of the given name from the class's superclass
	 * hierarchy.
	 * </p>
	 * <p>
	 * Note that this method does not guarantee that the returned field is
	 * accessible; if the field is not {@code public}, calling code will need to
	 * use {@link Field#setAccessible(boolean)} in order to manipulate the field's
	 * contents.
	 * </p>
	 * 
	 * @param c The class (or subclass thereof) containing the desired field.
	 * @param name
	 * @return The first field with the given name in the class's superclass
	 *         hierarchy.
	 * @throws IllegalArgumentException if the specified class does not contain a
	 *           method with the given name
	 */
	public static Field field(final Class<?> c, final String name) {
		if (c == null) throw iae("No such field: " + name);
		try {
			return c.getDeclaredField(name);
		}
		catch (final NoSuchFieldException e) {}
		return field(c.getSuperclass(), name);
	}

	/**
	 * Gets the method with the specified name and argument types, of the given
	 * class, or superclass thereof.
	 * <p>
	 * Unlike {@link Class#getMethod(String, Class[])}, this method will return
	 * methods of any visibility, not just {@code public}. And unlike
	 * {@link Class#getDeclaredMethod(String, Class[])}, it will do so
	 * recursively, returning the first method of the given name and argument
	 * types from the class's superclass hierarchy.
	 * </p>
	 * <p>
	 * Note that this method does not guarantee that the returned method is
	 * accessible; if the method is not {@code public}, calling code will need to
	 * use {@link Method#setAccessible(boolean)} in order to invoke the method.
	 * </p>
	 * 
	 * @param c The class (or subclass thereof) containing the desired method.
	 * @param name Name of the method.
	 * @param parameterTypes Types of the method parameters.
	 * @return The first method with the given name and argument types in the
	 *         class's superclass hierarchy.
	 * @throws IllegalArgumentException If the specified class does not contain a
	 *           method with the given name and argument types.
	 */
	public static Method method(final Class<?> c, final String name,
		final Class<?>... parameterTypes)
	{
		if (c == null) throw iae("No such field: " + name);
		try {
			return c.getDeclaredMethod(name, parameterTypes);
		}
		catch (final NoSuchMethodException exc) {}
		return method(c.getSuperclass(), name, parameterTypes);
	}

	/**
	 * Gets the array class corresponding to the given element type.
	 * <p>
	 * For example, {@code arrayType(double.class)} returns {@code double[].class}
	 * .
	 * </p>
	 *
	 * @param componentType The type of elements which the array possesses
	 * @throws IllegalArgumentException if the type cannot be the component type
	 *           of an array (this is the case e.g. for {@code void.class}).
	 */
	public static Class<?> array(final Class<?> componentType) {
		if (componentType == null) return null;
		// NB: It appears the reflection API has no built-in way to do this.
		// So unfortunately, we must allocate a new object and then inspect it.
		return Array.newInstance(componentType, 0).getClass();
	}

	/**
	 * Gets the array class corresponding to the given element type and
	 * dimensionality.
	 * <p>
	 * For example, {@code arrayType(double.class, 2)} returns
	 * {@code double[][].class} .
	 * </p>
	 *
	 * @param componentType The type of elements which the array possesses
	 * @param dim The dimensionality of the array
	 */
	public static Class<?> array(final Class<?> componentType, final int dim) {
		if (dim < 0) throw iae("Negative dimension");
		if (dim == 0) return componentType;
		return array(array(componentType), dim - 1);
	}

	/**
	 * Gets the array type&mdash;which might be a {@link Class} or a
	 * {@link GenericArrayType} depending on the argument&mdash;corresponding to
	 * the given element type.
	 * <p>
	 * For example, {@code arrayType(double.class)} returns {@code double[].class}
	 * .
	 * </p>
	 *
	 * @param componentType The type of elements which the array possesses
	 * @see #component
	 */
	public static Type array(final Type componentType) {
		if (componentType == null) return null;
		if (componentType instanceof Class) {
			return array((Class<?>) componentType);
		}
		return new TypeUtils.GenericArrayTypeImpl(componentType);
	}

	/**
	 * Gets the component type of the given array type, or null if not an array.
	 * <p>
	 * If you have a {@link Class}, you can call {@link Class#getComponentType()}
	 * for a narrower return type.
	 * </p>
	 * <p>
	 * This is the opposite of {@link #array(Type)}.
	 * </p>
	 */
	public static Type component(final Type type) {
		if (type instanceof Class) {
			return ((Class<?>) type).getComponentType();
		}
		if (type instanceof GenericArrayType) {
			return ((GenericArrayType) type).getGenericComponentType();
		}
		return null;
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
	 * field = Types.field(Thing.class, "thing");
	 *
	 * field.getType(); // Object
	 * field.getGenericType(); // T
	 *
	 * Types.fieldType(field, Thing.class); // T
	 * Types.fieldType(field, NumberThing.class); // N extends Number
	 * Types.fieldType(field, IntegerThing.class); // Integer
	 * </pre>
	 */
	public static Type fieldType(final Field field, final Class<?> type) {
		final Type wildType = GenericTypeReflector.addWildcardParameters(type);
		return GenericTypeReflector.getExactFieldType(field, wildType);
	}

	/**
	 * As {@link #fieldType(Field, Class)}, but with respect to the return type of
	 * the given {@link Method} rather than a {@link Field}.
	 */
	public static Type methodReturnType(final Method method,
		final Class<?> type)
	{
		final Type wildType = GenericTypeReflector.addWildcardParameters(type);
		return GenericTypeReflector.getExactReturnType(method, wildType);
	}

	/**
	 * As {@link #fieldType(Field, Class)}, but with respect to the parameter
	 * types of the given {@link Method} rather than a {@link Field}.
	 */
	public static Type[] methodParamTypes(final Method method,
		final Class<?> type)
	{
		final Type wildType = GenericTypeReflector.addWildcardParameters(type);
		return GenericTypeReflector.getExactParameterTypes(method, wildType);
	}

	/**
	 * Gets the given type's {@code n}th type parameter of the specified class.
	 * <p>
	 * For example, with class {@code StringList implements List<String>},
	 * {@code Types.param(StringList.class, Collection.class, 0)} returns
	 * {@code String}.
	 * </p>
	 */
	public static Type param(final Type type, final Class<?> c, final int no) {
		return GenericTypeReflector.getTypeParameter(type, //
			c.getTypeParameters()[no]);
	}

	/**
	 * Discerns whether it would be legal to assign a reference of type
	 * {@code source} to a reference of type {@code target}.
	 *
	 * @param source The type from which assignment is desired.
	 * @param target The type to which assignment is desired.
	 * @return True if the source is assignable to the target.
	 * @throws NullPointerException if {@code target} is null.
	 * @see Class#isAssignableFrom(Class)
	 */
	public static boolean isAssignable(final Type source, final Type target) {
		return TypeUtils.isAssignable(source, target);
	}

	/**
	 * Checks whether the given object can be cast to the specified type.
	 *
	 * @return true If the destination class is assignable from the source
	 *         object's class, or if the source object is null and destination
	 *         class is non-null.
	 * @see #cast(Object, Class)
	 */
	public static boolean isInstance(final Object obj, final Class<?> dest) {
		if (dest == null) return false;
		return obj == null || dest.isInstance(obj);
	}

	/**
	 * Casts the given object to the specified type, or null if the types are
	 * incompatible.
	 */
	public static <T> T cast(final Object src, final Class<T> dest) {
		if (!isInstance(src, dest)) return null;
		@SuppressWarnings("unchecked")
		final T result = (T) src;
		return result;
	}

	/**
	 * Converts the given string value to an enumeration constant of the specified
	 * type.
	 *
	 * @param name The value to convert.
	 * @param dest The type of the enumeration constant.
	 * @return The converted enumeration constant.
	 * @throws IllegalArgumentException if the type is not an enumeration type, or
	 *           has no such constant.
	 */
	public static <T> T enumValue(final String name, final Class<T> dest) {
		if (!dest.isEnum()) throw iae("Not an enum type: " + name(dest));
		@SuppressWarnings({ "rawtypes", "unchecked" })
		final Enum result = Enum.valueOf((Class) dest, name);
		@SuppressWarnings("unchecked")
		final T typedResult = (T) result;
		return typedResult;
	}

	/**
	 * Creates a new {@link ParameterizedType} of the given class together with
	 * the specified type arguments.
	 *
	 * @param rawType The class of the {@link ParameterizedType}.
	 * @param typeArgs The type arguments to use in parameterizing it.
	 * @return The newly created {@link ParameterizedType}.
	 */
	public static ParameterizedType parameterize(final Class<?> rawType,
		final Type... typeArgs)
	{
		return parameterize(rawType, rawType.getDeclaringClass(), typeArgs);
	}

	/**
	 * Creates a new {@link ParameterizedType} of the given class together with
	 * the specified type arguments.
	 *
	 * @param rawType The class of the {@link ParameterizedType}.
	 * @param ownerType The owner type of the parameterized class.
	 * @param typeArgs The type arguments to use in parameterizing it.
	 * @return The newly created {@link ParameterizedType}.
	 */
	public static ParameterizedType parameterize(final Class<?> rawType,
		final Type ownerType, final Type... typeArgs)
	{
		return new TypeUtils.ParameterizedTypeImpl(rawType, ownerType, typeArgs);
	}

	/**
	 * Creates a new {@link WildcardType} with no upper or lower bounds (i.e.,
	 * {@code ?}).
	 *
	 * @return The newly created {@link WildcardType}.
	 */
	public static WildcardType wildcard() {
		return wildcard((Type) null, (Type) null);
	}

	/**
	 * Creates a new {@link WildcardType} with the given upper and/or lower bound.
	 *
	 * @param upperBound Upper bound of the wildcard, or null for none.
	 * @param lowerBound Lower bound of the wildcard, or null for none.
	 * @return The newly created {@link WildcardType}.
	 */
	public static WildcardType wildcard(final Type upperBound,
		final Type lowerBound)
	{
		return new TypeUtils.WildcardTypeImpl(upperBound, lowerBound);
	}

	/**
	 * Creates a new {@link WildcardType} with the given upper and/or lower
	 * bounds.
	 *
	 * @param upperBounds Upper bounds of the wildcard, or null for none.
	 * @param lowerBounds Lower bounds of the wildcard, or null for none.
	 * @return The newly created {@link WildcardType}.
	 */
	public static WildcardType wildcard(final Type[] upperBounds,
		final Type[] lowerBounds)
	{
		return new TypeUtils.WildcardTypeImpl(upperBounds, lowerBounds);
	}

	/**
	 * Learn, recursively, whether any of the type parameters associated with
	 * {@code type} are bound to variables.
	 *
	 * @param type the type to check for type variables
	 * @return boolean
	 */
	public static boolean containsTypeVars(final Type type) {
		return TypeUtils.containsTypeVariables(type);
	}

	/**
	 * Gets the type arguments of a class/interface based on a subtype. For
	 * instance, this method will determine that both of the parameters for the
	 * interface {@link Map} are {@link Object} for the subtype
	 * {@link java.util.Properties Properties} even though the subtype does not
	 * directly implement the {@code Map} interface.
	 * <p>
	 * This method returns {@code null} if {@code type} is not assignable to
	 * {@code toClass}. It returns an empty map if none of the classes or
	 * interfaces in its inheritance hierarchy specify any type arguments.
	 * </p>
	 * <p>
	 * A side effect of this method is that it also retrieves the type arguments
	 * for the classes and interfaces that are part of the hierarchy between
	 * {@code type} and {@code toClass}. So with the above example, this method
	 * will also determine that the type arguments for {@link java.util.Hashtable
	 * Hashtable} are also both {@code Object}. In cases where the interface
	 * specified by {@code toClass} is (indirectly) implemented more than once
	 * (e.g. where {@code toClass} specifies the interface
	 * {@link java.lang.Iterable Iterable} and {@code type} specifies a
	 * parameterized type that implements both {@link java.util.Set Set} and
	 * {@link java.util.Collection Collection}), this method will look at the
	 * inheritance hierarchy of only one of the implementations/subclasses; the
	 * first interface encountered that isn't a subinterface to one of the others
	 * in the {@code type} to {@code toClass} hierarchy.
	 * </p>
	 *
	 * @param type the type from which to determine the type parameters of
	 *          {@code toClass}
	 * @param toClass the class whose type parameters are to be determined based
	 *          on the subtype {@code type}
	 * @return a {@code Map} of the type assignments for the type variables in
	 *         each type in the inheritance hierarchy from {@code type} to
	 *         {@code toClass} inclusive.
	 */
	public static Map<TypeVariable<?>, Type> args(final Type type,
		final Class<?> toClass)
	{
		return TypeUtils.getTypeArguments(type, toClass);
	}

	/**
	 * Tries to determine the type arguments of a class/interface based on a super
	 * parameterized type's type arguments. This method is the inverse of
	 * {@link #args(Type, Class)} which gets a class/interface's type arguments
	 * based on a subtype. It is far more limited in determining the type
	 * arguments for the subject class's type variables in that it can only
	 * determine those parameters that map from the subject {@link Class} object
	 * to the supertype.
	 * <p>
	 * Example: {@link java.util.TreeSet TreeSet} sets its parameter as the
	 * parameter for {@link java.util.NavigableSet NavigableSet}, which in turn
	 * sets the parameter of {@link java.util.SortedSet}, which in turn sets the
	 * parameter of {@link Set}, which in turn sets the parameter of
	 * {@link java.util.Collection}, which in turn sets the parameter of
	 * {@link java.lang.Iterable}. Since {@code TreeSet}'s parameter maps
	 * (indirectly) to {@code Iterable}'s parameter, it will be able to determine
	 * that based on the super type {@code Iterable<? extends
	 * Map<Integer, ? extends Collection<?>>>}, the parameter of {@code TreeSet}
	 * is {@code ? extends Map<Integer, ? extends Collection<?>>}.
	 * </p>
	 *
	 * @param c the class whose type parameters are to be determined, not
	 *          {@code null}
	 * @param superType the super type from which {@code c}'s type arguments are
	 *          to be determined, not {@code null}
	 * @return a {@code Map} of the type assignments that could be determined for
	 *         the type variables in each type in the inheritance hierarchy from
	 *         {@code type} to {@code c} inclusive.
	 */
	public static Map<TypeVariable<?>, Type> args(final Class<?> c,
		final ParameterizedType superType)
	{
		return TypeUtils.determineTypeArguments(c, superType);
	}

	/**
	 * Create a parameterized type instance.
	 *
	 * @param raw the raw class to create a parameterized type instance for
	 * @param typeArgMappings the mapping used for parameterization
	 * @return {@link ParameterizedType}
	 */
	public static final ParameterizedType parameterize(final Class<?> raw,
		final Map<TypeVariable<?>, Type> typeArgMappings)
	{
		return TypeUtils.parameterize(raw, typeArgMappings);
	}

	// -- Helper methods --

	private static IllegalArgumentException iae(final String... s) {
		return iae(null, s);
	}

	private static IllegalArgumentException iae(final Throwable cause,
		final String... notes)
	{
		final String s = String.join(", ", notes);
		final IllegalArgumentException exc = new IllegalArgumentException(s);
		if (cause != null) exc.initCause(cause);
		throw exc;
	}

	private static Class<?> arrayOrNull(final Class<?> componentType) {
		try {
			return Types.array(componentType);
		}
		catch (final IllegalArgumentException exc) {
			return null;
		}
	}

	// -- BEGIN FORK OF APACHE COMMONS LANG 3.4 CODE --

	/*
	 * Licensed to the Apache Software Foundation (ASF) under one or more
	 * contributor license agreements.  See the NOTICE file distributed with
	 * this work for additional information regarding copyright ownership.
	 * The ASF licenses this file to You under the Apache License, Version 2.0
	 * (the "License"); you may not use this file except in compliance with
	 * the License.  You may obtain a copy of the License at
	 *
	 *      http://www.apache.org/licenses/LICENSE-2.0
	 *
	 * Unless required by applicable law or agreed to in writing, software
	 * distributed under the License is distributed on an "AS IS" BASIS,
	 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	 * See the License for the specific language governing permissions and
	 * limitations under the License.
	 */

	/**
	 * <p>
	 * Utility methods focusing on type inspection, particularly with regard to
	 * generics.
	 * </p>
	 *
	 * @since 3.0
	 * @version $Id: TypeUtils.java 1606051 2014-06-27 12:22:17Z ggregory $
	 */
	@SuppressWarnings("unused")
	private static class TypeUtils {

		/**
		 * {@link WildcardType} builder.
		 *
		 * @since 3.2
		 */
		public static class WildcardTypeBuilder {

			/**
			 * Constructor
			 */
			private WildcardTypeBuilder() {}

			private Type[] upperBounds;
			private Type[] lowerBounds;

			/**
			 * Specify upper bounds of the wildcard type to build.
			 *
			 * @param bounds to set
			 * @return {@code this}
			 */
			public WildcardTypeBuilder withUpperBounds(final Type... bounds) {
				this.upperBounds = bounds;
				return this;
			}

			/**
			 * Specify lower bounds of the wildcard type to build.
			 *
			 * @param bounds to set
			 * @return {@code this}
			 */
			public WildcardTypeBuilder withLowerBounds(final Type... bounds) {
				this.lowerBounds = bounds;
				return this;
			}

			public WildcardType build() {
				return new WildcardTypeImpl(upperBounds, lowerBounds);
			}
		}

		/**
		 * GenericArrayType implementation class.
		 *
		 * @since 3.2
		 */
		private static final class GenericArrayTypeImpl implements
			GenericArrayType
		{

			private final Type componentType;

			/**
			 * Constructor
			 *
			 * @param componentType of this array type
			 */
			private GenericArrayTypeImpl(final Type componentType) {
				this.componentType = componentType;
			}

			@Override
			public Type getGenericComponentType() {
				return componentType;
			}

			@Override
			public String toString() {
				return TypeUtils.toString(this);
			}

			@Override
			public boolean equals(final Object obj) {
				return obj == this || obj instanceof GenericArrayType && TypeUtils
					.equals(this, (GenericArrayType) obj);
			}

			@Override
			public int hashCode() {
				int result = 67 << 4;
				result |= componentType.hashCode();
				return result;
			}
		}

		/**
		 * ParameterizedType implementation class.
		 *
		 * @since 3.2
		 */
		private static final class ParameterizedTypeImpl implements
			ParameterizedType
		{

			private final Class<?> raw;
			private final Type useOwner;
			private final Type[] typeArguments;

			/**
			 * Constructor
			 *
			 * @param raw type
			 * @param useOwner owner type to use, if any
			 * @param typeArguments formal type arguments
			 */
			private ParameterizedTypeImpl(final Class<?> raw, final Type useOwner,
				final Type[] typeArguments)
			{
				this.raw = raw;
				this.useOwner = useOwner;
				this.typeArguments = typeArguments;
			}

			@Override
			public Type getRawType() {
				return raw;
			}

			@Override
			public Type getOwnerType() {
				return useOwner;
			}

			@Override
			public Type[] getActualTypeArguments() {
				return typeArguments.clone();
			}

			@Override
			public String toString() {
				return TypeUtils.toString(this);
			}

			@Override
			public boolean equals(final Object obj) {
				return obj == this || obj instanceof ParameterizedType && TypeUtils
					.equals(this, ((ParameterizedType) obj));
			}

			@Override
			public int hashCode() {
				int result = 71 << 4;
				result |= raw.hashCode();
				result <<= 4;
				result |= Objects.hashCode(useOwner);
				result <<= 8;
				result |= Arrays.hashCode(typeArguments);
				return result;
			}
		}

		/**
		 * WildcardType implementation class.
		 *
		 * @since 3.2
		 */
		private static final class WildcardTypeImpl implements WildcardType {

			private static final Type[] EMPTY_BOUNDS = new Type[0];

			private final Type[] upperBounds;
			private final Type[] lowerBounds;

			/**
			 * Constructor
			 *
			 * @param upperBound of this type
			 * @param lowerBound of this type
			 */
			private WildcardTypeImpl(final Type upperBound, final Type lowerBound) {
				this(upperBound == null ? null : new Type[] { upperBound },
					lowerBound == null ? null : new Type[] { lowerBound });
			}

			/**
			 * Constructor
			 *
			 * @param upperBounds of this type
			 * @param lowerBounds of this type
			 */
			private WildcardTypeImpl(final Type[] upperBounds,
				final Type[] lowerBounds)
			{
				this.upperBounds = upperBounds == null ? EMPTY_BOUNDS : upperBounds;
				this.lowerBounds = lowerBounds == null ? EMPTY_BOUNDS : lowerBounds;
			}

			@Override
			public Type[] getUpperBounds() {
				return upperBounds.clone();
			}

			@Override
			public Type[] getLowerBounds() {
				return lowerBounds.clone();
			}

			@Override
			public String toString() {
				return TypeUtils.toString(this);
			}

			@Override
			public boolean equals(final Object obj) {
				return obj == this || obj instanceof WildcardType && TypeUtils.equals(
					this, (WildcardType) obj);
			}

			@Override
			public int hashCode() {
				int result = 73 << 8;
				result |= Arrays.hashCode(upperBounds);
				result <<= 8;
				result |= Arrays.hashCode(lowerBounds);
				return result;
			}
		}

		/**
		 * A wildcard instance matching {@code ?}.
		 *
		 * @since 3.2
		 */
		public static final WildcardType WILDCARD_ALL = //
			wildcardType().withUpperBounds(Object.class).build();

		/**
		 * <p>
		 * Checks if the subject type may be implicitly cast to the target type
		 * following the Java generics rules. If both types are {@link Class}
		 * objects, the method returns the result of
		 * {@link Class#isAssignableFrom(Class)}.
		 * </p>
		 *
		 * @param type the subject type to be assigned to the target type
		 * @param toType the target type
		 * @return {@code true} if {@code type} is assignable to {@code toType}.
		 * @throws NullPointerException if {@code toType} is null.
		 */
		public static boolean isAssignable(final Type type, final Type toType) {
			if (toType == null) {
				throw new NullPointerException("Destination type is null");
			}
			return isAssignable(type, toType, null);
		}

		/**
		 * <p>
		 * Checks if the subject type may be implicitly cast to the target type
		 * following the Java generics rules.
		 * </p>
		 *
		 * @param type the subject type to be assigned to the target type
		 * @param toType the target type
		 * @param typeVarAssigns optional map of type variable assignments
		 * @return {@code true} if {@code type} is assignable to {@code toType}.
		 */
		private static boolean isAssignable(final Type type, final Type toType,
			final Map<TypeVariable<?>, Type> typeVarAssigns)
		{
			if (toType == null || toType instanceof Class) {
				return isAssignable(type, (Class<?>) toType);
			}

			if (toType instanceof ParameterizedType) {
				return isAssignable(type, (ParameterizedType) toType, typeVarAssigns);
			}

			if (toType instanceof GenericArrayType) {
				return isAssignable(type, (GenericArrayType) toType, typeVarAssigns);
			}

			if (toType instanceof WildcardType) {
				return isAssignable(type, (WildcardType) toType, typeVarAssigns);
			}

			if (toType instanceof TypeVariable) {
				return isAssignable(type, (TypeVariable<?>) toType, typeVarAssigns);
			}

			throw new IllegalStateException("found an unhandled type: " + toType);
		}

		/**
		 * <p>
		 * Checks if the subject type may be implicitly cast to the target class
		 * following the Java generics rules.
		 * </p>
		 *
		 * @param type the subject type to be assigned to the target type
		 * @param toClass the target class
		 * @return {@code true} if {@code type} is assignable to {@code toClass}.
		 */
		private static boolean isAssignable(final Type type,
			final Class<?> toClass)
		{
			if (type == null) {
				// consistency with ClassUtils.isAssignable() behavior
				return toClass == null || !toClass.isPrimitive();
			}

			// only a null type can be assigned to null type which
			// would have cause the previous to return true
			if (toClass == null) {
				return false;
			}

			// all types are assignable to themselves
			if (toClass.equals(type)) {
				return true;
			}

			if (type instanceof Class) {
				// just comparing two classes
				return toClass.isAssignableFrom((Class<?>) type);
			}

			if (type instanceof ParameterizedType) {
				// only have to compare the raw type to the class
				return isAssignable(getRawType((ParameterizedType) type), toClass);
			}

			// *
			if (type instanceof TypeVariable) {
				// if any of the bounds are assignable to the class, then the
				// type is assignable to the class.
				for (final Type bound : ((TypeVariable<?>) type).getBounds()) {
					if (isAssignable(bound, toClass)) {
						return true;
					}
				}

				return false;
			}

			// the only classes to which a generic array type can be assigned
			// are class Object and array classes
			if (type instanceof GenericArrayType) {
				return toClass.equals(Object.class) || toClass.isArray() &&
					isAssignable(((GenericArrayType) type).getGenericComponentType(),
						toClass.getComponentType());
			}

			// wildcard types are not assignable to a class (though one would think
			// "? super Object" would be assignable to Object)
			if (type instanceof WildcardType) {
				return false;
			}

			throw new IllegalStateException("found an unhandled type: " + type);
		}

		/**
		 * <p>
		 * Checks if the subject type may be implicitly cast to the target
		 * parameterized type following the Java generics rules.
		 * </p>
		 *
		 * @param type the subject type to be assigned to the target type
		 * @param toParameterizedType the target parameterized type
		 * @param typeVarAssigns a map with type variables
		 * @return {@code true} if {@code type} is assignable to {@code toType}.
		 */
		private static boolean isAssignable(final Type type,
			final ParameterizedType toParameterizedType,
			final Map<TypeVariable<?>, Type> typeVarAssigns)
		{
			if (type == null) {
				return true;
			}

			// only a null type can be assigned to null type which
			// would have cause the previous to return true
			if (toParameterizedType == null) {
				return false;
			}

			// all types are assignable to themselves
			if (toParameterizedType.equals(type)) {
				return true;
			}

			// get the target type's raw type
			final Class<?> toClass = getRawType(toParameterizedType);
			// get the subject type's type arguments including owner type arguments
			// and supertype arguments up to and including the target class.
			final Map<TypeVariable<?>, Type> fromTypeVarAssigns = getTypeArguments(
				type, toClass, null);

			// null means the two types are not compatible
			if (fromTypeVarAssigns == null) {
				return false;
			}

			// compatible types, but there's no type arguments. this is equivalent
			// to comparing Map< ?, ? > to Map, and raw types are always assignable
			// to parameterized types.
			if (fromTypeVarAssigns.isEmpty()) {
				return true;
			}

			// get the target type's type arguments including owner type arguments
			final Map<TypeVariable<?>, Type> toTypeVarAssigns = getTypeArguments(
				toParameterizedType, toClass, typeVarAssigns);

			// now to check each type argument
			for (final TypeVariable<?> var : toTypeVarAssigns.keySet()) {
				final Type toTypeArg = unrollVariableAssignments(var, toTypeVarAssigns);
				final Type fromTypeArg = unrollVariableAssignments(var,
					fromTypeVarAssigns);

				// parameters must either be absent from the subject type, within
				// the bounds of the wildcard type, or be an exact match to the
				// parameters of the target type.
				if (fromTypeArg != null && !toTypeArg.equals(fromTypeArg) &&
					!(toTypeArg instanceof WildcardType && isAssignable(fromTypeArg,
						toTypeArg, typeVarAssigns)))
				{
					return false;
				}
			}
			return true;
		}

		/**
		 * Look up {@code var} in {@code typeVarAssigns} <em>transitively</em>, i.e.
		 * keep looking until the value found is <em>not</em> a type variable.
		 *
		 * @param var the type variable to look up
		 * @param typeVarAssigns the map used for the look up
		 * @return Type or {@code null} if some variable was not in the map
		 * @since 3.2
		 */
		private static Type unrollVariableAssignments(TypeVariable<?> var,
			final Map<TypeVariable<?>, Type> typeVarAssigns)
		{
			Type result;
			do {
				result = typeVarAssigns.get(var);
				if (result instanceof TypeVariable && !result.equals(var)) {
					var = (TypeVariable<?>) result;
					continue;
				}
				break;
			}
			while (true);
			return result;
		}

		/**
		 * <p>
		 * Checks if the subject type may be implicitly cast to the target generic
		 * array type following the Java generics rules.
		 * </p>
		 *
		 * @param type the subject type to be assigned to the target type
		 * @param toGenericArrayType the target generic array type
		 * @param typeVarAssigns a map with type variables
		 * @return {@code true} if {@code type} is assignable to
		 *         {@code toGenericArrayType}.
		 */
		private static boolean isAssignable(final Type type,
			final GenericArrayType toGenericArrayType,
			final Map<TypeVariable<?>, Type> typeVarAssigns)
		{
			if (type == null) {
				return true;
			}

			// only a null type can be assigned to null type which
			// would have cause the previous to return true
			if (toGenericArrayType == null) {
				return false;
			}

			// all types are assignable to themselves
			if (toGenericArrayType.equals(type)) {
				return true;
			}

			final Type toComponentType = toGenericArrayType.getGenericComponentType();

			if (type instanceof Class) {
				final Class<?> cls = (Class<?>) type;

				// compare the component types
				return cls.isArray() && isAssignable(cls.getComponentType(),
					toComponentType, typeVarAssigns);
			}

			if (type instanceof GenericArrayType) {
				// compare the component types
				return isAssignable(((GenericArrayType) type).getGenericComponentType(),
					toComponentType, typeVarAssigns);
			}

			if (type instanceof WildcardType) {
				// so long as one of the upper bounds is assignable, it's good
				for (final Type bound : getImplicitUpperBounds((WildcardType) type)) {
					if (isAssignable(bound, toGenericArrayType)) {
						return true;
					}
				}

				return false;
			}

			if (type instanceof TypeVariable) {
				// probably should remove the following logic and just return false.
				// type variables cannot specify arrays as bounds.
				for (final Type bound : getImplicitBounds((TypeVariable<?>) type)) {
					if (isAssignable(bound, toGenericArrayType)) {
						return true;
					}
				}

				return false;
			}

			if (type instanceof ParameterizedType) {
				// the raw type of a parameterized type is never an array or
				// generic array, otherwise the declaration would look like this:
				// Collection[]< ? extends String > collection;
				return false;
			}

			throw new IllegalStateException("found an unhandled type: " + type);
		}

		/**
		 * <p>
		 * Checks if the subject type may be implicitly cast to the target wildcard
		 * type following the Java generics rules.
		 * </p>
		 *
		 * @param type the subject type to be assigned to the target type
		 * @param toWildcardType the target wildcard type
		 * @param typeVarAssigns a map with type variables
		 * @return {@code true} if {@code type} is assignable to
		 *         {@code toWildcardType}.
		 */
		private static boolean isAssignable(final Type type,
			final WildcardType toWildcardType,
			final Map<TypeVariable<?>, Type> typeVarAssigns)
		{
			if (type == null) {
				return true;
			}

			// only a null type can be assigned to null type which
			// would have cause the previous to return true
			if (toWildcardType == null) {
				return false;
			}

			// all types are assignable to themselves
			if (toWildcardType.equals(type)) {
				return true;
			}

			final Type[] toUpperBounds = getImplicitUpperBounds(toWildcardType);
			final Type[] toLowerBounds = getImplicitLowerBounds(toWildcardType);

			if (type instanceof WildcardType) {
				final WildcardType wildcardType = (WildcardType) type;
				final Type[] upperBounds = getImplicitUpperBounds(wildcardType);
				final Type[] lowerBounds = getImplicitLowerBounds(wildcardType);

				for (Type toBound : toUpperBounds) {
					// if there are assignments for unresolved type variables,
					// now's the time to substitute them.
					toBound = substituteTypeVariables(toBound, typeVarAssigns);

					// each upper bound of the subject type has to be assignable to
					// each
					// upper bound of the target type
					for (final Type bound : upperBounds) {
						if (!isAssignable(bound, toBound, typeVarAssigns)) {
							return false;
						}
					}
				}

				for (Type toBound : toLowerBounds) {
					// if there are assignments for unresolved type variables,
					// now's the time to substitute them.
					toBound = substituteTypeVariables(toBound, typeVarAssigns);

					// each lower bound of the target type has to be assignable to
					// each
					// lower bound of the subject type
					for (final Type bound : lowerBounds) {
						if (!isAssignable(toBound, bound, typeVarAssigns)) {
							return false;
						}
					}
				}
				return true;
			}

			for (final Type toBound : toUpperBounds) {
				// if there are assignments for unresolved type variables,
				// now's the time to substitute them.
				if (!isAssignable(type, substituteTypeVariables(toBound,
					typeVarAssigns), typeVarAssigns))
				{
					return false;
				}
			}

			for (final Type toBound : toLowerBounds) {
				// if there are assignments for unresolved type variables,
				// now's the time to substitute them.
				if (!isAssignable(substituteTypeVariables(toBound, typeVarAssigns),
					type, typeVarAssigns))
				{
					return false;
				}
			}
			return true;
		}

		/**
		 * <p>
		 * Checks if the subject type may be implicitly cast to the target type
		 * variable following the Java generics rules.
		 * </p>
		 *
		 * @param type the subject type to be assigned to the target type
		 * @param toTypeVariable the target type variable
		 * @param typeVarAssigns a map with type variables
		 * @return {@code true} if {@code type} is assignable to
		 *         {@code toTypeVariable}.
		 */
		private static boolean isAssignable(final Type type,
			final TypeVariable<?> toTypeVariable,
			final Map<TypeVariable<?>, Type> typeVarAssigns)
		{
			if (type == null) {
				return true;
			}

			// only a null type can be assigned to null type which
			// would have cause the previous to return true
			if (toTypeVariable == null) {
				return false;
			}

			// all types are assignable to themselves
			if (toTypeVariable.equals(type)) {
				return true;
			}

			if (type instanceof TypeVariable) {
				// a type variable is assignable to another type variable, if
				// and only if the former is the latter, extends the latter, or
				// is otherwise a descendant of the latter.
				final Type[] bounds = getImplicitBounds((TypeVariable<?>) type);

				for (final Type bound : bounds) {
					if (isAssignable(bound, toTypeVariable, typeVarAssigns)) {
						return true;
					}
				}
			}

			if (type instanceof Class || type instanceof ParameterizedType ||
				type instanceof GenericArrayType || type instanceof WildcardType)
			{
				return false;
			}

			throw new IllegalStateException("found an unhandled type: " + type);
		}

		/**
		 * <p>
		 * Find the mapping for {@code type} in {@code typeVarAssigns}.
		 * </p>
		 *
		 * @param type the type to be replaced
		 * @param typeVarAssigns the map with type variables
		 * @return the replaced type
		 * @throws IllegalArgumentException if the type cannot be substituted
		 */
		private static Type substituteTypeVariables(final Type type,
			final Map<TypeVariable<?>, Type> typeVarAssigns)
		{
			if (type instanceof TypeVariable && typeVarAssigns != null) {
				final Type replacementType = typeVarAssigns.get(type);

				if (replacementType == null) {
					throw new IllegalArgumentException(
						"missing assignment type for type variable " + type);
				}
				return replacementType;
			}
			return type;
		}

		/**
		 * <p>
		 * Retrieves all the type arguments for this parameterized type including
		 * owner hierarchy arguments such as {@code Outer<K,V>.Inner<T>.DeepInner
		 * <E>} . The arguments are returned in a {@link Map} specifying the
		 * argument type for each {@link TypeVariable}.
		 * </p>
		 *
		 * @param type specifies the subject parameterized type from which to
		 *          harvest the parameters.
		 * @return a {@code Map} of the type arguments to their respective type
		 *         variables.
		 */
		public static Map<TypeVariable<?>, Type> getTypeArguments(
			final ParameterizedType type)
		{
			return getTypeArguments(type, getRawType(type), null);
		}

		/**
		 * <p>
		 * Gets the type arguments of a class/interface based on a subtype. For
		 * instance, this method will determine that both of the parameters for the
		 * interface {@link Map} are {@link Object} for the subtype
		 * {@link java.util.Properties Properties} even though the subtype does not
		 * directly implement the {@code Map} interface.
		 * </p>
		 * <p>
		 * This method returns {@code null} if {@code type} is not assignable to
		 * {@code toClass}. It returns an empty map if none of the classes or
		 * interfaces in its inheritance hierarchy specify any type arguments.
		 * </p>
		 * <p>
		 * A side effect of this method is that it also retrieves the type arguments
		 * for the classes and interfaces that are part of the hierarchy between
		 * {@code type} and {@code toClass}. So with the above example, this method
		 * will also determine that the type arguments for
		 * {@link java.util.Hashtable Hashtable} are also both {@code Object}. In
		 * cases where the interface specified by {@code toClass} is (indirectly)
		 * implemented more than once (e.g. where {@code toClass} specifies the
		 * interface {@link java.lang.Iterable Iterable} and {@code type} specifies
		 * a parameterized type that implements both {@link java.util.Set Set} and
		 * {@link java.util.Collection Collection}), this method will look at the
		 * inheritance hierarchy of only one of the implementations/subclasses; the
		 * first interface encountered that isn't a subinterface to one of the
		 * others in the {@code type} to {@code toClass} hierarchy.
		 * </p>
		 *
		 * @param type the type from which to determine the type parameters of
		 *          {@code toClass}
		 * @param toClass the class whose type parameters are to be determined based
		 *          on the subtype {@code type}
		 * @return a {@code Map} of the type assignments for the type variables in
		 *         each type in the inheritance hierarchy from {@code type} to
		 *         {@code toClass} inclusive.
		 */
		public static Map<TypeVariable<?>, Type> getTypeArguments(final Type type,
			final Class<?> toClass)
		{
			return getTypeArguments(type, toClass, null);
		}

		/**
		 * <p>
		 * Return a map of the type arguments of @{code type} in the context of
		 * {@code toClass}.
		 * </p>
		 *
		 * @param type the type in question
		 * @param toClass the class
		 * @param subtypeVarAssigns a map with type variables
		 * @return the {@code Map} with type arguments
		 */
		private static Map<TypeVariable<?>, Type> getTypeArguments(final Type type,
			final Class<?> toClass,
			final Map<TypeVariable<?>, Type> subtypeVarAssigns)
		{
			if (type instanceof Class) {
				return getTypeArguments((Class<?>) type, toClass, subtypeVarAssigns);
			}

			if (type instanceof ParameterizedType) {
				return getTypeArguments((ParameterizedType) type, toClass,
					subtypeVarAssigns);
			}

			if (type instanceof GenericArrayType) {
				return getTypeArguments(((GenericArrayType) type)
					.getGenericComponentType(), toClass.isArray() ? toClass
						.getComponentType() : toClass, subtypeVarAssigns);
			}

			// since wildcard types are not assignable to classes, should this just
			// return null?
			if (type instanceof WildcardType) {
				for (final Type bound : getImplicitUpperBounds((WildcardType) type)) {
					// find the first bound that is assignable to the target class
					if (isAssignable(bound, toClass)) {
						return getTypeArguments(bound, toClass, subtypeVarAssigns);
					}
				}

				return null;
			}

			if (type instanceof TypeVariable) {
				for (final Type bound : getImplicitBounds((TypeVariable<?>) type)) {
					// find the first bound that is assignable to the target class
					if (isAssignable(bound, toClass)) {
						return getTypeArguments(bound, toClass, subtypeVarAssigns);
					}
				}

				return null;
			}
			throw new IllegalStateException("found an unhandled type: " + type);
		}

		/**
		 * <p>
		 * Return a map of the type arguments of a parameterized type in the context
		 * of {@code toClass}.
		 * </p>
		 *
		 * @param parameterizedType the parameterized type
		 * @param toClass the class
		 * @param subtypeVarAssigns a map with type variables
		 * @return the {@code Map} with type arguments
		 */
		private static Map<TypeVariable<?>, Type> getTypeArguments(
			final ParameterizedType parameterizedType, final Class<?> toClass,
			final Map<TypeVariable<?>, Type> subtypeVarAssigns)
		{
			final Class<?> cls = getRawType(parameterizedType);

			// make sure they're assignable
			if (!isAssignable(cls, toClass)) {
				return null;
			}

			final Type ownerType = parameterizedType.getOwnerType();
			Map<TypeVariable<?>, Type> typeVarAssigns;

			if (ownerType instanceof ParameterizedType) {
				// get the owner type arguments first
				final ParameterizedType parameterizedOwnerType =
					(ParameterizedType) ownerType;
				typeVarAssigns = getTypeArguments(parameterizedOwnerType, getRawType(
					parameterizedOwnerType), subtypeVarAssigns);
			}
			else {
				// no owner, prep the type variable assignments map
				typeVarAssigns = subtypeVarAssigns == null ? new HashMap<>()
					: new HashMap<>(subtypeVarAssigns);
			}

			// get the subject parameterized type's arguments
			final Type[] typeArgs = parameterizedType.getActualTypeArguments();
			// and get the corresponding type variables from the raw class
			final TypeVariable<?>[] typeParams = cls.getTypeParameters();

			// map the arguments to their respective type variables
			for (int i = 0; i < typeParams.length; i++) {
				final Type typeArg = typeArgs[i];
				typeVarAssigns.put(typeParams[i], typeVarAssigns.containsKey(typeArg)
					? typeVarAssigns.get(typeArg) : typeArg);
			}

			if (toClass.equals(cls)) {
				// target class has been reached. Done.
				return typeVarAssigns;
			}

			// walk the inheritance hierarchy until the target class is reached
			return getTypeArguments(getClosestParentType(cls, toClass), toClass,
				typeVarAssigns);
		}

		/**
		 * <p>
		 * Return a map of the type arguments of a class in the context of @{code
		 * toClass}.
		 * </p>
		 *
		 * @param cls the class in question
		 * @param toClass the context class
		 * @param subtypeVarAssigns a map with type variables
		 * @return the {@code Map} with type arguments
		 */
		private static Map<TypeVariable<?>, Type> getTypeArguments(Class<?> cls,
			final Class<?> toClass,
			final Map<TypeVariable<?>, Type> subtypeVarAssigns)
		{
			// make sure they're assignable
			if (!isAssignable(cls, toClass)) {
				return null;
			}

			// can't work with primitives
			if (cls.isPrimitive()) {
				// both classes are primitives?
				if (toClass.isPrimitive()) {
					// dealing with widening here. No type arguments to be
					// harvested with these two types.
					return new HashMap<>();
				}

				// work with wrapper the wrapper class instead of the primitive
				cls = Types.box(cls);
			}

			// create a copy of the incoming map, or an empty one if it's null
			final HashMap<TypeVariable<?>, Type> typeVarAssigns =
				subtypeVarAssigns == null ? new HashMap<>() : new HashMap<>(
					subtypeVarAssigns);

			// has target class been reached?
			if (toClass.equals(cls)) {
				return typeVarAssigns;
			}

			// walk the inheritance hierarchy until the target class is reached
			return getTypeArguments(getClosestParentType(cls, toClass), toClass,
				typeVarAssigns);
		}

		/**
		 * <p>
		 * Tries to determine the type arguments of a class/interface based on a
		 * super parameterized type's type arguments. This method is the inverse of
		 * {@link #getTypeArguments(Type, Class)} which gets a class/interface's
		 * type arguments based on a subtype. It is far more limited in determining
		 * the type arguments for the subject class's type variables in that it can
		 * only determine those parameters that map from the subject {@link Class}
		 * object to the supertype.
		 * </p>
		 * <p>
		 * Example: {@link java.util.TreeSet TreeSet} sets its parameter as the
		 * parameter for {@link java.util.NavigableSet NavigableSet}, which in turn
		 * sets the parameter of {@link java.util.SortedSet}, which in turn sets the
		 * parameter of {@link Set}, which in turn sets the parameter of
		 * {@link java.util.Collection}, which in turn sets the parameter of
		 * {@link java.lang.Iterable}. Since {@code TreeSet}'s parameter maps
		 * (indirectly) to {@code Iterable}'s parameter, it will be able to
		 * determine that based on the super type {@code Iterable<? extends
		 * Map<Integer, ? extends Collection<?>>>}, the parameter of {@code TreeSet}
		 * is {@code ? extends Map<Integer, ? extends
		 * Collection<?>>}.
		 * </p>
		 *
		 * @param cls the class whose type parameters are to be determined, not
		 *          {@code null}
		 * @param superType the super type from which {@code cls}'s type arguments
		 *          are to be determined, not {@code null}
		 * @return a {@code Map} of the type assignments that could be determined
		 *         for the type variables in each type in the inheritance hierarchy
		 *         from {@code type} to {@code toClass} inclusive.
		 */
		public static Map<TypeVariable<?>, Type> determineTypeArguments(
			final Class<?> cls, final ParameterizedType superType)
		{
			validateNotNull(cls, "cls is null");
			validateNotNull(superType, "superType is null");

			final Class<?> superClass = getRawType(superType);

			// compatibility check
			if (!isAssignable(cls, superClass)) {
				return null;
			}

			if (cls.equals(superClass)) {
				return getTypeArguments(superType, superClass, null);
			}

			// get the next class in the inheritance hierarchy
			final Type midType = getClosestParentType(cls, superClass);

			// can only be a class or a parameterized type
			if (midType instanceof Class) {
				return determineTypeArguments((Class<?>) midType, superType);
			}

			final ParameterizedType midParameterizedType =
				(ParameterizedType) midType;
			final Class<?> midClass = getRawType(midParameterizedType);
			// get the type variables of the mid class that map to the type
			// arguments of the super class
			final Map<TypeVariable<?>, Type> typeVarAssigns = determineTypeArguments(
				midClass, superType);
			// map the arguments of the mid type to the class type variables
			mapTypeVariablesToArguments(cls, midParameterizedType, typeVarAssigns);

			return typeVarAssigns;
		}

		/**
		 * <p>
		 * Performs a mapping of type variables.
		 * </p>
		 *
		 * @param <T> the generic type of the class in question
		 * @param cls the class in question
		 * @param parameterizedType the parameterized type
		 * @param typeVarAssigns the map to be filled
		 */
		private static <T> void mapTypeVariablesToArguments(final Class<T> cls,
			final ParameterizedType parameterizedType,
			final Map<TypeVariable<?>, Type> typeVarAssigns)
		{
			// capture the type variables from the owner type that have assignments
			final Type ownerType = parameterizedType.getOwnerType();

			if (ownerType instanceof ParameterizedType) {
				// recursion to make sure the owner's owner type gets processed
				mapTypeVariablesToArguments(cls, (ParameterizedType) ownerType,
					typeVarAssigns);
			}

			// parameterizedType is a generic interface/class (or it's in the owner
			// hierarchy of said interface/class) implemented/extended by the class
			// cls. Find out which type variables of cls are type arguments of
			// parameterizedType:
			final Type[] typeArgs = parameterizedType.getActualTypeArguments();

			// of the cls's type variables that are arguments of parameterizedType,
			// find out which ones can be determined from the super type's arguments
			final TypeVariable<?>[] typeVars = getRawType(parameterizedType)
				.getTypeParameters();

			// use List view of type parameters of cls so the contains() method can be
			// used:
			final List<TypeVariable<Class<T>>> typeVarList = Arrays.asList(cls
				.getTypeParameters());

			for (int i = 0; i < typeArgs.length; i++) {
				final TypeVariable<?> typeVar = typeVars[i];
				final Type typeArg = typeArgs[i];

				// argument of parameterizedType is a type variable of cls
				if (typeVarList.contains(typeArg)
				// type variable of parameterizedType has an assignment in
				// the super type.
					&& typeVarAssigns.containsKey(typeVar)) {
					// map the assignment to the cls's type variable
					typeVarAssigns.put((TypeVariable<?>) typeArg, typeVarAssigns.get(
						typeVar));
				}
			}
		}

		/**
		 * <p>
		 * Get the closest parent type to the super class specified by
		 * {@code superClass}.
		 * </p>
		 *
		 * @param cls the class in question
		 * @param superClass the super class
		 * @return the closes parent type
		 */
		private static Type getClosestParentType(final Class<?> cls,
			final Class<?> superClass)
		{
			// only look at the interfaces if the super class is also an interface
			if (superClass.isInterface()) {
				// get the generic interfaces of the subject class
				final Type[] interfaceTypes = cls.getGenericInterfaces();
				// will hold the best generic interface match found
				Type genericInterface = null;

				// find the interface closest to the super class
				for (final Type midType : interfaceTypes) {
					Class<?> midClass = null;

					if (midType instanceof ParameterizedType) {
						midClass = getRawType((ParameterizedType) midType);
					}
					else if (midType instanceof Class) {
						midClass = (Class<?>) midType;
					}
					else {
						throw new IllegalStateException("Unexpected generic" +
							" interface type found: " + midType);
					}

					// check if this interface is further up the inheritance chain
					// than the previously found match
					if (isAssignable(midClass, superClass) && isAssignable(
						genericInterface, (Type) midClass))
					{
						genericInterface = midType;
					}
				}

				// found a match?
				if (genericInterface != null) {
					return genericInterface;
				}
			}

			// none of the interfaces were descendants of the target class, so the
			// super class has to be one, instead
			return cls.getGenericSuperclass();
		}

		/**
		 * <p>
		 * Checks if the given value can be assigned to the target type following
		 * the Java generics rules.
		 * </p>
		 *
		 * @param value the value to be checked
		 * @param type the target type
		 * @return {@code true} if {@code value} is an instance of {@code type}.
		 */
		public static boolean isInstance(final Object value, final Type type) {
			if (type == null) {
				return false;
			}

			return value == null ? !(type instanceof Class) || !((Class<?>) type)
				.isPrimitive() : isAssignable(value.getClass(), type, null);
		}

		/**
		 * <p>
		 * This method strips out the redundant upper bound types in type variable
		 * types and wildcard types (or it would with wildcard types if multiple
		 * upper bounds were allowed).
		 * </p>
		 * <p>
		 * Example, with the variable type declaration:
		 *
		 * <pre>
		 * &lt;K extends java.util.Collection&lt;String&gt; &amp;
		 * java.util.List&lt;String&gt;&gt;
		 * </pre>
		 * <p>
		 * since {@code List} is a subinterface of {@code Collection}, this method
		 * will return the bounds as if the declaration had been:
		 * </p>
		 *
		 * <pre>
		 * &lt;K extends java.util.List&lt;String&gt;&gt;
		 * </pre>
		 *
		 * @param bounds an array of types representing the upper bounds of either
		 *          {@link WildcardType} or {@link TypeVariable}, not {@code null}.
		 * @return an array containing the values from {@code bounds} minus the
		 *         redundant types.
		 */
		public static Type[] normalizeUpperBounds(final Type[] bounds) {
			validateNotNull(bounds, "null value specified for bounds array");
			// don't bother if there's only one (or none) type
			if (bounds.length < 2) {
				return bounds;
			}

			final Set<Type> types = new HashSet<>(bounds.length);

			for (final Type type1 : bounds) {
				boolean subtypeFound = false;

				for (final Type type2 : bounds) {
					if (type1 != type2 && isAssignable(type2, type1, null)) {
						subtypeFound = true;
						break;
					}
				}

				if (!subtypeFound) {
					types.add(type1);
				}
			}

			return types.toArray(new Type[types.size()]);
		}

		/**
		 * <p>
		 * Returns an array containing the sole type of {@link Object} if
		 * {@link TypeVariable#getBounds()} returns an empty array. Otherwise, it
		 * returns the result of {@link TypeVariable#getBounds()} passed into
		 * {@link #normalizeUpperBounds}.
		 * </p>
		 *
		 * @param typeVariable the subject type variable, not {@code null}
		 * @return a non-empty array containing the bounds of the type variable.
		 */
		public static Type[] getImplicitBounds(final TypeVariable<?> typeVariable) {
			validateNotNull(typeVariable, "typeVariable is null");
			final Type[] bounds = typeVariable.getBounds();

			return bounds.length == 0 ? new Type[] { Object.class }
				: normalizeUpperBounds(bounds);
		}

		/**
		 * <p>
		 * Returns an array containing the sole value of {@link Object} if
		 * {@link WildcardType#getUpperBounds()} returns an empty array. Otherwise,
		 * it returns the result of {@link WildcardType#getUpperBounds()} passed
		 * into {@link #normalizeUpperBounds}.
		 * </p>
		 *
		 * @param wildcardType the subject wildcard type, not {@code null}
		 * @return a non-empty array containing the upper bounds of the wildcard
		 *         type.
		 */
		public static Type[] getImplicitUpperBounds(
			final WildcardType wildcardType)
		{
			validateNotNull(wildcardType, "wildcardType is null");
			final Type[] bounds = wildcardType.getUpperBounds();

			return bounds.length == 0 ? new Type[] { Object.class }
				: normalizeUpperBounds(bounds);
		}

		/**
		 * <p>
		 * Returns an array containing a single value of {@code null} if
		 * {@link WildcardType#getLowerBounds()} returns an empty array. Otherwise,
		 * it returns the result of {@link WildcardType#getLowerBounds()}.
		 * </p>
		 *
		 * @param wildcardType the subject wildcard type, not {@code null}
		 * @return a non-empty array containing the lower bounds of the wildcard
		 *         type.
		 */
		public static Type[] getImplicitLowerBounds(
			final WildcardType wildcardType)
		{
			validateNotNull(wildcardType, "wildcardType is null");
			final Type[] bounds = wildcardType.getLowerBounds();

			return bounds.length == 0 ? new Type[] { null } : bounds;
		}

		/**
		 * <p>
		 * Determines whether or not specified types satisfy the bounds of their
		 * mapped type variables. When a type parameter extends another (such as
		 * {@code <T, S extends T>}), uses another as a type parameter (such as
		 * {@code <T, S extends Comparable>>}), or otherwise depends on another type
		 * variable to be specified, the dependencies must be included in
		 * {@code typeVarAssigns}.
		 * </p>
		 *
		 * @param typeVarAssigns specifies the potential types to be assigned to the
		 *          type variables, not {@code null}.
		 * @return whether or not the types can be assigned to their respective type
		 *         variables.
		 */
		public static boolean typesSatisfyVariables(
			final Map<TypeVariable<?>, Type> typeVarAssigns)
		{
			validateNotNull(typeVarAssigns, "typeVarAssigns is null");
			// all types must be assignable to all the bounds of the their mapped
			// type variable.
			for (final Map.Entry<TypeVariable<?>, Type> entry : typeVarAssigns
				.entrySet())
			{
				final TypeVariable<?> typeVar = entry.getKey();
				final Type type = entry.getValue();

				for (final Type bound : getImplicitBounds(typeVar)) {
					if (!isAssignable(type, substituteTypeVariables(bound,
						typeVarAssigns), typeVarAssigns))
					{
						return false;
					}
				}
			}
			return true;
		}

		/**
		 * <p>
		 * Transforms the passed in type to a {@link Class} object. Type-checking
		 * method of convenience.
		 * </p>
		 *
		 * @param parameterizedType the type to be converted
		 * @return the corresponding {@code Class} object
		 * @throws IllegalStateException if the conversion fails
		 */
		private static Class<?> getRawType(
			final ParameterizedType parameterizedType)
		{
			final Type rawType = parameterizedType.getRawType();

			// check if raw type is a Class object
			// not currently necessary, but since the return type is Type instead of
			// Class, there's enough reason to believe that future versions of Java
			// may return other Type implementations. And type-safety checking is
			// rarely a bad idea.
			if (!(rawType instanceof Class)) {
				throw new IllegalStateException("Wait... What!? Type of rawType: " +
					rawType);
			}

			return (Class<?>) rawType;
		}

		/**
		 * <p>
		 * Get the raw type of a Java type, given its context. Primarily for use
		 * with {@link TypeVariable}s and {@link GenericArrayType}s, or when you do
		 * not know the runtime type of {@code type}: if you know you have a
		 * {@link Class} instance, it is already raw; if you know you have a
		 * {@link ParameterizedType}, its raw type is only a method call away.
		 * </p>
		 *
		 * @param type to resolve
		 * @param assigningType type to be resolved against
		 * @return the resolved {@link Class} object or {@code null} if the type
		 *         could not be resolved
		 */
		public static Class<?> getRawType(final Type type,
			final Type assigningType)
		{
			if (type instanceof Class) {
				// it is raw, no problem
				return (Class<?>) type;
			}

			if (type instanceof ParameterizedType) {
				// simple enough to get the raw type of a ParameterizedType
				return getRawType((ParameterizedType) type);
			}

			if (type instanceof TypeVariable) {
				if (assigningType == null) {
					return null;
				}

				// get the entity declaring this type variable
				final Object genericDeclaration = ((TypeVariable<?>) type)
					.getGenericDeclaration();

				// can't get the raw type of a method- or constructor-declared type
				// variable
				if (!(genericDeclaration instanceof Class)) {
					return null;
				}

				// get the type arguments for the declaring class/interface based
				// on the enclosing type
				final Map<TypeVariable<?>, Type> typeVarAssigns = getTypeArguments(
					assigningType, (Class<?>) genericDeclaration);

				// enclosingType has to be a subclass (or subinterface) of the
				// declaring type
				if (typeVarAssigns == null) {
					return null;
				}

				// get the argument assigned to this type variable
				final Type typeArgument = typeVarAssigns.get(type);

				if (typeArgument == null) {
					return null;
				}

				// get the argument for this type variable
				return getRawType(typeArgument, assigningType);
			}

			if (type instanceof GenericArrayType) {
				// get raw component type
				final Class<?> rawComponentType = getRawType(((GenericArrayType) type)
					.getGenericComponentType(), assigningType);

				// return the corresponding array type
				return array(rawComponentType);
			}

			// (hand-waving) this is not the method you're looking for
			if (type instanceof WildcardType) {
				return null;
			}

			throw new IllegalArgumentException("unknown type: " + type);
		}

		/**
		 * Learn whether the specified type denotes an array type.
		 *
		 * @param type the type to be checked
		 * @return {@code true} if {@code type} is an array class or a
		 *         {@link GenericArrayType}.
		 */
		public static boolean isArrayType(final Type type) {
			return type instanceof GenericArrayType || type instanceof Class &&
				((Class<?>) type).isArray();
		}

		/**
		 * Get the array component type of {@code type}.
		 *
		 * @param type the type to be checked
		 * @return component type or null if type is not an array type
		 */
		public static Type getArrayComponentType(final Type type) {
			if (type instanceof Class) {
				final Class<?> clazz = (Class<?>) type;
				return clazz.isArray() ? clazz.getComponentType() : null;
			}
			if (type instanceof GenericArrayType) {
				return ((GenericArrayType) type).getGenericComponentType();
			}
			return null;
		}

		/**
		 * Get a type representing {@code type} with variable assignments
		 * "unrolled."
		 *
		 * @param typeArguments as from
		 *          {@link TypeUtils#getTypeArguments(Type, Class)}
		 * @param type the type to unroll variable assignments for
		 * @return Type
		 * @since 3.2
		 */
		public static Type unrollVariables(Map<TypeVariable<?>, Type> typeArguments,
			final Type type)
		{
			if (typeArguments == null) {
				typeArguments = Collections.<TypeVariable<?>, Type> emptyMap();
			}
			if (containsTypeVariables(type)) {
				if (type instanceof TypeVariable) {
					return unrollVariables(typeArguments, typeArguments.get(type));
				}
				if (type instanceof ParameterizedType) {
					final ParameterizedType p = (ParameterizedType) type;
					final Map<TypeVariable<?>, Type> parameterizedTypeArguments;
					if (p.getOwnerType() == null) {
						parameterizedTypeArguments = typeArguments;
					}
					else {
						parameterizedTypeArguments = new HashMap<>(typeArguments);
						parameterizedTypeArguments.putAll(TypeUtils.getTypeArguments(p));
					}
					final Type[] args = p.getActualTypeArguments();
					for (int i = 0; i < args.length; i++) {
						final Type unrolled = unrollVariables(parameterizedTypeArguments,
							args[i]);
						if (unrolled != null) {
							args[i] = unrolled;
						}
					}
					return parameterizeWithOwner(p.getOwnerType(), (Class<?>) p
						.getRawType(), args);
				}
				if (type instanceof WildcardType) {
					final WildcardType wild = (WildcardType) type;
					return wildcardType().withUpperBounds(unrollBounds(typeArguments, wild
						.getUpperBounds())).withLowerBounds(unrollBounds(typeArguments, wild
							.getLowerBounds())).build();
				}
			}
			return type;
		}

		/**
		 * Local helper method to unroll variables in a type bounds array.
		 *
		 * @param typeArguments assignments {@link Map}
		 * @param bounds in which to expand variables
		 * @return {@code bounds} with any variables reassigned
		 * @since 3.2
		 */
		private static Type[] unrollBounds(
			final Map<TypeVariable<?>, Type> typeArguments, final Type[] bounds)
		{
			final ArrayList<Type> result = new ArrayList<>();
			for (final Type bound : bounds) {
				final Type unrolled = unrollVariables(typeArguments, bound);
				if (unrolled != null) result.add(unrolled);
			}
			return result.toArray(new Type[result.size()]);
		}

		/**
		 * Learn, recursively, whether any of the type parameters associated with
		 * {@code type} are bound to variables.
		 *
		 * @param type the type to check for type variables
		 * @return boolean
		 * @since 3.2
		 */
		public static boolean containsTypeVariables(final Type type) {
			if (type instanceof TypeVariable) {
				return true;
			}
			if (type instanceof Class) {
				return ((Class<?>) type).getTypeParameters().length > 0;
			}
			if (type instanceof ParameterizedType) {
				for (final Type arg : ((ParameterizedType) type)
					.getActualTypeArguments())
				{
					if (containsTypeVariables(arg)) {
						return true;
					}
				}
				return false;
			}
			if (type instanceof WildcardType) {
				final WildcardType wild = (WildcardType) type;
				return containsTypeVariables(TypeUtils.getImplicitLowerBounds(
					wild)[0]) || containsTypeVariables(TypeUtils.getImplicitUpperBounds(
						wild)[0]);
			}
			return false;
		}

		/**
		 * Create a parameterized type instance.
		 *
		 * @param raw the raw class to create a parameterized type instance for
		 * @param typeArguments the types used for parameterization
		 * @return {@link ParameterizedType}
		 * @since 3.2
		 */
		public static final ParameterizedType parameterize(final Class<?> raw,
			final Type... typeArguments)
		{
			return parameterizeWithOwner(null, raw, typeArguments);
		}

		/**
		 * Create a parameterized type instance.
		 *
		 * @param raw the raw class to create a parameterized type instance for
		 * @param typeArgMappings the mapping used for parameterization
		 * @return {@link ParameterizedType}
		 * @since 3.2
		 */
		public static final ParameterizedType parameterize(final Class<?> raw,
			final Map<TypeVariable<?>, Type> typeArgMappings)
		{
			validateNotNull(raw, "raw class is null");
			validateNotNull(typeArgMappings, "typeArgMappings is null");
			return parameterizeWithOwner(null, raw, extractTypeArgumentsFrom(
				typeArgMappings, raw.getTypeParameters()));
		}

		/**
		 * Create a parameterized type instance.
		 *
		 * @param owner the owning type
		 * @param raw the raw class to create a parameterized type instance for
		 * @param typeArguments the types used for parameterization
		 * @return {@link ParameterizedType}
		 * @since 3.2
		 */
		public static final ParameterizedType parameterizeWithOwner(
			final Type owner, final Class<?> raw, final Type... typeArguments)
		{
			validateNotNull(raw, "raw class is null");
			final Type useOwner;
			if (raw.getEnclosingClass() == null) {
				validateIsTrue(owner == null, "no owner allowed for top-level %s", raw);
				useOwner = null;
			}
			else if (owner == null) {
				useOwner = raw.getEnclosingClass();
			}
			else {
				validateIsTrue(TypeUtils.isAssignable(owner, raw.getEnclosingClass()),
					"%s is invalid owner type for parameterized %s", owner, raw);
				useOwner = owner;
			}
			validateNoNullElements(typeArguments, "null type argument at index %s");
			validateIsTrue(raw.getTypeParameters().length == typeArguments.length,
				"invalid number of type parameters specified: expected %s, got %s", raw
					.getTypeParameters().length, typeArguments.length);

			return new ParameterizedTypeImpl(raw, useOwner, typeArguments);
		}

		/**
		 * Create a parameterized type instance.
		 *
		 * @param owner the owning type
		 * @param raw the raw class to create a parameterized type instance for
		 * @param typeArgMappings the mapping used for parameterization
		 * @return {@link ParameterizedType}
		 * @since 3.2
		 */
		public static final ParameterizedType parameterizeWithOwner(
			final Type owner, final Class<?> raw,
			final Map<TypeVariable<?>, Type> typeArgMappings)
		{
			validateNotNull(raw, "raw class is null");
			validateNotNull(typeArgMappings, "typeArgMappings is null");
			return parameterizeWithOwner(owner, raw, extractTypeArgumentsFrom(
				typeArgMappings, raw.getTypeParameters()));
		}

		/**
		 * Helper method to establish the formal parameters for a parameterized
		 * type.
		 *
		 * @param mappings map containing the assignements
		 * @param variables expected map keys
		 * @return array of map values corresponding to specified keys
		 */
		private static Type[] extractTypeArgumentsFrom(
			final Map<TypeVariable<?>, Type> mappings,
			final TypeVariable<?>[] variables)
		{
			final Type[] result = new Type[variables.length];
			int index = 0;
			for (final TypeVariable<?> var : variables) {
				validateIsTrue(mappings.containsKey(var),
					"missing argument mapping for %s", toString(var));
				result[index++] = mappings.get(var);
			}
			return result;
		}

		/**
		 * Get a {@link WildcardTypeBuilder}.
		 *
		 * @return {@link WildcardTypeBuilder}
		 * @since 3.2
		 */
		public static WildcardTypeBuilder wildcardType() {
			return new WildcardTypeBuilder();
		}

		/**
		 * Create a generic array type instance.
		 *
		 * @param componentType the type of the elements of the array. For example
		 *          the component type of {@code boolean[]} is {@code boolean}
		 * @return {@link GenericArrayType}
		 * @since 3.2
		 */
		public static GenericArrayType genericArrayType(final Type componentType) {
			return new GenericArrayTypeImpl(validateNotNull(componentType,
				"componentType is null"));
		}

		/**
		 * Check equality of types.
		 *
		 * @param t1 the first type
		 * @param t2 the second type
		 * @return boolean
		 * @since 3.2
		 */
		public static boolean equals(final Type t1, final Type t2) {
			if (Objects.equals(t1, t2)) {
				return true;
			}
			if (t1 instanceof ParameterizedType) {
				return equals((ParameterizedType) t1, t2);
			}
			if (t1 instanceof GenericArrayType) {
				return equals((GenericArrayType) t1, t2);
			}
			if (t1 instanceof WildcardType) {
				return equals((WildcardType) t1, t2);
			}
			return false;
		}

		/**
		 * Learn whether {@code t} equals {@code p}.
		 *
		 * @param p LHS
		 * @param t RHS
		 * @return boolean
		 * @since 3.2
		 */
		private static boolean equals(final ParameterizedType p, final Type t) {
			if (t instanceof ParameterizedType) {
				final ParameterizedType other = (ParameterizedType) t;
				if (equals(p.getRawType(), other.getRawType()) && equals(p
					.getOwnerType(), other.getOwnerType()))
				{
					return equals(p.getActualTypeArguments(), other
						.getActualTypeArguments());
				}
			}
			return false;
		}

		/**
		 * Learn whether {@code t} equals {@code a}.
		 *
		 * @param a LHS
		 * @param t RHS
		 * @return boolean
		 * @since 3.2
		 */
		private static boolean equals(final GenericArrayType a, final Type t) {
			return t instanceof GenericArrayType && equals(a
				.getGenericComponentType(), ((GenericArrayType) t)
					.getGenericComponentType());
		}

		/**
		 * Learn whether {@code t} equals {@code w}.
		 *
		 * @param w LHS
		 * @param t RHS
		 * @return boolean
		 * @since 3.2
		 */
		private static boolean equals(final WildcardType w, final Type t) {
			if (t instanceof WildcardType) {
				final WildcardType other = (WildcardType) t;
				return equals(getImplicitLowerBounds(w), getImplicitLowerBounds(
					other)) && equals(getImplicitUpperBounds(w), getImplicitUpperBounds(
						other));
			}
			return true;
		}

		/**
		 * Learn whether {@code t1} equals {@code t2}.
		 *
		 * @param t1 LHS
		 * @param t2 RHS
		 * @return boolean
		 * @since 3.2
		 */
		private static boolean equals(final Type[] t1, final Type[] t2) {
			if (t1.length == t2.length) {
				for (int i = 0; i < t1.length; i++) {
					if (!equals(t1[i], t2[i])) {
						return false;
					}
				}
				return true;
			}
			return false;
		}

		/**
		 * Present a given type as a Java-esque String.
		 *
		 * @param type the type to create a String representation for, not
		 *          {@code null}
		 * @return String
		 * @since 3.2
		 */
		public static String toString(final Type type) {
			return toString(type, new HashSet<>());
		}

		private static String toString(final Type type, final Set<Type> done) {
			validateNotNull(type);
			if (type instanceof Class) {
				return classToString((Class<?>) type, done);
			}
			if (type instanceof ParameterizedType) {
				return parameterizedTypeToString((ParameterizedType) type, done);
			}
			if (type instanceof WildcardType) {
				return wildcardTypeToString((WildcardType) type, done);
			}
			if (type instanceof TypeVariable) {
				return typeVariableToString((TypeVariable<?>) type, done);
			}
			if (type instanceof GenericArrayType) {
				return genericArrayTypeToString((GenericArrayType) type);
			}
			throw new IllegalArgumentException("Unknown generic type: " + //
				type.getClass().getName());
		}

		/**
		 * Format a {@link TypeVariable} including its {@link GenericDeclaration}.
		 *
		 * @param var the type variable to create a String representation for, not
		 *          {@code null}
		 * @return String
		 * @since 3.2
		 */
		public static String toLongString(final TypeVariable<?> var) {
			validateNotNull(var, "var is null");
			final StringBuilder buf = new StringBuilder();
			final GenericDeclaration d = ((TypeVariable<?>) var)
				.getGenericDeclaration();
			if (d instanceof Class) {
				Class<?> c = (Class<?>) d;
				while (true) {
					if (c.getEnclosingClass() == null) {
						buf.insert(0, c.getName());
						break;
					}
					buf.insert(0, c.getSimpleName()).insert(0, '.');
					c = c.getEnclosingClass();
				}
			}
			else if (d instanceof Type) {// not possible as of now
				buf.append(toString((Type) d));
			}
			else {
				buf.append(d);
			}
			return buf.append(':').append(typeVariableToString(var, new HashSet<>()))
				.toString();
		}

//		/**
//		 * Wrap the specified {@link Type} in a {@link Typed} wrapper.
//		 *
//		 * @param <T> inferred generic type
//		 * @param type to wrap
//		 * @return Typed&lt;T&gt;
//		 * @since 3.2
//		 */
//		public static <T> Typed<T> wrap(final Type type) {
//			return new Typed<T>() {
//
//				@Override
//				public Type getType() {
//					return type;
//				}
//			};
//		}
//
//		/**
//		 * Wrap the specified {@link Class} in a {@link Typed} wrapper.
//		 *
//		 * @param <T> generic type
//		 * @param type to wrap
//		 * @return Typed&lt;T&gt;
//		 * @since 3.2
//		 */
//		public static <T> Typed<T> wrap(final Class<T> type) {
//			return TypeUtils.<T> wrap((Type) type);
//		}

		/**
		 * Format a {@link Class} as a {@link String}.
		 *
		 * @param c {@code Class} to format
		 * @param done list of already-encountered types
		 * @return String
		 * @since 3.2
		 */
		private static String classToString(final Class<?> c,
			final Set<Type> done)
		{
			final StringBuilder buf = new StringBuilder();

			if (c.getEnclosingClass() != null) {
				buf.append(classToString(c.getEnclosingClass(), done)).append('.')
					.append(c.getSimpleName());
			}
			else {
				buf.append(c.getName());
			}
			if (c.getTypeParameters().length > 0) {
				buf.append('<');
				appendAllTo(buf, ", ", done, c.getTypeParameters());
				buf.append('>');
			}
			return buf.toString();
		}

		/**
		 * Format a {@link TypeVariable} as a {@link String}.
		 *
		 * @param v {@code TypeVariable} to format
		 * @param done list of already-encountered types
		 * @return String
		 * @since 3.2
		 */
		private static String typeVariableToString(final TypeVariable<?> v,
			final Set<Type> done)
		{
			final StringBuilder buf = new StringBuilder(v.getName());
			if (done.contains(v)) return buf.toString();
			done.add(v);
			final Type[] bounds = v.getBounds();
			if (bounds.length > 0 && !(bounds.length == 1 && Object.class.equals(
				bounds[0])))
			{
				buf.append(" extends ");
				appendAllTo(buf, " & ", done, v.getBounds());
			}
			return buf.toString();
		}

		/**
		 * Format a {@link ParameterizedType} as a {@link String}.
		 *
		 * @param p {@code ParameterizedType} to format
		 * @param done list of already-encountered types
		 * @return String
		 * @since 3.2
		 */
		private static String parameterizedTypeToString(final ParameterizedType p,
			final Set<Type> done)
		{
			final StringBuilder buf = new StringBuilder();

			final Type useOwner = p.getOwnerType();
			final Class<?> raw = (Class<?>) p.getRawType();
			final Type[] typeArguments = p.getActualTypeArguments();
			if (useOwner == null) {
				buf.append(raw.getName());
			}
			else {
				if (useOwner instanceof Class) {
					buf.append(((Class<?>) useOwner).getName());
				}
				else {
					buf.append(useOwner.toString());
				}
				buf.append('.').append(raw.getSimpleName());
			}

			appendAllTo(buf.append('<'), ", ", done, typeArguments).append('>');
			return buf.toString();
		}

		/**
		 * Format a {@link WildcardType} as a {@link String}.
		 *
		 * @param w {@code WildcardType} to format
		 * @param done list of already-encountered types
		 * @return String
		 * @since 3.2
		 */
		private static String wildcardTypeToString(final WildcardType w,
			final Set<Type> done)
		{
			final StringBuilder buf = new StringBuilder().append('?');
			if (done.contains(w)) return buf.toString();
			done.add(w);
			final Type[] lowerBounds = w.getLowerBounds();
			final Type[] upperBounds = w.getUpperBounds();
			if (lowerBounds.length > 1 || lowerBounds.length == 1 &&
				lowerBounds[0] != null)
			{
				appendAllTo(buf.append(" super "), " & ", done, lowerBounds);
			}
			else if (upperBounds.length > 1 || upperBounds.length == 1 &&
				!Object.class.equals(upperBounds[0]))
			{
				appendAllTo(buf.append(" extends "), " & ", done, upperBounds);
			}
			return buf.toString();
		}

		/**
		 * Format a {@link GenericArrayType} as a {@link String}.
		 *
		 * @param g {@code GenericArrayType} to format
		 * @return String
		 * @since 3.2
		 */
		private static String genericArrayTypeToString(final GenericArrayType g) {
			return String.format("%s[]", toString(g.getGenericComponentType()));
		}

		/**
		 * Append {@code types} to {@code buf} with separator {@code sep}.
		 *
		 * @param buf destination
		 * @param sep separator
		 * @param done list of already-encountered types
		 * @param types to append
		 * @return {@code buf}
		 * @since 3.2
		 */
		private static StringBuilder appendAllTo(final StringBuilder buf,
			final String sep, final Set<Type> done, final Type... types)
		{
			validateNotEmpty(validateNoNullElements(types));
			if (types.length > 0) {
				buf.append(toString(types[0], done));
				for (int i = 1; i < types.length; i++) {
					buf.append(sep).append(toString(types[i], done));
				}
			}
			return buf;
		}

		private static final String DEFAULT_IS_NULL_EX_MESSAGE =
			"The validated object is null";

		/** Forked from {@code org.apache.commons.lang3.Validate#notNull}. */
		private static <T> T validateNotNull(final T object) {
			return validateNotNull(object, DEFAULT_IS_NULL_EX_MESSAGE);
		}

		/** Forked from {@code org.apache.commons.lang3.Validate#notNull}. */
		private static <T> T validateNotNull(final T object, final String message,
			final Object... values)
		{
			if (object == null) {
				throw new NullPointerException(String.format(message, values));
			}
			return object;
		}

		/** Forked from {@code org.apache.commons.lang3.Validate#isTrue}. */
		private static void validateIsTrue(final boolean expression,
			final String message, final Object... values)
		{
			if (expression == false) {
				throw new IllegalArgumentException(String.format(message, values));
			}
		}

		private static final String DEFAULT_NO_NULL_ELEMENTS_ARRAY_EX_MESSAGE =
			"The validated array contains null element at index: %d";

		/** Forked from {@code org.apache.commons.lang3.Validate#noNullElements}. */
		private static <T> T[] validateNoNullElements(final T[] array) {
			return validateNoNullElements(array,
				DEFAULT_NO_NULL_ELEMENTS_ARRAY_EX_MESSAGE);
		}

		/** Forked from {@code org.apache.commons.lang3.Validate#noNullElements}. */
		private static <T> T[] validateNoNullElements(final T[] array,
			final String message, final Object... values)
		{
			validateNotNull(array);
			for (int i = 0; i < array.length; i++) {
				if (array[i] == null) {
					final Object[] values2 = new Object[values.length + 1];
					System.arraycopy(values, 0, values2, 0, values.length);
					values2[values.length] = Integer.valueOf(i);
					throw new IllegalArgumentException(String.format(message, values2));
				}
			}
			return array;
		}

		private static final String DEFAULT_NOT_EMPTY_ARRAY_EX_MESSAGE =
			"The validated array is empty";

		/** Forked from {@code org.apache.commons.lang3.Validate#notEmpty}. */
		private static <T> T[] validateNotEmpty(final T[] array) {
			return validateNotEmpty(array, DEFAULT_NOT_EMPTY_ARRAY_EX_MESSAGE);
		}

		/** Forked from {@code org.apache.commons.lang3.Validate#notEmpty}. */
		private static <T> T[] validateNotEmpty(final T[] array,
			final String message, final Object... values)
		{
			if (array == null) {
				throw new NullPointerException(String.format(message, values));
			}
			if (array.length == 0) {
				throw new IllegalArgumentException(String.format(message, values));
			}
			return array;
		}
	}

	// -- END FORK OF APACHE COMMONS LANG 3.4 CODE --

	// -- BEGIN FORK OF GENTYREF 1.1.0 CODE --

	/**
	 * Utility class for doing reflection on types.
	 *
	 * @author Wouter Coekaerts <wouter@coekaerts.be>
	 */
	private static class GenericTypeReflector {

		private static final Type UNBOUND_WILDCARD = new TypeUtils.WildcardTypeImpl(
			new Type[] { Object.class }, new Type[] {});

		/**
		 * Returns the erasure of the given type.
		 */
		public static Class<?> erase(final Type type) {
			if (type instanceof Class) {
				return (Class<?>) type;
			}
			else if (type instanceof ParameterizedType) {
				return (Class<?>) ((ParameterizedType) type).getRawType();
			}
			else if (type instanceof TypeVariable) {
				final TypeVariable<?> tv = (TypeVariable<?>) type;
				if (tv.getBounds().length == 0) return Object.class;
				return erase(tv.getBounds()[0]);
			}
			else if (type instanceof GenericArrayType) {
				final GenericArrayType aType = (GenericArrayType) type;
				return array(erase(aType.getGenericComponentType()));
			}
			else {
				// TODO at least support CaptureType here
				throw new RuntimeException("not supported: " + type.getClass());
			}
		}

		/**
		 * Maps type parameters in a type to their values.
		 * 
		 * @param toMapType Type possibly containing type arguments
		 * @param typeAndParams must be either ParameterizedType, or (in case there
		 *          are no type arguments, or it's a raw type) Class
		 * @return toMapType, but with type parameters from typeAndParams replaced.
		 */
		private static Type mapTypeParameters(final Type toMapType,
			final Type typeAndParams)
		{
			if (isMissingTypeParameters(typeAndParams)) {
				return erase(toMapType);
			}
			final VarMap varMap = new VarMap();
			Type handlingTypeAndParams = typeAndParams;
			while (handlingTypeAndParams instanceof ParameterizedType) {
				final ParameterizedType pType =
					(ParameterizedType) handlingTypeAndParams;
				// getRawType should always be Class
				final Class<?> clazz = (Class<?>) pType.getRawType();
				varMap.addAll(clazz.getTypeParameters(), pType
					.getActualTypeArguments());
				handlingTypeAndParams = pType.getOwnerType();
			}
			return varMap.map(toMapType);
		}

		/**
		 * Checks if the given type is a class that is supposed to have type
		 * parameters, but doesn't. In other words, if it's a really raw type.
		 */
		private static boolean isMissingTypeParameters(final Type type) {
			if (type instanceof Class) {
				for (Class<?> clazz = (Class<?>) type; clazz != null; clazz = clazz
					.getEnclosingClass())
				{
					if (clazz.getTypeParameters().length != 0) return true;
				}
				return false;
			}
			else if (type instanceof ParameterizedType) {
				return false;
			}
			else {
				throw new AssertionError("Unexpected type " + type.getClass());
			}
		}

		/**
		 * Returns a type representing the class, with all type parameters the
		 * unbound wildcard ("?"). For example,
		 * <tt>addWildcardParameters(Map.class)</tt> returns a type representing
		 * <tt>Map&lt;?,?&gt;</tt>.
		 * 
		 * @return
		 *         <ul>
		 *         <li>If clazz is a class or interface without type parameters,
		 *         clazz itself is returned.</li>
		 *         <li>If clazz is a class or interface with type parameters, an
		 *         instance of ParameterizedType is returned.</li>
		 *         <li>if clazz is an array type, an array type is returned with
		 *         unbound wildcard parameters added in the the component type.
		 *         </ul>
		 */
		public static Type addWildcardParameters(final Class<?> clazz) {
			if (clazz.isArray()) {
				return array(addWildcardParameters(clazz.getComponentType()));
			}
			else if (isMissingTypeParameters(clazz)) {
				final TypeVariable<?>[] vars = clazz.getTypeParameters();
				final Type[] arguments = new Type[vars.length];
				Arrays.fill(arguments, UNBOUND_WILDCARD);
				final Type owner = clazz.getDeclaringClass() == null ? null
					: addWildcardParameters(clazz.getDeclaringClass());
				return parameterize(clazz, owner, arguments);
			}
			else {
				return clazz;
			}
		}

		/**
		 * Finds the most specific supertype of <tt>type</tt> whose erasure is
		 * <tt>searchClass</tt>. In other words, returns a type representing the
		 * class <tt>searchClass</tt> plus its exact type parameters in
		 * <tt>type</tt>.
		 * <ul>
		 * <li>Returns an instance of {@link ParameterizedType} if
		 * <tt>searchClass</tt> is a real class or interface and <tt>type</tt> has
		 * parameters for it</li>
		 * <li>Returns an instance of {@link GenericArrayType} if
		 * <tt>searchClass</tt> is an array type, and <tt>type</tt> has type
		 * parameters for it</li>
		 * <li>Returns an instance of {@link Class} if <tt>type</tt> is a raw type,
		 * or has no type parameters for <tt>searchClass</tt></li>
		 * <li>Returns null if <tt>searchClass</tt> is not a superclass of type.
		 * </li>
		 * </ul>
		 * <p>
		 * For example, with
		 * <tt>class StringList implements List&lt;String&gt;</tt>,
		 * <tt>getExactSuperType(StringList.class, Collection.class)</tt> returns a
		 * {@link ParameterizedType} representing <tt>Collection&lt;String&gt;</tt>.
		 * </p>
		 */
		public static Type getExactSuperType(final Type type,
			final Class<?> searchClass)
		{
			if (type instanceof ParameterizedType || type instanceof Class ||
				type instanceof GenericArrayType)
			{
				final Class<?> clazz = erase(type);

				if (searchClass == clazz) {
					return type;
				}

				if (!searchClass.isAssignableFrom(clazz)) return null;
			}

			for (final Type superType : getExactDirectSuperTypes(type)) {
				final Type result = getExactSuperType(superType, searchClass);
				if (result != null) return result;
			}

			return null;
		}

		/**
		 * Gets the type parameter for a given type that is the value for a given
		 * type variable. For example, with
		 * <tt>class StringList implements List&lt;String&gt;</tt>,
		 * <tt>getTypeParameter(StringList.class, Collection.class.getTypeParameters()[0])</tt>
		 * returns <tt>String</tt>.
		 * 
		 * @param type The type to inspect.
		 * @param variable The type variable to find the value for.
		 * @return The type parameter for the given variable. Or null if type is not
		 *         a subtype of the type that declares the variable, or if the
		 *         variable isn't known (because of raw types).
		 */
		public static Type getTypeParameter(final Type type,
			final TypeVariable<? extends Class<?>> variable)
		{
			final Class<?> clazz = variable.getGenericDeclaration();
			final Type superType = getExactSuperType(type, clazz);
			if (superType instanceof ParameterizedType) {
				final int index = Arrays.asList(clazz.getTypeParameters()).indexOf(
					variable);
				return ((ParameterizedType) superType).getActualTypeArguments()[index];
			}
			return null;
		}

		/**
		 * Checks if the capture of subType is a subtype of superType
		 */
		public static boolean isSuperType(final Type superType,
			final Type subType)
		{
			if (superType instanceof ParameterizedType ||
				superType instanceof Class || superType instanceof GenericArrayType)
			{
				final Class<?> superClass = erase(superType);
				final Type mappedSubType = getExactSuperType(capture(subType),
					superClass);
				if (mappedSubType == null) {
					return false;
				}
				else if (superType instanceof Class<?>) {
					return true;
				}
				else if (mappedSubType instanceof Class<?>) {
					// TODO treat supertype by being raw type differently ("supertype, but
					// with warnings")
					return true; // class has no parameters, or it's a raw type
				}
				else if (mappedSubType instanceof GenericArrayType) {
					final Type superComponentType = getArrayComponentType(superType);
					assert superComponentType != null;
					final Type mappedSubComponentType = getArrayComponentType(
						mappedSubType);
					assert mappedSubComponentType != null;
					return isSuperType(superComponentType, mappedSubComponentType);
				}
				else {
					assert mappedSubType instanceof ParameterizedType;
					final ParameterizedType pMappedSubType =
						(ParameterizedType) mappedSubType;
					assert pMappedSubType.getRawType() == superClass;
					final ParameterizedType pSuperType = (ParameterizedType) superType;

					final Type[] superTypeArgs = pSuperType.getActualTypeArguments();
					final Type[] subTypeArgs = pMappedSubType.getActualTypeArguments();
					assert superTypeArgs.length == subTypeArgs.length;
					for (int i = 0; i < superTypeArgs.length; i++) {
						if (!contains(superTypeArgs[i], subTypeArgs[i])) {
							return false;
						}
					}
					// params of the class itself match, so if the owner types are
					// supertypes too, it's a supertype.
					return pSuperType.getOwnerType() == null || isSuperType(pSuperType
						.getOwnerType(), pMappedSubType.getOwnerType());
				}
			}
			else if (superType instanceof CaptureType) {
				if (superType.equals(subType)) return true;
				for (final Type lowerBound : ((CaptureType) superType)
					.getLowerBounds())
				{
					if (isSuperType(lowerBound, subType)) {
						return true;
					}
				}
				return false;
			}
			else if (superType instanceof GenericArrayType) {
				return isArraySupertype(superType, subType);
			}
			else {
				throw new RuntimeException("not implemented: " + superType.getClass());
			}
		}

		private static boolean isArraySupertype(final Type arraySuperType,
			final Type subType)
		{
			final Type superTypeComponent = getArrayComponentType(arraySuperType);
			assert superTypeComponent != null;
			final Type subTypeComponent = getArrayComponentType(subType);
			if (subTypeComponent == null) { // subType is not an array type
				return false;
			}
			return isSuperType(superTypeComponent, subTypeComponent);
		}

		/**
		 * If type is an array type, returns the type of the component of the array.
		 * Otherwise, returns null.
		 */
		public static Type getArrayComponentType(final Type type) {
			if (type instanceof Class) {
				final Class<?> clazz = (Class<?>) type;
				return clazz.getComponentType();
			}
			else if (type instanceof GenericArrayType) {
				final GenericArrayType aType = (GenericArrayType) type;
				return aType.getGenericComponentType();
			}
			else {
				return null;
			}
		}

		private static boolean contains(final Type containingType,
			final Type containedType)
		{
			if (containingType instanceof WildcardType) {
				final WildcardType wContainingType = (WildcardType) containingType;
				for (final Type upperBound : wContainingType.getUpperBounds()) {
					if (!isSuperType(upperBound, containedType)) {
						return false;
					}
				}
				for (final Type lowerBound : wContainingType.getLowerBounds()) {
					if (!isSuperType(containedType, lowerBound)) {
						return false;
					}
				}
				return true;
			}
			return containingType.equals(containedType);
		}

		/**
		 * Returns the direct supertypes of the given type. Resolves type
		 * parameters.
		 */
		private static Type[] getExactDirectSuperTypes(final Type type) {
			if (type instanceof ParameterizedType || type instanceof Class) {
				Class<?> clazz;
				if (type instanceof ParameterizedType) {
					clazz = (Class<?>) ((ParameterizedType) type).getRawType();
				}
				else {
					// TODO primitive types?
					clazz = (Class<?>) type;
					if (clazz.isArray()) return getArrayExactDirectSuperTypes(clazz);
				}

				final Type[] superInterfaces = clazz.getGenericInterfaces();
				final Type superClass = clazz.getGenericSuperclass();
				Type[] result;
				int resultIndex;
				if (superClass == null) {
					result = new Type[superInterfaces.length];
					resultIndex = 0;
				}
				else {
					result = new Type[superInterfaces.length + 1];
					resultIndex = 1;
					result[0] = mapTypeParameters(superClass, type);
				}
				for (final Type superInterface : superInterfaces) {
					result[resultIndex++] = mapTypeParameters(superInterface, type);
				}

				return result;
			}
			else if (type instanceof TypeVariable) {
				final TypeVariable<?> tv = (TypeVariable<?>) type;
				return tv.getBounds();
			}
			else if (type instanceof WildcardType) {
				// This should be a rare case: normally this wildcard is already
				// captured.
				// But it does happen if the upper bound of a type variable contains a
				// wildcard
				// TODO shouldn't upper bound of type variable have been captured too?
				// (making this case impossible?)
				return ((WildcardType) type).getUpperBounds();
			}
			else if (type instanceof CaptureType) {
				return ((CaptureType) type).getUpperBounds();
			}
			else if (type instanceof GenericArrayType) {
				return getArrayExactDirectSuperTypes(type);
			}
			else {
				throw new RuntimeException("not implemented type: " + type);
			}
		}

		private static Type[] getArrayExactDirectSuperTypes(final Type arrayType) {
			// see
			// http://java.sun.com/docs/books/jls/third_edition/html/typesValues.html#4.10.3
			final Type typeComponent = getArrayComponentType(arrayType);

			Type[] result;
			int resultIndex;
			if (typeComponent instanceof Class && ((Class<?>) typeComponent)
				.isPrimitive())
			{
				resultIndex = 0;
				result = new Type[3];
			}
			else {
				final Type[] componentSupertypes = getExactDirectSuperTypes(
					typeComponent);
				result = new Type[componentSupertypes.length + 3];
				for (resultIndex =
					0; resultIndex < componentSupertypes.length; resultIndex++)
				{
					result[resultIndex] = array(componentSupertypes[resultIndex]);
				}
			}
			result[resultIndex++] = Object.class;
			result[resultIndex++] = Cloneable.class;
			result[resultIndex++] = Serializable.class;
			return result;
		}

		/**
		 * Returns the exact return type of the given method in the given type. This
		 * may be different from <tt>m.getGenericReturnType()</tt> when the method
		 * was declared in a superclass, or <tt>type</tt> has a type parameter that
		 * is used in the return type, or <tt>type</tt> is a raw type.
		 */
		public static Type getExactReturnType(final Method m, final Type type) {
			final Type returnType = m.getGenericReturnType();
			final Type exactDeclaringType = getExactSuperType(capture(type), m
				.getDeclaringClass());
			if (exactDeclaringType == null) {
				// capture(type) is not a subtype of m.getDeclaringClass()
				throw new IllegalArgumentException("The method " + m +
					" is not a member of type " + type);
			}
			return mapTypeParameters(returnType, exactDeclaringType);
		}

		/**
		 * Returns the exact type of the given field in the given type. This may be
		 * different from <tt>f.getGenericType()</tt> when the field was declared in
		 * a superclass, or <tt>type</tt> has a type parameter that is used in the
		 * type of the field, or <tt>type</tt> is a raw type.
		 */
		public static Type getExactFieldType(final Field f, final Type type) {
			final Type returnType = f.getGenericType();
			final Type exactDeclaringType = getExactSuperType(capture(type), f
				.getDeclaringClass());
			if (exactDeclaringType == null) {
				// capture(type) is not a subtype of f.getDeclaringClass()
				throw new IllegalArgumentException("The field " + f +
					" is not a member of type " + type);
			}
			return mapTypeParameters(returnType, exactDeclaringType);
		}

		/**
		 * Returns the exact parameter types of the given method in the given type.
		 * This may be different from <tt>m.getGenericParameterTypes()</tt> when the
		 * method was declared in a superclass, or <tt>type</tt> has a type
		 * parameter that is used in one of the parameters, or <tt>type</tt> is a
		 * raw type.
		 */
		public static Type[] getExactParameterTypes(final Method m,
			final Type type)
		{
			final Type[] parameterTypes = m.getGenericParameterTypes();
			final Type exactDeclaringType = getExactSuperType(capture(type), m
				.getDeclaringClass());
			if (exactDeclaringType == null) {
				// capture(type) is not a subtype of m.getDeclaringClass()
				throw new IllegalArgumentException("The method " + m +
					" is not a member of type " + type);
			}

			final Type[] result = new Type[parameterTypes.length];
			for (int i = 0; i < parameterTypes.length; i++) {
				result[i] = mapTypeParameters(parameterTypes[i], exactDeclaringType);
			}
			return result;
		}

		/**
		 * Applies capture conversion to the given type.
		 */
		public static Type capture(final Type type) {
			final VarMap varMap = new VarMap();
			final List<CaptureTypeImpl> toInit = new ArrayList<>();
			if (type instanceof ParameterizedType) {
				final ParameterizedType pType = (ParameterizedType) type;
				final Class<?> clazz = (Class<?>) pType.getRawType();
				final Type[] arguments = pType.getActualTypeArguments();
				final TypeVariable<?>[] vars = clazz.getTypeParameters();
				final Type[] capturedArguments = new Type[arguments.length];
				assert arguments.length == vars.length;
				for (int i = 0; i < arguments.length; i++) {
					Type argument = arguments[i];
					if (argument instanceof WildcardType) {
						final CaptureTypeImpl captured = new CaptureTypeImpl(
							(WildcardType) argument, vars[i]);
						argument = captured;
						toInit.add(captured);
					}
					capturedArguments[i] = argument;
					varMap.add(vars[i], argument);
				}
				for (final CaptureTypeImpl captured : toInit) {
					captured.init(varMap);
				}
				final Type ownerType = (pType.getOwnerType() == null) ? null : capture(
					pType.getOwnerType());
				return parameterize(clazz, ownerType, capturedArguments);
			}
			return type;
		}

		/**
		 * Returns list of classes and interfaces that are supertypes of the given
		 * type. For example given this class:
		 * <tt>class Foo&lt;A extends Number & Iterable&lt;A&gt;, B extends A&gt;</tt>
		 * <br>
		 * calling this method on type parameters <tt>B</tt>
		 * (<tt>Foo.class.getTypeParameters()[1]</tt>) returns a list containing
		 * <tt>Number</tt> and <tt>Iterable</tt>.
		 * <p>
		 * This is mostly useful if you get a type from one of the other methods in
		 * <tt>GenericTypeReflector</tt>, but you don't want to deal with all the
		 * different sorts of types, and you are only really interested in concrete
		 * classes and interfaces.
		 * </p>
		 * 
		 * @return A List of classes, each of them a supertype of the given type. If
		 *         the given type is a class or interface itself, returns a List
		 *         with just the given type. The list contains no duplicates, and is
		 *         ordered in the order the upper bounds are defined on the type.
		 */
		public static List<Class<?>> getUpperBoundClassAndInterfaces(
			final Type type)
		{
			final LinkedHashSet<Class<?>> result = new LinkedHashSet<>();
			buildUpperBoundClassAndInterfaces(type, result);
			return new ArrayList<>(result);
		}

		/**
		 * Helper method for getUpperBoundClassAndInterfaces, adding the result to
		 * the given set.
		 */
		private static void buildUpperBoundClassAndInterfaces(final Type type,
			final Set<Class<?>> result)
		{
			if (type instanceof ParameterizedType || type instanceof Class<?>) {
				result.add(erase(type));
				return;
			}

			for (final Type superType : getExactDirectSuperTypes(type)) {
				buildUpperBoundClassAndInterfaces(superType, result);
			}
		}
	}

	/**
	 * CaptureType represents a wildcard that has gone through capture conversion.
	 * It is a custom subinterface of Type, not part of the java builtin Type
	 * hierarchy.
	 *
	 * @author Wouter Coekaerts <wouter@coekaerts.be>
	 */
	private interface CaptureType extends Type {

		/**
		 * Returns an array of <tt>Type</tt> objects representing the upper bound(s)
		 * of this capture. This includes both the upper bound of a
		 * <tt>? extends</tt> wildcard, and the bounds declared with the type
		 * variable. References to other (or the same) type variables in bounds
		 * coming from the type variable are replaced by their matching capture.
		 */
		Type[] getUpperBounds();

		/**
		 * Returns an array of <tt>Type</tt> objects representing the lower bound(s)
		 * of this type variable. This is the bound of a <tt>? super</tt> wildcard.
		 * This normally contains only one or no types; it is an array for
		 * consistency with {@link WildcardType#getLowerBounds()}.
		 */
		Type[] getLowerBounds();
	}

	private static class CaptureTypeImpl implements CaptureType {

		private final WildcardType wildcard;
		private final TypeVariable<?> variable;
		private final Type[] lowerBounds;
		private Type[] upperBounds;

		/**
		 * Creates an uninitialized CaptureTypeImpl. Before using this type,
		 * {@link #init(VarMap)} must be called.
		 * 
		 * @param wildcard The wildcard this is a capture of
		 * @param variable The type variable where the wildcard is a parameter for.
		 */
		public CaptureTypeImpl(final WildcardType wildcard,
			final TypeVariable<?> variable)
		{
			this.wildcard = wildcard;
			this.variable = variable;
			this.lowerBounds = wildcard.getLowerBounds();
		}

		/**
		 * Initialize this CaptureTypeImpl. This is needed for type variable bounds
		 * referring to each other: we need the capture of the argument.
		 */
		void init(final VarMap varMap) {
			final ArrayList<Type> upperBoundsList = new ArrayList<>();
			upperBoundsList.addAll(Arrays.asList(varMap.map(variable.getBounds())));

			final List<Type> wildcardUpperBounds = Arrays.asList(wildcard
				.getUpperBounds());
			if (wildcardUpperBounds.size() > 0 && wildcardUpperBounds.get(
				0) == Object.class)
			{
				// skip the Object bound, we already have a first upper bound from
				// 'variable'
				upperBoundsList.addAll(wildcardUpperBounds.subList(1,
					wildcardUpperBounds.size()));
			}
			else {
				upperBoundsList.addAll(wildcardUpperBounds);
			}
			upperBounds = new Type[upperBoundsList.size()];
			upperBoundsList.toArray(upperBounds);
		}

		@Override
		public Type[] getLowerBounds() {
			return lowerBounds.clone();
		}

		@Override
		public Type[] getUpperBounds() {
			assert upperBounds != null;
			return upperBounds.clone();
		}

		@Override
		public String toString() {
			return "capture of " + wildcard;
		}
	}

	/**
	 * Mapping between type variables and actual parameters.
	 *
	 * @author Wouter Coekaerts <wouter@coekaerts.be>
	 */
	private static class VarMap {

		private final Map<TypeVariable<?>, Type> map =
			new HashMap<>();

		/**
		 * Creates an empty VarMap
		 */
		VarMap() {}

		void add(final TypeVariable<?> variable, final Type value) {
			map.put(variable, value);
		}

		void addAll(final TypeVariable<?>[] variables, final Type[] values) {
			assert variables.length == values.length;
			for (int i = 0; i < variables.length; i++) {
				map.put(variables[i], values[i]);
			}
		}

		Type map(final Type type) {
			if (type instanceof Class) {
				return type;
			}
			else if (type instanceof TypeVariable) {
				assert map.containsKey(type);
				return map.get(type);
			}
			else if (type instanceof ParameterizedType) {
				final ParameterizedType pType = (ParameterizedType) type;
				return parameterize((Class<?>) pType.getRawType(), pType
					.getOwnerType() == null ? pType.getOwnerType() : map(pType
						.getOwnerType()), map(pType.getActualTypeArguments()));
			}
			else if (type instanceof WildcardType) {
				final WildcardType wType = (WildcardType) type;
				return new TypeUtils.WildcardTypeImpl(map(wType.getUpperBounds()), map(
					wType.getLowerBounds()));
			}
			else if (type instanceof GenericArrayType) {
				return array(map(((GenericArrayType) type).getGenericComponentType()));
			}
			else {
				throw new RuntimeException("not implemented: mapping " + type
					.getClass() + " (" + type + ")");
			}
		}

		Type[] map(final Type[] types) {
			final Type[] result = new Type[types.length];
			for (int i = 0; i < types.length; i++) {
				result[i] = map(types[i]);
			}
			return result;
		}
	}

	// -- END FORK OF GENTYREF 1.1.0 CODE --
}
