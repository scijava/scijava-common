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

package org.scijava.ui.viewer;

/**
 * A user interface window associated with a Display, containing a
 * {@link DisplayPanel}.
 * 
 * @author Grant Harris
 * @author Barry DeZonia
 */
public interface DisplayWindow {

	void setTitle(String s);

	void setContent(DisplayPanel panel);

	void pack(); // or reformat, or (re)validate, or somesuch.

	/**
	 * Places this component into the desktop environment. It should do
	 * appropriate size and locate the window. Different types of DisplayWindows
	 * (e.g. Image, Text) can implement this differently; for instance, in a
	 * tabbed enviroment, it is added to the appropriate set of tabs.
	 */
	void showDisplay(boolean visible);

	void requestFocus();

	void close();
	
	/**
	 * Finds the x coordinate on the screen of the origin of the display window's
	 * content.
	 */
	int findDisplayContentScreenX();

	/**
	 * Finds the y coordinate on the screen of the origin of the display window's
	 * content.
	 */
	int findDisplayContentScreenY();

}
