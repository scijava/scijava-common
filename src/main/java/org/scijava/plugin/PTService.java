/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2013 Board of Regents of the University of
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

package org.scijava.plugin;

import java.util.List;

import org.scijava.service.SciJavaService;

/**
 * A service for managing a particular sort of {@link SciJavaPlugin}.
 * <p>
 * There are many kinds of services, but most of them share one common
 * characteristic: they provide API specific to a particular type of plugin. A
 * few examples from ImageJ:
 * </p>
 * <ul>
 * <li>{@code imagej.command.CommandService} works with
 * {@code imagej.command.Command}s.</li>
 * <li>{@code imagej.text.TextService} works with {@code imagej.text.TextFormat}
 * s.</li>
 * <li>{@code imagej.platform.PlatformService} works with
 * {@code imagej.platform.Platform}s.</li>
 * </ul>
 * <p>
 * Most services fit this pattern in one way or another. When you wish to
 * provide a new extensibility point, you create a new type of
 * {@link SciJavaPlugin}, and a corresponding {@link PTService} for working with
 * it. Depending on the nature of your new plugin type, this service might be a
 * {@link SingletonService} (such as {@code imagej.platform.PlatformService}), a
 * {@link HandlerService} (such as {@code imagej.text.TextService}) or a
 * {@link WrapperService} (such as {@code imagej.widget.WidgetService}).
 * </p>
 * <p>
 * It is named {@code PTService} rather than {@code PluginTypeService} or
 * similar to avoid confusion with A) the {@link PluginService} itself, and B)
 * any other service interface intended to define the API of a concrete service.
 * In contrast to such services, the {@code PTService} is a more general layer
 * in a type hierarchy intended to ease creation of services that fit its
 * pattern.
 * </p>
 * 
 * @author Curtis Rueden
 * @param <PT> Plugin type of the plugins being managed.
 * @see SingletonService
 * @see TypedService
 * @see WrapperService
 */
public interface PTService<PT extends SciJavaPlugin> extends SciJavaService {

	/**
	 * Gets the service responsible for discovering and managing this service's
	 * plugins.
	 */
	PluginService getPluginService();

	/** Gets the plugins managed by this service. */
	List<PluginInfo<PT>> getPlugins();

	/** Gets the type of plugins managed by this service. */
	Class<PT> getPluginType();

}
