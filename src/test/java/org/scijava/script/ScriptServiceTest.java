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

package org.scijava.script;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.List;

import org.junit.Test;
import org.scijava.Context;
import org.scijava.util.AppUtils;

/**
 * Tests the {@link DefaultScriptService}.
 * 
 * @author Curtis Rueden
 */
public class ScriptServiceTest {

	/**
	 * Tests that the "scijava.scripts.path" system property is handled correctly.
	 */
	@Test
	public void testSystemProperty() {
		final String slash = File.separator;
		final String sep = File.pathSeparator;
		final String dir1 = slash + "foo" + slash + "bar";
		final String dir2 = slash + "to" + slash + "the" + slash + "moon";
		System.setProperty("scijava.scripts.path", dir1 + sep + dir2);

		final Context context = new Context(ScriptService.class);
		final ScriptService scriptService = context.service(ScriptService.class);

		final List<File> scriptDirs = scriptService.getScriptDirectories();
		assertEquals(3, scriptDirs.size());

		final File baseDir = AppUtils.getBaseDirectory(ScriptService.class);
		final String dir0 = baseDir.getPath() + slash + "scripts";
		assertEquals(dir0, scriptDirs.get(0).getAbsolutePath());
		assertEquals(dir1, scriptDirs.get(1).getAbsolutePath());
		assertEquals(dir2, scriptDirs.get(2).getAbsolutePath());
	}

}
