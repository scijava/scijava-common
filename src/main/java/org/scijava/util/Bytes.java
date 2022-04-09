/*
 * #%L
 * SciJava Common shared library for SciJava software.
 * %%
 * Copyright (C) 2009 - 2022 SciJava developers.
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

// This class was derived from the loci.common.DataTools class of the
// Bio-Formats library, licensed according to Simplified BSD, as follows:
//
// Copyright (C) 2005 - 2015 Open Microscopy Environment:
//   - Board of Regents of the University of Wisconsin-Madison
//   - Glencoe Software, Inc.
//   - University of Dundee
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions are met:
//
// 1. Redistributions of source code must retain the above copyright notice,
//    this list of conditions and the following disclaimer.
// 2. Redistributions in binary form must reproduce the above copyright notice,
//    this list of conditions and the following disclaimer in the documentation
//    and/or other materials provided with the distribution.
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
// AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
// IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
// ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
// LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
// CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
// SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
// INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
// CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
// ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
// POSSIBILITY OF SUCH DAMAGE.

package org.scijava.util;

/**
 * Useful methods for reading, writing, decoding and converting {@code byte}s
 * and {@code byte} arrays.
 *
 * @author Curtis Rueden
 * @author Melissa Linkert
 * @author Chris Allan
 */
public final class Bytes {

	private Bytes() {
		// NB: prevent instantiation of utility class.
	}

	// -- Word decoding - bytes to primitive types --

	/**
	 * Translates up to the first {@code len} bytes of a {@code byte} array beyond
	 * the given offset to a {@code short}. If there are fewer than {@code len}
	 * bytes available, the MSBs are all assumed to be zero (regardless of
	 * endianness).
	 */
	public static short toShort(final byte[] bytes, final int off, int len,
		final boolean little)
	{
		if (bytes.length - off < len) len = bytes.length - off;
		short total = 0;
		for (int i = 0, ndx = off; i < len; i++, ndx++) {
			total |=
				(bytes[ndx] < 0 ? 256 + bytes[ndx] : (int) bytes[ndx]) << ((little ? i
					: len - i - 1) * 8);
		}
		return total;
	}

	/**
	 * Translates up to the first 2 bytes of a {@code byte} array beyond the given
	 * offset to a {@code short}. If there are fewer than 2 bytes available the
	 * MSBs are all assumed to be zero (regardless of endianness).
	 */
	public static short toShort(final byte[] bytes, final int off,
		final boolean little)
	{
		return toShort(bytes, off, 2, little);
	}

	/**
	 * Translates up to the first 2 bytes of a {@code byte} array to a
	 * {@code short}. If there are fewer than 2 bytes available, the MSBs are all
	 * assumed to be zero (regardless of endianness).
	 */
	public static short toShort(final byte[] bytes, final boolean little) {
		return toShort(bytes, 0, 2, little);
	}

	/**
	 * Translates up to the first {@code len} bytes of a {@code byte} array beyond
	 * the given offset to a {@code short}. If there are fewer than {@code len}
	 * bytes available, the MSBs are all assumed to be zero (regardless of
	 * endianness).
	 */
	public static short toShort(final short[] bytes, final int off, int len,
		final boolean little)
	{
		if (bytes.length - off < len) len = bytes.length - off;
		short total = 0;
		for (int i = 0, ndx = off; i < len; i++, ndx++) {
			total |= bytes[ndx] << ((little ? i : len - i - 1) * 8);
		}
		return total;
	}

	/**
	 * Translates up to the first 2 bytes of a {@code byte} array beyond the given
	 * offset to a {@code short}. If there are fewer than 2 bytes available, the
	 * MSBs are all assumed to be zero (regardless of endianness).
	 */
	public static short toShort(final short[] bytes, final int off,
		final boolean little)
	{
		return toShort(bytes, off, 2, little);
	}

	/**
	 * Translates up to the first 2 bytes of a {@code byte} array to a
	 * {@code short}. If there are fewer than 2 bytes available, the MSBs are all
	 * assumed to be zero (regardless of endianness).
	 */
	public static short toShort(final short[] bytes, final boolean little) {
		return toShort(bytes, 0, 2, little);
	}

	/**
	 * Translates up to the first {@code len} bytes of a {@code byte} array beyond
	 * the given offset to an {@code int}. If there are fewer than {@code len}
	 * bytes available, the MSBs are all assumed to be zero (regardless of
	 * endianness).
	 */
	public static int toInt(final byte[] bytes, final int off, int len,
		final boolean little)
	{
		if (bytes.length - off < len) len = bytes.length - off;
		int total = 0;
		for (int i = 0, ndx = off; i < len; i++, ndx++) {
			total |=
				(bytes[ndx] < 0 ? 256 + bytes[ndx] : (int) bytes[ndx]) << ((little ? i
					: len - i - 1) * 8);
		}
		return total;
	}

	/**
	 * Translates up to the first 4 bytes of a {@code byte} array beyond the given
	 * offset to an {@code int}. If there are fewer than 4 bytes available, the
	 * MSBs are all assumed to be zero (regardless of endianness).
	 */
	public static int toInt(final byte[] bytes, final int off,
		final boolean little)
	{
		return toInt(bytes, off, 4, little);
	}

	/**
	 * Translates up to the first 4 bytes of a {@code byte} array to an
	 * {@code int}. If there are fewer than 4 bytes available, the MSBs are all
	 * assumed to be zero (regardless of endianness).
	 */
	public static int toInt(final byte[] bytes, final boolean little) {
		return toInt(bytes, 0, 4, little);
	}

	/**
	 * Translates up to the first {@code len} bytes of a {@code byte} array beyond
	 * the given offset to an {@code int}. If there are fewer than {@code len}
	 * bytes available, the MSBs are all assumed to be zero (regardless of
	 * endianness).
	 */
	public static int toInt(final short[] bytes, final int off, int len,
		final boolean little)
	{
		if (bytes.length - off < len) len = bytes.length - off;
		int total = 0;
		for (int i = 0, ndx = off; i < len; i++, ndx++) {
			total |= bytes[ndx] << ((little ? i : len - i - 1) * 8);
		}
		return total;
	}

	/**
	 * Translates up to the first 4 bytes of a {@code byte} array beyond the given
	 * offset to an {@code int}. If there are fewer than 4 bytes available, the
	 * MSBs are all assumed to be zero (regardless of endianness).
	 */
	public static int toInt(final short[] bytes, final int off,
		final boolean little)
	{
		return toInt(bytes, off, 4, little);
	}

	/**
	 * Translates up to the first 4 bytes of a {@code byte} array to an
	 * {@code int}. If there are fewer than 4 bytes available, the MSBs are all
	 * assumed to be zero (regardless of endianness).
	 */
	public static int toInt(final short[] bytes, final boolean little) {
		return toInt(bytes, 0, 4, little);
	}

	/**
	 * Translates up to the first {@code len} bytes of a {@code byte} array beyond
	 * the given offset to a {@code float}. If there are fewer than {@code len}
	 * bytes available, the MSBs are all assumed to be zero (regardless of
	 * endianness).
	 */
	public static float toFloat(final byte[] bytes, final int off, final int len,
		final boolean little)
	{
		return Float.intBitsToFloat(toInt(bytes, off, len, little));
	}

	/**
	 * Translates up to the first 4 bytes of a {@code byte} array beyond a given
	 * offset to a {@code float}. If there are fewer than 4 bytes available, the
	 * MSBs are all assumed to be zero (regardless of endianness).
	 */
	public static float toFloat(final byte[] bytes, final int off,
		final boolean little)
	{
		return toFloat(bytes, off, 4, little);
	}

	/**
	 * Translates up to the first 4 bytes of a {@code byte} array to a
	 * {@code float}. If there are fewer than 4 bytes available, the MSBs are all
	 * assumed to be zero (regardless of endianness).
	 */
	public static float toFloat(final byte[] bytes, final boolean little) {
		return toFloat(bytes, 0, 4, little);
	}

	/**
	 * Translates up to the first {@code len} bytes of a {@code byte} array beyond
	 * a given offset to a {@code float}. If there are fewer than {@code len}
	 * bytes available, the MSBs are all assumed to be zero (regardless of
	 * endianness).
	 */
	public static float toFloat(final short[] bytes, final int off,
		final int len, final boolean little)
	{
		return Float.intBitsToFloat(toInt(bytes, off, len, little));
	}

	/**
	 * Translates up to the first 4 bytes of a {@code byte} array beyond a given
	 * offset to a {@code float}. If there are fewer than 4 bytes available, the
	 * MSBs are all assumed to be zero (regardless of endianness).
	 */
	public static float toFloat(final short[] bytes, final int off,
		final boolean little)
	{
		return toInt(bytes, off, 4, little);
	}

	/**
	 * Translates up to the first 4 bytes of a {@code byte} array to a
	 * {@code float}. If there are fewer than 4 bytes available, the MSBs are all
	 * assumed to be zero (regardless of endianness).
	 */
	public static float toFloat(final short[] bytes, final boolean little) {
		return toInt(bytes, 0, 4, little);
	}

	/**
	 * Translates up to the first {@code len} bytes of a {@code byte} array beyond
	 * the given offset to a {@code long}. If there are fewer than {@code len}
	 * bytes available, the MSBs are all assumed to be zero (regardless of
	 * endianness).
	 */
	public static long toLong(final byte[] bytes, final int off, int len,
		final boolean little)
	{
		if (bytes.length - off < len) len = bytes.length - off;
		long total = 0;
		for (int i = 0, ndx = off; i < len; i++, ndx++) {
			total |=
				(bytes[ndx] < 0 ? 256L + bytes[ndx] : (long) bytes[ndx]) << ((little
					? i : len - i - 1) * 8);
		}
		return total;
	}

	/**
	 * Translates up to the first 8 bytes of a {@code byte} array beyond the given
	 * offset to a {@code long}. If there are fewer than 8 bytes available, the
	 * MSBs are all assumed to be zero (regardless of endianness).
	 */
	public static long toLong(final byte[] bytes, final int off,
		final boolean little)
	{
		return toLong(bytes, off, 8, little);
	}

	/**
	 * Translates up to the first 8 bytes of a {@code byte} array to a
	 * {@code long}. If there are fewer than 8 bytes available, the MSBs are all
	 * assumed to be zero (regardless of endianness).
	 */
	public static long toLong(final byte[] bytes, final boolean little) {
		return toLong(bytes, 0, 8, little);
	}

	/**
	 * Translates up to the first {@code len} bytes of a {@code byte} array beyond
	 * the given offset to a {@code long}. If there are fewer than {@code len}
	 * bytes available, the MSBs are all assumed to be zero (regardless of
	 * endianness).
	 */
	public static long toLong(final short[] bytes, final int off, int len,
		final boolean little)
	{
		if (bytes.length - off < len) len = bytes.length - off;
		long total = 0;
		for (int i = 0, ndx = off; i < len; i++, ndx++) {
			total |= ((long) bytes[ndx]) << ((little ? i : len - i - 1) * 8);
		}
		return total;
	}

	/**
	 * Translates up to the first 8 bytes of a {@code byte} array beyond the given
	 * offset to a {@code long}. If there are fewer than 8 bytes available, the
	 * MSBs are all assumed to be zero (regardless of endianness).
	 */
	public static long toLong(final short[] bytes, final int off,
		final boolean little)
	{
		return toLong(bytes, off, 8, little);
	}

	/**
	 * Translates up to the first 8 bytes of a {@code byte} array to a
	 * {@code long}. If there are fewer than 8 bytes available, the MSBs are all
	 * assumed to be zero (regardless of endianness).
	 */
	public static long toLong(final short[] bytes, final boolean little) {
		return toLong(bytes, 0, 8, little);
	}

	/**
	 * Translates up to the first {@code len} bytes of a {@code byte} array beyond
	 * the given offset to a {@code double}. If there are fewer than {@code len}
	 * bytes available, the MSBs are all assumed to be zero (regardless of
	 * endianness).
	 */
	public static double toDouble(final byte[] bytes, final int off,
		final int len, final boolean little)
	{
		return Double.longBitsToDouble(toLong(bytes, off, len, little));
	}

	/**
	 * Translates up to the first 8 bytes of a {@code byte} array beyond the given
	 * offset to a {@code double}. If there are fewer than 8 bytes available, the
	 * MSBs are all assumed to be zero (regardless of endianness).
	 */
	public static double toDouble(final byte[] bytes, final int off,
		final boolean little)
	{
		return toDouble(bytes, off, 8, little);
	}

	/**
	 * Translates up to the first 8 bytes of a {@code byte} array to a
	 * {@code double}. If there are fewer than 8 bytes available, the MSBs are all
	 * assumed to be zero (regardless of endianness).
	 */
	public static double toDouble(final byte[] bytes, final boolean little) {
		return toDouble(bytes, 0, 8, little);
	}

	/**
	 * Translates up to the first {@code len} bytes of a {@code byte} array beyond
	 * the given offset to a {@code double}. If there are fewer than {@code len}
	 * bytes available, the MSBs are all assumed to be zero (regardless of
	 * endianness).
	 */
	public static double toDouble(final short[] bytes, final int off,
		final int len, final boolean little)
	{
		return Double.longBitsToDouble(toLong(bytes, off, len, little));
	}

	/**
	 * Translates up to the first 8 bytes of a {@code byte} array beyond the given
	 * offset to a {@code double}. If there are fewer than 8 bytes available, the
	 * MSBs are all assumed to be zero (regardless of endianness).
	 */
	public static double toDouble(final short[] bytes, final int off,
		final boolean little)
	{
		return toDouble(bytes, off, 8, little);
	}

	/**
	 * Translates up to the first 8 bytes of a {@code byte} array to a
	 * {@code double}. If there are fewer than 8 bytes available, the MSBs are all
	 * assumed to be zero (regardless of endianness).
	 */
	public static double toDouble(final short[] bytes, final boolean little) {
		return toDouble(bytes, 0, 8, little);
	}

	// -- Word decoding - primitive types to bytes --

	/** Translates the {@code short} value into an array of two {@code byte}s. */
	public static byte[] fromShort(final short value, final boolean little) {
		final byte[] v = new byte[2];
		unpack(value, v, 0, 2, little);
		return v;
	}

	/** Translates the {@code int} value into an array of four {@code byte}s. */
	public static byte[] fromInt(final int value, final boolean little) {
		final byte[] v = new byte[4];
		unpack(value, v, 0, 4, little);
		return v;
	}

	/** Translates the {@code float} value into an array of four {@code byte}s. */
	public static byte[] fromFloat(final float value, final boolean little) {
		final byte[] v = new byte[4];
		unpack(Float.floatToIntBits(value), v, 0, 4, little);
		return v;
	}

	/** Translates the {@code long} value into an array of eight {@code byte}s. */
	public static byte[] fromLong(final long value, final boolean little) {
		final byte[] v = new byte[8];
		unpack(value, v, 0, 8, little);
		return v;
	}

	/** Translates the {@code double} value into an array of eight {@code byte}s. */
	public static byte[] fromDouble(final double value, final boolean little) {
		final byte[] v = new byte[8];
		unpack(Double.doubleToLongBits(value), v, 0, 8, little);
		return v;
	}

	/**
	 * Translates an array of {@code short} values into an array of {@code byte}
	 * values.
	 */
	public static byte[] fromShorts(final short[] values, final boolean little) {
		final byte[] v = new byte[values.length * 2];
		for (int i = 0; i < values.length; i++) {
			unpack(values[i], v, i * 2, 2, little);
		}
		return v;
	}

	/**
	 * Translates an array of {@code int} values into an array of {@code byte}
	 * values.
	 */
	public static byte[] fromInts(final int[] values, final boolean little) {
		final byte[] v = new byte[values.length * 4];
		for (int i = 0; i < values.length; i++) {
			unpack(values[i], v, i * 4, 4, little);
		}
		return v;
	}

	/**
	 * Translates an array of {@code float} values into an array of {@code byte}
	 * values.
	 */
	public static byte[] fromFloats(final float[] values, final boolean little) {
		final byte[] v = new byte[values.length * 4];
		for (int i = 0; i < values.length; i++) {
			unpack(Float.floatToIntBits(values[i]), v, i * 4, 4, little);
		}
		return v;
	}

	/**
	 * Translates an array of {@code long} values into an array of {@code byte}
	 * values.
	 */
	public static byte[] fromLongs(final long[] values, final boolean little) {
		final byte[] v = new byte[values.length * 8];
		for (int i = 0; i < values.length; i++) {
			unpack(values[i], v, i * 8, 8, little);
		}
		return v;
	}

	/**
	 * Translates an array of {@code double} values into an array of {@code byte}
	 * values.
	 */
	public static byte[] fromDoubles(final double[] values, final boolean little)
	{
		final byte[] v = new byte[values.length * 8];
		for (int i = 0; i < values.length; i++) {
			unpack(Double.doubleToLongBits(values[i]), v, i * 8, 8, little);
		}
		return v;
	}

	/**
	 * Translates {@code nBytes} of the given {@code long} and places the result
	 * in the given {@code byte} array.
	 *
	 * @throws IllegalArgumentException if the specified indices fall outside the
	 *           buffer
	 */
	public static void unpack(final long value, final byte[] buf, final int ndx,
		final int nBytes, final boolean little)
	{
		if (buf.length < ndx + nBytes) {
			throw new IllegalArgumentException("Invalid indices: buf.length=" +
				buf.length + ", ndx=" + ndx + ", nBytes=" + nBytes);
		}
		if (little) {
			for (int i = 0; i < nBytes; i++) {
				buf[ndx + i] = (byte) ((value >> (8 * i)) & 0xff);
			}
		}
		else {
			for (int i = 0; i < nBytes; i++) {
				buf[ndx + i] = (byte) ((value >> (8 * (nBytes - i - 1))) & 0xff);
			}
		}
	}

	/**
	 * Converts a {@code byte} array to the appropriate 1D primitive type array.
	 *
	 * @param b Byte array to convert.
	 * @param bpp Denotes the number of bytes in the returned primitive type (e.g.
	 *          if bpp == 2, we should return an array of type {@code short}).
	 * @param fp If set and bpp == 4 or bpp == 8, then return {@code float}s or
	 *          {@code double}s.
	 * @param little Whether {@code byte} array is in little-endian order.
	 */
	public static Object makeArray(final byte[] b, final int bpp,
		final boolean fp, final boolean little)
	{
		if (bpp == 1) {
			return b;
		}
		else if (bpp == 2) {
			final short[] s = new short[b.length / 2];
			for (int i = 0; i < s.length; i++) {
				s[i] = toShort(b, i * 2, 2, little);
			}
			return s;
		}
		else if (bpp == 4 && fp) {
			final float[] f = new float[b.length / 4];
			for (int i = 0; i < f.length; i++) {
				f[i] = toFloat(b, i * 4, 4, little);
			}
			return f;
		}
		else if (bpp == 4) {
			final int[] i = new int[b.length / 4];
			for (int j = 0; j < i.length; j++) {
				i[j] = toInt(b, j * 4, 4, little);
			}
			return i;
		}
		else if (bpp == 8 && fp) {
			final double[] d = new double[b.length / 8];
			for (int i = 0; i < d.length; i++) {
				d[i] = toDouble(b, i * 8, 8, little);
			}
			return d;
		}
		else if (bpp == 8) {
			final long[] l = new long[b.length / 8];
			for (int i = 0; i < l.length; i++) {
				l[i] = toLong(b, i * 8, 8, little);
			}
			return l;
		}
		return null;
	}

	/**
	 * Converts a {@code byte} array to the appropriate 2D primitive type array.
	 *
	 * @param b Byte array to convert.
	 * @param bpp Denotes the number of bytes in the returned primitive type (e.g.
	 *          if bpp == 2, we should return an array of type {@code short}).
	 * @param fp If set and bpp == 4 or bpp == 8, then return {@code float}s or
	 *          {@code double}s.
	 * @param little Whether {@code byte} array is in little-endian order.
	 * @param height The height of the output primitive array (2nd dim length).
	 * @return a 2D primitive array of appropriate type, dimensioned
	 *         [height][b.length / (bpp * height)]
	 * @throws IllegalArgumentException if input {@code byte} array does not
	 *           divide evenly into height pieces
	 */
	public static Object makeArray2D(final byte[] b, final int bpp,
		final boolean fp, final boolean little, final int height)
	{
		if (b.length % (bpp * height) != 0) {
			throw new IllegalArgumentException("Array length mismatch: " +
				"b.length=" + b.length + "; bpp=" + bpp + "; height=" + height);
		}
		final int width = b.length / (bpp * height);
		if (bpp == 1) {
			final byte[][] b2 = new byte[height][width];
			for (int y = 0; y < height; y++) {
				final int index = width * y;
				System.arraycopy(b, index, b2[y], 0, width);
			}
			return b2;
		}
		else if (bpp == 2) {
			final short[][] s = new short[height][width];
			for (int y = 0; y < height; y++) {
				for (int x = 0; x < width; x++) {
					final int index = 2 * (width * y + x);
					s[y][x] = toShort(b, index, 2, little);
				}
			}
			return s;
		}
		else if (bpp == 4 && fp) {
			final float[][] f = new float[height][width];
			for (int y = 0; y < height; y++) {
				for (int x = 0; x < width; x++) {
					final int index = 4 * (width * y + x);
					f[y][x] = toFloat(b, index, 4, little);
				}
			}
			return f;
		}
		else if (bpp == 4) {
			final int[][] i = new int[height][width];
			for (int y = 0; y < height; y++) {
				for (int x = 0; x < width; x++) {
					final int index = 4 * (width * y + x);
					i[y][x] = toInt(b, index, 4, little);
				}
			}
			return i;
		}
		else if (bpp == 8 && fp) {
			final double[][] d = new double[height][width];
			for (int y = 0; y < height; y++) {
				for (int x = 0; x < width; x++) {
					final int index = 8 * (width * y + x);
					d[y][x] = toDouble(b, index, 8, little);
				}
			}
			return d;
		}
		else if (bpp == 8) {
			final long[][] l = new long[height][width];
			for (int y = 0; y < height; y++) {
				for (int x = 0; x < width; x++) {
					final int index = 8 * (width * y + x);
					l[y][x] = toLong(b, index, 8, little);
				}
			}
			return l;
		}
		return null;
	}

	// -- Byte swapping --

	public static short swap(final short x) {
		return (short) ((x << 8) | ((x >> 8) & 0xFF));
	}

	public static char swap(final char x) {
		return (char) ((x << 8) | ((x >> 8) & 0xFF));
	}

	public static int swap(final int x) {
		return (swap((short) x) << 16) | (swap((short) (x >> 16)) & 0xFFFF);
	}

	public static long swap(final long x) {
		return ((long) swap((int) x) << 32) | (swap((int) (x >> 32)) & 0xFFFFFFFFL);
	}

	public static float swap(final float x) {
		return Float.intBitsToFloat(swap(Float.floatToIntBits(x)));
	}

	public static double swap(final double x) {
		return Double.longBitsToDouble(swap(Double.doubleToLongBits(x)));
	}

	// -- Normalization --

	/**
	 * Normalize the given {@code float} array so that the minimum value maps to
	 * 0.0 and the maximum value maps to 1.0.
	 */
	public static float[] normalize(final float[] data) {
		final float[] rtn = new float[data.length];

		// determine the finite min and max values
		float min = Float.MAX_VALUE;
		float max = Float.MIN_VALUE;
		for (final float floatValue : data) {
			if (floatValue == Float.POSITIVE_INFINITY ||
				floatValue == Float.NEGATIVE_INFINITY)
			{
				continue;
			}
			if (floatValue < min) min = floatValue;
			if (floatValue > max) max = floatValue;
		}

		// normalize infinity values
		for (int i = 0; i < data.length; i++) {
			if (data[i] == Float.POSITIVE_INFINITY) data[i] = max;
			else if (data[i] == Float.NEGATIVE_INFINITY) data[i] = min;
		}

		// now normalize; min => 0.0, max => 1.0
		final float range = max - min;
		for (int i = 0; i < rtn.length; i++) {
			rtn[i] = (data[i] - min) / range;
		}
		return rtn;
	}

	/**
	 * Normalize the given {@code double} array so that the minimum value maps to
	 * 0.0 and the maximum value maps to 1.0.
	 */
	public static double[] normalize(final double[] data) {
		final double[] rtn = new double[data.length];

		// determine the finite min and max values
		double min = Double.MAX_VALUE;
		double max = Double.MIN_VALUE;
		for (final double doubleValue : data) {
			if (doubleValue == Double.POSITIVE_INFINITY ||
				doubleValue == Double.NEGATIVE_INFINITY)
			{
				continue;
			}
			if (doubleValue < min) min = doubleValue;
			if (doubleValue > max) max = doubleValue;
		}

		// normalize infinity values
		for (int i = 0; i < data.length; i++) {
			if (data[i] == Double.POSITIVE_INFINITY) data[i] = max;
			else if (data[i] == Double.NEGATIVE_INFINITY) data[i] = min;
		}

		// now normalize; min => 0.0, max => 1.0
		final double range = max - min;
		for (int i = 0; i < rtn.length; i++) {
			rtn[i] = (data[i] - min) / range;
		}
		return rtn;
	}

	// -- Signed data conversion --

	public static byte[] makeSigned(final byte[] b) {
		for (int i = 0; i < b.length; i++) {
			b[i] = (byte) (b[i] + 128);
		}
		return b;
	}

	public static short[] makeSigned(final short[] s) {
		for (int i = 0; i < s.length; i++) {
			s[i] = (short) (s[i] + 32768);
		}
		return s;
	}

	public static int[] makeSigned(final int[] i) {
		for (int j = 0; j < i.length; j++) {
			i[j] = (int) (i[j] + 2147483648L);
		}
		return i;
	}

}
