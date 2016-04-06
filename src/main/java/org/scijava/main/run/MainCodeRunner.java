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

package org.scijava.main.run;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

import org.scijava.Priority;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.run.AbstractCodeRunner;
import org.scijava.run.CodeRunner;
import org.scijava.util.ClassUtils;

/**
 * Executes the given class's {@code main} method.
 * 
 * @author Curtis Rueden
 */
@Plugin(type = CodeRunner.class, priority = Priority.LOW_PRIORITY)
public class MainCodeRunner extends AbstractCodeRunner {

	@Parameter(required = false)
	private LogService log;

	// -- CodeRunner methods --

	@Override
	public void run(final Object code, final Object... args)
		throws InvocationTargetException
	{
		final Object[] sArgs = stringify(args);
		try {
			getMain(code).invoke(null, new Object[] { sArgs });
		}
		catch (final IllegalArgumentException exc) {
			throw new InvocationTargetException(exc);
		}
		catch (final IllegalAccessException exc) {
			throw new InvocationTargetException(exc);
		}
	}

	@Override
	public void run(final Object code, final Map<String, Object> inputMap)
		throws InvocationTargetException
	{
		throw new UnsupportedOperationException(
			"Cannot execute main method with a map of inputs");
	}

	// -- Typed methods --

	@Override
	public boolean supports(final Object code) {
		return getMain(code) != null;
	}

	// -- Helper methods --

	private Method getMain(final Object code) {
		final Class<?> c = getClass(code);
		if (c == null) return null;
		try {
			return c.getMethod("main", String[].class);
		}
		catch (final SecurityException exc) {
			if (log != null) log.debug(exc);
			return null;
		}
		catch (final NoSuchMethodException exc) {
			if (log != null) log.debug(exc);
			return null;
		}
	}

	private Class<?> getClass(final Object code) {
		if (code instanceof Class) return (Class<?>) code;
		if (code instanceof String) return ClassUtils.loadClass((String) code);
		return null;
	}

	/** Ensures each element is a {@link String}. */
	private String[] stringify(final Object... o) {
		final String[] s;
		if (o == null) s = null;
		else {
			s = new String[o.length];
			for (int i = 0; i < o.length; i++) {
				if (o[i] == null) s[i] = null;
				else if (o[i] instanceof String) s[i] = (String) o[i];
				else s[i] = o[i].toString();
			}
		}
		return s;
	}

}
