/*-
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

package org.scijava.ui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.scijava.Context;
import org.scijava.display.Display;
import org.scijava.ui.headless.HeadlessUI;
import org.scijava.ui.viewer.DisplayWindow;

/**
 * Tests for {@link DefaultUIService}.
 *
 * @author Richard Domander (Royal Veterinary College, London)
 * @author Curtis Rueden
 */
public class UIServiceTest {

	private Context context;
	private UIService uiService;

	@Before
	public void setUp() {
		context = new Context(UIService.class);
		uiService = context.service(UIService.class);
	}

	@After
	public void tearDown() {
		context.dispose();
	}

	@Test
	public void testDefaultUI() {
		assertTrue(uiService.getDefaultUI() instanceof HeadlessUI);
	}

	@Test
	public void testAvailableUIs() {
		final List<UserInterface> uiList = uiService.getAvailableUIs();
		assertEquals(1, uiList.size());
		assertTrue(uiList.get(0) instanceof HeadlessUI);
	}

	@Test
	public void testHeadlessUI() {
		// If true here, before we messed with it, the assumption is that we are
		// in a truly headless environment, not just forced via setHeadless(true).
		boolean reallyHeadless = uiService.isHeadless();

		final MockUserInterface mockUI = new MockUserInterface();
		uiService.setDefaultUI(mockUI);

		// test non-headless behavior
		if (reallyHeadless) {
			// This environment is truly headless, and
			// we should not be able to override it.
			uiService.setHeadless(false);
			assertTrue(uiService.isHeadless());
			assertTrue("UIService should return HeadlessUI when running \"headless\"",
				uiService.getDefaultUI() instanceof HeadlessUI);
		}
		else {
			// This environment is not headless! We can test more things.
			assertSame("UIService default UI override failed",
				mockUI, uiService.getDefaultUI());

			// This environment isn't headless now;
			// let's test overriding it to be so.
			uiService.setHeadless(true);
			assertTrue(uiService.isHeadless());
			assertTrue("UIService should return HeadlessUI when running \"headless\"",
				uiService.getDefaultUI() instanceof HeadlessUI);

			// Now we put it back!
			uiService.setHeadless(false);
			assertFalse(uiService.isHeadless());
			assertSame("UIService default UI override was not restored",
				mockUI, uiService.getDefaultUI());
		}
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
