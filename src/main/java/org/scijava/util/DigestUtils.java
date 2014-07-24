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

package org.scijava.util;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.xml.bind.DatatypeConverter;

/**
 * Utility class for computing cryptographic hashes.
 * 
 * @author Curtis Rueden
 * @author Johannes Schindelin
 */
public final class DigestUtils {

	private final static char[] hex = { '0', '1', '2', '3', '4', '5', '6', '7',
		'8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };

	private DigestUtils() {
		// NB: Prevent instantiation of utility class.
	}

	/**
	 * Converts the given byte array to a string. UTF-8 encoding is used if
	 * available, with the platform's default encoding as a fallback.
	 */
	public static String string(final byte[] bytes) {
		try {
			return new String(bytes, "UTF-8");
		}
		catch (final UnsupportedEncodingException exc) {
			return new String(bytes);
		}
	}

	/**
	 * Converts the given string to a byte array. UTF-8 encoding is used if
	 * available, with the platform's default encoding as a fallback.
	 */
	public static byte[] bytes(final String s) {
		try {
			return s.getBytes("UTF-8");
		}
		catch (final UnsupportedEncodingException exc) {
			return s.getBytes();
		}
	}

	/** Converts the given integer into a byte array. */
	public static byte[] bytes(final int i) {
		return new byte[] {
			(byte) (0xff & (i >>> 24)),
			(byte) (0xff & (i >>> 16)),
			(byte) (0xff & (i >>> 8)),
			(byte) (0xff & i),
		};
	}

	/** Converts the given byte array to a hexidecimal string. */
	public static String hex(final byte[] bytes) {
		final char[] buffer = new char[bytes.length * 2];
		for (int i = 0; i < bytes.length; i++) {
			buffer[i * 2] = hex[(bytes[i] & 0xf0) >> 4];
			buffer[i * 2 + 1] = hex[bytes[i] & 0xf];
		}
		return new String(buffer);
	}

	/** Converts the given byte array to a base64 string. */
	public static String base64(final byte[] bytes) {
		return DatatypeConverter.printBase64Binary(bytes);
	}

	/**
	 * Gets the Java hash code of the given string, as a byte array.
	 * 
	 * @see String#hashCode()
	 */
	public static byte[] hash(final String s) {
		return bytes(s.hashCode());
	}

	/**
	 * Gets the hash code of the given byte array, as a byte array.
	 * 
	 * @see String#hashCode()
	 * @see #string(byte[])
	 */
	public static byte[] hash(final byte[] bytes) {
		// NB: We cannot use the hash code of the byte array directly,
		// because it is not deterministic. Primitive byte arrays use the
		// default Object implementation of hashCode(), which differs for
		// every object instance. We need a consistent hash code, so we
		// convert to String first, which fulfills this requirement.
		return hash(string(bytes));
	}

	/** Gets the given byte array's SHA-1 checksum, or null if unavailable. */
	public static byte[] sha1(final byte[] bytes) {
		return digest("SHA-1", bytes);
	}

	/** Gets the given byte array's MD5 checksum, or null if unavailable. */
	public static byte[] md5(final byte[] bytes) {
		return digest("MD5", bytes);
	}

	/**
	 * Gets the given byte array's hash value according to the specified
	 * algorithm.
	 * 
	 * @param algorithm The algorithm to use when generating the hash value.
	 * @param bytes The byte array for which to compute the hash value.
	 * @return The computed hash value, or null if the digest algorithm is not
	 *         available.
	 * @see MessageDigest
	 */
	public static byte[] digest(final String algorithm, final byte[] bytes) {
		try {
			final MessageDigest digest = MessageDigest.getInstance(algorithm);
			digest.update(bytes);
			return digest.digest();
		}
		catch (final NoSuchAlgorithmException exc) {
			return null;
		}
	}

	/**
	 * Gets the given string's best available hash. Tries SHA-1 first, then MD5,
	 * then Java hash code.
	 * 
	 * @see #bytes(String)
	 * @see #sha1(byte[])
	 * @see #md5(byte[])
	 * @see #hash(String)
	 */
	public static byte[] best(final String s) {
		final byte[] bytes = bytes(s);
		byte[] best = sha1(bytes);
		if (best == null) best = md5(bytes);
		if (best == null) best = hash(s);
		return best;
	}

	/**
	 * Gets the given byte array's best available hash. Tries SHA-1 first, then
	 * MD5, then Java hash code.
	 * 
	 * @see #sha1(byte[])
	 * @see #md5(byte[])
	 * @see #hash(byte[])
	 */
	public static byte[] best(final byte[] bytes) {
		byte[] best = sha1(bytes);
		if (best == null) best = md5(bytes);
		if (best == null) best = hash(bytes);
		return best;
	}

	/**
	 * Gets the hex string of the given string's best available hash.
	 * 
	 * @see #best(String)
	 * @see #hex(byte[])
	 */
	public static String bestHex(final String text) {
		return hex(best(text));
	}

	/**
	 * Gets the hex string of the given byte array's best available hash.
	 * 
	 * @see #best(byte[])
	 * @see #hex(byte[])
	 */
	public static String bestHex(final byte[] bytes) {
		return hex(best(bytes));
	}

	/**
	 * Gets the base64 string of the given string's best available hash.
	 * 
	 * @see #best(String)
	 * @see #base64(byte[])
	 */
	public static String bestBase64(final String text) {
		return base64(best(text));
	}

	/**
	 * Gets the base64 string of the given byte array's best available hash.
	 * 
	 * @see #best(byte[])
	 * @see #base64(byte[])
	 */
	public static String bestBase64(final byte[] bytes) {
		return base64(best(bytes));
	}

}
