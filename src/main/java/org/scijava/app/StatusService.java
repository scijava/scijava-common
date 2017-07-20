/*
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

package org.scijava.app;

import org.scijava.app.event.StatusEvent;
import org.scijava.service.SciJavaService;

/**
 * Interface for the status notification service.
 * 
 * @author Curtis Rueden
 */
public interface StatusService extends SciJavaService {

	/** Updates the progress bar. */
	void showProgress(int value, int maximum);

	/** Updates the status message. */
	void showStatus(String message);

	/** Updates the status message and progress bar. */
	void showStatus(int progress, int maximum, String message);

	/**
	 * Updates the status message and progress bar, optionally flagging the status
	 * notification as a warning.
	 * 
	 * @param progress New progress value
	 * @param maximum New progress maximum
	 * @param message New status message
	 * @param warn Whether or not this notification constitutes a warning
	 */
	void showStatus(int progress, int maximum, String message, boolean warn);

	/** Issues a warning message. */
	void warn(String message);

	/** Clears the status message. */
	void clearStatus();

	/**
	 * Gets the status message of the given event. In the case of the empty string
	 * (""), an alternative default string will be returned instead using the
	 * application version of the given application.
	 * 
	 * @see StatusEvent#getStatusMessage()
	 * @see App#getInfo(boolean)
	 */
	String getStatusMessage(String appName, StatusEvent statusEvent);

}
