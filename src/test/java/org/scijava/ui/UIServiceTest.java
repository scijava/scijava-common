package org.scijava.ui;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.scijava.display.Display;
import org.scijava.ui.headlessUI.HeadlessUI;
import org.scijava.ui.viewer.DisplayWindow;

/**
 * Tests for {@link DefaultUIService}
 *
 * @author Richard Domander (Royal Veterinary College, London)
 */
public class UIServiceTest {

	@Test
	public void testHeadlessUI() {
		final MockUserInterface mockUI = new MockUserInterface();
		DefaultUIService uiService = new DefaultUIService();
		uiService.setDefaultUI(mockUI);

		// Sanity checks to assure that UIService changes behaviour
		assertFalse(uiService.isHeadless());
		assertTrue(uiService.getDefaultUI() instanceof MockUserInterface);

		uiService.setHeadless(true);

		assertTrue(uiService.isHeadless());
		assertTrue("UIService should return HeadlessUI when running \"headless\"",
			uiService.getDefaultUI() instanceof HeadlessUI);
	}

	private static final class MockUserInterface extends AbstractUserInterface {
		@Override
		public SystemClipboard getSystemClipboard() { return null; }

		@Override
		public DisplayWindow createDisplayWindow(final Display<?> display) { return null; }

		@Override
		public DialogPrompt dialogPrompt(final String message, final String title,
								 final DialogPrompt.MessageType messageType,
								 final DialogPrompt.OptionType optionType) {
			return null;
		}

		@Override
		public void showContextMenu(final String menuRoot, final Display<?> display, final int x, final int y) {}

		@Override
		public boolean requiresEDT() { return false; }

		@Override
		public void dispose() {}
	}
}