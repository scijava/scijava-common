/*
 * #%L
 * SciJava Common shared library for SciJava software.
 * %%
 * Copyright (C) 2009 - 2017 Board of Regents of the University of
 * Wisconsin-Madison, Broad Institute of MIT and Harvard, Max Planck
 * Institute of Molecular Cell Biology and Genetics, University of
 * Konstanz, and KNIME GmbH.
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

package org.scijava.convert;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import java.io.File;

import org.junit.Test;
import org.scijava.convert.FileListConverters.FileArrayToStringConverter;
import org.scijava.convert.FileListConverters.FileToStringConverter;
import org.scijava.convert.FileListConverters.StringToFileArrayConverter;
import org.scijava.convert.FileListConverters.StringToFileConverter;

public class FileListConverterTest {

	@Test
	public void testStringToFileConverter() {
		final StringToFileConverter conv = new StringToFileConverter();
		final String path = "C:\\temp\\f,i;l-ename.txt";
		assertTrue("Cannot convert from String to File",
				conv.canConvert(String.class, File.class));
		assertFalse("Can erroneously convert from String to File[]",
				conv.canConvert(String.class, File[].class));
		assertEquals(new File(path),
				conv.convert(path, File.class));
	}

	@Test
	public void testStringToFileArrayConverter() {
		final StringToFileArrayConverter conv = new StringToFileArrayConverter();
		final String path = "\"C:\\temp\\f,i;l-ename.txt\",C:\\temp";
		assertTrue("Cannot convert from String to File[]",
				conv.canConvert(String.class, File[].class));
		assertFalse("Can erroneously convert from String to File",
				conv.canConvert(String.class, File.class));
		assertEquals("Wrong array length", 2,
				conv.convert(path, File[].class).length);
		assertEquals("Wrong file name", new File("C:\\temp\\f,i;l-ename.txt"),
				conv.convert(path, File[].class)[0]);
		assertEquals("Wrong file name", new File("C:\\temp"),
				conv.convert(path, File[].class)[1]);
	}

	@Test
	public void testFileToStringConverter() {
		final FileToStringConverter conv = new FileToStringConverter();
		final File file = new File("C:\\temp\\f,i;l-ename.txt");
		assertTrue("Cannot convert from File to String",
				conv.canConvert(File.class, String.class));
		assertFalse("Can erroneously convert from File[] to String",
				conv.canConvert(File[].class, String.class));
		assertEquals(file.getAbsolutePath(),
				conv.convert(file, String.class));
	}

	@Test
	public void testFileArrayToStringConverter() {
		final FileArrayToStringConverter conv = new FileArrayToStringConverter();
		final File[] fileArray = new File[2];
		fileArray[0] = new File("C:\\temp\\f,i;l-ename.txt");
		fileArray[1] = new File("C:\\temp");
		final String expected = "\"" + fileArray[0].getAbsolutePath() + "\"," + fileArray[1].getAbsolutePath();
		assertTrue("Cannot convert from File[] to String",
				conv.canConvert(File[].class, String.class));
		assertFalse("Can erroneously convert from File to String",
				conv.canConvert(File.class, String.class));
		assertEquals("Wrong output string", expected,
				conv.convert(fileArray, String.class));
	}
}
