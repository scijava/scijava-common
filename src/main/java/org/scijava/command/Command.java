/*
 * #%L
 * SciJava Common shared library for SciJava software.
 * %%
 * Copyright (C) 2009 - 2025 SciJava developers.
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

package org.scijava.command;

import org.scijava.module.Module;
import org.scijava.plugin.Plugin;
import org.scijava.plugin.PluginService;
import org.scijava.plugin.SciJavaPlugin;

/**
 * {@code Command} is a plugin that is executable. A {@code Command} can be
 * executed as a {@link Module} by wrapping it in an instance of
 * {@link CommandModule}.
 * <p>
 * Commands discoverable at runtime must implement this interface and be
 * annotated with @{@link Plugin} with attribute {@link Plugin#type()} =
 * {@link Command}.class. While it possible to create a command merely by
 * implementing this interface, it is encouraged to instead extend
 * {@link ContextCommand} (or in some cases {@link ModuleCommand},
 * {@link DynamicCommand} or {@link InteractiveCommand}) for convenience.
 * </p>
 * 
 * @author Curtis Rueden
 * @see Plugin
 * @see PluginService
 */
public interface Command extends SciJavaPlugin, Runnable {
	// Command is a plugin that extends Runnable,
	// discoverable via the plugin discovery mechanism.
}
