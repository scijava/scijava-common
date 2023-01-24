/*-
 * #%L
 * SciJava Common shared library for SciJava software.
 * %%
 * Copyright (C) 2009 - 2023 SciJava developers.
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
package org.scijava.script.process;

import static org.junit.Assert.*;

import java.io.StringReader;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.scijava.Context;
import org.scijava.script.ScriptInfo;

public class ParameterScriptProcessorTest {

	private Context context;

	@Before
	public void setUp() {
		context = new Context();
	}

	@After
	public void tearDown() {
		context.dispose();
	}

	@Test
	public void testScriptParameterParsing() {
		String script = "" + //
				"% @String legacyStyleParameter\n" +
				"% #@ String commentedHeaderParameter\n" +
				"% ############## Some Comment ###########\n" +
				"#@ String implicitInputParameter\n" +
				"#@input String explicitInputParameter\n" +
				"\n" +
				"% @String legacyStyleBodyParameter\n" +
				"% #@ String commentedBodyParameter\n" +
				"\n" +
				"#@output implicitlyTypedOutputParameter\n" +
				"#@output String explicitlyTypedOutputParameter\n";
		final ScriptInfo info = new ScriptInfo(context, ".bsizes", new StringReader(script));
		assertEquals("legacyStyleParameter", info.getInput("legacyStyleParameter").getName());
		assertEquals("implicitInputParameter", info.getInput("implicitInputParameter").getName());
		assertEquals("explicitInputParameter", info.getInput("explicitInputParameter").getName());

		assertEquals("implicitlyTypedOutputParameter", info.getOutput("implicitlyTypedOutputParameter").getName());
		assertEquals("explicitlyTypedOutputParameter", info.getOutput("explicitlyTypedOutputParameter").getName());

		assertNull(info.getInput("commentedHeaderParameter"));
		assertNull(info.getInput("legacyStyleBodyParameter"));
		assertNull(info.getInput("commentedBodyParameter"));
	}

}
