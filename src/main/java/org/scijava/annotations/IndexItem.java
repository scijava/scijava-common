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

package org.scijava.annotations;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Map;

/**
 * Allows access to individual annotations.
 * 
 * @author Johannes Schindelin
 */
public class IndexItem<A extends Annotation> {

	private final Class<A> annotation;
	private final ClassLoader loader;
	private final String className;
	private final Map<Object, Object> map;

	IndexItem(final Class<A> annotation, final ClassLoader loader,
		final String className, final Map<Object, Object> map)
	{
		this.annotation = annotation;
		this.loader = loader;
		this.className = className;
		this.map = map;
	}

	/**
	 * Obtains the annotation values.
	 * 
	 * @return the annotation values
	 */
	public A annotation() {
		return proxy(annotation, loader, className, map);
	}

	/**
	 * Returns the name of the annotated class.
	 * 
	 * @return the name of the annotated class.
	 */
	public String className() {
		return className;
	}

	@SuppressWarnings("unchecked")
	private static <A extends Annotation> A proxy(final Class<A> annotation,
		final ClassLoader loader, final String className,
		final Map<Object, Object> map)
	{
		return (A) Proxy.newProxyInstance(loader, new Class<?>[] { annotation },
			new InvocationHandler() {

				@Override
				public Object invoke(Object proxy, Method method, Object[] args)
					throws Throwable
				{
					if (map.containsKey(method)) {
						return map.get(method);
					}
					final String name = method.getName();
					Object value;
					if (map.containsKey(name)) {
						value = map.get(name);
						final Class<?> expectedType = method.getReturnType();
						if (!expectedType.isAssignableFrom(value.getClass())) {
							value = adapt(value, loader, expectedType, className);
						}
					}
					else if (name.equals("toString") &&
						(args == null || args.length == 0))
					{
						value = "@" + annotation.getName() + map;
					}
					else if (name.equals("annotationType") &&
						(args == null || args.length == 0))
					{
						value = annotation;
					}
					else if (name.equals("hashCode") &&
						(args == null || args.length == 0))
					{
						return annotation.hashCode() ^ map.hashCode();
					}
					else if (name.equals("equals") && args != null && args.length == 1) {
						if (!(args[0] instanceof Annotation) ||
							((Annotation) args[0]).annotationType() != annotation)
						{
							return false;
						}
						for (Method method2 : annotation.getMethods()) {
							if (method2.getDeclaringClass() == annotation) {
								if (!invoke(proxy, method2, new Object[0]).equals(
									method2.invoke(args[0])))
								{
									return false;
								}
							}
						}
						return true;
					}
					else {
						value = method.getDefaultValue();
						if (value == null) {
							throw new IllegalArgumentException("Could not find value for " +
								name);
						}
					}
					map.put(method, value);
					return value;
				}
			});
	}

	private static Object adapt(final Object o, final ClassLoader loader,
		final Class<?> expectedType, final String className)
	{
		if (o == null) {
			return null;
		}
		else if (expectedType.isAssignableFrom(o.getClass())) {
			return o;
		}
		else if (o instanceof Boolean) {
			return (boolean) (Boolean) o;
		}
		else if (o instanceof Long) {
			long l = (Long) o;
			if (expectedType == Byte.TYPE) {
				return (byte) l;
			}
			else if (expectedType == Short.TYPE) {
				return (short) l;
			}
			else if (expectedType == Integer.TYPE) {
				return (int) l;
			}
			else if (expectedType == Long.TYPE) {
				return l;
			}
		}
		else if (o instanceof Double) {
			double d = (Double) o;
			if (expectedType == Float.TYPE) {
				return (float) d;
			}
			return d;
		}
		else if (expectedType == Character.TYPE) {
			String s = (String) o;
			if (s.length() == 1) {
				return s.charAt(0);
			}
		}
		else if (expectedType == Class.class) {
			try {
				return loader.loadClass((String) o);
			}
			catch (Throwable t) {
				throw cce(o, expectedType, className, t);
			}
		}
		else if (expectedType.isArray()) {
			@SuppressWarnings("unchecked")
			List<Object> list = (List<Object>) o;
			Class<?> type = expectedType.getComponentType();
			int length = list.size();
			Object array = Array.newInstance(type, length);
			for (int i = 0; i < length; i++) {
				Array.set(array, i, adapt(list.get(i), loader, type, className));
			}
			return array;
		}
		else if (Enum.class.isAssignableFrom(expectedType)) {
			@SuppressWarnings("unchecked")
			final Map<Object, Object> map = (Map<Object, Object>) o;
			final String enumName = (String) map.get("enum");
			final String constName = (String) map.get("value");
			try {
				return loader.loadClass(enumName).getField(constName).get(null);
			}
			catch (Throwable t) {
				throw cce(o, expectedType, className, t);
			}
		}
		else if (Annotation.class.isAssignableFrom(expectedType)) {
			@SuppressWarnings("unchecked")
			final Class<Annotation> annotation = (Class<Annotation>) expectedType;
			@SuppressWarnings("unchecked")
			final Map<Object, Object> map = (Map<Object, Object>) o;
			return proxy(annotation, loader, className, map);
		}
		throw cce(o, expectedType, className, null);
	}

	private static ClassCastException cce(final Object o,
		final Class<?> expectedType, final String className, final Throwable cause)
	{
		final String oType = o == null ? "<null>" : o.getClass().getName();
		final String eType =
			expectedType == null ? "<null>" : expectedType.getName();
		final ClassCastException cce =
			new ClassCastException(className + ": cannot cast object of type " +
				oType + " to " + eType);
		if (cause != null) cce.initCause(cause);
		return cce;
	}

}
