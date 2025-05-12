/*-
 * #%L
 * SciJava Common shared library for SciJava software.
 * %%
 * Copyright (C) 2009 - 2025 SciJava developers.
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

package org.scijava.util;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Tests for {@link PropertiesHelper}
 */
public class PropertiesHelperTest {

	private static final String EXPECTED_1 = "a=b";
	private static final String EXPECTED_2 = "hello=goodbye";

	private Map<String, String> props;
	private File temp;

	@Before
	public void setup() throws IOException {
		temp = File.createTempFile("PropertiesHelper", "txt");
		props = new HashMap<>();
		props.put("a", "b");
		props.put("hello", "goodbye");
	}

	@After
	public void cleanup() {
		temp.delete();
	}

	@Test
	public void testWrite() throws IOException {
		PropertiesHelper.put(props, temp);

		int count = 0;
		boolean saw1 = false, saw2 = false;

		try (BufferedReader reader = new BufferedReader(new FileReader(temp))) {
			String line;
			while ((line = reader.readLine()) != null) {
				if (line.equals(EXPECTED_1)) {
					saw1 = true;
				}
				if (line.equals(EXPECTED_2)) {
					saw2 = true;
				}
				count++;
			}
		}
		assertTrue(saw1);
		assertTrue(saw2);
		assertEquals(2, count);
	}

	@Test
	public void testRead() throws IOException {
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(temp))) {
			writer.write(EXPECTED_1);
			writer.newLine();
			writer.write(EXPECTED_2);
			writer.newLine();
		}

		Map<String, String> propsMap = PropertiesHelper.get(temp);
		assertTrue(props.equals(propsMap));
	}

	@Test
	public void testIO() throws IOException {
		PropertiesHelper.put(props, temp);
		Map<String, String> propsMap = PropertiesHelper.get(temp);
		assertTrue(props.equals(propsMap));
	}

	@Test
	public void testMultipleEquals() throws IOException {
		props.clear();
		final String K = "hello", V = "world=true";
		props.put(K, V);
		PropertiesHelper.put(props, temp);
		Map<String, String> propsMap = PropertiesHelper.get(temp);
		assertEquals(1, propsMap.size());
		assertEquals(props.get(K), propsMap.get(K));
	}

	@Test
	public void testOverwrite() throws IOException {
		PropertiesHelper.put(props, temp);
		props.put("myname", "jonas");
		PropertiesHelper.put(props, temp);
		Map<String, String> loadedProps = PropertiesHelper.get(temp);
		assertEquals(3, loadedProps.size());
		int count = 0;
		try (BufferedReader reader = new BufferedReader(new FileReader(temp))) {
			String line;
			while ((line = reader.readLine()) != null) {
				count++;
			}
		}
		assertEquals(3, count);
	}
}
