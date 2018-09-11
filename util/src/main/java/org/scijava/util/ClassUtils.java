/*
 * #%L
 * SciJava Common shared library for SciJava software.
 * %%
 * Copyright (C) 2009 - 2017 Board of Regents of the University of
 * Wisconsin-Madison, Broad Institute of MIT and Harvard, Max Planck
 * Institute of Molecular Cell Biology and Genetics, University of
 * Konstanz, and KNIME GmbH.
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
	 * 
	 * @see <a href="https://github.com/scijava/scijava-common/issues/142">issue
	 *      #142</a>
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
	 * 
	 * @see <a href="https://github.com/scijava/scijava-common/issues/142">issue
	 *      #142</a>
	 */
	private static final MethodCache methodCache = new MethodCache();

	// -- Class loading, querying and reflection --

	/**
	 * Loads the class with the given name, using the current thread's context
	 * class loader, or null if it cannot be loaded.
	 *
	 * @param name The name of the class to load.
	 * @return The loaded class, or null if the class could not be loaded.
	 * @see #loadClass(String, ClassLoader, boolean)
	 */
	public static Class<?> loadClass(final String name) {
		return loadClass(name, null, true);
	}

	/**
	 * Loads the class with the given name, using the specified
	 * {@link ClassLoader}, or null if it cannot be loaded.
	 *
	 * @param name The name of the class to load.
	 * @param classLoader The class loader with which to load the class; if null,
	 *          the current thread's context class loader will be used.
	 * @return The loaded class, or null if the class could not be loaded.
	 * @see #loadClass(String, ClassLoader, boolean)
	 */
	public static Class<?> loadClass(final String name,
		final ClassLoader classLoader)
	{
		return loadClass(name, classLoader, true);
	}

	/**
	 * Loads the class with the given name, using the current thread's context
	 * class loader.
	 *
	 * @param className the name of the class to load
	 * @param quietly Whether to return {@code null} (rather than throwing
	 *          {@link IllegalArgumentException}) if something goes wrong loading
	 *          the class
	 * @return The loaded class, or {@code null} if the class could not be loaded
	 *         and the {@code quietly} flag is set.
	 * @see #loadClass(String, ClassLoader, boolean)
	 * @throws IllegalArgumentException If the class cannot be loaded and the
	 *           {@code quietly} flag is not set.
	 */
	public static Class<?> loadClass(final String className,
		final boolean quietly)
	{
		return loadClass(className, null, quietly);
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
	public static Class<?> loadClass(final String name,
		final ClassLoader classLoader, final boolean quietly)
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
		catch (final Throwable t) {
			// NB: Do not allow any failure to load the class to crash us.
			// Not ClassNotFoundException.
			// Not NoClassDefFoundError.
			// Not UnsupportedClassVersionError!
			if (quietly) return null;
			throw new IllegalArgumentException("Cannot load class: " + className, t);
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
	 * @return A list containing all methods with the requested annotation. Note
	 *         that for performance reasons, lists may be cached and reused, so it
	 *         is best to make a copy of the result if you need to modify it.
	 */
	public static <A extends Annotation> List<Method> getAnnotatedMethods(
		final Class<?> c, final Class<A> annotationClass)
	{
		List<Method> methods = methodCache.getList(c, annotationClass);

		if (methods == null) {
			methods = new ArrayList<>();
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
			final Query query = new Query();
			query.put(annotationClass, Method.class);
			cacheAnnotatedObjects(c, query);
			cachedMethods = methodCache.getList(c, annotationClass);
		}

		if (cachedMethods != null) methods.addAll(cachedMethods);
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
	 * @return A list containing all fields with the requested annotation. Note
	 *         that for performance reasons, lists may be cached and reused, so it
	 *         is best to make a copy of the result if you need to modify it.
	 */
	public static <A extends Annotation> List<Field> getAnnotatedFields(
		final Class<?> c, final Class<A> annotationClass)
	{
		List<Field> fields = fieldCache.getList(c, annotationClass);

		if (fields == null) {
			fields = new ArrayList<>();
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
			final Query query = new Query();
			query.put(annotationClass, Field.class);
			cacheAnnotatedObjects(c, query);
			cachedFields = fieldCache.getList(c, annotationClass);
		}

		fields.addAll(cachedFields);
	}

	/**
	 * This method scans the provided class, its superclasses and interfaces for
	 * all supported {@link Annotation} : {@link AnnotatedElement} pairs.
	 * These are then cached to remove the need for future queries.
	 * <p>
	 * By combining multiple {@code Annotation : AnnotatedElement} pairs in one
	 * query, we can limit the number of times a class's superclass and interface
	 * hierarchy are traversed.
	 * </p>
	 *
	 * @param scannedClass Class to scan
	 * @param query Pairs of {@link Annotation} and {@link AnnotatedElement}s to
	 *          discover.
	 */
	public static void cacheAnnotatedObjects(final Class<?> scannedClass,
		final Query query)
	{
		// Only allow one thread at a time to populate cached objects for a given
		// class. This lock is technically overkill - the minimal lock would be
		// on the provided Query contents + class. Devising a way to obtain this
		// lock could be useful - as if Query A and Query B were executed on the
		// same class by different threads, there are three scenarios:
		// 1) intersection of A + B is empty - then they can run on separate threads
		// 2) A == B - whichever was received second must wait for the first to
		// 	  finish.
		// 3) A != B and intersection of A + B is not empty - the intersection subset
		//    can be safely performed on a separate thread, but the later query must
		//    still wait for the earlier query to complete.
		//
		// NB: an alternative would be to update the getAnnotatedxxx methods to
		// return Sets instead of Lists. Then threads can pretty much go nuts
		// as long as you double lock the Set creation in a synchronized block.
		//
		// NB: another possibility would be to keep this synchronized entry point
		// but divide the work for each Query into asynchronous blocks. However, it
		// has not been investigated how much of a performance boost that would
		// provide as it would then cause multiple traversals of the class hierarchy
		// - which is exactly what the Query notation was created to avoid.
		synchronized (scannedClass) {
			// NB: The java.lang.Object class does not have any annotated methods.
			// And even if it did, it definitely does not have any methods annotated
			// with SciJava annotations such as org.scijava.event.EventHandler, which
			// are the main sorts of methods we are interested in.
			if (scannedClass == null || scannedClass == Object.class) return;

			// Initialize step - determine which queries are solved
			final Set<Class<? extends Annotation>> keysToDrop =
				new HashSet<>();
			for (final Class<? extends Annotation> annotationClass : query.keySet()) {
				// Fields
				if (fieldCache.getList(scannedClass, annotationClass) != null) {
					keysToDrop.add(annotationClass);
				}
				else if (methodCache.getList(scannedClass, annotationClass) != null) {
					keysToDrop.add(annotationClass);
				}
			}

			// Clean up resolved keys
			for (final Class<? extends Annotation> key : keysToDrop) {
				query.remove(key);
			}

			// Stop now if we know all requested information is cached
			if (query.isEmpty()) return;

			final List<Class<?>> inherited = new ArrayList<>();

			// cache all parents recursively
			final Class<?> superClass = scannedClass.getSuperclass();
			if (superClass != null) {
				// Recursive step
				cacheAnnotatedObjects(superClass, new Query(query));
				inherited.add(superClass);
			}

			// cache all interfaces recursively
			for (final Class<?> ifaceClass : scannedClass.getInterfaces()) {
				// Recursive step
				cacheAnnotatedObjects(ifaceClass, new Query(query));
				inherited.add(ifaceClass);
			}

			// Populate supported objects for scanned class
			for (final Class<? extends Annotation> annotationClass : query.keySet()) {
				final Class<? extends AnnotatedElement> objectClass =
					query.get(annotationClass);

				try {
					// Methods
					if (Method.class.isAssignableFrom(objectClass)) {
						populateCache(scannedClass, inherited, annotationClass, methodCache,
							scannedClass.getDeclaredMethods());
					}
					// Fields
					else if (Field.class.isAssignableFrom(objectClass)) {
						populateCache(scannedClass, inherited, annotationClass, fieldCache,
							scannedClass.getDeclaredFields());
					}
				}
				catch (final Throwable t) {
					// NB: No action needed?
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

	// -- Helper methods --

	/**
	 * Populates the cache of annotated elements for a particular class by looking
	 * for all inherited and declared instances annotated with the specified
	 * annotationClass. If no matches are found, an empty mapping is created to
	 * mark this class complete.
	 */
	private static <T extends AnnotatedElement> void populateCache(
		final Class<?> scannedClass, final List<Class<?>> inherited,
		final Class<? extends Annotation> annotationClass,
		final CacheMap<T> cacheMap, final T[] declaredElements)
	{
		// Add inherited elements
		for (final Class<?> inheritedClass : inherited) {
			final List<T> annotatedElements =
				cacheMap.getList(inheritedClass, annotationClass);

			if (annotatedElements != null && !annotatedElements.isEmpty()) {
				final List<T> scannedElements =
					cacheMap.makeList(scannedClass, annotationClass);

				scannedElements.addAll(annotatedElements);
			}
		}

		// Add declared elements
		if (declaredElements != null && declaredElements.length > 0) {
			List<T> scannedElements = null;

			for (final T t : declaredElements) {
				if (t.getAnnotation(annotationClass) != null) {
					if (scannedElements == null) {
						scannedElements = cacheMap.makeList(scannedClass, annotationClass);
					}
					scannedElements.add(t);
				}
			}
		}

		// If there were no elements for this query, map an empty
		// list to mark the query complete
		if (cacheMap.getList(scannedClass, annotationClass) == null) {
			cacheMap.putList(scannedClass, annotationClass, Collections
				.<T> emptyList());
		}
	}

	// -- Helper classes --

	/**
	 * Convenience class for a {@link CacheMap} that stores annotated
	 * {@link Field}s.
	 */
	private static class FieldCache extends CacheMap<Field> {
		// Trivial subclass to narrow generic params
	}

	/**
	 * Convenience class for a {@link CacheMap} that stores annotated
	 * {@link Method}s.
	 */
	private static class MethodCache extends CacheMap<Method> {
		// Trivial subclass to narrow generic params
	}

	/**
	 * Convenience class for {@code Map > Map > List} hierarchy. Cleans up
	 * generics and contains helper methods for traversing the two map levels.
	 * <p>
	 * The intent for this class is to allow subclasses to specify the generic
	 * parameter ultimately referenced by the at the end of these maps.
	 * </p>
	 * <p>
	 * The first map key is a base class, presumably with various types of
	 * annotations. The second map key is the annotation class, for example
	 * {@link Method} or {@link Field}. The list then contains all instances of
	 * the annotated type within the original base class.
	 * </p>
	 *
	 * @param <T> - The type of {@link AnnotatedElement} contained by the
	 *          {@link List} ultimately referenced by these {@link Map}s
	 */
	private static class CacheMap<T extends AnnotatedElement> extends
		HashMap<Class<?>, Map<Class<? extends Annotation>, List<T>>>
	{

		/**
		 * @param c Base class of interest
		 * @param annotationClass {@link Annotation} type within the base class
		 * @return A {@link List} of instances in the base class with the specified
		 *         {@link Annotation}, or null if a cached list does not exist.
		 */
		public List<T> getList(final Class<?> c,
			final Class<? extends Annotation> annotationClass)
		{
			List<T> annotatedFields = null;
			final Map<Class<? extends Annotation>, List<T>> annotationTypes = get(c);
			if (annotationTypes != null) {
				annotatedFields = annotationTypes.get(annotationClass);
			}
			return annotatedFields;
		}

		/**
		 * Creates a {@code base class > annotation > list of elements} mapping to
		 * the provided list, creating the intermediate map if needed.
		 *
		 * @param c Base class of interest
		 * @param annotationClass {@link Annotation} type of interest
		 * @param annotatedElements List of {@link AnnotatedElement}s to map
		 */
		public void putList(final Class<?> c,
			final Class<? extends Annotation> annotationClass,
			final List<T> annotatedElements)
		{
			Map<Class<? extends Annotation>, List<T>> map = get(c);
			if (map == null) {
				map = new HashMap<>();
				put(c, map);
			}

			map.put(annotationClass, annotatedElements);
		}

		/**
		 * Generates mappings as in {@link #putList(Class, Class, List)}, but also
		 * creates the {@link List} if it doesn't already exist. Returns the final
		 * list at this mapping, for external population.
		 *
		 * @param c Base class of interest
		 * @param annotationClass {@link Annotation} type of interest
		 * @return Cached list of {@link AnnotatedElement}s in the base class with
		 *         the specified {@link Annotation}.
		 */
		public List<T> makeList(final Class<?> c,
			final Class<? extends Annotation> annotationClass)
		{
			List<T> elements = getList(c, annotationClass);
			if (elements == null) {
				elements = new ArrayList<>();
				putList(c, annotationClass, elements);
			}
			return elements;
		}

	}
}
