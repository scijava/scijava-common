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

package org.scijava.plugin;

/**
 * Top-level interface for plugins. Plugins discoverable at runtime must
 * implement this interface and be annotated with @{@link Plugin}.
 * <p>
 * What all plugins have in common is that they are declared using an annotation
 * (@{@link Plugin}), and discovered if present on the classpath at runtime.
 * </p>
 * <p>
 * The core types of plugins are as follows:
 * </p>
 * <ul>
 * <li>{@link org.scijava.app.App} - metadata about a SciJava application.</li>
 * <li>{@link org.scijava.command.Command} - plugins that are executable. These
 * plugins typically perform a discrete operation, and are accessible via the
 * application menus.</li>
 * <li>{@link org.scijava.console.ConsoleArgument} - plugins that handle
 * arguments passed to the application as command line parameters.</li>
 * <li>{@link org.scijava.convert.Converter} - plugins which translate objects
 * between data types.</li>
 * <li>{@link org.scijava.display.Display} - plugins that visualize objects,
 * often used to display module outputs.</li>
 * <li>{@link org.scijava.io.IOPlugin} - plugins that read or write data.</li>
 * <li>{@link org.scijava.module.process.PreprocessorPlugin} - plugins that
 * perform preprocessing on modules. A preprocessor plugin is a discoverable
 * {@link org.scijava.module.process.ModulePreprocessor}.</li>
 * <li>{@link org.scijava.module.process.PostprocessorPlugin} - plugins that
 * perform postprocessing on modules. A
 * {@link org.scijava.module.process.PostprocessorPlugin} is a discoverable
 * {@link org.scijava.module.process.ModulePostprocessor}.</li>
 * <li>{@link org.scijava.platform.Platform} - plugins for defining
 * platform-specific behavior.</li>
 * <li>{@link org.scijava.script.ScriptLanguage} - plugins that enable executing
 * scripts in particular languages as SciJava modules.</li>
 * <li>{@link org.scijava.service.Service} - plugins that define new API in a
 * particular area.</li>
 * <li>{@link org.scijava.tool.Tool} - plugins that map user input (e.g.,
 * keyboard and mouse actions) to behavior. They are usually rendered as icons
 * in the application toolbar.</li>
 * <li>{@link org.scijava.widget.InputWidget} - plugins that render UI widgets
 * for the {@link org.scijava.widget.InputHarvester} preprocessor.</li>
 * </ul>
 * 
 * @author Curtis Rueden
 * @see Plugin
 * @see PluginService
 */
public interface SciJavaPlugin {
	// top-level marker interface for discovery via annotation indexes
}
