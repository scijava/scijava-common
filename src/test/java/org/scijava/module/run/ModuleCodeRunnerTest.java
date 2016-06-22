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

package org.scijava.module.run;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.scijava.Context;
import org.scijava.ItemIO;
import org.scijava.module.DefaultMutableModule;
import org.scijava.module.DefaultMutableModuleInfo;
import org.scijava.module.DefaultMutableModuleItem;
import org.scijava.module.ModuleService;

/**
 * Tests {@link ModuleCodeRunner}.
 * 
 * @author Curtis Rueden
 */
public class ModuleCodeRunnerTest {

	private Context context;
	private ModuleCodeRunner runner;

	@Before
	public void setUp() {
		context = new Context(ModuleService.class);
		context.service(ModuleService.class).addModule(new AlphabetModuleInfo());
		runner = new ModuleCodeRunner();
		context.inject(runner);
	}

	@After
	public void tearDown() {
		context.dispose();
	}

	@Test
	public void testRunList() throws InvocationTargetException {
		final StringBuilder sb = new StringBuilder();
		runner.run("module:" + AlphabetModule.class.getName(), //
			"buffer", sb, "length", 3);
		assertEquals("ABC", sb.toString());
	}

	@Test
	public void testRunMap() throws InvocationTargetException {
		final StringBuilder sb = new StringBuilder();
		final Map<String, Object> inputMap = new HashMap<>();
		inputMap.put("buffer", sb);
		inputMap.put("length", 4);
		runner.run("module:" + AlphabetModule.class.getName(), inputMap);
		assertEquals("ABCD", sb.toString());
	}

	@Test
	public void testSupports() {
		assertTrue(runner.supports("module:" + AlphabetModule.class.getName()));

		assertFalse(runner.supports("module:" + getClass().getName()));
	}

	// -- Helper classes --

	/** A module that writes the alphabet into a buffer. */
	public static class AlphabetModule extends DefaultMutableModule {

		@Override
		public AlphabetModuleInfo getInfo() { return new AlphabetModuleInfo(); }

		@Override
		public void run() {
			final StringBuilder sb = (StringBuilder) getInput("buffer");
			final int length = (Integer) getInput("length");
			sb.setLength(0);
			for (int i = 0; i < length; i++) {
				final char letter = (char) ('A' + i);
				sb.append(letter);
			}
		}
	}

	/** Module metadata for {@link AlphabetModule}. */
	public static class AlphabetModuleInfo extends DefaultMutableModuleInfo {

		public AlphabetModuleInfo() {
			// So much fun to construct modules by hand! Who needs commands?
			setModuleClass(AlphabetModule.class);
			final DefaultMutableModuleItem<StringBuilder> bufferItem =
				new DefaultMutableModuleItem<>(this, "buffer",
					StringBuilder.class);
			bufferItem.setIOType(ItemIO.BOTH);
			addInput(bufferItem);
			addInput(new DefaultMutableModuleItem<>(this, "length",
				int.class));
		}

	}

}
