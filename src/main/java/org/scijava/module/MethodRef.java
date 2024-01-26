/*
 * #%L
 * SciJava Common shared library for SciJava software.
 * %%
 * Copyright (C) 2009 - 2024 SciJava developers.
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

package org.scijava.module;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.scijava.Validated;
import org.scijava.ValidityProblem;

/**
 * A reference to a {@link Method}, which can be invoked at will.
 * 
 * @author Curtis Rueden
 */
public class MethodRef implements Validated {

	private final Method method;
	private final String label;

	/** List of problems when initializing the method reference. */
	private final List<ValidityProblem> problems =
		new ArrayList<>();

	public MethodRef(final Class<?> clazz, final String methodName,
		final Class<?>... params)
	{
		method = findMethod(clazz, methodName, params);
		if (method == null) label = null;
		else label = clazz.getName() + "#" + method.getName();
	}

	public void execute(final Object obj, final Object... args)
		throws MethodCallException
	{
		if (method == null) return;
		try {
			method.invoke(obj, args);
		}
		catch (final Exception exc) {
			// NB: Several types of exceptions; simpler to handle them all the same.
			throw new MethodCallException("Error executing method: " + label, exc);
		}
	}

	private Method findMethod(final Class<?> clazz, final String methodName,
		final Class<?>... params)
	{
		if (clazz == null) return null;
		if (methodName == null || methodName.isEmpty()) return null;
		for (Class<?> c = clazz; c != null; c = c.getSuperclass()) {
			try {
				final Method m = c.getDeclaredMethod(methodName, params);
				m.setAccessible(true);
				return m;
			}
			catch (final NoSuchMethodException e) {
				// NB: Continue to loop into super class methods.
			}
			catch (final Exception e) {
				// NB: Multiple types of exceptions; handle them all the same.
				break;
			}
		}
		final String problem =
			"Method not found: " + clazz.getName() + "#" + methodName;
		problems.add(new ValidityProblem(problem));
		return null;
	}

	// -- Validated methods --

	@Override
	public boolean isValid() {
		return problems.isEmpty();
	}

	@Override
	public List<ValidityProblem> getProblems() {
		return Collections.unmodifiableList(problems);
	}

}
