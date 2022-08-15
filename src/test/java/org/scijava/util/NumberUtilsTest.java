
package org.scijava.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Tests {@link NumberUtils} functionality
 *
 * @author Gabriel Selzer
 */
public class NumberUtilsTest {

	/** Tests {@link NumberUtils#getMinimumNumber(Class)} */
	@Test
	public void testGetMinimumNumber() {
		assertEquals(Byte.MIN_VALUE, NumberUtils.getMinimumNumber(Byte.class));
		assertEquals(Short.MIN_VALUE, NumberUtils.getMinimumNumber(Short.class));
		assertEquals(Integer.MIN_VALUE, NumberUtils.getMinimumNumber(
			Integer.class));
		assertEquals(Long.MIN_VALUE, NumberUtils.getMinimumNumber(Long.class));
		assertEquals(-Float.MAX_VALUE, NumberUtils.getMinimumNumber(Float.class));
		assertEquals(-Double.MAX_VALUE, NumberUtils.getMinimumNumber(Double.class));
		// Number's minimum value should be the smallest of all the above -> Double
		assertEquals(-Double.MAX_VALUE, NumberUtils.getMinimumNumber(Number.class));
	}

	/** Tests {@link NumberUtils#getMaximumNumber(Class)} */
	@Test
	public void testGetMaximumNumber() {
		assertEquals(Byte.MAX_VALUE, NumberUtils.getMaximumNumber(Byte.class));
		assertEquals(Short.MAX_VALUE, NumberUtils.getMaximumNumber(Short.class));
		assertEquals(Integer.MAX_VALUE, NumberUtils.getMaximumNumber(
			Integer.class));
		assertEquals(Long.MAX_VALUE, NumberUtils.getMaximumNumber(Long.class));
		assertEquals(Float.MAX_VALUE, NumberUtils.getMaximumNumber(Float.class));
		assertEquals(Double.MAX_VALUE, NumberUtils.getMaximumNumber(Double.class));
		// Number's minimum value should be the smallest of all the above -> Double
		assertEquals(Double.MAX_VALUE, NumberUtils.getMaximumNumber(Number.class));
	}
}
