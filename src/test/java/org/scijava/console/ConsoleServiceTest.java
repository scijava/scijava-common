/*
 * #%L
 * SciJava Common shared library for SciJava software.
 * %%
 * Copyright (C) 2009 - 2015 Board of Regents of the University of
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

package org.scijava.console;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.LinkedList;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.scijava.Context;
import org.scijava.Priority;
import org.scijava.plugin.Plugin;

/**
 * Tests {@link ConsoleService}.
 *
 * @author Curtis Rueden
 */
public class ConsoleServiceTest {

	private ConsoleService consoleService;

	@Before
	public void setUp() {
		consoleService = new Context().service(ConsoleService.class);
	}

	@After
	public void tearDown() {
		consoleService.context().dispose();
	}

	/** Tests {@link ConsoleService#processArgs(String...)}. */
	@Test
	public void testProcessArgs() {
		consoleService.processArgs("--foo", "--bar");
		assertTrue(consoleService.getInstance(FooArgument.class).argsHandled);
	}

	// -- Helper classes --

	@Plugin(type = ConsoleArgument.class, priority = Priority.HIGH_PRIORITY)
	public static class FooArgument extends AbstractConsoleArgument {

		private boolean argsHandled;

		@Override
		public void handle(final LinkedList<String> args) {
			assertNotNull(args);
			assertEquals(2, args.size());
			assertEquals("--foo", args.get(0));
			assertEquals("--bar", args.get(1));
			args.clear();
			argsHandled = true;
		}

	}

}
