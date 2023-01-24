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

package org.scijava.module.event;

import org.scijava.Cancelable;
import org.scijava.module.Module;
import org.scijava.module.process.ModulePreprocessor;
import org.scijava.widget.InputHarvester;

/**
 * An event indicating a {@link Module} execution has been canceled.
 * <p>
 * Cancelation can occur due to a {@link ModulePreprocessor}, such as an
 * {@link InputHarvester} when the user presses the Cancel button, or due to the
 * module itself implementing the {@link Cancelable} interface and then
 * declaring itself canceled (via the {@link Cancelable#isCanceled()} method
 * returning true) after its {@link Module#run()} method returns.
 * </p>
 * 
 * @author Curtis Rueden
 */
public class ModuleCanceledEvent extends ModuleExecutionEvent {

	private final String reason;

	public ModuleCanceledEvent(final Module module) {
		this(module, null);
	}

	public ModuleCanceledEvent(final Module module, final String reason) {
		super(module);
		this.reason = reason;
	}

	// -- ModuleCanceledEvent methods --

	public String getReason() {
		return reason;
	}

}
