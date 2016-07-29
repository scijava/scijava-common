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

import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

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

	// TODO: Migrate all GenericUtils methods here.

	public static String name(final Type t) {
		// NB: It is annoying that Class.toString() prepends "class " or
		// "interface "; this method exists to work around that behavior.
		return t instanceof Class ? ((Class<?>) t).getName() : t.toString();
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
	 */
	public static Class<?> raw(final Type type) {
		// TODO: Consolidate with GenericUtils.
		return GenericUtils.getClass(type);
	}

	/**
	 * Gets all raw classes corresponding to the given type.
	 * <p>
	 * For example, a type parameter {@code A extends Number & Iterable} will
	 * return both {@link Number} and {@link Iterable} as its raw classes.
	 * </p>
	 *
	 * @see #raw
	 */
	public static List<Class<?>> raws(final Type type) {
		// TODO: Consolidate with GenericUtils.
		return GenericUtils.getClasses(type);
	}

	public static Field field(final Class<?> c, final String name) {
		if (c == null) throw new IllegalArgumentException("No such field: " + name);
		try {
			return c.getDeclaredField(name);
		}
		catch (final NoSuchFieldException e) {}
		return field(c.getSuperclass(), name);
	}
}
