/*
 * #%L
 * SciJava Common shared library for SciJava software.
 * %%
 * Copyright (C) 2009 - 2015 Board of Regents of the University of
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

package org.scijava.plugin;

import org.scijava.log.LogService;

/**
 * Abstract base class for {@link WrapperService}s.
 * 
 * @author Curtis Rueden
 * @param <DT> Base data type wrapped by the wrappers.
 * @param <PT> Plugin type of the wrappers.
 */
public abstract class AbstractWrapperService<DT, PT extends WrapperPlugin<DT>>
	extends AbstractTypedService<DT, PT> implements WrapperService<DT, PT>
{

	@Parameter(required = false)
	private LogService log;

	// -- WrapperService methods --

	@Override
	public <D extends DT> PT create(final D data) {
		final PT instance = findWrapper(data);
		if (instance != null) instance.set(data);
		return instance;
	}

	// -- Service methods --

	@Override
	public void initialize() {
		if (log != null) {
			log.debug("Found " + getPlugins().size() + " " +
				getPluginType().getSimpleName() + " plugins.");
		}
	}

	// -- Typed methods --

	@Override
	public boolean supports(final DT data) {
		return findWrapper(data) != null;
	}

	// -- Helper methods --

	private <D extends DT> PT findWrapper(final D data) {
		for (final PluginInfo<PT> plugin : getPlugins()) {
			final PT instance = getPluginService().createInstance(plugin);
			if (instance.supports(data)) return instance;
		}
		return null;
	}

}
