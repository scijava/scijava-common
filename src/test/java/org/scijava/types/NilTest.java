/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2014 - 2016 Board of Regents of the University of
 * Wisconsin-Madison, University of Konstanz and Brian Northan.
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Collection;
import java.util.List;

import org.junit.Test;
import org.scijava.types.ExampleTypes.Bag;
import org.scijava.types.ExampleTypes.Thing;

/**
 * Tests {@link Nil}.
 *
 * @author Curtis Rueden
 */
public class NilTest {

	@Test
	public <Q extends Thing<Q>> void testType() {
		final Nil<Bag<Q>> nilBag = new Nil<Bag<Q>>() {};

		// Bag<Q>
		final ParameterizedType pType = (ParameterizedType) nilBag.getType();
		assertSame(Bag.class, pType.getRawType());
		final Type pBound = extractSingleBound(pType);

		// Q extends Thing<Q>
		final ParameterizedType qType = (ParameterizedType) pBound;
		final Type qBound = extractSingleBound(qType);
		assertSame(qType, qBound); // recursive type!
	}

	@SuppressWarnings("cast")
	@Test
	public <N extends Number> void testProxy() {
		final List<N> listProxy = new Nil<List<N>>() {}.proxy();
		assertTrue(listProxy instanceof Iterable);
		assertTrue(listProxy instanceof Collection);
		assertTrue(listProxy instanceof List);
		assertTrue(listProxy instanceof GenericTyped);
		final GenericTyped genericTyped = (GenericTyped) listProxy;

		// List<N>
		final ParameterizedType pType = (ParameterizedType) genericTyped.getType();
		assertSame(List.class, pType.getRawType());
		final Type pBound = extractSingleBound(pType);

		// N extends Number
		assertSame(Number.class, pBound);
	}

	@Test
	public <N extends Number> void testCallbacks() {
		final Nil<List<N>> listNil = new Nil<List<N>>() {};
		final List<N> listProxy = listNil.proxy();
		assertEquals(0, listProxy.size()); // default method return value is 0/null
		assertNull(listProxy.iterator());

		final Nil<List<N>> listNil2 = new Nil<List<N>>() {

			@SuppressWarnings("unused")
			public int size() {
				return 21;
			}
		};
		final List<N> listProxy2 = listNil2.proxy();
		assertEquals(21, listProxy2.size()); // overridden method behavior

		final Collection<?> cProxy2 = new Nil<Collection<?>>(listProxy2) {}.proxy();
		assertEquals(21, cProxy2.size()); // behavior preserved by wrapping proxy

		final Collection<?> cProxy3 = new Nil<Collection<?>>(listNil2) {}.proxy();
		assertEquals(21, cProxy3.size()); // behavior preserved by wrapping nil
	}

	// -- Helper methods --

	private Type extractSingleBound(final ParameterizedType pType) {
		final Type[] typeArgs = pType.getActualTypeArguments();
		assertEquals(1, typeArgs.length);
		final Type[] bounds = ((TypeVariable<?>) typeArgs[0]).getBounds();
		assertEquals(1, bounds.length);
		return bounds[0];
	}
}
