/*
 * #%L
 * SciJava Common shared library for SciJava software.
 * %%
 * Copyright (C) 2009 - 2024 SciJava developers.
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

package org.scijava.module.run;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import org.scijava.Identifiable;
import org.scijava.module.ModuleInfo;
import org.scijava.module.ModuleService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.run.AbstractCodeRunner;
import org.scijava.run.CodeRunner;

/**
 * Runs the given {@link Identifiable} SciJava module.
 * 
 * @author Curtis Rueden
 * @see ModuleInfo
 */
@Plugin(type = CodeRunner.class)
public class ModuleCodeRunner extends AbstractCodeRunner {

	@Parameter
	private ModuleService moduleService;

	// -- CodeRunner methods --

	@Override
	public void run(final Object code, final Object... args)
		throws InvocationTargetException
	{
		waitFor(moduleService.run(getModuleInfo(code), true, args));
	}

	@Override
	public void run(final Object code, final Map<String, Object> inputMap)
		throws InvocationTargetException
	{
		waitFor(moduleService.run(getModuleInfo(code), true, inputMap));
	}

	// -- Typed methods --

	@Override
	public boolean supports(final Object code) {
		return getModuleInfo(code) != null;
	}

	// -- Helper methods --

	private ModuleInfo getModuleInfo(final Object code) {
		if (!(code instanceof String)) return null;
		final String id = (String) code;
		return moduleService.getModuleById(id);
	}

}
