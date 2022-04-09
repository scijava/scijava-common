/*-
 * #%L
 * SciJava Common shared library for SciJava software.
 * %%
 * Copyright (C) 2009 - 2022 SciJava developers.
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
package org.scijava.task;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.scijava.event.EventService;
import org.scijava.task.event.TaskEvent;
import org.scijava.thread.ThreadService;

/**
 * Default implementation of {@link Task}. Throughout the task (or job),
 * {@link Task#setProgressValue(long)} can be called to inform
 * how the job is progressing.
 *
 * Asynchronous case:
 * - A job (runnable) is sent for execution to the linked {@link ThreadService}.
 * It reports status updates via the linked {@link EventService}.
 * A {@link org.scijava.task.event.TaskEvent} is sent before the job
 * is started and when finished.
 * In the asynchronous case, upon task cancellation ({@link Task#cancel(String)} call),
 * the runnable associated to the ThreadService is attempted to be stopped
 * by calling {@link Future#cancel(boolean)}.
 * This default behaviour can be supplemented by an additional
 * custom callback which can be set in {@link Task#setCancelCallBack(Runnable)}.
 *
 * Synchronous case:
 * - A job that reports its status in between calls of {@link Task#start()},
 * and {@link Task#finish()}. It also reports its status via
 * the linked {@link EventService}.
 * Start and finish calls allow publishing proper {@link org.scijava.task.event.TaskEvent}
 * to subscribers (with the EventService).
 * Upon cancellation of a synchronous task, it is the responsibility
 * of the synchronous task to handle its own cancellation through
 * a custom callback which can be set via {@link Task#setCancelCallBack(Runnable)}.
 *
 * @author Curtis Rueden, Nicolas Chiaruttini
 */
public class DefaultTask implements Task {

	private final ThreadService threadService;
	private final EventService eventService;

	private Future<?> future;

	private boolean canceled;
	private String cancelReason;
	volatile boolean isDone = false;

	private String status;
	private long step;
	private long max;

	private String name;

	private Runnable cancelCallBack;

	/**
	 * Creates a new task.
	 *
	 * @param threadService Service to use for launching the task in its own
	 *          thread. Required.
	 * @param eventService Service to use for reporting status updates as
	 *          {@link TaskEvent}s. May be null, in which case no events are
	 *          reported.
	 */
	public DefaultTask(final ThreadService threadService,
		final EventService eventService)
	{
		this.threadService = threadService;
		this.eventService = eventService;
		cancelCallBack = this::defaultCancelCallback;
	}

	// -- Task methods --

	// - Asynchronous
	@Override
	public void run(final Runnable r) {
		if (r == null) throw new NullPointerException();
		future(r);
	}

	// - Asynchronous
	@Override
	public void waitFor() throws InterruptedException, ExecutionException {
		future().get();
	}

	// - Synchronous
	@Override
	public void start() {
		fireTaskEvent();
	}

	// - Synchronous
	@Override
	public void finish() {
		isDone = true;
		fireTaskEvent();
	}

	@Override
	public boolean isDone() {
		return (isDone) || (future != null && future.isDone());
	}

	@Override
	public String getStatusMessage() {
		return status;
	}

	@Override
	public long getProgressValue() {
		return step;
	}

	@Override
	public long getProgressMaximum() {
		return max;
	}

	@Override
	public void setStatusMessage(final String status) {
		this.status = status;
		fireTaskEvent();
	}

	@Override
	public void setProgressValue(final long step) {
		this.step = step;
		fireTaskEvent();
	}

	@Override
	public void setProgressMaximum(final long max) {
		this.max = max;
		fireTaskEvent();
	}

	// -- Cancelable methods --

	@Override
	public boolean isCanceled() {
		return canceled;
	}

	@Override
	public void cancel(final String reason) {
		canceled = true;
		cancelReason = reason;
		if (cancelCallBack != null) cancelCallBack.run();
		fireTaskEvent();
	}

	void defaultCancelCallback() {
		if (future != null) {
			isDone = future.cancel(true);
		}
	}

	@Override
	public void setCancelCallBack(Runnable r) {
		this.cancelCallBack = r;
	}

	@Override
	public Runnable getCancelCallBack() {
		return this.cancelCallBack;
	}

	@Override
	public String getCancelReason() {
		return cancelReason;
	}

	// -- Named methods --

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(final String name) {
		this.name = name;
	}

	// -- Helper methods --

	private Future<?> future() {
		return future(null);
	}

	private Future<?> future(final Runnable r) {
		if (future == null) initFuture(r);
		return future;
	}

	private synchronized void initFuture(final Runnable r) {
		if (future != null) return;
		if (r == null) throw new IllegalArgumentException("Must call run first");
		future = threadService.run(() -> {
			try {
				fireTaskEvent(); // Triggers an event just before the task is executed
				r.run();
			}
			finally {
				isDone = true;
				fireTaskEvent(); // Triggers an event just after the task has
													// successfully completed or failed
			}
		});
	}

	private void fireTaskEvent() {
		if (eventService != null) eventService.publish(new TaskEvent(this));
	}
}
