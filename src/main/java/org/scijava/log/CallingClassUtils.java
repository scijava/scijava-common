/*
 * #%L
 * SciJava Common shared library for SciJava software.
 * %%
 * Copyright (C) 2009 - 2025 SciJava developers.
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

package org.scijava.log;

import org.scijava.Context;

/**
 * Utility class for getting the calling class of a method.
 *
 * @author Matthias Arzt
 */

@IgnoreAsCallingClass
public final class CallingClassUtils {

	private CallingClassUtils() {
		// prevent instantiation of utility class
	}

	/**
	 * Inspects the stack trace to return the name of the class that calls this
	 * method, but ignores every class annotated with @IgnoreAsCallingClass.
	 * <p>
	 * If every class on the stack trace is annotated, then the class at the root
	 * of the stack trace is returned.
	 */
	public static String getCallingClassName() {
		StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
		for (int i = 1; i < stackTrace.length - 2; i++) {
			String className = stackTrace[i].getClassName();
			if (!hasIgnoreAsCallingClassAnnotation(className)) return className;
		}
		return stackTrace[stackTrace.length - 1].getClassName();
	}

	private static boolean hasIgnoreAsCallingClassAnnotation(String className) {
		try {
			Class<?> clazz = Context.getClassLoader().loadClass(className);
			return clazz.isAnnotationPresent(IgnoreAsCallingClass.class);
		}
		catch (ClassNotFoundException ignore) {
			return false;
		}
	}

	/**
	 * @deprecated Use {@link #getCallingClassName()} instead. Warning: This
	 *             method throws a IllegalStateException as soon as it comes
	 *             across a class that can't be loaded with the default class
	 *             loader. Inspects the stack trace to return the class that calls
	 *             this method, but ignores every class annotated
	 *             with @IgnoreAsCallingClass.
	 * @throws IllegalStateException if every method on the stack, is in a class
	 *           annotated with @IgnoreAsCallingClass.
	 */
	@Deprecated
	public static Class<?> getCallingClass() {
		try {
			StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
			for (int i = 1; i < stackTrace.length - 1; i++) {
				Class<?> clazz = Class.forName(stackTrace[i].getClassName());
				if (!clazz.isAnnotationPresent(IgnoreAsCallingClass.class))
					return clazz;
			}
		}
		catch (ClassNotFoundException ignore) {}
		throw new IllegalStateException();
	}

}
