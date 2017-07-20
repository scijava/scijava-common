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

package org.scijava.util;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Tests {@link DigestUtils}.
 *
 * @author Curtis Rueden
 */
public class DigestUtilsTest {

	private static final byte[] CAFEBABE_SHA1 = { 20, 101, -38, -47, 38, -45, 43,
		-9, -86, 93, 59, -107, -91, -57, -61, 49, -51, -1, 52, -33 };

	private static final byte[] CAFEBABE_MD5 = { 45, 27, -67, -30, -84, -84, 10,
		-3, 7, 100, 109, -104, 21, 79, 64, 46 };

	private static final byte[] HELLO_WORLD_SHA1 = { 123, 80, 44, 58, 31, 72,
		-56, 96, -102, -30, 18, -51, -5, 99, -99, -18, 57, 103, 63, 94 };

	private static final String HELLO_WORLD_SHA1_HEX =
		"7b502c3a1f48c8609ae212cdfb639dee39673f5e";

	private static final String CAFEBABE_SHA1_HEX =
		"1465dad126d32bf7aa5d3b95a5c7c331cdff34df";

	private static final String HELLO_WORLD_SHA1_BASE64 =
		"e1AsOh9IyGCa4hLN+2Od7jlnP14=";

	private static final String CAFEBABE_SHA1_BASE64 =
		"FGXa0SbTK/eqXTuVpcfDMc3/NN8=";

	/** Tests {@link DigestUtils#bytes(String)}. */
	@Test
	public void testBytesString() {
		final String s = "Hello world";
		final byte[] bytes = DigestUtils.bytes(s);
		final byte[] expected =
			{ 72, 101, 108, 108, 111, 32, 119, 111, 114, 108, 100 };
		assertArrayEquals(expected, bytes);
	}

	/** Tests {@link DigestUtils#bytes(int)}. */
	@Test
	public void testBytesInt() {
		final byte[] bytes = DigestUtils.bytes(0xcafebabe);
		final byte[] expected = { -54, -2, -70, -66 };
		assertArrayEquals(expected, bytes);
	}

	/** Tests {@link DigestUtils#hex(byte[])}. */
	@Test
	public void testHex() {
		assertEquals("cafebabe", DigestUtils.hex(DigestUtils.bytes(0xcafebabe)));
		assertEquals("deadbeef", DigestUtils.hex(DigestUtils.bytes(0xdeadbeef)));
		assertEquals("00000000", DigestUtils.hex(DigestUtils.bytes(0x00000000)));
		assertEquals("ffffffff", DigestUtils.hex(DigestUtils.bytes(0xffffffff)));
	}

	/** Tests {@link DigestUtils#base64(byte[])}. */
	@Test
	public void testBase64() {
		assertEquals("yv66vg==", DigestUtils.base64(DigestUtils.bytes(0xcafebabe)));
		assertEquals("3q2+7w==", DigestUtils.base64(DigestUtils.bytes(0xdeadbeef)));
		assertEquals("AAAAAA==", DigestUtils.base64(DigestUtils.bytes(0x00000000)));
		assertEquals("/////w==", DigestUtils.base64(DigestUtils.bytes(0xffffffff)));
	}

	/** Tests {@link DigestUtils#hash(String)}. */
	@Test
	public void testHashString() {
		final byte[] hash = DigestUtils.hash("Hello world");
		final byte[] expected = { -50, 89, -118, -92 };
		assertArrayEquals(expected, hash);
	}

	/** Tests {@link DigestUtils#hash(byte[])}. */
	@Test
	public void testHashBytes() {
		final byte[] bytes = DigestUtils.bytes("Hello world");
		final byte[] hash = DigestUtils.hash(bytes);
		final byte[] expected = { -50, 89, -118, -92 };
		assertArrayEquals(expected, hash);
	}

	/** Tests {@link DigestUtils#sha1(byte[])}. */
	@Test
	public void testSHA1() {
		final byte[] bytes = DigestUtils.bytes(0xcafebabe);
		final byte[] sha1 = DigestUtils.sha1(bytes);
		assertArrayEquals(CAFEBABE_SHA1, sha1);
	}

	/** Tests {@link DigestUtils#md5(byte[])}. */
	@Test
	public void testMD5() {
		final byte[] bytes = DigestUtils.bytes(0xcafebabe);
		final byte[] md5 = DigestUtils.md5(bytes);
		assertArrayEquals(CAFEBABE_MD5, md5);
	}

	/** Tests {@link DigestUtils#digest(String, byte[])}. */
	@Test
	public void testDigest() {
		final byte[] bytes = DigestUtils.bytes(0xcafebabe);

		final byte[] sha1 = DigestUtils.digest("SHA-1", bytes);
		final byte[] expectedSHA1 = DigestUtils.sha1(bytes);
		assertArrayEquals(expectedSHA1, sha1);

		final byte[] md5 = DigestUtils.digest("MD5", bytes);
		final byte[] expectedMD5 = DigestUtils.md5(bytes);
		assertArrayEquals(expectedMD5, md5);
	}

	/** Tests {@link DigestUtils#best(String)}. */
	@Test
	public void testBestString() {
		final byte[] best = DigestUtils.best("Hello world");
		assertArrayEquals(HELLO_WORLD_SHA1, best);
	}

	/** Tests {@link DigestUtils#best(byte[])}. */
	@Test
	public void testBestBytes() {
		final byte[] bytes = DigestUtils.bytes(0xcafebabe);
		final byte[] best = DigestUtils.best(bytes);
		assertArrayEquals(CAFEBABE_SHA1, best);
	}

	/** Tests {@link DigestUtils#bestHex(String)}. */
	@Test
	public void testBestHexString() {
		assertEquals(HELLO_WORLD_SHA1_HEX, DigestUtils.bestHex("Hello world"));
	}

	/** Tests {@link DigestUtils#hex(byte[])}. */
	@Test
	public void testBestHexBytes() {
		final byte[] bytes = DigestUtils.bytes(0xcafebabe);
		assertEquals(CAFEBABE_SHA1_HEX, DigestUtils.bestHex(bytes));
	}

	/** Tests {@link DigestUtils#bestBase64(String)}. */
	@Test
	public void testBestBase64String() {
		assertEquals(HELLO_WORLD_SHA1_BASE64, DigestUtils.bestBase64("Hello world"));
	}

	/** Tests {@link DigestUtils#bestBase64(byte[])}. */
	@Test
	public void testBestBase64Bytes() {
		final byte[] bytes = DigestUtils.bytes(0xcafebabe);
		assertEquals(CAFEBABE_SHA1_BASE64, DigestUtils.bestBase64(bytes));
	}

}
