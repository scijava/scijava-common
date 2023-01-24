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

import org.scijava.AbstractContextual;
import org.scijava.Cancelable;

/**
 * A command that knows its context. Its service parameters are automatically
 * populated when {@link #setContext} is called, to make it easier to use via
 * Java API calls (i.e., without invoking it via {@link CommandService#run}).
 * This improves compile-time safety of downstream code that calls the command.
 * <p>
 * Here is an example command execution using {@link CommandService#run}:
 * </p>
 * {@code
 * Future<CommandModule<FindEdges>> future =<br/>
 *   commandService.run(findEdges.class, "display", myDisplay);<br/>
 * CommandModule<FindEdges> module = future.get(); // block till complete<br/>
 * ImageDisplay outDisplay = (ImageDisplay) module.getOutput("display");
 * }
 * <p>
 * Note that {@code FindEdges} also has two other inputs, an
 * {@code ImageDisplayService} and an {@code OverlayService}, which get
 * automatically populated when the application context is injected.
 * </p>
 * <p>
 * Here is the same command execution via direct Java calls:
 * </p>
 * {@code
 * FindEdges findEdges = new FindEdges();<br/>
 * findEdges.setContext(context); // populates service parameters<br/>
 * findEdges.setDisplay(myDisplay);<br/>
 * findEdges.run(); // execute on the same thread<br/>
 * ImageDisplay outDisplay = findEdges.getDisplay();
 * }
 * <p>
 * We believe the latter is more intuitive for most Java programmers, and so
 * encourage commands to extend this class and provide API to use them directly.
 * </p>
 * <p>
 * That said, there are times when you cannot extend a particular class (usually
 * because you must extend a different class instead). In that case, you can
 * still implement the {@link Command} interface and end up with a perfectly
 * serviceable command. The consequence is only that other Java programmers will
 * not be able to use the latter paradigm above to invoke your code in a fully
 * compile-time-safe way.
 * </p>
 * 
 * @author Curtis Rueden
 */
public abstract class ContextCommand extends AbstractContextual implements
	Cancelable, Command
{

	/** Reason for cancelation, or null if not canceled. */
	private String cancelReason;

	// -- Cancelable methods --

	@Override
	public boolean isCanceled() {
		return cancelReason != null;
	}

	/** Cancels the command execution, with the given reason for doing so. */
	@Override
	public void cancel(final String reason) {
		cancelReason = reason == null ? "" : reason;
	}

	@Override
	public String getCancelReason() {
		return cancelReason;
	}

}
