
package org.scijava.console;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;

import org.junit.Test;

/**
 * Tests {@link ListenableSystemStreams}
 *
 * @author Matthias Arzt
 */
public class ListenableSystemStreamsTest {

	private final String TEXT = "Hello World!\n";

	private static PrintStream bufferingPrintStream() {
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		return new PrintStream(buffer) {

			@Override
			public String toString() {
				return buffer.toString();
			}
		};
	}

	@Test
	public void testListenerSystemOut() {
		OutputStream listener = new ByteArrayOutputStream();
		ListenableSystemStreams.out().addOutputStream(listener);
		System.out.print(TEXT);
		ListenableSystemStreams.out().removeOutputStream(listener);
		System.out.print(TEXT);
		assertEquals(TEXT, listener.toString());
	}

	@Test
	public void testListening() {
		// setup
		PrintStream output = bufferingPrintStream();
		OutputStream listener = new ByteArrayOutputStream();
		MultiPrintStream multiPrintStream = new MultiPrintStream(output);
		// process
		multiPrintStream.addOutputStream(listener);
		multiPrintStream.print(TEXT);
		// test
		assertEquals(TEXT, listener.toString());
		assertEquals(TEXT, output.toString());
	}

	@Test
	public void testBypass() {
		// setup
		PrintStream output = bufferingPrintStream();
		OutputStream listener = new ByteArrayOutputStream();
		MultiPrintStream multiPrintStream = new MultiPrintStream(output);
		multiPrintStream.addOutputStream(listener);
		// process
		multiPrintStream.bypass().print(TEXT);
		// test
		assertTrue(listener.toString().isEmpty());
		assertEquals(TEXT, output.toString());
	}
}
