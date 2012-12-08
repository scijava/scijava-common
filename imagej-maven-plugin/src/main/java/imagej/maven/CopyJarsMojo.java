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

package imagej.maven;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.metadata.ArtifactMetadataSource;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactCollector;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.artifact.resolver.filter.ArtifactFilter;
import org.apache.maven.artifact.resolver.filter.ScopeArtifactFilter;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.dependency.AbstractAnalyzeMojo;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.dependency.tree.DependencyNode;
import org.apache.maven.shared.dependency.tree.DependencyTreeBuilder;
import org.apache.maven.shared.dependency.tree.DependencyTreeBuilderException;
import org.apache.maven.shared.dependency.tree.traversal.CollectingDependencyNodeVisitor;
import org.codehaus.plexus.util.FileUtils;

/**
 * Copies .jar artifacts and their dependencies into an ImageJ.app/ directory structure.
 * 
 * @author Johannes Schindelin
 * 
 * @goal copy-jars
 * @phase install
 */
public class CopyJarsMojo extends AbstractAnalyzeMojo {

	/**
	 * The ImageJ.app/ directory.
	 *
	 * @parameter
	 * @required
	 */
	private File imagejDirectory;

	/**
	 * @parameter expression="${project}"
	 * @required
	 * @readonly
	 */
	private MavenProject project;

	/**
	 * List of Remote Repositories used by the resolver
	 * 
	 * @parameter expression="${project.remoteArtifactRepositories}"
	 * @readonly
	 * @required
	 */
	protected List<String> remoteRepositories;

	/**
	 * Location of the local repository.
	 * 
	 * @parameter expression="${localRepository}"
	 * @readonly
	 * @required
	 */
	protected ArtifactRepository localRepository;

	/**
	 * @component role="org.apache.maven.artifact.metadata.ArtifactMetadataSource"
     *            hint="maven"
	 * @required
	 * @readonly
	 */
	private ArtifactMetadataSource artifactMetadataSource;

	/**
	 * @component role="org.apache.maven.artifact.resolver.ArtifactCollector"
	 * @required
	 * @readonly
	 */
	private ArtifactCollector artifactCollector;

	/**
	 * @component
	 * @required
	 * @readonly
	 */
	private DependencyTreeBuilder treeBuilder;

	/**
	 * Used to look up Artifacts in the remote repository.
	 * 
	 * @component
	 * @required
	 * @readonly
	 */
	protected ArtifactFactory artifactFactory;

	/**
	 * Used to look up Artifacts in the remote repository.
	 * 
	 * @component
	 * @required
	 * @readonly
	 */
	protected ArtifactResolver artifactResolver;

	@SuppressWarnings("unchecked")
	@Override
	public void execute() throws MojoExecutionException {
		if (imagejDirectory == null)
			throw new MojoExecutionException(this, "Missing imagejDirectory setting", "This goal requires the imagejDirectory setting to point to an ImageJ.app/ directory.");
		if (!imagejDirectory.isDirectory()) {
			if (imagejDirectory.mkdirs())
				getLog().warn("Initialized ImageJ directory at " + imagejDirectory);
			else
				throw new MojoExecutionException(this, "Could not create " + imagejDirectory, "Error occurred while trying to create the ImageJ directory at " + imagejDirectory);
		}

		try {
			ArtifactFilter artifactFilter = new ScopeArtifactFilter(Artifact.SCOPE_COMPILE);
			DependencyNode rootNode = treeBuilder.buildDependencyTree(project,
					localRepository, artifactFactory, artifactMetadataSource,
					artifactFilter, artifactCollector);

			CollectingDependencyNodeVisitor visitor = new CollectingDependencyNodeVisitor();
			rootNode.accept(visitor);

			for (final DependencyNode dependencyNode : (List<DependencyNode>)visitor.getNodes()) {
				if (dependencyNode.getState() == DependencyNode.INCLUDED) {
					final Artifact artifact = dependencyNode.getArtifact();
					final String scope = artifact.getScope();
					if (scope != null && !scope.equals(Artifact.SCOPE_COMPILE) && !scope.equals(Artifact.SCOPE_RUNTIME))
						continue;
					try {
						installArtifact(artifact, false);
					} catch (Exception e) {
						throw new MojoExecutionException("Could not copy " + artifact + " to " + imagejDirectory, e);
					}
				}
			}
		} catch (DependencyTreeBuilderException e) {
			throw new MojoExecutionException("Could not get the dependencies for " + project.getArtifactId(), e);
		}
	}

	private void installArtifact(final Artifact artifact, boolean force) throws ArtifactResolutionException, ArtifactNotFoundException, IOException {
		artifactResolver.resolve(artifact, remoteRepositories, localRepository);

		if (!"jar".equals(artifact.getType()))
			return;


		final File source = artifact.getFile();
		final File targetDirectory = new File(imagejDirectory, isIJ1Plugin(source) ? "plugins" : "jars");
		final File target = new File(targetDirectory, source.getName());

		if (!force && target.exists() && target.lastModified() > source.lastModified()) {
			getLog().info("Dependency " + target.getName() + " is already there; skipping");
			return;
		}

		getLog().info("Copying " + target.getName() + " to " + targetDirectory);
		FileUtils.copyFile(source, target);
	}

	private static boolean isIJ1Plugin(final File file) {
		final String name = file.getName();
		if (name.indexOf('_') < 0 || !file.exists())
			return false;
		if (file.isDirectory())
			return new File(file, "src/main/resources/plugins.config").exists();
		if (name.endsWith(".jar")) try {
			final JarFile jar = new JarFile(file);
			for (JarEntry entry : Collections.list(jar.entries()))
				if (entry.getName().equals("plugins.config")) {
					jar.close();
					return true;
				}
			jar.close();
		} catch (Throwable t) {
			// obviously not a plugin...
		}
		return false;
	}

}
