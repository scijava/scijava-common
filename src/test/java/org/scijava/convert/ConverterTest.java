/*
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

package org.scijava.convert;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;

import org.junit.Test;
import org.scijava.util.Types;

/**
 * Tests individual {@link Converter}s.
 * <p>
 * TODO: Consider splitting up the {@link DefaultConverter} into component parts
 * and testing each individually. As is, the {@code DefaultConverter} is a bit
 * monolithic to test (without duplicating all of the ConvertServiceTest).
 * </p>
 *
 * @author Mark Hiner
 * @author Curtis Rueden
 */
public class ConverterTest {

	/**
	 * Test case for the {@link NullConverter}
	 */
	@Test
	public void testNullConverter() {
		final NullConverter nc = new NullConverter();
		assertFalse(nc.canConvert(Object.class, Object.class));
		assertFalse(nc.canConvert(Object.class, (Type) Object.class));
		assertTrue(nc.canConvert((Class<?>) null, Object.class));
		assertTrue(nc.canConvert((Object) null, Object.class));
		assertTrue(nc.canConvert((ConverterTest) null, ArrayList.class));
		assertNull(nc.convert((Object) null, Object.class));
		assertNull(nc.convert((Class<Object>) null, Object.class));
		assertNull(nc.convert(Object.class, (Class<Object>) null));
		assertNull(nc.convert(Object.class, (Type) null));
		assertNull(nc.convert(new Object(), (Class<Object>) null));
		assertNull(nc.convert(new Object(), (Type) null));
	}

	/**
	 * Test the default {@link AbstractConverter#canConvert} behavior.
	 */
	@Test
	public void testCanConvert() {
		NumberConverter nc = new NumberConverter();

		assertFalse(nc.canConvert(Integer.class, Double.class));
		assertTrue(nc.canConvert(Integer.class, Number.class));
	}

	@SuppressWarnings("unused")
	private Collection<Number> collection;

	@Test
	public void testCanConvertToGenericCollection() {
		final CastingConverter cc = new CastingConverter();

		final Field destField = Types.field(getClass(), "collection");
		final Type destType = Types.fieldType(destField, getClass());
		assertTrue(cc.canConvert(ArrayList.class, destType));
	}

	private static class NumberConverter extends AbstractConverter<Number, Number> {

		@SuppressWarnings("unchecked")
		@Override
		public <T> T convert(Object src, Class<T> dest) {
			return (T)src;
		}

		@Override
		public Class<Number> getOutputType() {
			return Number.class;
		}

		@Override
		public Class<Number> getInputType() {
			return Number.class;
		}
		
	}
}
