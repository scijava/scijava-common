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

import org.scijava.Identifiable;
import org.scijava.plugin.HandlerPlugin;
import org.scijava.plugin.Plugin;

/**
 * A plugin which extends the {@link RunService}'s execution handling. A
 * {@link CodeRunner} knows how to execute code of a certain form, such as the
 * {@code main} method of a Java {@link Class}, or an {@link Identifiable}
 * SciJava module.
 * <p>
 * Code runner plugins discoverable at runtime must implement this interface
 * and be annotated with @{@link Plugin} with attribute {@link Plugin#type()} =
 * {@link CodeRunner}.class. While it possible to create a class runner plugin
 * merely by implementing this interface, it is encouraged to instead extend
 * {@link AbstractCodeRunner}, for convenience.
 * </p>
 * 
 * @author Curtis Rueden
 */
public interface CodeRunner extends HandlerPlugin<Object> {

	/**
	 * Executes the code identified by the given object, passing the
	 * specified arguments as inputs.
	 */
	void run(Object code, Object... args) throws InvocationTargetException;

	/**
	 * Executes the code identified by the given object, passing the arguments in
	 * the specified map as inputs.
	 */
	void run(Object code, Map<String, Object> inputMap)
		throws InvocationTargetException;

	// -- Typed methods --

	@Override
	default Class<Object> getType() {
		return Object.class;
	}
}
