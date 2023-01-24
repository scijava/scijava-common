/*
 * #%L
 * SciJava Common shared library for SciJava software.
 * %%
 * Copyright (C) 2009 - 2023 SciJava developers.
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

package org.scijava.command;

import org.scijava.display.Display;
import org.scijava.display.DisplayService;
import org.scijava.display.event.DisplayActivatedEvent;
import org.scijava.event.EventHandler;
import org.scijava.event.EventService;
import org.scijava.log.LogService;
import org.scijava.log.Logged;
import org.scijava.module.MethodCallException;
import org.scijava.module.ModuleItem;
import org.scijava.plugin.Parameter;
import org.scijava.thread.ThreadService;

/**
 * A command intended to be run interactively.
 * <p>
 * It is {@link Interactive} and {@link Previewable}, with the previews used for
 * interactive exploration.
 * </p>
 * <p>
 * Further, this class provides added convenience for keeping certain input
 * parameters synced with active {@link Display}s. It listens for
 * {@link DisplayActivatedEvent}s, updating the inputs specified in the
 * constructor when such events occur. Individual interactive commands can then
 * add callback methods to affected inputs, for reacting to a change in the
 * active display.
 * </p>
 * 
 * @author Curtis Rueden
 */
public abstract class InteractiveCommand extends DynamicCommand implements
	Interactive, Previewable, Logged
{

	@Parameter
	private DisplayService displayService;

	@Parameter
	private EventService eventService;

	@Parameter
	private ThreadService threadService;

	@Parameter
	private LogService log;

	/** List of names of inputs to keep in sync when the active display changes. */
	private final String[] listenerNames;

	/**
	 * Creates a new interactive command.
	 * 
	 * @param listenerNames The list of names of inputs to keep in sync when the
	 *          active display changes. Each input must be a {@link Display}.
	 */
	public InteractiveCommand(final String... listenerNames) {
		this.listenerNames = listenerNames;
	}

	// -- Previewable methods --

	@Override
	public void preview() {
		// NB: Interactive commands call run upon any parameter change.
		run();
		saveInputs();
	}

	@Override
	public void cancel() {
		// NB: Interactive commands cannot be canceled.
		// That is, closing the non-modal dialog does nothing.
	}

	// -- Logged methods --

	@Override
	public LogService log() {
		return log;
	}

	// -- Internal methods --

	protected void updateInput(final ModuleItem<?> item) {
		final ModuleItem<Display<?>> displayItem = asDisplay(item);
		if (displayItem != null) updateDisplay(displayItem);
		else {
			log.warn("Input '" + item.getName() + "' (" + item.getClass().getName() +
				") is not supported");
		}
	}

	protected <T> ModuleItem<T> asType(final ModuleItem<?> item,
		final Class<T> type)
	{
		if (!type.isAssignableFrom(item.getType())) {
			return null;
		}
		@SuppressWarnings("unchecked")
		final ModuleItem<T> typedItem = (ModuleItem<T>) item;
		return typedItem;
	}

	protected <T> void update(final ModuleItem<T> item, final T newValue) {
		final T oldValue = item.getValue(this);
		if (oldValue != newValue) {
			item.setValue(this, newValue);
			try {
				item.callback(this);
			}
			catch (final MethodCallException exc) {
				log.error(exc);
			}
		}
	}

	// -- Event handlers --

	@EventHandler
	protected void onEvent(
		@SuppressWarnings("unused") final DisplayActivatedEvent evt)
	{
		// NB: Update inputs on a thread *other* than the EDT, to avoid deadlocks.
		// While updating, many of these inputs may interact with the UI (e.g.
		// reporting
		// status) which would require the EDT - thus updateInput() can not
		// be launched from the EDT.
		threadService.run(new Runnable() {

			@Override
			public void run() {
				for (final String listenerName : listenerNames) {
					final ModuleItem<?> item = getInfo().getInput(listenerName);
					updateInput(item);
				}
			}
		});
	}

	// -- Helper methods --

	private ModuleItem<Display<?>> asDisplay(final ModuleItem<?> item) {
		@SuppressWarnings({ "rawtypes", "unchecked" })
		final Class<Display<?>> displayClass = (Class) Display.class;
		return asType(item, displayClass);
	}

	private <D extends Display<?>> void updateDisplay(final ModuleItem<D> item) {
		update(item, displayService.getActiveDisplay(item.getType()));
	}

}
