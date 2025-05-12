/*-
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

package org.scijava.task;

import java.util.concurrent.ExecutionException;

import org.scijava.Cancelable;
import org.scijava.Named;

/**
 * A self-aware job which reports its status and progress as it runs.
 *
 * There are two ways to use a Task object:
 * - A job can be run asynchronously by using {@link Task#run(Runnable)}, and
 * can report its progression from within the Runnable.
 *
 * - A {@link Task} object can simply be used to report in a synchronous manner
 * the progression of a piece of code. In the case of synchronous reporting,
 * the job is considered started when {@link Task#start()} is called and
 * finished when {@link Task#finish()} is called. A finished job can be finished
 * either because it is done or because it has been cancelled.
 *
 * A cancel callback can be set with {@link Task#setCancelCallBack(Runnable)}.
 * The runnable argument will be executed in the case of an external event
 * requesting a cancellation of the task - typically, if a user clicks
 * a cancel button on the GUI, task.cancel("User cancellation requested") will
 * be called. As a result, the task implementors should run the callback.
 * This callback can be used to make the task aware that a cancellation
 * has been requested, and should proceed to stop its execution.
 *
 * See also {@link TaskService}, {@link DefaultTask}
 *
 * @author Curtis Rueden, Nicolas Chiaruttini
 */
public interface Task extends Cancelable, Named {

	/**
	 * Starts running the task - asynchronous job
	 *
	 * @throws IllegalStateException if the task was already started.
	 */
	void run(Runnable r);

	/**
	 * Waits for the task to complete - asynchronous job
	 *
	 * @throws IllegalStateException if {@link #run} has not been called yet.
	 * @throws InterruptedException if the task is interrupted.
	 * @throws ExecutionException if the task throws an exception while running.
	 */
	void waitFor() throws InterruptedException, ExecutionException;

	/**
	 * reports that the task is started - synchronous job
	 */
	default void start() {}

	/**
	 * reports that the task is finished - synchronous job
	 */
	default void finish() {}

	/** Checks whether the task has completed. */
	boolean isDone();

	/** Gets a status message describing what the task is currently doing. */
	String getStatusMessage();

	/**
	 * Gets the step the task is currently performing.
	 *
	 * @return A value between 0 and {@link #getProgressMaximum()} inclusive.
	 * @see #getProgressMaximum()
	 */
	long getProgressValue();

	/**
	 * Gets the number of steps the task performs in total.
	 *
	 * @return Total number of steps the task will perform, or 0 if unknown.
	 * @see #getProgressValue()
	 */
	long getProgressMaximum();

	/**
	 * Sets the status message. Called by task implementors.
	 *
	 * @param status The message to set.
	 * @see #getStatusMessage()
	 */
	void setStatusMessage(String status);

	/**
	 * Sets the current step. Called by task implementors.
	 *
	 * @param step The step vaule to set.
	 * @see #getProgressValue()
	 */
	void setProgressValue(long step);

	/**
	 * Sets the total number of steps. Called by task implementors.
	 *
	 * @param max The step count to set.
	 * @see #getProgressMaximum()
	 */
	void setProgressMaximum(long max);

	/**
	 * If the task is cancelled (external call to {@link Task#cancel(String)}),
	 * the input runnable argument should be executed by task implementors.
	 *
	 * @param runnable : should be executed if this task is cancelled through
	 *          {@link Task#cancel(String)}
	 */
	default void setCancelCallBack(Runnable runnable) {}

	/**
	 * Returns the current cancel callback runnable, This can be used to
	 * concatenate callbacks in order, for instance, to ask for a user
	 * confirmation before cancelling the task
	 */
	default Runnable getCancelCallBack() {
		return () -> {};
	}

}
