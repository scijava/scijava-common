/*
 * #%L
 * SciJava Common shared library for SciJava software.
 * %%
 * Copyright (C) 2009 - 2013 Board of Regents of the University of
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
 * 
 * The views and conclusions contained in the software and documentation are
 * those of the authors and should not be interpreted as representing official
 * policies, either expressed or implied, of any organization.
 * #L%
 */

package org.scijava.app;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;
import org.scijava.service.AbstractService;
import org.scijava.service.Service;
import org.scijava.util.Manifest;
import org.scijava.util.POM;

/**
 * Default service for application-level functionality.
 * 
 * @author Curtis Rueden
 */
@Plugin(type = Service.class, priority = Priority.LOW_PRIORITY)
public class DefaultAppService extends AbstractService implements AppService {

	/** Maven POM with metadata about the application. */
	private POM pom;

	/** JAR manifest with metadata about the application. */
	private Manifest manifest;

	// -- AppService methods --

	@Override
	public String getTitle() {
		return "SciJava";
	}

	@Override
	public String getVersion() {
		return pom == null ? "Unknown" : pom.getVersion();
	}

	@Override
	public POM getPOM() {
		return pom;
	}

	@Override
	public Manifest getManifest() {
		return manifest;
	}

	@Override
	public String getInfo(final boolean mem) {
		final String appTitle = getTitle();
		final String appVersion = getVersion();
		final String javaVersion = System.getProperty("java.version");
		final String osArch = System.getProperty("os.arch");
		final long maxMem = Runtime.getRuntime().maxMemory();
		final long totalMem = Runtime.getRuntime().totalMemory();
		final long freeMem = Runtime.getRuntime().freeMemory();
		final long usedMem = totalMem - freeMem;
		final long usedMB = usedMem / 1048576;
		final long maxMB = maxMem / 1048576;
		final StringBuilder sb = new StringBuilder();
		sb.append(appTitle + " " + appVersion);
		sb.append("; Java " + javaVersion + " [" + osArch + "]");
		if (mem) sb.append("; " + usedMB + "MB of " + maxMB + "MB");
		return sb.toString();
	}

	// -- Service methods --

	@Override
	public void initialize() {
		pom = loadPOM();
		manifest = Manifest.getManifest(getClass());
	}

	// -- Internal methods --

	protected POM loadPOM() {
		return POM.getPOM(getClass(), "org.scijava", "scijava-common");
	}

}
