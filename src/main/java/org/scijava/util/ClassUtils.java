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

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
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
		// CTR START HERE: cachedFields should never be null now.
		// But it is, with FilamentDetector 1.0.0. Figure out why and fix.
		// Then cut release of scijava-common, and FilamentDetector.

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
				final Type fieldType = Types.fieldType(field, instance.getClass());
				@SuppressWarnings("deprecation")
				final Object convertedValue = ConversionUtils.convert(value, fieldType);
				compatibleValue = convertedValue;
			}
			field.set(instance, compatibleValue);
		}
		catch (final IllegalAccessException e) {
			throw new IllegalArgumentException("No access to field: " +
				field.getName(), e);
		}
	}

	// -- Type querying --

	// -- Comparison --

	// START HERE: Migrate remaining methods to Types, then deprecate this class.
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

	private static Class<?> arrayOrNull(final Class<?> componentType) {
		try {
			return Types.array(componentType);
		}
		catch (final IllegalArgumentException exc) {
			return null;
		}
	}

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

	// -- Deprecated methods --

	/** @deprecated Use {@link Types#load(String)} instead. */
	@Deprecated
	public static Class<?> loadClass(final String name) {
		return Types.load(name);
	}

	/** @deprecated Use {@link Types#load(String, ClassLoader)} instead. */
	@Deprecated
	public static Class<?> loadClass(final String name,
		final ClassLoader classLoader)
	{
		return Types.load(name, classLoader);
	}

	/** @deprecated Use {@link Types#load(String, boolean)} instead. */
	@Deprecated
	public static Class<?> loadClass(final String className,
		final boolean quietly)
	{
		return Types.load(className, quietly);
	}

	/**
	 * @deprecated Use {@link Types#load(String, ClassLoader, boolean)} instead.
	 */
	@Deprecated
	public static Class<?> loadClass(final String name,
		final ClassLoader classLoader, final boolean quietly)
	{
		return Types.load(name, classLoader, quietly);
	}

	/** @deprecated Use {@link Types#load(String)} instead. */
	@Deprecated
	public static boolean hasClass(final String className) {
		return Types.load(className) != null;
	}

	/** @deprecated Use {@link Types#load(String, ClassLoader)} instead. */
	@Deprecated
	public static boolean hasClass(final String className,
		final ClassLoader classLoader)
	{
		return Types.load(className, classLoader) != null;
	}

	/** @deprecated Use {@link Types#location} and {@link Types#load} instead. */
	@Deprecated
	public static URL getLocation(final String className) {
		return Types.location(Types.load(className));
	}

	/** @deprecated Use {@link Types#location} and {@link Types#load} instead. */
	@Deprecated
	public static URL getLocation(final String className,
		final ClassLoader classLoader)
	{
		return Types.location(Types.load(className, classLoader));
	}

	/** @deprecated Use {@link Types#location} and {@link Types#load} instead. */
	@Deprecated
	public static URL getLocation(final Class<?> c) {
		return Types.location(c);
	}

	/** @deprecated Use {@link Types#isBoolean} instead. */
	@Deprecated
	public static boolean isBoolean(final Class<?> type) {
		return Types.isBoolean(type);
	}

	/** @deprecated Use {@link Types#isByte} instead. */
	@Deprecated
	public static boolean isByte(final Class<?> type) {
		return Types.isByte(type);
	}

	/** @deprecated Use {@link Types#isCharacter} instead. */
	@Deprecated
	public static boolean isCharacter(final Class<?> type) {
		return Types.isCharacter(type);
	}

	/** @deprecated Use {@link Types#isDouble} instead. */
	@Deprecated
	public static boolean isDouble(final Class<?> type) {
		return Types.isDouble(type);
	}

	/** @deprecated Use {@link Types#isFloat} instead. */
	@Deprecated
	public static boolean isFloat(final Class<?> type) {
		return Types.isFloat(type);
	}

	/** @deprecated Use {@link Types#isInteger} instead. */
	@Deprecated
	public static boolean isInteger(final Class<?> type) {
		return Types.isInteger(type);
	}

	/** @deprecated Use {@link Types#isLong} instead. */
	@Deprecated
	public static boolean isLong(final Class<?> type) {
		return Types.isLong(type);
	}

	/** @deprecated Use {@link Types#isShort} instead. */
	@Deprecated
	public static boolean isShort(final Class<?> type) {
		return Types.isShort(type);
	}

	/** @deprecated Use {@link Types#isNumber} instead. */
	@Deprecated
	public static boolean isNumber(final Class<?> type) {
		return Types.isNumber(type);
	}

	/** @deprecated Use {@link Types#isText} instead. */
	@Deprecated
	public static boolean isText(final Class<?> type) {
		return Types.isText(type);
	}

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

	/** @deprecated use {@link Types#cast(Object, Class)} */
	@Deprecated
	public static <T> T cast(final Object obj, final Class<T> type) {
		return Types.cast(obj, type);
	}

	/** @deprecated use {@link Types#isAssignable(Type, Type)} */
	@Deprecated
	public static boolean canCast(final Class<?> c, final Class<?> type) {
		return Types.isAssignable(c, type);
	}

	/** @deprecated use {@link Types#isInstance(Object, Class)} */
	@Deprecated
	public static boolean canCast(final Object obj, final Class<?> type) {
		return Types.isInstance(obj, type);
	}

	/** @deprecated use {@link Types#box(Class)} */
	@Deprecated
	public static <T> Class<T> getNonprimitiveType(final Class<T> type) {
		return Types.box(type);
	}

	/** @deprecated use {@link Types#nullValue(Class)} */
	@Deprecated
	public static <T> T getNullValue(final Class<T> type) {
		return Types.nullValue(type);
	}

	/**
	 * @deprecated Use {@link Types#fieldType(Field, Class)} and {@link Types#raws}
	 *             instead.
	 */
	@Deprecated
	public static List<Class<?>> getTypes(final Field field, final Class<?> type)
	{
		return Types.raws(Types.fieldType(field, type));
	}

	/** @deprecated Use {@link Types#fieldType(Field, Class)} instead. */
	@Deprecated
	public static Type getGenericType(final Field field, final Class<?> type) {
		return Types.fieldType(field, type);
	}

	/** @deprecated Use {@link Types#field} instead. */
	@Deprecated
	public static Field getField(final String className, final String fieldName) {
		try {
			return Types.field(Types.load(className), fieldName);
		} catch (final IllegalArgumentException e) {
			return null;
		}
	}

	/** @deprecated Use {@link Types#field} instead. */
	@Deprecated
	public static Field getField(final Class<?> c, final String fieldName) {
		try {
			return Types.field(c, fieldName);
		} catch (final IllegalArgumentException e) {
			return null;
		}
	}

	/** @deprecated Use {@link Types#array(Class)} instead. */
	@Deprecated
	public static Class<?> getArrayClass(final Class<?> elementClass) {
		return Types.raw(arrayOrNull(elementClass));
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
			final Map<Class<? extends Annotation>, List<T>> annotationTypes = get(c);
			return annotationTypes == null ? null : annotationTypes.get(annotationClass);
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
