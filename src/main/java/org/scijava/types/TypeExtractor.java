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

package org.scijava.types;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;

import org.scijava.plugin.Plugin;
import org.scijava.plugin.SingletonPlugin;
import org.scijava.util.Types;

/**
 * A plugin for extracting generic {@link Type} from instances at runtime.
 * <p>
 * This is an extensible way to achieve quasi-preservation of generic types at
 * runtime, for types which do not normally support it.
 * </p>
 * <p>
 * Type extractor plugins discoverable at runtime must implement this interface
 * and be annotated with @{@link Plugin} with attribute {@link Plugin#type()
 * type} = {@link TypeExtractor}.class.
 * </p>
 * 
 * @author Curtis Rueden
 */
public interface TypeExtractor<T> extends SingletonPlugin {

	/**
	 * Extracts the generic type of the given object.
	 * 
	 * @return The object's generic {@link Type}, or {@code null} if the object is
	 *         not supported by this extractor.
	 */
	default ParameterizedType reify(final T o) {
		final TypeVariable<Class<T>>[] typeVars = getRawType().getTypeParameters();
		if (typeVars.length == 0) {
			throw new IllegalStateException("Class " + getRawType().getName() +
				" is not a parameterized type");
		}
		final Type[] types = new Type[typeVars.length];
		for (int i = 0; i < types.length; i++) {
			types[i] = reify(o, i);
			if (types[i] == null) types[i] = Types.wildcard();
		}
		return Types.parameterize(getRawType(), types);
	}

	/**
	 * Extracts the generic type of the given object's Nth type parameter, with
	 * respect to the class handled by this type extractor.
	 * 
	 * @param o Object for which the type should be reified.
	 * @param n Index of the type parameter whose type should be extracted.
	 * @return The reified Nth type parameter, or {@code null} if the extractor
	 *         cannot process the object.
	 * @throws IndexOutOfBoundsException if {@code n} is less than 0, or greater
	 *           than {@code getType().getTypeParameters().length}.
	 * @throws UnsupportedOperationException if the supported class does not have
	 *           any type parameters.
	 */
	default Type reify(final T o, final int n) {
		final Type type = reify(o);
		if (!(type instanceof ParameterizedType)) {
			throw new UnsupportedOperationException("Not a parameterized type");
		}
		final ParameterizedType pType = (ParameterizedType) type;
		return pType.getActualTypeArguments()[n];
	}

	/** Gets the {@link Class} handled by this type extractor. */
	Class<T> getRawType();
}
