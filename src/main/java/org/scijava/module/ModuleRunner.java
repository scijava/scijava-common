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

package org.scijava.module;

import java.util.List;
import java.util.concurrent.Callable;

import org.scijava.AbstractContextual;
import org.scijava.Cancelable;
import org.scijava.Context;
import org.scijava.app.StatusService;
import org.scijava.event.EventService;
import org.scijava.log.LogService;
import org.scijava.module.event.ModuleCanceledEvent;
import org.scijava.module.event.ModuleErroredEvent;
import org.scijava.module.event.ModuleExecutedEvent;
import org.scijava.module.event.ModuleExecutingEvent;
import org.scijava.module.event.ModuleFinishedEvent;
import org.scijava.module.event.ModulePostprocessEvent;
import org.scijava.module.event.ModulePreprocessEvent;
import org.scijava.module.event.ModuleStartedEvent;
import org.scijava.module.process.ModulePostprocessor;
import org.scijava.module.process.ModulePreprocessor;
import org.scijava.plugin.Parameter;

/**
 * Helper class for executing a {@link Module}, including pre- and
 * post-processing and event notification.
 * <p>
 * This class implements both {@link Runnable} and {@link Callable}, to make it
 * easier to invoke in a variety of ways, such as with the
 * {@link java.util.concurrent} package.
 * </p>
 * 
 * @author Curtis Rueden
 */
public class ModuleRunner extends AbstractContextual implements
	Callable<Module>, Runnable
{

	private final Module module;
	private final List<? extends ModulePreprocessor> pre;
	private final List<? extends ModulePostprocessor> post;

	@Parameter(required = false)
	private EventService es;

	@Parameter(required = false)
	private StatusService ss;

	@Parameter(required = false)
	private LogService log;

	public ModuleRunner(final Context context, final Module module,
		final List<? extends ModulePreprocessor> pre,
		final List<? extends ModulePostprocessor> post)
	{
		setContext(context);
		this.module = module;
		this.pre = pre;
		this.post = post;
	}

	// -- ModuleRunner methods --

	/**
	 * Feeds the module through the {@link ModulePreprocessor}s.
	 * 
	 * @return The preprocessor that canceled the execution, or null if all
	 *         preprocessors completed successfully.
	 */
	public ModulePreprocessor preProcess() {
		if (pre == null) return null; // no preprocessors

		for (final ModulePreprocessor p : pre) {
			p.process(module);
			if (es != null) es.publish(new ModulePreprocessEvent(module, p));
			if (p.isCanceled()) return p;
		}
		return null;
	}

	/** Feeds the module through the {@link ModulePostprocessor}s. */
	public void postProcess() {
		if (post == null) return; // no postprocessors

		for (final ModulePostprocessor p : post) {
			p.process(module);
			if (es != null) es.publish(new ModulePostprocessEvent(module, p));
		}
	}

	// -- Callable methods --

	@Override
	public Module call() {
		try {
			run();
		}
		catch (final RuntimeException exc) {
			throw new RuntimeException("Module threw exception", exc);
		}
		catch (final Error err) {
			throw new RuntimeException("Module threw error", err);
		}
		return module;
	}

	// -- Runnable methods --

	/**
	 * Executes the module, including pre- and post-processing and event
	 * notification.
	 */
	@Override
	public void run() {
		if (module == null) return;

		final String title = module.getInfo().getTitle();

		try {
			// announce start of execution process
			if (ss != null) ss.showStatus("Running command: " + title);
			if (es != null) es.publish(new ModuleStartedEvent(module));

			// execute preprocessors
			final ModulePreprocessor canceler = preProcess();
			if (canceler != null) {
				// module execution was canceled by preprocessor
				final String reason = canceler.getCancelReason();
				cancel(reason);
				cleanupAndBroadcastCancelation(title, reason);
				return;
			}

			// execute module
			if (es != null) es.publish(new ModuleExecutingEvent(module));
			module.run();
			if (isCanceled()) {
				// module execution was canceled by the module itself
				cleanupAndBroadcastCancelation(title, getCancelReason());
				return;
			}
			if (es != null) es.publish(new ModuleExecutedEvent(module));

			// execute postprocessors
			postProcess();

			// announce completion of execution process
			if (es != null) es.publish(new ModuleFinishedEvent(module));
			if (ss != null) ss.showStatus("Command finished: " + title);
		}
		catch (final Throwable t) {
			cleanupAndBroadcastException(title, t);
			throw t;
		}
	}

	// -- Helper methods --

	private void cleanupAndBroadcastCancelation(final String title,
		final String reason)
	{
		if (ss != null) ss.showStatus("Canceling command: " + title);
		module.cancel();
		if (es != null) es.publish(new ModuleCanceledEvent(module, reason));
		if (ss != null) {
			ss.showStatus("Command canceled: " + title);
			if (reason != null) ss.warn(reason);
		}
	}

	private void cleanupAndBroadcastException(final String title,
		final Throwable t)
	{
		final ModuleErroredEvent evt = new ModuleErroredEvent(module, t);
		if (es != null) es.publish(evt);
		if (log != null && !evt.isConsumed()) {
			// Nothing else handled the error, so log it.
			log.error("Command errored: " + title, t);
		}
	}

	private boolean isCanceled() {
		return module instanceof Cancelable && ((Cancelable) module).isCanceled();
	}

	private String getCancelReason() {
		return module instanceof Cancelable ?
			((Cancelable) module).getCancelReason() : null;
	}

	private void cancel(final String reason) {
		if (!(module instanceof Cancelable)) return;
		((Cancelable) module).cancel(reason);
	}

}
