/*
 * #%L
 * SciJava Common shared library for SciJava software.
 * %%
 * Copyright (C) 2009 - 2014 Board of Regents of the University of
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

package org.scijava.module;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import org.junit.Test;
import org.scijava.Context;

/**
 * Tests {@link ModuleService}.
 * 
 * @author Curtis Rueden
 */
public class ModuleServiceTest {

	@Test
	public void testGetSingleInput() throws ModuleException {
		final Context context = new Context(ModuleService.class);
		final ModuleService moduleService = context.getService(ModuleService.class);

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
			// TODO Auto-generated method stub
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

	}

}
