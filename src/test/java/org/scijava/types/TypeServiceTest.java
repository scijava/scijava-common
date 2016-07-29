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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.scijava.Context;
import org.scijava.types.ExampleTypes.Bag;
import org.scijava.types.ExampleTypes.BlueThing;
import org.scijava.types.ExampleTypes.RedThing;

/**
 * Tests {@link TypeService}, including core {@link TypeExtractor}
 * implementations.
 *
 * @author Curtis Rueden
 */
public class TypeServiceTest {

	private TypeService types;

	@Before
	public void setUp() {
		types = new Context(TypeService.class).service(TypeService.class);
	}

	@After
	public void tearDown() {
		if (types != null) {
			types.context().dispose();
			types = null;
		}
	}

	/** Tests type extraction for non-generic objects. */
	@Test
	public void testClass() {
		final Type stringType = types.reify("Hello");
		assertEquals(String.class, stringType);
	}

	/** Tests type extraction for {@code null} objects. */
	@Test
	public void testNull() {
		final Type nullType = types.reify(null);
		assertNull(nullType);
	}

	/** Tests type extraction for {@link Nil} objects. */
	@Test
	public void testNil() {
		final Nil<List<Float>> nilFloatList = new Nil<List<Float>>() {};
		final Type nilFloatListType = types.reify(nilFloatList);
		assertEquals(nilFloatList.getType(), nilFloatListType);
	}

	/** Tests type extraction for {@link GenericTyped} objects. */
	@Test
	public void testGenericTyped() {
		final Object numberThing = new GenericTyped() {

			@Override
			public Type getType() {
				return Number.class;
			}
		};
		assertEquals(Number.class, types.reify(numberThing));
	}

	/** Tests type extraction for {@link Iterable} objects. */
	@Test
	public void testIterable() {
		final List<String> stringList = //
			new ArrayList<>(Collections.singletonList("Hi"));
		final Type stringListType = types.reify(stringList);
		assertEquals(new Nil<ArrayList<String>>() {}.getType(), stringListType);
	}

	/** Tests type extraction for {@link Map} objects. */
	@Test
	public void testMap() {
		final Map<String, Integer> mapSI = //
			new HashMap<>(Collections.singletonMap("Curtis", 37));
		final Type mapSIType = types.reify(mapSI);
		assertEquals(new Nil<HashMap<String, Integer>>() {}.getType(), mapSIType);
	}

	/** Tests nested type extraction of a complex object. */
	@Test
	public void testNested() {
		// List of organization test scores.
		// For each organization, we have a table of students by name.
		final List<Map<String, List<Integer>>> testScores = new ArrayList<>();

		final Map<String, List<Integer>> hogwarts = new HashMap<>();
		hogwarts.put("Hermione", new ArrayList<>(Arrays.asList(100, 99, 101)));
		hogwarts.put("Ron", new ArrayList<>(Arrays.asList(45, 56, 82)));
		testScores.add(hogwarts);

		final Map<String, List<Integer>> highlights = new HashMap<>();
		highlights.put("Goofus", new ArrayList<>(Arrays.asList(12, 0, 23)));
		highlights.put("Gallant", new ArrayList<>(Arrays.asList(87, 92, 96)));
		testScores.add(highlights);

		final Type testScoresType = types.reify(testScores);
		assertEquals(new Nil<ArrayList<HashMap<String, ArrayList<Integer>>>>() {}
			.getType(), testScoresType);
	}

	/**
	 * Tests type extraction for recursively typed objects (e.g.,
	 * {@code F extends Foo<F>}.
	 */
	@Test
	public void testRecursiveTyping() {
		final Bag<BlueThing> blueBag = new Bag<>();
		blueBag.add(new BlueThing());

		final Type blueBagType = types.reify(blueBag);
		assertEquals(new Nil<Bag<BlueThing>>() {}.getType(), blueBagType);

		final Bag<RedThing> redBag = new Bag<>();
		redBag.add(new RedThing());

		final Type redBagType = types.reify(redBag);
		assertEquals(new Nil<Bag<RedThing>>() {}.getType(), redBagType);
	}
}
