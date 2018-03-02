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

import org.scijava.Priority;
import org.scijava.plugin.Plugin;
import org.scijava.util.BoolArray;
import org.scijava.util.ByteArray;
import org.scijava.util.CharArray;
import org.scijava.util.DoubleArray;
import org.scijava.util.FloatArray;
import org.scijava.util.IntArray;
import org.scijava.util.LongArray;
import org.scijava.util.ShortArray;

/**
 * Container class for all desired combinations of
 * {@link PrimitiveArrayUnwrapper} and {@link PrimitiveArrayWrapper}
 * implementations.
 *
 * @author Mark Hiner
 */
public class ArrayConverters {

	// -- Integer array converters --

	@Plugin(type = Converter.class, priority = Priority.HIGH)
	public static class IntArrayWrapper extends
		PrimitiveArrayWrapper<int[], Integer, IntArray>
	{

		@Override
		public Class<IntArray> getOutputType() {
			return IntArray.class;
		}

		@Override
		public Class<int[]> getInputType() {
			return int[].class;
		}
	}

	@Plugin(type = Converter.class, priority = Priority.HIGH)
	public static class IntArrayUnwrapper extends
		PrimitiveArrayUnwrapper<int[], Integer, IntArray>
	{

		@Override
		public Class<int[]> getOutputType() {
			return int[].class;
		}

		@Override
		public Class<IntArray> getInputType() {
			return IntArray.class;
		}
	}

	// -- Byte array converters --

	@Plugin(type = Converter.class, priority = Priority.HIGH)
	public static class ByteArrayWrapper extends
		PrimitiveArrayWrapper<byte[], Byte, ByteArray>
	{

		@Override
		public Class<ByteArray> getOutputType() {
			return ByteArray.class;
		}

		@Override
		public Class<byte[]> getInputType() {
			return byte[].class;
		}
	}

	@Plugin(type = Converter.class, priority = Priority.HIGH)
	public static class ByteArrayUnwrapper extends
		PrimitiveArrayUnwrapper<byte[], Byte, ByteArray>
	{

		@Override
		public Class<byte[]> getOutputType() {
			return byte[].class;
		}

		@Override
		public Class<ByteArray> getInputType() {
			return ByteArray.class;
		}
	}

	// -- Bool array converters --

	@Plugin(type = Converter.class, priority = Priority.HIGH)
	public static class BoolArrayWrapper extends
		PrimitiveArrayWrapper<boolean[], Boolean, BoolArray>
	{

		@Override
		public Class<BoolArray> getOutputType() {
			return BoolArray.class;
		}

		@Override
		public Class<boolean[]> getInputType() {
			return boolean[].class;
		}
	}

	@Plugin(type = Converter.class, priority = Priority.HIGH)
	public static class BoolArrayUnwrapper extends
		PrimitiveArrayUnwrapper<boolean[], Boolean, BoolArray>
	{

		@Override
		public Class<boolean[]> getOutputType() {
			return boolean[].class;
		}

		@Override
		public Class<BoolArray> getInputType() {
			return BoolArray.class;
		}
	}

	// -- Char array converters --

	@Plugin(type = Converter.class, priority = Priority.HIGH)
	public static class CharArrayWrapper extends
		PrimitiveArrayWrapper<char[], Character, CharArray>
	{

		@Override
		public Class<CharArray> getOutputType() {
			return CharArray.class;
		}

		@Override
		public Class<char[]> getInputType() {
			return char[].class;
		}
	}

	@Plugin(type = Converter.class, priority = Priority.HIGH)
	public static class CharArrayUnwrapper extends
		PrimitiveArrayUnwrapper<char[], Character, CharArray>
	{

		@Override
		public Class<char[]> getOutputType() {
			return char[].class;
		}

		@Override
		public Class<CharArray> getInputType() {
			return CharArray.class;
		}
	}

	// -- Short array converters --

	@Plugin(type = Converter.class, priority = Priority.HIGH)
	public static class ShortArrayWrapper extends
		PrimitiveArrayWrapper<short[], Short, ShortArray>
	{

		@Override
		public Class<ShortArray> getOutputType() {
			return ShortArray.class;
		}

		@Override
		public Class<short[]> getInputType() {
			return short[].class;
		}
	}

	@Plugin(type = Converter.class, priority = Priority.HIGH)
	public static class ShortArrayUnwrapper extends
		PrimitiveArrayUnwrapper<short[], Short, ShortArray>
	{

		@Override
		public Class<short[]> getOutputType() {
			return short[].class;
		}

		@Override
		public Class<ShortArray> getInputType() {
			return ShortArray.class;
		}
	}

	// -- Float array converters --

	@Plugin(type = Converter.class, priority = Priority.HIGH)
	public static class FloatArrayWrapper extends
		PrimitiveArrayWrapper<float[], Float, FloatArray>
	{

		@Override
		public Class<FloatArray> getOutputType() {
			return FloatArray.class;
		}

		@Override
		public Class<float[]> getInputType() {
			return float[].class;
		}
	}

	@Plugin(type = Converter.class, priority = Priority.HIGH)
	public static class FloatArrayUnwrapper extends
		PrimitiveArrayUnwrapper<float[], Float, FloatArray>
	{

		@Override
		public Class<float[]> getOutputType() {
			return float[].class;
		}

		@Override
		public Class<FloatArray> getInputType() {
			return FloatArray.class;
		}
	}

	// -- Double array converters --

	@Plugin(type = Converter.class, priority = Priority.HIGH)
	public static class DoubleArrayWrapper extends
		PrimitiveArrayWrapper<double[], Double, DoubleArray>
	{

		@Override
		public Class<DoubleArray> getOutputType() {
			return DoubleArray.class;
		}

		@Override
		public Class<double[]> getInputType() {
			return double[].class;
		}
	}

	@Plugin(type = Converter.class, priority = Priority.HIGH)
	public static class DoubleArrayUnwrapper extends
		PrimitiveArrayUnwrapper<double[], Double, DoubleArray>
	{

		@Override
		public Class<double[]> getOutputType() {
			return double[].class;
		}

		@Override
		public Class<DoubleArray> getInputType() {
			return DoubleArray.class;
		}
	}

	// -- Long array converters --

	@Plugin(type = Converter.class, priority = Priority.HIGH)
	public static class LongArrayWrapper extends
		PrimitiveArrayWrapper<long[], Long, LongArray>
	{

		@Override
		public Class<LongArray> getOutputType() {
			return LongArray.class;
		}

		@Override
		public Class<long[]> getInputType() {
			return long[].class;
		}
	}

	@Plugin(type = Converter.class, priority = Priority.HIGH)
	public static class LongArrayUnwrapper extends
		PrimitiveArrayUnwrapper<long[], Long, LongArray>
	{

		@Override
		public Class<long[]> getOutputType() {
			return long[].class;
		}

		@Override
		public Class<LongArray> getInputType() {
			return LongArray.class;
		}
	}

}
