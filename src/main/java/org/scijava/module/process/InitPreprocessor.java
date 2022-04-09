/*
 * #%L
 * SciJava Common shared library for SciJava software.
 * %%
 * Copyright (C) 2009 - 2022 SciJava developers.
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

package org.scijava.module.process;

import org.scijava.Cancelable;
import org.scijava.Priority;
import org.scijava.log.LogService;
import org.scijava.module.MethodCallException;
import org.scijava.module.Module;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 * A preprocessor plugin that populates initial parameter values.
 * <p>
 * This is done via a single call to {@link Module#initialize()}.
 * </p>
 * 
 * @author Curtis Rueden
 */
@Plugin(type = PreprocessorPlugin.class, priority = Priority.HIGH)
public class InitPreprocessor extends AbstractPreprocessorPlugin {

	@Parameter(required = false)
	private LogService log;

	// -- ModuleProcessor methods --

	@Override
	public void process(final Module module) {
		try {
			module.initialize();
			if (isCanceled(module)) cancel(getCancelReason(module));
		}
		catch (final MethodCallException exc) {
			if (log != null) log.error(exc);
			final String moduleClass = module.getInfo().getDelegateClassName();
			cancel("The module \"" + moduleClass + "\" failed to initialize.");
		}
	}

	// -- Helper methods --

	private boolean isCanceled(final Module module) {
		return module instanceof Cancelable && ((Cancelable) module).isCanceled();
	}

	private String getCancelReason(final Module module) {
		if (!(module instanceof Cancelable)) return null;
		final String cancelReason = ((Cancelable) module).getCancelReason();
		return cancelReason == null ? "" : cancelReason;
	}

}
