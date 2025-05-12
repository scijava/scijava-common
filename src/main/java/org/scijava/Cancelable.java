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

package org.scijava;

/**
 * An operation that can be canceled.
 * 
 * @author Curtis Rueden
 */
public interface Cancelable {

	/** Gets whether the operation has been canceled. */
	boolean isCanceled();

	/**
	 * Cancels the operation execution, with the given reason for doing so.
	 * <p>
	 * This method merely sets the operation status to canceled; it cannot
	 * necessarily stop the operation itself. That is, it is the responsibility of
	 * each individual operation to check {@link #isCanceled()} in a timely manner
	 * during execution, and stop doing whatever it is doing if the flag has been
	 * tripped.
	 * </p>
	 * 
	 * @param reason A message describing why the operation is being canceled.
	 */
	void cancel(String reason);

	/**
	 * Gets a message describing why the operation was canceled.
	 * 
	 * @return The reason for cancelation, which may be null if no reason was
	 *         given, or if the operation was not in fact canceled.
	 */
	String getCancelReason();

}
