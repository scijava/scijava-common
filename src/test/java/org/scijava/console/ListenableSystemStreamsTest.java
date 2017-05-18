
package org.scijava.console;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

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
		assertEquals(TEXT, listener.toString());
	}

	@Test
	public void testListening() {
		// setup
		PrintStream output = bufferingPrintStream();
		OutputStream listener = new ByteArrayOutputStream();
		ListenableSystemStreams.ListenableStream l =
			new ListenableSystemStreams.ListenableStream(output, s -> {});
		// process
		l.addOutputStream(listener);
		l.stream().print(TEXT);
		// test
		assertEquals(TEXT, listener.toString());
		assertEquals(TEXT, output.toString());
	}

	@Test
	public void testBypass() {
		// setup
		PrintStream output = bufferingPrintStream();
		OutputStream listener = new ByteArrayOutputStream();
		ListenableSystemStreams.ListenableStream l =
			new ListenableSystemStreams.ListenableStream(output, s -> {});
		l.addOutputStream(listener);
		// process
		l.bypass().print(TEXT);
		// test
		assertTrue(listener.toString().isEmpty());
		assertEquals(TEXT, output.toString());
	}

	@Test
	public void testSetter() {
		// setup
		List<PrintStream> set = new ArrayList<>();
		PrintStream output = bufferingPrintStream();
		// process
		ListenableSystemStreams.ListenableStream l =
			new ListenableSystemStreams.ListenableStream(output, set::add);
		// test
		assertEquals(1, set.size());
		assertSame(l.stream(), set.get(0));
	}

}
