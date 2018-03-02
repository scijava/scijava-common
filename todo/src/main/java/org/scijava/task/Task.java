/*-
 * #%L
 * SciJava Common shared library for SciJava software.
 * %%
 * Copyright (C) 2009 - 2017 Board of Regents of the University of
 * Wisconsin-Madison, Broad Institute of MIT and Harvard, Max Planck
 * Institute of Molecular Cell Biology and Genetics, University of
 * Konstanz, and KNIME GmbH.
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
 * @author Curtis Rueden
 */
public interface Task extends Cancelable, Named {

	/**
	 * Starts running the task.
	 *
	 * @throws IllegalStateException if the task was already started.
	 */
	void run(Runnable r);

	/**
	 * Waits for the task to complete.
	 *
	 * @throws IllegalStateException if {@link #run} has not been called yet.
	 * @throws InterruptedException if the task is interrupted.
	 * @throws ExecutionException if the task throws an exception while running.
	 */
	void waitFor() throws InterruptedException, ExecutionException;

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
}
