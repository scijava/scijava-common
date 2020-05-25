/*
 * #%L
 * SciJava Common shared library for SciJava software.
 * %%
 * Copyright (C) 2009 - 2020 SciJava developers.
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

import org.scijava.log.LogService;
import org.scijava.log.Logger;
import org.scijava.module.Module;
import org.scijava.module.ModuleItem;
import org.scijava.module.ModuleService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 * This {@link PreprocessorPlugin} affects {@link Module}s with a single
 * {@link Parameter} of type {@link Logger}. It will assign a Logger to that
 * Parameter, that is named like the modules class.
 *
 * @author Matthias Arzt
 */
@Plugin(type = PreprocessorPlugin.class)
public class LoggerPreprocessor extends AbstractPreprocessorPlugin {

	@Parameter(required = false)
	private LogService logService;

	@Parameter(required = false)
	private ModuleService moduleService;

	// -- ModuleProcessor methods --

	@Override
	public void process(final Module module) {
		if (logService == null || moduleService == null) return;

		final ModuleItem<?> loggerInput = moduleService.getSingleInput(module,
			Logger.class);
		if (loggerInput == null || !loggerInput.isAutoFill()) return;

		String loggerName = loggerInput.getLabel();
		if(loggerName == null || loggerName.isEmpty())
			loggerName = module.getDelegateObject().getClass().getSimpleName();
		Logger logger = logService.subLogger(loggerName);

		final String name = loggerInput.getName();
		module.setInput(name, logger);
		module.resolveInput(name);
	}

}
