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

package org.scijava.plugin;

import org.scijava.Typed;

/**
 * A service for managing a particular sort of {@link TypedPlugin}.
 * <p>
 * Note that like {@link PTService}, {@link SingletonService} and
 * {@link WrapperService}, {@code TypedService} is not a service interface
 * defining API for a specific concrete service implementation, but rather a
 * more general layer in a type hierarchy intended to ease creation of services
 * that fit its pattern.
 * </p>
 * 
 * @author Curtis Rueden
 * @param <DT> Base data type of the {@link TypedPlugin}s.
 * @param <PT> Plugin type of the {@link TypedPlugin}s.
 * @see TypedPlugin
 * @see PTService
 */
public interface TypedService<DT, PT extends TypedPlugin<DT>> extends
	PTService<PT>, Typed<DT>
{

	/**
	 * Gets a new instance of the highest priority plugin managed by this service
	 * which supports the given data object according to the {@link Typed}
	 * interface.
	 * <p>
	 * Note that this newly created plugin instance will <em>not</em> actually be
	 * injected with the given data object!
	 * </p>
	 * 
	 * @see HandlerService#getHandler(Object)
	 * @see WrapperService#create(Object)
	 */
	default PT find(final DT data) {
		for (final PluginInfo<PT> plugin : getPlugins()) {
			try {
				final PT instance = pluginService().createInstance(plugin);
				if (instance != null && instance.supports(data)) return instance;
			}
			catch (final Throwable t) {
				log().error("Malfunctioning plugin: " + plugin.getClassName(), t);
			}
		}
		return null;
	}
}
