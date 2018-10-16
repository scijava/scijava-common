/*
 * #%L
 * SciJava Common shared library for SciJava software.
 * %%
 * Copyright (C) 2009 - 2017 Board of Regents of the University of
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

package org.scijava.io.handle;

import java.io.IOException;

import org.scijava.io.location.Location;

public abstract class AbstractSeekableStreamHandle<L extends Location> extends
	AbstractStreamHandle<L> implements SeekableStreamHandle<L>
{

	private long jumpCutoff = 10000;

	@Override
	public void seek(final long pos) throws IOException {

		// how much and which direction we have to jump
		final long delta = pos - offset();

		if (delta == 0) {
			return;
			// nothing to do
		}
		else if (delta > 0) {
			// offset position is "downstream"

			// try to reconnect instead of linearly reading large chunks
			if (recreatePossible() && delta > jumpCutoff) {
				recreateStreamFromPos(pos);
			}
			else {
				jump(delta);
			}

		}
		else { // delta < 0
			// need to recreate the stream
			if (recreatePossible()) {
				recreateStreamFromPos(pos);
			}
			else {
				resetStream();
				jump(pos);
			}
		}
		setOffset(pos);
	}

	/**
	 * Recreates the internal input stream available through {@link #in()}, so
	 * that it starts from the specified position.
	 * 
	 * @param pos
	 * @throws IOException
	 */
	protected abstract void recreateStreamFromPos(long pos) throws IOException;

	/**
	 * In some implementations of this class, the ability to recreate the stream
	 * depends on external factors (e.g. server support). This influences a
	 * 
	 * @return if recreate is actually possible.
	 * @throws IOException
	 */
	protected abstract boolean recreatePossible() throws IOException;

	/**
	 * Sets the maximum of bytes which are read from the stream when seeking
	 * forward. Any larger number will result in a call to
	 * {@link #recreateStreamFromPos(long)}.
	 */
	protected void setJumpCutoff(long jumpCutoff) {
		this.jumpCutoff = jumpCutoff;
	}
}
