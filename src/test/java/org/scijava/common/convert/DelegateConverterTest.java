package org.scijava.common.convert;

import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.scijava.common.Context;
import org.scijava.common.convert.AbstractConverter;
import org.scijava.common.convert.AbstractDelegateConverter;
import org.scijava.common.convert.ConvertService;
import org.scijava.common.convert.Converter;
import org.scijava.common.plugin.Plugin;


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
