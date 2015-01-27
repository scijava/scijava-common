/*
 * #%L
 * SciJava Common shared library for SciJava software.
 * %%
 * Copyright (C) 2009 - 2014 Board of Regents of the University of
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

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Useful methods for working with {@link Class} objects and primitive types.
 * 
 * @author Curtis Rueden
 */
public final class ClassUtils {

	private ClassUtils() {
		// prevent instantiation of utility class
	}

	/**
	 * This maps a base class (key1) to a map of annotation classes (key2), which
	 * then maps to a list of {@link Field} instances, being the set of fields in
	 * the base class with the specified annotation.
	 * <p>
	 * This map serves as a cache, as these annotations should not change at
	 * runtime and thus can alleviate the frequency field querying.
	 * </p>
	 * @see <a href="https://github.com/scijava/scijava-common/issues/142">issue #142</a>
	 */
	private static final FieldCache fieldCache = new FieldCache();

	/**
	 * This maps a base class (key1) to a map of annotation classes (key2), which
	 * then maps to a list of {@link Method} instances, being the set of methods
	 * in the base class with the specified annotation.
	 * <p>
	 * This map serves as a cache, as these annotations should not change at
	 * runtime and thus can alleviate the frequency of method querying.
	 * </p>
	 * @see <a href="https://github.com/scijava/scijava-common/issues/142">issue #142</a>
	 */
	private static final MethodCache methodCache = new MethodCache();

	// -- Class loading, querying and reflection --

	/**
	 * Loads the class with the given name, using the current thread's context
	 * class loader, or null if it cannot be loaded.
	 * 
	 * @see #loadClass(String, ClassLoader)
	 */
	public static Class<?> loadClass(final String className) {
		return loadClass(className, null);
	}

	/**
	 * Loads the class with the given name, using the specified
	 * {@link ClassLoader}, or null if it cannot be loaded.
	 * <p>
	 * This method is capable of parsing several different class name syntaxes.
	 * In particular, array classes (including primitives) represented using
	 * either square brackets or internal Java array name syntax are supported.
	 * Examples:
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
	 */
	public static Class<?> loadClass(final String name,
		final ClassLoader classLoader)
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
			return getArrayClass(loadClass(elementClassName, classLoader));
		}

		// handle non-primitive internal arrays (e.g.: "[Ljava.lang.String;")
		if (name.startsWith("[L") && name.endsWith(";")) {
			final String elementClassName = name.substring(2, name.length() - 1);
			return getArrayClass(loadClass(elementClassName, classLoader));
		}

		// handle other internal arrays (e.g.: "[I", "[[I", "[[Ljava.lang.String;")
		if (name.startsWith("[")) {
			final String elementClassName = name.substring(1);
			return getArrayClass(loadClass(elementClassName, classLoader));
		}

		// load the class!
		try {
			final ClassLoader cl =
				classLoader == null ? Thread.currentThread().getContextClassLoader()
					: classLoader;
			return cl.loadClass(className);
		}
		catch (final ClassNotFoundException e) {
			return null;
		}
	}

	/**
	 * Gets the array class corresponding to the given element type.
	 * <p>
	 * For example, {@code getArrayClass(double.class)} returns
	 * {@code double[].class}.
	 * </p>
	 */
	public static Class<?> getArrayClass(final Class<?> elementClass) {
		if (elementClass == null) return null;
		// NB: It appears the reflection API has no built-in way to do this.
		// So unfortunately, we must allocate a new object and then inspect it.
		try {
			return Array.newInstance(elementClass, 0).getClass();
		}
		catch (final IllegalArgumentException exc) {
			return null;
		}
	}

	/** Checks whether a class with the given name exists. */
	public static boolean hasClass(final String className) {
		return hasClass(className, null);
	}

	/** Checks whether a class with the given name exists. */
	public static boolean hasClass(final String className,
		final ClassLoader classLoader)
	{
		return loadClass(className, classLoader) != null;
	}

	/**
	 * Gets the base location of the given class.
	 * <p>
	 * If the class is directly on the file system (e.g.,
	 * "/path/to/my/package/MyClass.class") then it will return the base directory
	 * (e.g., "/path/to").
	 * </p>
	 * <p>
	 * If the class is within a JAR file (e.g.,
	 * "/path/to/my-jar.jar!/my/package/MyClass.class") then it will return the
	 * path to the JAR (e.g., "/path/to/my-jar.jar").
	 * </p>
	 * 
	 * @param className The name of the class whose location is desired.
	 * @see FileUtils#urlToFile(URL) to convert the result to a {@link File}.
	 */
	public static URL getLocation(final String className) {
		return getLocation(className, null);
	}

	/**
	 * Gets the base location of the given class.
	 * <p>
	 * If the class is directly on the file system (e.g.,
	 * "/path/to/my/package/MyClass.class") then it will return the base directory
	 * (e.g., "/path/to").
	 * </p>
	 * <p>
	 * If the class is within a JAR file (e.g.,
	 * "/path/to/my-jar.jar!/my/package/MyClass.class") then it will return the
	 * path to the JAR (e.g., "/path/to/my-jar.jar").
	 * </p>
	 * 
	 * @param className The name of the class whose location is desired.
	 * @param classLoader The class loader to use when loading the class.
	 * @see FileUtils#urlToFile(URL) to convert the result to a {@link File}.
	 */
	public static URL getLocation(final String className,
		final ClassLoader classLoader)
	{
		final Class<?> c = loadClass(className, classLoader);
		return getLocation(c);
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
	 * @see FileUtils#urlToFile(URL) to convert the result to a {@link File}.
	 */
	public static URL getLocation(final Class<?> c) {
		if (c == null) return null; // could not load the class

		// try the easy way first
		try {
			final URL codeSourceLocation =
				c.getProtectionDomain().getCodeSource().getLocation();
			if (codeSourceLocation != null) return codeSourceLocation;
		}
		catch (final SecurityException e) {
			// NB: Cannot access protection domain.
		}
		catch (final NullPointerException e) {
			// NB: Protection domain or code source is null.
		}

		// NB: The easy way failed, so we try the hard way. We ask for the class
		// itself as a resource, then strip the class's path from the URL string,
		// leaving the base path.

		// get the class's raw resource path
		final URL classResource = c.getResource(c.getSimpleName() + ".class");
		if (classResource == null) return null; // cannot find class resource

		final String url = classResource.toString();
		final String suffix = c.getCanonicalName().replace('.', '/') + ".class";
		if (!url.endsWith(suffix)) return null; // weird URL

		// strip the class's path from the URL string
		final String base = url.substring(0, url.length() - suffix.length());

		String path = base;

		// remove the "jar:" prefix and "!/" suffix, if present
		if (path.startsWith("jar:")) path = path.substring(4, path.length() - 2);

		try {
			return new URL(path);
		}
		catch (final MalformedURLException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Gets the given class's {@link Method}s marked with the annotation of the
	 * specified class.
	 * <p>
	 * Unlike {@link Class#getMethods()}, the result will include any non-public
	 * methods, including methods defined in supertypes of the given class.
	 * </p>
	 *
	 * @param c The class to scan for annotated methods.
	 * @param annotationClass The type of annotation for which to scan.
	 * @return A new list containing all methods with the requested annotation.
	 */
	public static <A extends Annotation> List<Method> getAnnotatedMethods(
		final Class<?> c, final Class<A> annotationClass)
	{
		List<Method> methods = methodCache.getList(c, annotationClass);

		if (methods == null) {
			methods = new ArrayList<Method>();
			getAnnotatedMethods(c, annotationClass, methods);
		}

		return methods;
	}

	/**
	 * Gets the given class's {@link Method}s marked with the annotation of the
	 * specified class.
	 * <p>
	 * Unlike {@link Class#getMethods()}, the result will include any non-public
	 * methods, including methods defined in supertypes of the given class.
	 * </p>
	 *
	 * @param c The class to scan for annotated methods.
	 * @param annotationClass The type of annotation for which to scan.
	 * @param methods The list to which matching methods will be added.
	 */
	public static <A extends Annotation> void
		getAnnotatedMethods(final Class<?> c, final Class<A> annotationClass,
			final List<Method> methods)
	{
		List<Method> cachedMethods = methodCache.getList(c, annotationClass);

		if (cachedMethods == null) {
			Query query = new Query();
			query.put(annotationClass, Method.class);
			cacheAnnotatedObjects(c, query);
			cachedMethods = methodCache.getList(c, annotationClass);
		}

		methods.addAll(cachedMethods);
	}

	/**
	 * Gets the given class's {@link Field}s marked with the annotation of the
	 * specified class.
	 * <p>
	 * Unlike {@link Class#getFields()}, the result will include any non-public
	 * fields, including fields defined in supertypes of the given class.
	 * </p>
	 * 
	 * @param c The class to scan for annotated fields.
	 * @param annotationClass The type of annotation for which to scan.
	 * @return A new list containing all fields with the requested annotation.
	 */
	public static <A extends Annotation> List<Field> getAnnotatedFields(
		final Class<?> c, final Class<A> annotationClass)
	{
		List<Field> fields = fieldCache.getList(c, annotationClass);

		if (fields == null) {
			fields = new ArrayList<Field>();
			getAnnotatedFields(c, annotationClass, fields);
		}

		return fields;
	}

	/**
 * Gets the given class's {@link Field}s marked with the annotation of the
 * specified class.
 * <p>
	 * Unlike {@link Class#getFields()}, the result will include any non-public
	 * fields, including fields defined in supertypes of the given class.
	 * </p>
	 * 
	 * @param c The class to scan for annotated fields.
	 * @param annotationClass The type of annotation for which to scan.
	 * @param fields The list to which matching fields will be added.
	 */
	public static <A extends Annotation> void getAnnotatedFields(
		final Class<?> c, final Class<A> annotationClass, final List<Field> fields)
	{
		List<Field> cachedFields = fieldCache.getList(c, annotationClass);

		if (cachedFields == null) {
			Query query = new Query();
			query.put(annotationClass, Field.class);
			cacheAnnotatedObjects(c, query);
			cachedFields = fieldCache.getList(c, annotationClass);
		}

		fields.addAll(cachedFields);
	}

	/**
	 * This method scans the provided class, its superclasses and interfaces for
	 * all supported {@code {@link Annotation} : {@link AnnotatedObject} pairs.
	 * These are then cached to remove the need for future queries.
	 * <p>
	 * By combining multiple {@code Annotation : AnnotatedObject} pairs in one
	 * query, we can limit the number of times a class's superclass and interface
	 * hierarchy are traversed.
	 * </p>
	 *
	 * @param scannedClass Class to scan
	 * @param query Pairs of {@link Annotation} and {@link AnnotatedObject}s to
	 *          discover.
	 */
	public static void cacheAnnotatedObjects(final Class<?> scannedClass,
		final Query query)
	{
		// NB: The java.lang.Object class does not have any annotated methods.
		// And even if it did, it definitely does not have any methods annotated
		// with SciJava annotations such as org.scijava.event.EventHandler, which
		// are the main sorts of methods we are interested in.
		if (scannedClass == null || scannedClass == Object.class) return;

		// Initialize step - determine which queries are solved
		final Set<Class<? extends Annotation>> keysToDrop =
			new HashSet<Class<? extends Annotation>>();
		for (final Class<? extends Annotation> annotationClass : query.keySet()) {
			final Class<? extends AnnotatedElement> objectClass =
				query.get(annotationClass);

			// Fields
			if (Field.class.isAssignableFrom(objectClass)) {
				if (fieldCache.getList(scannedClass, annotationClass) != null) keysToDrop
					.add(annotationClass);
			}
			// Methods
			else if (Method.class.isAssignableFrom(objectClass)) {
				if (methodCache.getList(scannedClass, annotationClass) != null) keysToDrop
					.add(annotationClass);
			}
		}

		// Clean up resolved keys
		for (final Class<? extends Annotation> key : keysToDrop) {
			query.remove(key);
		}

		// Stop now if we know all requested information is cached
		if (query.isEmpty()) return;

		final List<Class<?>> inherited = new ArrayList<Class<?>>();

		// cache all parents recursively
		final Class<?> superClass = scannedClass.getSuperclass();
		if (superClass != null) {
			// Recursive step
			cacheAnnotatedObjects(
				superClass, new Query(query));
			inherited.add(superClass);
		}

		// cache all interfaces recursively
		for (final Class<?> ifaceClass : scannedClass.getInterfaces()) {
			// Recursive step
			cacheAnnotatedObjects(
				ifaceClass,
				new Query(query));
			inherited.add(ifaceClass);
		}

		// Populate supported objects for scanned class
		for (final Class<? extends Annotation> annotationClass : query.keySet()) {
			final Class<? extends AnnotatedElement> objectClass =
				query.get(annotationClass);

			// Methods
			if (Method.class.isAssignableFrom(objectClass)) {
				for (final Class<?> inheritedClass : inherited) {
					final List<Method> annotatedMethods =
							methodCache.getList(inheritedClass, annotationClass);

					if (annotatedMethods != null && !annotatedMethods.isEmpty()) {
						final List<Method> scannedMethods =
								methodCache.makeList(scannedClass, annotationClass);

						scannedMethods.addAll(annotatedMethods);
					}
				}

				// Add declared methods
				final Method[] declaredMethods = scannedClass.getDeclaredMethods();
				if (declaredMethods != null && declaredMethods.length > 0) {
					List<Method> scannedMethods = null;

					for (final Method m : declaredMethods) {
						if (m.getAnnotation(annotationClass) != null) {
							if (scannedMethods == null) {
								scannedMethods = methodCache.makeList(scannedClass, annotationClass);
							}
							scannedMethods.add(m);
						}
					}
				}

				// If there were no methods for this query, map an empty
				// list to mark the query complete
				if (methodCache.getList(scannedClass, annotationClass) == null) {
					methodCache.putList(scannedClass, annotationClass, Collections.<Method>emptyList());
				}
			}
			// Fields
			else if (Field.class.isAssignableFrom(objectClass)) {
				for (final Class<?> inheritedClass : inherited) {
					final List<Field> annotatedFields =
							fieldCache.getList(inheritedClass, annotationClass);

					if (annotatedFields != null && !annotatedFields.isEmpty()) {
						final List<Field> scannedFields =
								fieldCache.makeList(scannedClass, annotationClass);

						scannedFields.addAll(annotatedFields);
					}
				}

				// Add declared fields
				final Field[] declaredFields = scannedClass.getDeclaredFields();
				if (declaredFields != null && declaredFields.length > 0) {
					List<Field> scannedFields = null;

					for (final Field f : declaredFields) {
						if (f.getAnnotation(annotationClass) != null) {
							if (scannedFields == null) {
								scannedFields = fieldCache.makeList(scannedClass, annotationClass);
							}
							scannedFields.add(f);
						}
					}
				}

				// If there were no fields for this query, map an empty
				// list to mark the query complete
				if (fieldCache.getList(scannedClass, annotationClass) == null) {
					fieldCache.putList(scannedClass, annotationClass, Collections.<Field>emptyList());
				}
			}
		}
	}

	/**
	 * Gets the specified field of the given class, or null if it does not exist.
	 */
	public static Field getField(final String className, final String fieldName) {
		return getField(loadClass(className), fieldName);
	}

	/**
	 * Gets the specified field of the given class, or null if it does not exist.
	 */
	public static Field getField(final Class<?> c, final String fieldName) {
		if (c == null) return null;
		try {
			return c.getDeclaredField(fieldName);
		}
		catch (final NoSuchFieldException e) {
			return null;
		}
	}

	/**
	 * Gets the given field's value of the specified object instance, or null if
	 * the value cannot be obtained.
	 */
	public static Object getValue(final Field field, final Object instance) {
		try {
			field.setAccessible(true);
			return field.get(instance);
		}
		catch (final IllegalAccessException e) {
			return null;
		}
	}

	/**
	 * Sets the given field's value of the specified object instance.
	 * 
	 * @throws IllegalArgumentException if the value cannot be set.
	 */
	// FIXME: Move to ConvertService and deprecate this signature.
	public static void setValue(final Field field, final Object instance,
		final Object value)
	{
		try {
			field.setAccessible(true);
			final Object compatibleValue;
			if (value == null || field.getType().isInstance(value)) {
				// the given value is compatible with the field
				compatibleValue = value;
			}
			else {
				// the given value needs to be converted to a compatible type
				final Type fieldType =
						GenericUtils.getFieldType(field, instance.getClass());
				compatibleValue = ConversionUtils.convert(value, fieldType);
			}
			field.set(instance, compatibleValue);
		}
		catch (final IllegalAccessException e) {
			throw new IllegalArgumentException("No access to field: " +
				field.getName(), e);
		}
	}

	// -- Type querying --

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

	// -- Comparison --

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

	// -- Helper methods --



	// -- Deprecated methods --

	/** @deprecated use {@link ConversionUtils#convert(Object, Class)} */
	@Deprecated
	public static <T> T convert(final Object value, final Class<T> type) {
		return ConversionUtils.convert(value, type);
	}

	/** @deprecated use {@link ConversionUtils#canConvert(Class, Class)} */
	@Deprecated
	public static boolean canConvert(final Class<?> c, final Class<?> type) {
		return ConversionUtils.canConvert(c, type);
	}

	/** @deprecated use {@link ConversionUtils#canConvert(Object, Class)} */
	@Deprecated
	public static boolean canConvert(final Object value, final Class<?> type) {
		return ConversionUtils.canConvert(value, type);
	}

	/** @deprecated use {@link ConversionUtils#cast(Object, Class)} */
	@Deprecated
	public static <T> T cast(final Object obj, final Class<T> type) {
		return ConversionUtils.cast(obj, type);
	}

	/** @deprecated use {@link ConversionUtils#canCast(Class, Class)} */
	@Deprecated
	public static boolean canCast(final Class<?> c, final Class<?> type) {
		return ConversionUtils.canCast(c, type);
	}

	/** @deprecated use {@link ConversionUtils#canCast(Object, Class)} */
	@Deprecated
	public static boolean canCast(final Object obj, final Class<?> type) {
		return ConversionUtils.canCast(obj, type);
	}

	/** @deprecated use {@link ConversionUtils#getNonprimitiveType(Class)} */
	@Deprecated
	public static <T> Class<T> getNonprimitiveType(final Class<T> type) {
		return ConversionUtils.getNonprimitiveType(type);
	}

	/** @deprecated use {@link ConversionUtils#getNullValue(Class)} */
	@Deprecated
	public static <T> T getNullValue(final Class<T> type) {
		return ConversionUtils.getNullValue(type);
	}

	/** @deprecated use {@link GenericUtils#getFieldClasses(Field, Class)} */
	@Deprecated
	public static List<Class<?>> getTypes(final Field field, final Class<?> type)
	{
		return GenericUtils.getFieldClasses(field, type);
	}

	/** @deprecated use {@link GenericUtils#getFieldType(Field, Class)} */
	@Deprecated
	public static Type getGenericType(final Field field, final Class<?> type) {
		return GenericUtils.getFieldType(field, type);
	}

	/**
	 * Convenience class to further type narrow {@link CacheMap} to {@link Field}s.
	 */
	private static class FieldCache extends CacheMap<Field> { }

	/**
	 * Convenience class to further type narrow {@link CacheMap} to {@link Method}s.
	 */
	private static class MethodCache extends CacheMap<Method> { }

	/**
	 * Convenience class for {@code Map > Map > List} hierarchy. Cleans up generics
	 * and contains helper methods for traversing the two map levels.
	 *
	 * @param <T> - {@link AnnotatedElement} {@link List} ultimately referenced by
	 *          this map
	 */
	private static class CacheMap<T extends AnnotatedElement> extends
		HashMap<Class<?>, Map<Class<? extends Annotation>, List<T>>>
	{

		/**
		 * @param c Base class
		 * @param annotationClass Annotation type
		 * @return Cached list of Methods in the base class with the specified
		 *         annotation, or null if a cached list does not exist.
		 */
		public List<T> getList(final Class<?> c,
			final Class<? extends Annotation> annotationClass)
		{
			List<T> annotatedFields = null;
			Map<Class<? extends Annotation>, List<T>> annotationTypes = get(c);
			if (annotationTypes != null) {
				annotatedFields = annotationTypes.get(annotationClass);
			}
			return annotatedFields;
		}

		/**
		 * Populates the provided list with {@link Method} entries of the given base
		 * class which are annotated with the specified annotation type.
		 *
		 * @param c Base class
		 * @param annotationClass Annotation type
		 * @param annotatedFields Method list to populate
		 */
		public void putList(final Class<?> c,
			final Class<? extends Annotation> annotationClass,
			List<T> annotatedMethods)
		{
			Map<Class<? extends Annotation>, List<T>> map = get(c);
			if (map == null) {
				map = new HashMap<Class<? extends Annotation>, List<T>>();
				put(c, map);
			}

			map.put(annotationClass, annotatedMethods);
		}

		/**
		 * As {@link #getList(Class, Class)} but ensures an array is created and
		 * mapped, if it doesn't already exist.
		 *
		 * @param c Base class
		 * @param annotationClass Annotation type
		 * @return Cached list of Fields in the base class with the specified
		 *         annotation.
		 */
		public List<T> makeList(final Class<?> c,
			final Class<? extends Annotation> annotationClass)
		{
			List<T> methods = getList(c, annotationClass);
			if (methods == null) {
				methods = new ArrayList<T>();
				putList(c, annotationClass, methods);
			}
			return methods;
		}

	}
}
