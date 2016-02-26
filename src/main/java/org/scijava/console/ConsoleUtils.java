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
package org.scijava.console;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import org.scijava.command.CommandInfo;
import org.scijava.log.LogService;
import org.scijava.module.ModuleInfo;
import org.scijava.module.ModuleItem;

/**
 * Helper class for {@link ConsoleArgument}s.
 *
 * @author Mark Hiner hinerm at gmail.com
 */
public final class ConsoleUtils {

	public static Map<String, Object> parseParameterString(final String parameterString) {
		return parseParameterString(parameterString, (CommandInfo)null);
	}

	public static Map<String, Object> parseParameterString(final String parameterString, final ModuleInfo info) {
		return parseParameterString(parameterString, info, null);
	}

	public static Map<String, Object> parseParameterString(final String parameterString, final LogService logService) {
		return parseParameterString(parameterString, null, logService);
	}

	public static Map<String, Object> parseParameterString(final String parameterString, final ModuleInfo info, final LogService logService) {
		final Map<String, Object> inputMap = new HashMap<String, Object>();

		if (!parameterString.isEmpty()) {
			Iterator<ModuleItem<?>> inputs = null;
			if (info != null) {
				inputs = info.inputs().iterator();
			}
			final String[] pairs = parameterString.split(",");
			for (final String pair : pairs) {
				final String[] split = pair.split("=");
				if (split.length == 2)
					inputMap.put(split[0], split[1]);
				else if (inputs != null && inputs.hasNext() && split.length == 1) {
					inputMap.put(inputs.next().getName(), split[0]);
				}
				else if (logService != null)
					logService.error("Parameters must be formatted as a comma-separated list of key=value pairs");

			}
		}

		return inputMap;

	}

	/**
	 * Test if the next argument is an appropriate parameter to a
	 * {@link ConsoleArgument}.
	 *
	 * @return {@code true} if the first argument of the given list does not
	 *         start with a {@code '-'} character.
	 */
	public static boolean hasParam(final LinkedList<String> args) {
		return !(args.isEmpty() || args.getFirst().startsWith("-"));
	}
}
