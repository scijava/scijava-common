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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.scijava.Context;
import org.scijava.parse.ParseService;

/**
 * Tests {@link ModuleService}.
 * 
 * @author Curtis Rueden
 */
public class ModuleServiceTest {

	private ModuleService moduleService;

	@Before
	public void setUp() {
		final Context context = new Context(ModuleService.class, ParseService.class);
		moduleService = context.service(ModuleService.class);
	}

	@After
	public void tearDown() {
		moduleService.context().dispose();
	}

	/** Tests {@link ModuleService#run(ModuleInfo, boolean, Object...)}. */
	@Test
	public void testRunModuleInfoArray() throws InterruptedException,
		ExecutionException
	{
		final ModuleInfo info = new FooModuleInfo();
		final Module m = moduleService.run(info, false, createInputArray()).get();
		assertEquals(expectedResult(), m.getOutput("result"));
	}

	/** Tests {@link ModuleService#run(ModuleInfo, boolean, Map)}. */
	@Test
	public void testRunModuleInfoMap() throws InterruptedException,
		ExecutionException
	{
		final ModuleInfo info = new FooModuleInfo();
		final Module m = moduleService.run(info, false, createInputMap()).get();
		assertEquals(expectedResult(), m.getOutput("result"));
	}

	/** Tests {@link ModuleService#run(Module, boolean, Object...)}. */
	@Test
	public void testRunModuleArray() throws ModuleException, InterruptedException,
		ExecutionException
	{
		final ModuleInfo info = new FooModuleInfo();
		final Module module = info.createModule();
		final Module m = moduleService.run(module, false, createInputArray()).get();
		assertSame(module, m);
		assertEquals(expectedResult(), m.getOutput("result"));
	}

	/** Tests {@link ModuleService#run(Module, boolean, Map)}. */
	@Test
	public void testRunModuleMap() throws ModuleException, InterruptedException,
		ExecutionException
	{
		final ModuleInfo info = new FooModuleInfo();
		final Module module = info.createModule();
		final Module m = moduleService.run(module, false, createInputMap()).get();
		assertSame(module, m);
		assertEquals(expectedResult(), m.getOutput("result"));
	}

	/**
	 * Tests that {@link ModuleService#run(ModuleInfo, boolean, Object...)} and
	 * {@link ModuleService#run(Module, boolean, Object...)} intelligently handle
	 * a single-element {@link Object} array consisting of a {@code Map<String,
	 * Object>}.
	 * <p>
	 * This situation can happen e.g. due to Jython choosing the wrong overloaded
	 * {@code run} method. We correct for the issue on our side, for convenience.
	 * </p>
	 */
	@Test
	public void testRunMapHack() throws ModuleException, InterruptedException,
		ExecutionException
	{
		final ModuleInfo info = new FooModuleInfo();
		final Object[] inputs = new Object[] { createInputMap() };
		final Module m = moduleService.run(info, false, inputs).get();
		assertEquals(expectedResult(), m.getOutput("result"));

		final Module module = info.createModule();
		final Module m2 = moduleService.run(module, false, inputs).get();
		assertEquals(expectedResult(), m2.getOutput("result"));
	}

	@Test
	public void testGetSingleInput() throws ModuleException {
		final ModuleInfo info = new FooModuleInfo();
		final Module module = info.createModule();

		// verify single string input is detected
		final ModuleItem<String> singleString =
			moduleService.getSingleInput(module, String.class);
		assertSame(info.getInput("string"), singleString);

		// check that non-autofilled inputs are not detected
		final ModuleItem<Float> singleFloat =
			moduleService.getSingleInput(module, Float.class);
		assertNull(singleFloat);

		// verify that multiple inputs of the same type are not detected
		final ModuleItem<Integer> singleInteger =
			moduleService.getSingleInput(module, Integer.class);
		assertNull(singleInteger);

		// verify that single input is detected if there are
		// non-autofilled inputs of the same kind too
		final ModuleItem<Double> singleDouble =
			moduleService.getSingleInput(module, Double.class);
		assertSame(info.getInput("double2"), singleDouble);
	}

	@Test
	public void testSaveAndLoad() {
		List<Object> objects = Arrays.asList( //
			new double[] {}, //
			new double[] { 1., 2., 3. }, //
			new Double[] {}, //
			new Double[] { 1., 2., 3. } //
		);
		objects.forEach(this::assertParamSavedAndLoaded);
	}

	private <T> void assertParamSavedAndLoaded(T object) {
		@SuppressWarnings("unchecked")
		Class<T> c = (Class<T>) object.getClass();
		// Get a ModuleItem of the right type
		MutableModule m = new DefaultMutableModule();
		m.getInfo().addInput(new DefaultMutableModuleItem<>(m, "a", c));
		final ModuleItem<T> item = moduleService.getSingleInput(m, c);
		// Save a value to the ModuleItem
		moduleService.save(item, object);
		// Load that value from the ModuleItem
		Object actual = moduleService.load(item);
		// Assert equality
		if (object.getClass().isArray()) assertArrayEquality(object, actual);
		else assertEquals(object, actual);
	}

	private void assertArrayEquality(Object arr1, Object arr2) {
		// Ensure that both Objects are arrays of the same type!
		assertEquals(arr1.getClass(), arr2.getClass());
		assertTrue(arr1.getClass().isArray());

		// We must check primitive arrays as they cannot be cast to Object[]
		if (arr1 instanceof boolean[]) {
			assertArrayEquals((boolean[]) arr1, (boolean[]) arr2);
		}
		else if (arr1 instanceof byte[]) {
			assertArrayEquals((byte[]) arr1, (byte[]) arr2);
		}
		else if (arr1 instanceof short[]) {
			assertArrayEquals((short[]) arr1, (short[]) arr2);
		}
		else if (arr1 instanceof int[]) {
			assertArrayEquals((int[]) arr1, (int[]) arr2);
		}
		else if (arr1 instanceof long[]) {
			assertArrayEquals((long[]) arr1, (long[]) arr2);
		}
		else if (arr1 instanceof float[]) {
			assertArrayEquals((float[]) arr1, (float[]) arr2, 1e-6f);
		}
		else if (arr1 instanceof double[]) {
			assertArrayEquals((double[]) arr1, (double[]) arr2, 1e-6);
		}
		else if (arr1 instanceof char[]) {
			assertArrayEquals((char[]) arr1, (char[]) arr2);
		}
		// Otherwise we can just cast to Object[]
		else assertArrayEquals((Object[]) arr1, (Object[]) arr2);
	}

	// -- Helper methods --

	private Object[] createInputArray() {
		return new Object[] { //
			"string", "hello", //
			"float", 1.234f, //
			"integer1", -2, //
			"integer2", 7, //
			"double1", Math.E, //
			"double2", Math.PI, //
		};
	}

	private Map<String, Object> createInputMap() {
		final Map<String, Object> inputMap = new HashMap<>();
		inputMap.put("string", "hello");
		inputMap.put("float", 1.234f);
		inputMap.put("integer1", -2);
		inputMap.put("integer2", 7);
		inputMap.put("double1", Math.E);
		inputMap.put("double2", Math.PI);
		return inputMap;
	}

	private String expectedResult() {
		return mapToString(createInputMap());
	}

	private static String mapToString(final Map<String, Object> map) {
		final StringBuilder sb = new StringBuilder();
		for (final Entry<String, Object> entry : map.entrySet()) {
			sb.append(entry.getKey() + " = " + entry.getValue() + "\n");
		}
		return sb.toString();
	}

	// -- Helper classes --

	/** A sample module for testing the module service. */
	public static class FooModule extends AbstractModule {

		private final FooModuleInfo info;

		public FooModule(final FooModuleInfo info) {
			this.info = info;
		}

		@Override
		public ModuleInfo getInfo() {
			return info;
		}

		@Override
		public void run() {
			setOutput("result", mapToString(getInputs()));
		}

	}

	/** {@link ModuleInfo} implementation for the {@link FooModule}. */
	public static class FooModuleInfo extends AbstractModuleInfo {

		@Override
		public String getDelegateClassName() {
			return FooModule.class.getName();
		}

		@Override
		public Class<?> loadDelegateClass() throws ClassNotFoundException {
			return FooModule.class;
		}

		@Override
		public Module createModule() throws ModuleException {
			return new FooModule(this);
		}

		@Override
		protected void parseParameters() {
			addInput("string", String.class, true);
			addInput("float", Float.class, false);
			addInput("integer1", Integer.class, true);
			addInput("integer2", Integer.class, true);
			addInput("double1", Double.class, false);
			addInput("double2", Double.class, true);
			addOutput("result", String.class);
		}

		private <T> void addInput(final String name, final Class<T> type,
			final boolean autoFill)
		{
			registerInput(new AbstractModuleItem<T>(this) {

				@Override
				public String getName() {
					return name;
				}

				@Override
				public Class<T> getType() {
					return type;
				}

				@Override
				public boolean isAutoFill() {
					return autoFill;
				}

			});
		}

		private <T> void addOutput(final String name, final Class<T> type) {
			registerOutput(new AbstractModuleItem<T>(this) {

				@Override
				public String getName() {
					return name;
				}

				@Override
				public Class<T> getType() {
					return type;
				}

			});
		}

	}

}
