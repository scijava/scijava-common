/*-
 * #%L
 * SciJava Common shared library for SciJava software.
 * %%
 * Copyright (C) 2009 - 2016 Board of Regents of the University of
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

package org.scijava.ui;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.scijava.Context;
import org.scijava.display.Display;
import org.scijava.ui.headlessUI.HeadlessUI;
import org.scijava.ui.viewer.DisplayWindow;

/**
 * Tests for {@link DefaultUIService}.
 *
 * @author Richard Domander (Royal Veterinary College, London)
 */
public class UIServiceTest {

	@Test
	public void testHeadlessUI() {
		final Context context = new Context(UIService.class);
		final UIService uiService = context.service(UIService.class);

		final MockUserInterface mockUI = new MockUserInterface();
		uiService.setDefaultUI(mockUI);

		// test non-headless behavior
		uiService.setHeadless(false);
		assertFalse(uiService.isHeadless());
		assertTrue(uiService.getDefaultUI() instanceof MockUserInterface);

		// test headless behavior
		uiService.setHeadless(true);
		assertTrue(uiService.isHeadless());
		assertTrue("UIService should return HeadlessUI when running \"headless\"",
			uiService.getDefaultUI() instanceof HeadlessUI);

		context.dispose();
	}

	private static final class MockUserInterface extends AbstractUserInterface {

		@Override
		public DisplayWindow createDisplayWindow(final Display<?> display) {
			return null;
		}

		@Override
		public DialogPrompt dialogPrompt(final String message, final String title,
			final DialogPrompt.MessageType messageType,
			final DialogPrompt.OptionType optionType)
		{
			return null;
		}

		@Override
		public void showContextMenu(final String menuRoot, final Display<?> display,
			final int x, final int y)
		{}

		@Override
		public boolean requiresEDT() {
			return false;
		}

		@Override
		public void dispose() {}
	}
}
