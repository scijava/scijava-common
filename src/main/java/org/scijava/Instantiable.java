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

package org.scijava;

/**
 * An interface declaring the ability to create objects.
 * 
 * @param <T> The type of objects that can be created.
 * @author Curtis Rueden
 */
public interface Instantiable<T> {

	/**
	 * Gets the fully qualified name of the {@link Class} of the objects that can
	 * be created.
	 */
	String getClassName();

	/**
	 * Loads the class corresponding to the objects that are created by
	 * {@link #createInstance()}.
	 * <p>
	 * Note that this class may not be precisely {@code T.class} but instead a
	 * subclass thereof.
	 * </p>
	 * 
	 * @see org.scijava.plugin.PluginInfo for an example of an
	 *      {@code Instantiable} type that typically instantiates objects of a
	 *      subtype of {@code T} rather than {@code T} itself.
	 */
	Class<? extends T> loadClass() throws InstantiableException;

	/** Creates an object. */
	T createInstance() throws InstantiableException;

}
