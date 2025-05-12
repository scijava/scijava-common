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

package org.scijava.util;

/**
 * This class represents an (X, Y) coordinate pair in integer coordinates.
 * <p>
 * It exists mainly to avoid AWT references to {@link java.awt.Point}.
 * </p>
 * 
 * @author Grant Harris
 * @author Curtis Rueden
 */
public class IntCoords {

	public int x;
	public int y;

	public IntCoords(final int x, final int y) {
		this.x = x;
		this.y = y;
	}

	// -- Object methods --

	@Override
	public String toString() {
		return "[Coords: x=" + x + ", y=" + y + "]";
	}

	@Override
	public boolean equals(final Object o) {
		if (!(o instanceof IntCoords)) return false;
		final IntCoords that = (IntCoords) o;
		return x == that.x && y == that.y;
	}

	@Override
	public int hashCode() {
		// combine 16 least significant bits of x and y
		final int b1 = x & 0xffff;
		final int b2 = y & 0xffff;
		return b1 | (b2 << 16);
	}

}
