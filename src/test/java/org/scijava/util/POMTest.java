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

package org.scijava.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.Test;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

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

	@Test
	public void testAccessors() throws ParserConfigurationException,
		SAXException, IOException
	{
		final POM pom = new POM(new File("pom.xml"));
		assertEquals("org.scijava", pom.getParentGroupId());
		assertEquals("pom-scijava", pom.getParentArtifactId());
		assertNotNull(pom.getParentVersion());
		assertEquals("org.scijava", pom.getGroupId());
		assertEquals("scijava-common", pom.getArtifactId());
		assertNotNull(pom.getVersion());
		assertEquals("Jenkins", pom.getCIManagementSystem());
		final String ciManagementURL = pom.getCIManagementURL();
		assertEquals("http://jenkins.imagej.net/job/SciJava-common/",
			ciManagementURL);
		assertEquals("GitHub Issues", pom.getIssueManagementSystem());
		final String issueManagementURL = pom.getIssueManagementURL();
		assertEquals("https://github.com/scijava/scijava-common/issues",
			issueManagementURL);
		assertEquals("SciJava", pom.getOrganizationName());
		assertEquals("http://www.scijava.org/", pom.getOrganizationURL());
		assertTrue(pom.getPath().endsWith("pom.xml"));
		assertTrue(pom.getProjectDescription().startsWith(
			"SciJava Common is a shared library for SciJava software."));
		assertEquals("2009", pom.getProjectInceptionYear());
		assertEquals("SciJava Common", pom.getProjectName());
		assertEquals("http://scijava.org/", pom.getProjectURL());
		final String scmConnection = pom.getSCMConnection();
		assertEquals("scm:git:git://github.com/scijava/scijava-common",
			scmConnection);
		final String scmDeveloperConnection = pom.getSCMDeveloperConnection();
		assertEquals("scm:git:git@github.com:scijava/scijava-common",
			scmDeveloperConnection);
		assertNotNull(pom.getSCMTag()); // won't be HEAD for release tags
		assertEquals("https://github.com/scijava/scijava-common", pom.getSCMURL());
	}

	@Test
	public void testCdata() throws ParserConfigurationException,
		SAXException, IOException
	{
		final POM pom = new POM(new File("pom.xml"));
		assertEquals("repo", pom.cdata("//project/licenses/license/distribution"));
		assertEquals("http://scijava.org/", pom.cdata("//project/url"));
	}

	@Test
	public void testElements() throws ParserConfigurationException,
		SAXException, IOException
	{
		final POM pom = new POM(new File("pom.xml"));
		final ArrayList<Element> developers =
			pom.elements("//project/developers/developer");
		assertEquals(1, developers.size());
		assertEquals("ctrueden", XML.cdata(developers.get(0), "id"));
	}

}
