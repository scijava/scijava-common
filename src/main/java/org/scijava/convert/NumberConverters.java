/*
 * #%L
 * SciJava Common shared library for SciJava software.
 * %%
 * Copyright (C) 2009 - 2016 Board of Regents of the University of
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

package org.scijava.convert;

import java.math.BigInteger;

import org.scijava.plugin.Plugin;

/**
 * Converter plugins that convert from primitive numeric types to other
 * primitive numeric types.
 *
 * @author Alison Walter
 */
public final class NumberConverters {

	private NumberConverters() {
		// prevent instantiation of container class
	}
	
	//convert to short 
	@Plugin(type = Converter.class)
	public static class ByteToShortConverter extends NumberToShortConverter<Byte> {
		@Override public Class<Byte> getInputType() { return Byte.class; }
	}
	
	//convert to int
	@Plugin(type = Converter.class)
	public static class ByteToIntegerConverter extends NumberToIntegerConverter<Byte> {
		@Override public Class<Byte> getInputType() { return Byte.class; }
	}
	
	@Plugin(type = Converter.class)
	public static class ShortToIntegerConverter extends NumberToIntegerConverter<Short> {
		@Override public Class<Short> getInputType() { return Short.class; }
	}
	
	//convert to long
	@Plugin(type = Converter.class)
	public static class ByteToLongConverter extends NumberToLongConverter<Byte> {
		@Override public Class<Byte> getInputType() { return Byte.class; }
	}
	
	@Plugin(type = Converter.class)
	public static class ShortToLongConverter extends NumberToLongConverter<Short> {
		@Override public Class<Short> getInputType() { return Short.class; }
	}
	
	@Plugin(type = Converter.class)
	public static class IntegerToLongConverter extends NumberToLongConverter<Integer> {
		@Override public Class<Integer> getInputType() { return Integer.class; }
	}
	
	//convert to float
	@Plugin(type = Converter.class)
	public static class ByteToFloatConverter extends NumberToFloatConverter<Byte> {
		@Override public Class<Byte> getInputType() { return Byte.class; }
	}
	
	@Plugin(type = Converter.class)
	public static class ShortToFloatConverter extends NumberToFloatConverter<Short> {
		@Override public Class<Short> getInputType() { return Short.class; }
	}

	
	//convert to double
	@Plugin(type = Converter.class)
	public static class ByteToDoubleConverter extends NumberToDoubleConverter<Byte> {
		@Override public Class<Byte> getInputType() { return Byte.class; }
	}

	@Plugin(type = Converter.class)
	public static class ShortToDoubleConverter extends NumberToDoubleConverter<Short> {
		@Override public Class<Short> getInputType() { return Short.class; }
	}

	@Plugin(type = Converter.class)
	public static class IntegerToDoubleConverter extends NumberToDoubleConverter<Integer> {
		@Override public Class<Integer> getInputType() { return Integer.class; }
	}

	@Plugin(type = Converter.class)
	public static class FloatToDoubleConverter extends NumberToDoubleConverter<Float> {
		@Override public Class<Float> getInputType() { return Float.class; }
	}
	
	//convert to BigInteger
	@Plugin(type = Converter.class)
	public static class ByteToBigIntegerConverter extends NumberToBigIntegerConverter<Byte> {
		@Override public Class<Byte> getInputType() { return Byte.class; }
	}

	@Plugin(type = Converter.class)
	public static class ShortToBigIntegerConverter extends NumberToBigIntegerConverter<Short> {
		@Override public Class<Short> getInputType() { return Short.class; }
	}

	@Plugin(type = Converter.class)
	public static class IntegerToBigIntegerConverter extends NumberToBigIntegerConverter<Integer> {
		@Override public Class<Integer> getInputType() { return Integer.class; }
	}

	@Plugin(type = Converter.class)
	public static class LongToBigIntegerConverter extends NumberToBigIntegerConverter<Long> {
		@Override public Class<Long> getInputType() { return Long.class; }
	}
	
	//convert to BigDecimal 
	@Plugin(type = Converter.class)
	public static class ByteToBigDecimalConverter extends NumberToBigDecimalConverter<Byte> {
		@Override public Class<Byte> getInputType() { return Byte.class; }
	}

	@Plugin(type = Converter.class)
	public static class ShortToBigDecimalConverter extends NumberToBigDecimalConverter<Short> {
		@Override public Class<Short> getInputType() { return Short.class; }
	}

	@Plugin(type = Converter.class)
	public static class IntegerToBigDecimalConverter extends NumberToBigDecimalConverter<Integer> {
		@Override public Class<Integer> getInputType() { return Integer.class; }
	}
	
	@Plugin(type = Converter.class)
	public static class LongToBigDecimalConverter extends NumberToBigDecimalConverter<Long> {
		@Override public Class<Long> getInputType() { return Long.class; }
	}

	@Plugin(type = Converter.class)
	public static class FloatToBigDecimalConverter extends NumberToBigDecimalConverter<Float> {
		@Override public Class<Float> getInputType() { return Float.class; }
	}
	
	@Plugin(type = Converter.class)
	public static class DoubleToBigDecimalConverter extends NumberToBigDecimalConverter<Double> {
		@Override public Class<Double> getInputType() { return Double.class; }
	}
	
	@Plugin(type = Converter.class)
	public static class BigIntegerToBigDecimalConverter extends NumberToBigDecimalConverter<BigInteger> {
		@Override public Class<BigInteger> getInputType() { return BigInteger.class; }
	}

}
