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

import static org.junit.Assert.assertEquals;

import java.lang.reflect.InvocationTargetException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.scijava.Context;
import org.scijava.plugin.Plugin;

/**
 * Tests {@link RunService}.
 *
 * @author Curtis Rueden
 */
public class RunServiceTest {

	private RunService runService;

	@Before
	public void setUp() {
		runService = new Context().service(RunService.class);
	}

	@After
	public void tearDown() {
		runService.context().dispose();
	}

	/** Tests {@link RunService#run(Object, Object...)}. */
	@Test
	public void testRunList() throws InvocationTargetException {
		final StringBuilder sb = new StringBuilder();
		runService.run(sb, "foo", "bar", "fu bar");
		assertEquals("|foo|bar|fu bar|", sb.toString());
	}

	/** Tests {@link RunService#run(Object, Object...)}. */
	@Test
	public void testRunMap() throws InvocationTargetException {
		final StringBuilder sb = new StringBuilder();
		final Map<String, Object> inputMap = new LinkedHashMap<>();
		inputMap.put("foo", "bar");
		inputMap.put("animal", "quick brown fox");
		inputMap.put("number", 33);
		runService.run(sb, inputMap);
		assertEquals("|foo=bar|animal=quick brown fox|number=33|", sb.toString());
	}

	// -- Helper classes --

	/** A {@link CodeRunner} that stringifies its arguments. */
	@Plugin(type = CodeRunner.class)
	public static class StringRunner extends AbstractCodeRunner {

		@Override
		public void run(final Object code, final Object... args)
			throws InvocationTargetException
		{
			final StringBuilder sb = getStringBuilder(code);
			sb.append("|");
			for (final Object arg : args) {
				sb.append(arg);
				sb.append("|");
			}
		}

		@Override
		public void run(final Object code, final Map<String, Object> inputMap)
			throws InvocationTargetException
		{
			final StringBuilder sb = getStringBuilder(code);
			sb.append("|");
			for (final String key : inputMap.keySet()) {
				sb.append(key);
				sb.append("=");
				sb.append(inputMap.get(key));
				sb.append("|");
			}
		}

		@Override
		public boolean supports(final Object code) {
			return getStringBuilder(code) != null;
		}

		private StringBuilder getStringBuilder(final Object code) {
			return code instanceof StringBuilder ? (StringBuilder) code : null;
		}
	}

}
