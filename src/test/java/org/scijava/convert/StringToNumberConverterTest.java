
package org.scijava.convert;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests {@link StringToNumberConverter}
 *
 * @author Gabriel Selzer
 */
public class StringToNumberConverterTest {

	Converter<String, Number> conv;

	@Before
	public void setUp() {
		conv = new StringToNumberConverter();
	}

	@Test
	public void stringToByteTest() {
		String s = "0";
		Assert.assertTrue(conv.canConvert(s, Byte.class));
		Assert.assertEquals(new Byte((byte) 0), conv.convert(s, Byte.class));
	}

	@Test
	public void stringToPrimitiveByteTest() {
		String s = "0";
		Assert.assertTrue(conv.canConvert(s, byte.class));
		Assert.assertEquals(0, (int) conv.convert(s, byte.class));
	}

	@Test
	public void stringToShortTest() {
		String s = "0";
		Assert.assertTrue(conv.canConvert(s, Short.class));
		Assert.assertEquals(new Short((short) 0), conv.convert(s, Short.class));
	}

	@Test
	public void stringToPrimitiveShortTest() {
		String s = "0";
		Assert.assertTrue(conv.canConvert(s, short.class));
		Assert.assertEquals(0, (int) conv.convert(s, short.class));
	}

	@Test
	public void stringToIntegerTest() {
		String s = "0";
		Assert.assertTrue(conv.canConvert(s, Integer.class));
		Assert.assertEquals(new Integer(0), conv.convert(s, Integer.class));
	}

	@Test
	public void stringToPrimitiveIntegerTest() {
		String s = "0";
		Assert.assertTrue(conv.canConvert(s, int.class));
		Assert.assertEquals(0, (int) conv.convert(s, int.class));
	}

	@Test
	public void stringToLongTest() {
		String s = "0";
		Assert.assertTrue(conv.canConvert(s, Long.class));
		Assert.assertEquals(new Long(0), conv.convert(s, Long.class));
	}

	@Test
	public void stringToPrimitiveLongTest() {
		String s = "0";
		Assert.assertTrue(conv.canConvert(s, long.class));
		Assert.assertEquals(0L, (long) conv.convert(s, long.class));
	}

	@Test
	public void stringToFloatTest() {
		String s = "0";
		Assert.assertTrue(conv.canConvert(s, Float.class));
		Assert.assertEquals(new Float(0), conv.convert(s, Float.class));
	}

	@Test
	public void stringToPrimitiveFloat() {
		String s = "0";
		Assert.assertTrue(conv.canConvert(s, float.class));
		Assert.assertEquals(0f, conv.convert(s, float.class), 1e-6);
	}

	@Test
	public void stringToDoubleTest() {
		String s = "0";
		Assert.assertTrue(conv.canConvert(s, Double.class));
		Assert.assertEquals(new Double(0), conv.convert(s, Double.class));
	}

	@Test
	public void stringToPrimitiveDouble() {
		String s = "0";
		Assert.assertTrue(conv.canConvert(s, double.class));
		Assert.assertEquals(0d, conv.convert(s, double.class), 1e-6);
	}

	@Test
	public void stringToNumberTest() {
		String s = "0";
		Assert.assertTrue(conv.canConvert(s, Number.class));
		Assert.assertEquals(0d, conv.convert(s, Number.class));
	}
}
