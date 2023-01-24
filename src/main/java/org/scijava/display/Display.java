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

package org.scijava.display;

import java.util.List;

import org.scijava.Named;
import org.scijava.plugin.Plugin;
import org.scijava.plugin.RichPlugin;

/**
 * A display is a particular type of plugin intended to collect objects for
 * visualization. For example, a {@link TextDisplay} displays {@link String}s.
 * <p>
 * Displays discoverable at runtime must implement this interface and be
 * annotated with @{@link Plugin} with attribute {@link Plugin#type()} =
 * {@link Display}.class. While it possible to create a display merely by
 * implementing this interface, it is encouraged to instead extend
 * {@link AbstractDisplay}, for convenience.
 * </p>
 * 
 * @author Curtis Rueden
 * @author Grant Harris
 * @see Plugin
 * @see DisplayService
 */
public interface Display<T> extends List<T>, RichPlugin, Named {

	/**
	 * Tests whether the display is capable of visualizing objects of the given
	 * class.
	 * 
	 * @param c The class to check for visualization capabilities.
	 * @return True if the display can handle certain objects of the given class;
	 *         false if it cannot visualize any objects of that class.
	 */
	boolean canDisplay(Class<?> c);

	/** Tests whether the display is capable of visualizing the given object. */
	default boolean canDisplay(final Object o) {
		if (o == null) return false;
		return canDisplay(o.getClass());
	}

	/**
	 * Displays the given object in this display.
	 * <p>
	 * This method is essentially the same as {@link #add} except that it accepts
	 * any {@link Object} regardless of type.
	 * </p>
	 * 
	 * @throws IllegalArgumentException if the object cannot be displayed (i.e.,
	 *           if {@link #canDisplay(Object)} returns false).
	 */
	void display(Object o);

	/**
	 * Tests whether the display is currently visualizing the given object.
	 * <p>
	 * Note that this method may behave differently than {@link #contains}; in
	 * general, any time {@link #contains} returns true, this method will also
	 * return true, but not vice versa. For example, in ImageJ, an
	 * {@code ImageDisplay} is a {@code Display<DataView>} but calling
	 * {@code isDisplaying} on a {@code Dataset} (which is not a {@code DataView})
	 * will return true if the {@code ImageDisplay} currently contains a
	 * {@code DatasetView} that wraps that {@code Dataset}.
	 * </p>
	 */
	default boolean isDisplaying(final Object o) {
		return contains(o);
	}

	/** Updates and redraws the display onscreen. */
	void update();

	/** Closes the display and disposes its resources. */
	void close();

}
