/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2012 Board of Regents of the University of
 * Wisconsin-Madison, Broad Institute of MIT and Harvard, and Max Planck
 * Institute of Molecular Cell Biology and Genetics.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *	this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *	this list of conditions and the following disclaimer in the documentation
 *	and/or other materials provided with the distribution.
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

package imagej.maven;

import java.io.File;
import java.util.List;

import org.apache.maven.model.Build;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;

/**
 * Sets the <tt>project.rootdir</tt> property to the top-level directory of
 * the current Maven project structure.
 * 
 * @author Johannes Schindelin
 * 
 * @goal set-rootdir
 * @phase validate
 */
public class SetRootDirPropertyMojo extends AbstractMojo {

	 /**
	  * You can rename the timestamp property name to another property name if desired.
	  *
	  * @parameter expression="${setRootdir.rootdirPropertyName}" default-value="rootdir"
	  */
	private String rootdirPropertyName;

	/**
	 * @parameter expression="${project}"
	 * @required
	 * @readonly
	 */
	private MavenProject currentProject;

	/**
	 * Contains the full list of projects in the reactor.
	 *
	 * @parameter expression="${reactorProjects}"
	 * @readonly
	 */
	private List<MavenProject> reactorProjects;

	@Override
	public void execute() throws MojoExecutionException {
		if (currentProject.getProperties().getProperty(rootdirPropertyName) != null) {
			getLog().debug("Using previously defined rootdir");
			return;
		}

		if (!isLocalProject(currentProject))
			return;

		MavenProject project = currentProject;
		for (;;) {
			final MavenProject parent = project.getParent();
			if (parent == null || !isLocalProject(parent))
				break;
			project = parent;
		}

		final String rootdir = project.getBasedir().getAbsolutePath();
		getLog().info("Setting rootdir: " + rootdir);
		for (final MavenProject reactorProject : reactorProjects)
			reactorProject.getProperties().setProperty(rootdirPropertyName, rootdir);
	}

	/**
	 * Determines whether the project has a valid output directory.
	 *
	 * @param project the Maven project
	 * @return true iff the project is local
	 */
	private static boolean isLocalProject(final MavenProject project) {
		final File baseDir = project.getBasedir();
		return baseDir != null && baseDir.exists();
	}

}
