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

package org.scijava.util;


/**
 * Utility methods for working with units.
 * 
 * @author Barry DeZonia
 * @author Curtis Rueden
 */
public final class UnitUtils {

	private static final String[] BYTE_UNITS = { "B", "KiB", "MiB", "GiB", "TiB",
		"PiB", "EiB", "ZiB", "YiB" };

	private static final double LOG1024 = Math.log(1024);

	private UnitUtils() {
		// prevent instantiation of utility class
	}

	/**
	 * @return A properly formatted String representation of the given bytes, in
	 *         the default {@link java.util.Locale} for this JVM.
	 */
	public static String getAbbreviatedByteLabel(final double totBytes) {
		if (totBytes < 0) {
			throw new IllegalArgumentException("Bytes must be non-negative");
		}
		if (totBytes == 0) return "0B";

		// compute unit
		final int rawPow = (int) (Math.log(totBytes) / LOG1024);
		final int pow = Math.min(rawPow, BYTE_UNITS.length - 1);

		// compute value from unit
		final double value = totBytes / Math.pow(1024.0, pow);

		final String format = format(pow);
		return String.format(format, value, BYTE_UNITS[pow]);
	}

	/**
	 * @return Format result with 0 decimal places for bytes, or 1 for larger values
	 */
	public static String format(final double power) {
		return power == 0 ? "%.0f%s" : "%.1f%s";
	}
}
