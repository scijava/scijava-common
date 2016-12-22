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

import org.scijava.Identifiable;
import org.scijava.Locatable;
import org.scijava.UIDetails;
import org.scijava.Validated;
import org.scijava.ValidityProblem;
import org.scijava.Versioned;
import org.scijava.event.EventService;
import org.scijava.module.event.ModulesUpdatedEvent;
import org.scijava.util.ClassUtils;
import org.scijava.util.VersionUtils;

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
public interface ModuleInfo extends UIDetails, Validated, Identifiable,
	Locatable, Versioned
{

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
	Iterable<ModuleItem<?>> inputs();

	/** Gets the list of output items. */
	Iterable<ModuleItem<?>> outputs();

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
	default boolean isInteractive() {
		return false;
	}

	/**
	 * Gets whether the module supports previews. A preview is a quick
	 * approximation of the results that would be obtained by actually executing
	 * the module with {@link Module#run()}. If this method returns false, then
	 * calling {@link Module#preview()} will have no effect.
	 */
	default boolean canPreview() {
		return false;
	}

	/**
	 * Gets whether the module condones cancellation. Strictly speaking, any
	 * module execution can be canceled during preprocessing, but this flag is a
	 * hint that doing so may be a bad idea, and the UI may want to disallow it.
	 * If this method returns false, then calling {@link Module#cancel()} will
	 * have no effect.
	 */
	default boolean canCancel() {
		return true;
	}

	/**
	 * Gets whether the module condones headless execution. Strictly speaking,
	 * there is no guarantee that any module will work headless just because it
	 * declares itself so, but this flag hints that headless execution is likely
	 * to succeed (if flag is true), or fail (if flag is false).
	 */
	default boolean canRunHeadless() {
		return false;
	}

	/** Gets the function that is called to initialize the module's values. */
	default String getInitializer() {
		return null;
	}

	/**
	 * Notifies interested parties that the module info has been modified. This
	 * mechanism is useful for updating any corresponding user interface such as
	 * menu items that are linked to the module.
	 */
	default void update(final EventService eventService) {
		eventService.publish(new ModulesUpdatedEvent(this));
	}

	// -- UIDetails methods --

	@Override
	default String getTitle() {
		final String title = UIDetails.super.getTitle();
		if (!title.equals(getClass().getSimpleName())) return title;

		// use delegate class name rather than actual class name
		final String className = getDelegateClassName();
		final int dot = className.lastIndexOf(".");
		return dot < 0 ? className : className.substring(dot + 1);
	}

	// -- Validated methods --

	@Override
	default boolean isValid() {
		return true;
	}

	@Override
	default List<ValidityProblem> getProblems() {
		return null;
	}

	// -- Identifiable methods --

	@Override
	default String getIdentifier() {
		// NB: By default, we assume that the delegate class name uniquely
		// distinguishes the module from others. If the same delegate class is used
		// for more than one module, though, it may need to override this method to
		// provide more differentiating details.
		return "module:" + getDelegateClassName();
	}

	// -- Locatable methods --

	@Override
	default String getLocation() {
		// NB: By default, we use the location of the delegate class.
		// If the same delegate class is used for more than one module, though,
		// it may need to override this method to indicate a different location.
		try {
			return ClassUtils.getLocation(loadDelegateClass()).toExternalForm();
		}
		catch (final ClassNotFoundException exc) {
			return null;
		}
	}

	// -- Versioned methods --

	@Override
	default String getVersion() {
		// NB: By default, we use the version of the delegate class's JAR archive.
		// If the same delegate class is used for more than one module, though,
		// it may need to override this method to indicate a different version.
		try {
			return VersionUtils.getVersion(loadDelegateClass());
		}
		catch (final ClassNotFoundException exc) {
			return null;
		}
	}
}
