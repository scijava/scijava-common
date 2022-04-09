/*-
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
package org.scijava.convert;

import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.scijava.Context;
import org.scijava.plugin.Plugin;


public class DelegateConverterTest {
	private Context context;

	@Before
	public void setUp() {
		context = new Context();
	}

	@After
	public void tearDown() {
		context.dispose();
		context = null;
	}

	@Test
	public void testDelegateConverters() {
		ConvertService convertService = context.getService(ConvertService.class);
		
		// Test conversion from AType to BType
		AType a = new AType();
		assertTrue(convertService.supports(a, BType.class));
		BType b = convertService.convert(a, BType.class);
		assertSame(BType.class, b.getClass());
		
		// Test conversion from BType to CType
		assertTrue(convertService.supports(b, CType.class));
		CType c = convertService.convert(b, CType.class);
		assertSame(CType.class, c.getClass());

		// Test chained conversion
		assertTrue(convertService.supports(a, CType.class));
		CType converted = convertService.convert(a, CType.class);
		assertSame(c.getClass(), converted.getClass());
	}

	public static class AType {
		// empty class
	}

	public static class BType {
		// empty class
	}

	public static class CType {
		// empty class
	}

	@Plugin(type=Converter.class)
	public static class ABConverter extends AbstractConverter<AType, BType> {

		@SuppressWarnings("unchecked")
		@Override
		public <T> T convert(Object src, Class<T> dest) {
			return (T) new BType();
		}

		@Override
		public Class<BType> getOutputType() {
			return BType.class;
		}

		@Override
		public Class<AType> getInputType() {
			return AType.class;
		}		
	}

	@Plugin(type=Converter.class)
	public static class BCConverter extends AbstractConverter<BType, CType> {

		@SuppressWarnings("unchecked")
		@Override
		public <T> T convert(Object src, Class<T> dest) {
			return (T) new CType();
		}

		@Override
		public Class<CType> getOutputType() {
			return CType.class;
		}

		@Override
		public Class<BType> getInputType() {
			return BType.class;
		}		
	}

	@Plugin(type=Converter.class)
	public static class DelegateConverter extends AbstractDelegateConverter<AType, BType, CType> {

		@Override
		public Class<CType> getOutputType() {
			return CType.class;
		}

		@Override
		public Class<AType> getInputType() {
			return AType.class;
		}

		@Override
		protected Class<BType> getDelegateType() {
			return BType.class;
		}
	}
}
