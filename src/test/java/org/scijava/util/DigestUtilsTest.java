/*
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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Tests {@link DigestUtils}.
 *
 * @author Curtis Rueden
 */
public class DigestUtilsTest {

	private static final byte[] COFFEE_SHA1 = { -71, 2, 27, -126, -23, -70, -89,
		35, -65, -15, -108, 66, 72, 113, 29, -32, -12, -42, -49, 6 };

	private static final byte[] COFFEE_MD5 = { -39, -98, 9, -40, -39, 44, 31, -62,
		23, 9, 38, 101, 85, -57, 121, -110 };

	private static final byte[] HELLO_WORLD_SHA1 = { 123, 80, 44, 58, 31, 72, -56,
		96, -102, -30, 18, -51, -5, 99, -99, -18, 57, 103, 63, 94 };

	private static final String HELLO_WORLD_SHA1_HEX =
		"7b502c3a1f48c8609ae212cdfb639dee39673f5e";

	private static final String COFFEE_SHA1_HEX =
		"b9021b82e9baa723bff1944248711de0f4d6cf06";

	private static final String HELLO_WORLD_SHA1_BASE64 =
		"e1AsOh9IyGCa4hLN+2Od7jlnP14=";

	private static final String COFFEE_SHA1_BASE64 =
		"uQIbgum6pyO/8ZRCSHEd4PTWzwY=";

	/** Tests {@link DigestUtils#bytes(String)}. */
	@Test
	public void testBytesString() {
		final String s = "Hello world";
		final byte[] bytes = DigestUtils.bytes(s);
		final byte[] expected = { 72, 101, 108, 108, 111, 32, 119, 111, 114, 108,
			100 };
		assertArrayEquals(expected, bytes);
	}

	/** Tests {@link DigestUtils#bytes(int)}. */
	@Test
	public void testBytesInt() {
		final byte[] bytes = DigestUtils.bytes(0xc0ffee);
		final byte[] expected = { 0, -64, -1, -18 };
		assertArrayEquals(expected, bytes);
	}

	/** Tests {@link DigestUtils#hex(byte[])}. */
	@Test
	public void testHex() {
		assertEquals("00c0ffee", DigestUtils.hex(DigestUtils.bytes(0xc0ffee)));
		assertEquals("deadbeef", DigestUtils.hex(DigestUtils.bytes(0xdeadbeef)));
		assertEquals("00000000", DigestUtils.hex(DigestUtils.bytes(0x00000000)));
		assertEquals("ffffffff", DigestUtils.hex(DigestUtils.bytes(0xffffffff)));
	}

	/** Tests {@link DigestUtils#base64(byte[])}. */
	@Test
	public void testBase64() {
		assertEquals("AMD/7g==", DigestUtils.base64(DigestUtils.bytes(0xc0ffee)));
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
		final byte[] bytes = DigestUtils.bytes(0xc0ffee);
		final byte[] sha1 = DigestUtils.sha1(bytes);
		assertArrayEquals(COFFEE_SHA1, sha1);
	}

	/** Tests {@link DigestUtils#md5(byte[])}. */
	@Test
	public void testMD5() {
		final byte[] bytes = DigestUtils.bytes(0xc0ffee);
		final byte[] md5 = DigestUtils.md5(bytes);
		assertArrayEquals(COFFEE_MD5, md5);
	}

	/** Tests {@link DigestUtils#digest(String, byte[])}. */
	@Test
	public void testDigest() {
		final byte[] bytes = DigestUtils.bytes(0xc0ffee);

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
		final byte[] bytes = DigestUtils.bytes(0xc0ffee);
		final byte[] best = DigestUtils.best(bytes);
		assertArrayEquals(COFFEE_SHA1, best);
	}

	/** Tests {@link DigestUtils#bestHex(String)}. */
	@Test
	public void testBestHexString() {
		assertEquals(HELLO_WORLD_SHA1_HEX, DigestUtils.bestHex("Hello world"));
	}

	/** Tests {@link DigestUtils#hex(byte[])}. */
	@Test
	public void testBestHexBytes() {
		final byte[] bytes = DigestUtils.bytes(0xc0ffee);
		assertEquals(COFFEE_SHA1_HEX, DigestUtils.bestHex(bytes));
	}

	/** Tests {@link DigestUtils#bestBase64(String)}. */
	@Test
	public void testBestBase64String() {
		assertEquals(HELLO_WORLD_SHA1_BASE64, DigestUtils.bestBase64(
			"Hello world"));
	}

	/** Tests {@link DigestUtils#bestBase64(byte[])}. */
	@Test
	public void testBestBase64Bytes() {
		final byte[] bytes = DigestUtils.bytes(0xc0ffee);
		assertEquals(COFFEE_SHA1_BASE64, DigestUtils.bestBase64(bytes));
	}

}
