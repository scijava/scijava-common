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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;

import org.scijava.util.Types;

/** @deprecated Use {@link Types} instead. */
@Deprecated
public final class GenericUtils {

	private GenericUtils() {
		// prevent instantiation of utility class
	}

	/** @deprecated Use {@link Types#raw} instead. */
	@Deprecated
	public static Class<?> getClass(final Type type) {
		final List<Class<?>> bounds = Types.raws(type);
		return bounds != null && bounds.size() == 1 ? bounds.get(0) : null;
	}

	/** @deprecated Use {@link Types#raws} instead. */
	@Deprecated
	public static List<Class<?>> getClasses(final Type type) {
		return Types.raws(type);
	}

	/** @deprecated Use {@link Types#component} instead. */
	@Deprecated
	public static Type getComponentType(final Type type) {
		return Types.component(type);
	}

	/**
	 * @deprecated Use {@link Types#component} and {@link Types#raw} instead.
	 */
	@Deprecated
	public static Class<?> getComponentClass(final Type type) {
		return Types.raw(Types.component(type));
	}

	/** @deprecated Use {@link Types#type(Field, Class)} instead. */
	@Deprecated
	public static Type getFieldType(final Field field, final Class<?> type) {
		return Types.type(field, type);
	}

	/**
	 * @deprecated Use {@link Types#type(Field, Class)} and {@link Types#raws}
	 *             instead.
	 */
	@Deprecated
	public static List<Class<?>> getFieldClasses(final Field field,
		final Class<?> type)
	{
		return Types.raws(Types.type(field, type));
	}

	/** @deprecated Use {@link Types#returnType} instead. */
	@Deprecated
	public static Type getMethodReturnType(final Method method,
		final Class<?> type)
	{
		return Types.returnType(method, type);
	}

	/**
	 * @deprecated Use {@link Types#returnType} and {@link Types#raws} instead.
	 */
	@Deprecated
	public static List<Class<?>> getMethodReturnClasses(final Method method,
		final Class<?> type)
	{
		return Types.raws(Types.returnType(method, type));
	}

	/** @deprecated Use {@link Types#param} instead. */
	@Deprecated
	public static Type getTypeParameter(final Type type, final Class<?> c,
		final int paramNo)
	{
		return Types.param(type, c, paramNo);
	}

}
