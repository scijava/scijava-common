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

package org.scijava.run;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import org.scijava.plugin.HandlerService;
import org.scijava.service.SciJavaService;

/**
 * Interface for service that manages available {@link CodeRunner} plugins.
 * 
 * @author Curtis Rueden
 */
public interface RunService extends
	HandlerService<Object, CodeRunner>, SciJavaService
{

	/**
	 * Executes the given code using the most appropriate handler, passing the
	 * specified arguments as inputs.
	 */
	default void run(final Object code, final Object... args)
		throws InvocationTargetException
	{
		for (final CodeRunner runner : getInstances()) {
			if (runner.supports(code)) {
				runner.run(code, args);
				return;
			}
		}
		throw new IllegalArgumentException("Unknown code type: " + code);
	}

	/**
	 * Executes the given code using the most appropriate handler, passing the
	 * arguments in the specified map as inputs.
	 */
	default void run(final Object code, final Map<String, Object> inputMap)
		throws InvocationTargetException
	{
		for (final CodeRunner runner : getInstances()) {
			if (runner.supports(code)) {
				runner.run(code, inputMap);
				return;
			}
		}
		throw new IllegalArgumentException("Unknown code type: " + code);
	}

	// -- PTService methods --

	@Override
	default Class<CodeRunner> getPluginType() {
		return CodeRunner.class;
	}

	// -- Typed methods --

	@Override
	default Class<Object> getType() {
		return Object.class;
	}
}
