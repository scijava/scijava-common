/*
 * #%L
 * SciJava Common shared library for SciJava software.
 * %%
 * Copyright (C) 2009 - 2015 Board of Regents of the University of
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

package org.scijava.util;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Tests methods of {@link POM}.
 * 
 * @author Curtis Rueden
 */
public class POMTest {

	@Test
	public void testCompareVersions() {
		// basic checks
		assertTrue(POM.compareVersions("1", "2") < 0);
		assertTrue(POM.compareVersions("1.0", "2.0") < 0);
		assertTrue(POM.compareVersions("1.0", "1.1") < 0);
		assertTrue(POM.compareVersions("1.0.0", "2.0.0") < 0);

		// sanity checks for argument order
		assertTrue(POM.compareVersions("2", "1") > 0);
		assertTrue(POM.compareVersions("2.0", "1.0") > 0);
		assertTrue(POM.compareVersions("1.1", "1.0") > 0);
		assertTrue(POM.compareVersions("2.0.0", "1.0.0") > 0);

		// more complex/unusual checks
		assertTrue(POM.compareVersions("1.0-RC1", "1.0-RC2") < 0);
		assertTrue(POM.compareVersions("1.0-RC-1", "1.0-RC-2") < 0);
		assertTrue(POM.compareVersions("1.0-RC-2", "1.0-RC-10") < 0);
		assertTrue(POM.compareVersions("0.4-alpha", "0.4-beta") < 0);
		assertTrue(POM.compareVersions("foo", "bar") > 0);

		// checks which expose bugs/limitations
		assertTrue(POM.compareVersions("1.0-RC2", "1.0-RC10") > 0);
		assertTrue(POM.compareVersions("1.0-rc1", "1.0-RC2") > 0);

		// check that varying numbers of digits are handled properly
		assertTrue(POM.compareVersions("2.0.0", "2.0.0.1") < 0);
		// check that SemVer prerelease versions are handled properly
		assertTrue(POM.compareVersions("2.0.0", "2.0.0-beta-1") > 0);
	}

}
