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

package org.scijava.main;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.service.AbstractService;
import org.scijava.service.Service;
import org.scijava.util.Types;

/**
 * Default implementation of {@link MainService}.
 * 
 * @author Curtis Rueden
 */
@Plugin(type = Service.class)
public class DefaultMainService extends AbstractService implements MainService {

	@Parameter(required = false)
	private LogService log;

	private final List<Main> mains = new ArrayList<>();

	@Override
	public int execMains() {
		int mainCount = 0;
		for (final Main main : mains) {
			main.exec();
			mainCount++;
		}
		return mainCount;
	}

	@Override
	public void addMain(final String className, final String... args) {
		mains.add(new DefaultMain(className, args));
	}

	@Override
	public Main[] getMains() {
		return mains.toArray(new Main[mains.size()]);
	}

	// -- Helper classes --

	/** Default implementation of {@link MainService.Main}. */
	private class DefaultMain implements Main {
		private String className;
		private String[] args;

		public DefaultMain(final String className, final String... args) {
			this.className = className;
			this.args = args.clone();
		}

		@Override
		public String className() {
			return className;
		}

		@Override
		public String[] args() {
			return args;
		}

		@Override
		public void exec() {
			try {
				final Class<?> mainClass = Types.load(className, false);
				final Method main = mainClass.getMethod("main", String[].class);
				main.invoke(null, new Object[] { args });
			}
			catch (final IllegalArgumentException exc) {
				if (log != null) log.error(exc);
			}
			catch (final NoSuchMethodException exc) {
				if (log != null) {
					log.error("No main method for class: " + className, exc);
				}
			}
			catch (final IllegalAccessException exc) {
				if (log != null) log.error(exc);
			}
			catch (final InvocationTargetException exc) {
				if (log != null) log.error(exc);
			}
		}
	}

}
