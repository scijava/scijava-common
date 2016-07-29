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

import com.google.common.reflect.TypeToken;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.HashMap;
import java.util.Map;

import org.scijava.plugin.SingletonService;
import org.scijava.service.SciJavaService;
import org.scijava.util.Types;

/**
 * Service for working with generic {@link Type}s. This service can extract
 * generic types from objects in an extensible way, via {@link TypeExtractor}
 * plugins. It also provides some methods for reasoning about generic types in
 * general.
 * 
 * @author Curtis Rueden
 */
public interface TypeService extends
	SingletonService<TypeExtractor<?>>, SciJavaService
{

	/** Gets the type extractor which handles the given class, or null if none. */
	<T> TypeExtractor<T> getExtractor(Class<T> c);

	/**
	 * Extracts the generic {@link Type} of the given {@link Object}.
	 * <p>
	 * The ideal goal of the extraction is to reconstitute a fully concrete
	 * generic type, with all type variables fully resolved&mdash;e.g.:
	 * {@code ArrayList<Integer>} rather than a raw {@code ArrayList} class or
	 * even {@code ArrayList<N extends Number>}. Failing that, though, type
	 * variables which are still unknown after analysis will be replaced with
	 * wildcards&mdash;e.g., {@code HashMap} might become
	 * {@code HashMap<String, ?>} if a concrete type for the map values cannot be
	 * determined.
	 * </p>
	 * <p>
	 * For objects whose concrete type has no parameters, this method simply
	 * returns {@code o.getClass()}. For example:
	 * 
	 * <pre>
	 *      StringList implements List&lt;String&gt;
	 * </pre>
	 * 
	 * will return {@code StringList.class}.
	 * <p>
	 * The interesting case is for objects whose concrete class <em>does</em> have
	 * type parameters. E.g.:
	 * 
	 * <pre>
	 *      NumberList&lt;N extends Number&gt; implements List&lt;N&gt;
	 *      ListMap&lt;K, V, T&gt; implements Map&lt;K, V&gt;, List&lt;T&gt;
	 * </pre>
	 * 
	 * For such types, we try to fill the type parameters recursively, using
	 * {@link TypeExtractor} plugins that know how to glean types at runtime from
	 * specific sorts of objects.
	 * </p>
	 * <p>
	 * For example, {@link IterableTypeExtractor} knows how to guess a {@code T}
	 * for any {@code Iterable<T>} by examining the type of the elements in its
	 * iteration. (Of course, this may be inaccurate if the elements in the
	 * iteration are heterogeneously typed, but for many use cases this guess is
	 * better than nothing.)
	 * </p>
	 * <p>
	 * In this way, the behavior of the generic type extraction is fully
	 * extensible, since additional {@link TypeExtractor} plugins can always be
	 * introduced which extract types more intelligently in cases where more
	 * <em>a priori</em> knowledge about that type is available at runtime.
	 * </p>
	 */
	default Type reify(final Object o) {
		if (o == null) return null;
		
		if (o instanceof GenericTyped) {
			// Object implements the GenericTyped interface; it explicitly declares
			// the generic type by which it wants to be known. This makes life easy!
			return ((GenericTyped) o).getType();
		}

		final Class<?> c = o.getClass();
		final TypeVariable<?>[] typeVars = c.getTypeParameters();
		final int numVars = typeVars.length;

		if (numVars == 0) {
			// Object has no generic parameters; we are done!
			return c;
		}

		// Object has parameters which need to be resolved. Let's do it.

		// Here we will store all of our object's resolved type variables.
		final Map<TypeVariable<?>, Type> resolved = new HashMap<>();

		for (final TypeToken<?> token : TypeToken.of(c).getTypes()) {
			if (resolved.size() == numVars) break; // Got 'em all!

			final Type type = token.getType();
			if (!Types.containsTypeVars(type)) {
				// No type variables are buried in this type; it is useless to us!
				continue;
			}

			// Populate relevant type variables from the reified supertype!
			final Map<TypeVariable<?>, Type> vars = //
				args(o, token.getRawType());

			if (vars != null) {
				// Remember any resolved type variables.
				// Note that vars may contain other type variables from other layers
				// of the generic type hierarchy, which we don't care about here.
				for (final TypeVariable<?> typeVar : typeVars) {
					if (vars.containsKey(typeVar)) {
						resolved.putIfAbsent(typeVar, vars.get(typeVar));
					}
				}
			}
		}

		// fill in any remaining unresolved type parameters with wildcards
		for (final TypeVariable<?> typeVar : typeVars) {
			resolved.putIfAbsent(typeVar, Types.wildcard());
		}

		// now apply all the type variables we resolved
		return Types.parameterize(c, resolved);
	}

	/**
	 * Extracts the resolved type variables of the given {@link Object}, as viewed
	 * through the specified supertype.
	 * <p>
	 * For example, if you call:
	 * </p>
	 * 
	 * <pre>
	 * args(Collections.singleton("Hi"), Iterable.class)
	 * </pre>
	 * <p>
	 * Then it returns a map with contents <code>{T: String}</code> by using the
	 * {@link IterableTypeExtractor} to analyze the object.
	 * </p>
	 * <p>
	 * Note that this method only provides results if there is a
	 * {@link TypeExtractor} plugin which handles <em>exactly</em> the given
	 * supertype.
	 * </p>
	 * 
	 * @see #reify(Object)
	 */
	default <T> Map<TypeVariable<?>, Type> args(final Object o,
		final Class<T> superType)
	{
		final Class<?> c = o.getClass();

		final TypeExtractor<T> extractor = getExtractor(superType);
		if (extractor == null) return null; // No plugin for this specific class.

		if (!superType.isInstance(o)) {
			throw new IllegalStateException("'" + o.getClass() +
				"' is not an instance of '" + superType.getName() + "'");
		}
		@SuppressWarnings("unchecked")
		final T t = (T) o;

		final ParameterizedType extractedType = extractor.reify(t);

		// Populate type variables to fully populate the supertype.
		return Types.args(c, extractedType);
	}

	// -- PTService methods --

	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	default Class<TypeExtractor<?>> getPluginType() {
		return (Class) TypeExtractor.class;
	}

}
