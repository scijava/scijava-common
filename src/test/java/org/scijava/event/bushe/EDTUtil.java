/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.scijava.event.bushe;

import java.awt.EventQueue;
import java.awt.Toolkit;

/**
 * @author Michael Bushe
 */
public class EDTUtil {

	/**
	 * Since we are using the event bus from a non-awt thread, stay alive for a
	 * sec to give time for the EDT to start and post the message
	 */
	public static void waitForEDT() {
		EventQueue eventQueue = Toolkit.getDefaultToolkit().getSystemEventQueue();
		long start = System.currentTimeMillis();
		do {
			// wait at least once - plenty of time for the event sent to the queue to
			// get there
			long now = System.currentTimeMillis();
			if (now > start + (1000 * 5)) {
				throw new RuntimeException("Waited too long for the EDT to finish.");
			}
			try {
				Thread.sleep(100);
			}
			catch (Throwable e) {}
		}
		while (eventQueue.peekEvent() != null);
	}
}
