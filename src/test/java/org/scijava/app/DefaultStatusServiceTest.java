package org.scijava.app;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;
import org.scijava.Context;
import org.scijava.Contextual;
import org.scijava.app.event.StatusEvent;
import org.scijava.event.EventHandler;
import org.scijava.plugin.Parameter;

public class DefaultStatusServiceTest {
	Context context;
	StatusListener statusListener;
	BlockingQueue<StatusEvent> queue;
	StatusService statusService;
	class StatusListener implements Contextual {
		int progress;
		int maximum;
		String status;
		boolean warning;
		@Parameter
		StatusService statusService;
		
		@Override
		public Context getContext() {
			return context;
		}

		@Override
		public void setContext(Context context) {
			context.inject(this);
		}
		@EventHandler
		void eventHandler(StatusEvent e) {
			try {
				queue.put(new StatusEvent(
						e.getProgressValue(),
						e.getProgressMaximum(),
						e.getStatusMessage(),
						e.isWarning()));
			} catch (InterruptedException e1) {
				e1.printStackTrace();
				fail();
			}
		}
	}

	@Before
	public void setUp() throws Exception {
		context = new Context();
		queue = new ArrayBlockingQueue<StatusEvent>(10);
		statusListener = new StatusListener();
		statusListener.setContext(context);
		statusService = statusListener.statusService; 
	}

	@Test
	public void testShowProgress() {
		statusService.showProgress(15, 45);
		try {
			final StatusEvent event = queue.poll(10, TimeUnit.SECONDS);
			assertEquals(event.getProgressValue(), 15);
			assertEquals(event.getProgressMaximum(), 45);
			assertFalse(event.isWarning());
		} catch (InterruptedException e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void testShowStatusString() {
		final String text = "Hello, world";
		statusService.showStatus(text);
		try {
			final StatusEvent event = queue.poll(10, TimeUnit.SECONDS);
			assertEquals(event.getStatusMessage(), text);
			assertFalse(event.isWarning());
		} catch (InterruptedException e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void testShowStatusIntIntString() {
		final String text = "Working...";
		statusService.showStatus(25, 55, text);
		try {
			final StatusEvent event = queue.poll(10, TimeUnit.SECONDS);
			assertEquals(event.getProgressValue(), 25);
			assertEquals(event.getProgressMaximum(), 55);
			assertEquals(event.getStatusMessage(), text);
			assertFalse(event.isWarning());
		} catch (InterruptedException e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void testWarn() {
		final String text = "Totally hosed";
		statusService.warn(text);
		try {
			final StatusEvent event = queue.poll(10, TimeUnit.SECONDS);
			assertEquals(event.getStatusMessage(), text);
			assertTrue(event.isWarning());
		} catch (InterruptedException e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void testShowStatusIntIntStringBoolean() {
		final String text = "Working and hosed...";
		statusService.showStatus(33, 44, text, true);
		try {
			final StatusEvent event = queue.poll(10, TimeUnit.SECONDS);
			assertEquals(event.getStatusMessage(), text);
			assertEquals(event.getProgressValue(), 33);
			assertEquals(event.getProgressMaximum(), 44);
			assertTrue(event.isWarning());
		} catch (InterruptedException e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void testClearStatus() {
		statusService.clearStatus();
		try {
			final StatusEvent event = queue.poll(10, TimeUnit.SECONDS);
			assertEquals(event.getStatusMessage(), "");
			assertFalse(event.isWarning());
		} catch (InterruptedException e) {
			e.printStackTrace();
			fail();
		}
	}

}
