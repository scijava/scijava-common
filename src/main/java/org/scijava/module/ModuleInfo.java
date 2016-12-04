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

package org.scijava.module;

import java.util.List;

import org.scijava.UIDetails;
import org.scijava.Validated;
import org.scijava.event.EventService;
import org.scijava.module.event.ModulesUpdatedEvent;

/**
 * A ModuleInfo object encapsulates metadata about a particular {@link Module}
 * (but not a specific instance of it). In particular, it can report details on
 * the names and types of inputs and outputs.
 * <p>
 * A special class of implicit input parameters is available: when the
 * {@code name} starts with a dot (e.g. {@code .helloWorld}), no warning is
 * issued about an unmatched input.
 * </p>
 * 
 * @author Aivar Grislis
 * @author Curtis Rueden
 */
public interface ModuleInfo extends UIDetails, Validated {

	/** Gets the input item with the given name. */
	ModuleItem<?> getInput(String name);

	/**
	 * Gets the input item with the given name and type.
	 * 
	 * @throws IllegalArgumentException if the given type is incompatible with the
	 *           named input item
	 */
	<T> ModuleItem<T> getInput(String name, Class<T> type);

	/** Gets the output item with the given name. */
	ModuleItem<?> getOutput(String name);

	/**
	 * Gets the output item with the given name and type.
	 * 
	 * @throws IllegalArgumentException if the given type is incompatible with the
	 *           named output item
	 */
	<T> ModuleItem<T> getOutput(String name, Class<T> type);

	/** Gets the list of input items. */
	List<ModuleItem<?>> inputs();

	/** Gets the list of output items. */
	List<ModuleItem<?>> outputs();

	/**
	 * Gets the fully qualified name of the class containing the module's actual
	 * implementation. By definition, this is the same value returned by
	 * {@code createModule().getDelegateObject().getClass().getName()}, and hence
	 * is also the class containing any callback methods specified by
	 * {@link ModuleItem#getCallback()}.
	 * <p>
	 * The nature of this method is implementation-specific; for example, a
	 * {@code CommandModule} will return the class name of its associated
	 * {@code Command}. For modules that are not commands, the result may be
	 * something else.
	 * </p>
	 * <p>
	 * If you are implementing this interface directly, a good rule of thumb is to
	 * return the class name of the associated {@link Module} (i.e., the same
	 * value given by {@code createModule().getClass().getName()}).
	 * </p>
	 */
	String getDelegateClassName();

	/**
	 * Loads the class containing the module's actual implementation. The name of
	 * the loaded class will match the value returned by
	 * {@link #getDelegateClassName()}.
	 * 
	 * @see org.scijava.Instantiable#loadClass()
	 */
	Class<?> loadDelegateClass() throws ClassNotFoundException;

	/** Instantiates the module described by this module info. */
	Module createModule() throws ModuleException;

	/**
	 * Gets whether the module is intended to be run interactively. Typically this
	 * means its inputs are supposed to be presented in a non-modal dialog box,
	 * with {@link Module#run()} being called whenever any of the values change.
	 */
	boolean isInteractive();

	/**
	 * Gets whether the module supports previews. A preview is a quick
	 * approximation of the results that would be obtained by actually executing
	 * the module with {@link Module#run()}. If this method returns false, then
	 * calling {@link Module#preview()} will have no effect.
	 */
	boolean canPreview();

	/**
	 * Gets whether the module condones cancellation. Strictly speaking, any
	 * module execution can be canceled during preprocessing, but this flag is a
	 * hint that doing so may be a bad idea, and the UI may want to disallow it.
	 * If this method returns false, then calling {@link Module#cancel()} will
	 * have no effect.
	 */
	boolean canCancel();

	/**
	 * Gets whether the module condones headless execution. Strictly speaking,
	 * there is no guarantee that any module will work headless just because it
	 * declares itself so, but this flag hints that headless execution is likely
	 * to succeed (if flag is true), or fail (if flag is false).
	 */
	boolean canRunHeadless();

	/** Gets the function that is called to initialize the module's values. */
	String getInitializer();

	/**
	 * Notifies interested parties that the module info has been modified. This
	 * mechanism is useful for updating any corresponding user interface such as
	 * menu items that are linked to the module.
	 * <p>
	 * For classes implementing this interface directly, this method should
	 * publish a {@link ModulesUpdatedEvent} to the event bus (see
	 * {@link AbstractModuleInfo#update(EventService)} for an example).
	 * </p>
	 */
	void update(EventService eventService);

}
