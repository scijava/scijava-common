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

package org.scijava.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Tests {@link UnitUtils}.
 * 
 * @author Curtis Rueden
 */
public class UnitUtilsTest {

	@Test
	public void testGetAbbreviatedByteLabel() {
		assertEquals(local(0, "B"), UnitUtils.getAbbreviatedByteLabel(0));
		assertEquals(local(1, "B"), UnitUtils.getAbbreviatedByteLabel(1));
		assertEquals(local(123, "B"), UnitUtils.getAbbreviatedByteLabel(123));

		assertEquals(local(10, "B"), UnitUtils.getAbbreviatedByteLabel(1e1));
		assertEquals(local(100, "B"), UnitUtils.getAbbreviatedByteLabel(1e2));
		assertEquals(local(1000, "B"), UnitUtils.getAbbreviatedByteLabel(1e3));
		assertEquals(local(9.8, "KiB"), UnitUtils.getAbbreviatedByteLabel(1e4));
		assertEquals(local(97.7, "KiB"), UnitUtils.getAbbreviatedByteLabel(1e5));
		assertEquals(local(976.6, "KiB"), UnitUtils.getAbbreviatedByteLabel(1e6));
		assertEquals(local(9.5, "MiB"), UnitUtils.getAbbreviatedByteLabel(1e7));
		assertEquals(local(95.4, "MiB"), UnitUtils.getAbbreviatedByteLabel(1e8));
		assertEquals(local(953.7, "MiB"), UnitUtils.getAbbreviatedByteLabel(1e9));
		assertEquals(local(9.3, "GiB"), UnitUtils.getAbbreviatedByteLabel(1e10));
		assertEquals(local(93.1, "GiB"), UnitUtils.getAbbreviatedByteLabel(1e11));
		assertEquals(local(931.3, "GiB"), UnitUtils.getAbbreviatedByteLabel(1e12));
		assertEquals(local(9.1, "TiB"), UnitUtils.getAbbreviatedByteLabel(1e13));
		assertEquals(local(90.9, "TiB"), UnitUtils.getAbbreviatedByteLabel(1e14));
		assertEquals(local(909.5, "TiB"), UnitUtils.getAbbreviatedByteLabel(1e15));
		assertEquals(local(8.9, "PiB"), UnitUtils.getAbbreviatedByteLabel(1e16));
		assertEquals(local(88.8, "PiB"), UnitUtils.getAbbreviatedByteLabel(1e17));
		assertEquals(local(888.2, "PiB"), UnitUtils.getAbbreviatedByteLabel(1e18));
		assertEquals(local(8.7, "EiB"), UnitUtils.getAbbreviatedByteLabel(1e19));
		assertEquals(local(86.7, "EiB"), UnitUtils.getAbbreviatedByteLabel(1e20));
		assertEquals(local(867.4, "EiB"), UnitUtils.getAbbreviatedByteLabel(1e21));
		assertEquals(local(8.5, "ZiB"), UnitUtils.getAbbreviatedByteLabel(1e22));
		assertEquals(local(84.7, "ZiB"), UnitUtils.getAbbreviatedByteLabel(1e23));
		assertEquals(local(847.0, "ZiB"), UnitUtils.getAbbreviatedByteLabel(1e24));
		assertEquals(local(8.3, "YiB"), UnitUtils.getAbbreviatedByteLabel(1e25));
		assertEquals(local(82.7, "YiB"), UnitUtils.getAbbreviatedByteLabel(1e26));
		assertEquals(local(827.2, "YiB"), UnitUtils.getAbbreviatedByteLabel(1e27));
		assertEquals(local(8271.8, "YiB"), UnitUtils.getAbbreviatedByteLabel(1e28));
		assertEquals(local(82718.1, "YiB"), UnitUtils.getAbbreviatedByteLabel(1e29));
		assertEquals(local(827180.6, "YiB"), UnitUtils.getAbbreviatedByteLabel(1e30));
	}

	/**
	 * Helper method to ensure the strings tested here match the
	 * {@link java.util.Locale} of the current JVM.
	 */
	private String local(final double value, final String unit) {
		final String format = UnitUtils.format(unit.equals("B") ? 0 : 1);
		return String.format(format, value, unit);
	}
}
