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
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Tests {@link VersionUtils}.
 * 
 * @author Curtis Rueden
 */
public class VersionUtilsTest {

	/** Tests {@link VersionUtils#compare(String, String)}. */
	@Test
	public void testCompare() {
		// SemVer PATCH version.
		assertTrue(VersionUtils.compare("1.5.1", "1.5.2") < 0);
		assertTrue(VersionUtils.compare("1.5.2", "1.5.1") > 0);

		// SemVer MINOR version.
		assertTrue(VersionUtils.compare("1.5.2", "1.6.2") < 0);
		assertTrue(VersionUtils.compare("1.6.2", "1.5.2") > 0);

		// SemVer MAJOR version.
		assertTrue(VersionUtils.compare("1.7.3", "2.7.3") < 0);
		assertTrue(VersionUtils.compare("2.7.3", "1.7.3") > 0);

		// Suffix indicates version is older than final release.
		assertTrue(VersionUtils.compare("1.5.2", "1.5.2-beta-1") < 0);

		// Check when number of version tokens does not match.
		assertTrue(VersionUtils.compare("1.5", "1.5.1") < 0);
		assertTrue(VersionUtils.compare("1.5.1", "1.5") > 0);

		// Check equality.
		assertEquals(VersionUtils.compare("1.5", "1.5"), 0);

		// Check ImageJ 1.x style versions.
		assertTrue(VersionUtils.compare("1.50a", "1.50b") < 0);

		// Check four version tokens.
		assertTrue(VersionUtils.compare("1.5.1.3", "1.5.1.4") < 0);
		assertTrue(VersionUtils.compare("1.5.1.6", "1.5.1.5") > 0);
		assertEquals(VersionUtils.compare("10.4.9.8", "10.4.9.8"), 0);

		// Check non-numeric tokens.
		assertTrue(VersionUtils.compare("a.b.c", "a.b.d") < 0);
		
		// Check for numerical (not lexicographic) comparison.
		assertTrue(VersionUtils.compare("2.0", "23.0") < 0);
		assertTrue(VersionUtils.compare("23.0", "2.0") > 0);
		assertTrue(VersionUtils.compare("3.0", "23.0") < 0);
		assertTrue(VersionUtils.compare("23.0", "3.0") > 0);

		// Check weird stuff.
		assertTrue(VersionUtils.compare("1", "a") < 0);
		assertTrue(VersionUtils.compare("", "1") < 0);
		assertTrue(VersionUtils.compare("1", "1.") < 0);
		assertTrue(VersionUtils.compare("", ".") < 0);
		assertTrue(VersionUtils.compare("", "..") < 0);
		assertTrue(VersionUtils.compare(".", "..") < 0);
	}
}
