/*
 * #%L
 * SciJava Common shared library for SciJava software.
 * %%
 * Copyright (C) 2009 - 2017 Board of Regents of the University of
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

package org.scijava.log;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;

/**
 * @author Matthias Arzt
 */
public class LogRecorderTest {

	private LogRecorder recorder;
	private MyListener listener;

	@Before
	public void setup() {
		recorder = new LogRecorder();
		listener = new MyListener(recorder);
	}

	@Test
	public void testLogMessageDelivery() {
		recorder.clear();
		LogMessage message = newLogMessage();
		recorder.messageLogged(message);
		assertEquals(Arrays.asList(message), listener.messages());
	}

	private LogMessage newLogMessage() {
		return new LogMessage(LogSource.root(), LogLevel.INFO, "Hello World!");
	}

	@Test
	public void testStreamMessageDelivery() {
		listener.clear();
		String message = "Hello World!\n";
		recorder.printStream("").print(message);
		assertTrue(listener.lines().contains(message));
	}

	@Test
	public void testStreamMessageDeliveryPrintLn() {
		listener.clear();
		String message = "Hello World!";
		recorder.printStream("").println(message);
		assertTrue(listener.lines().contains(message + "\n"));
	}

	@Test
	public void testStreamMessageDeliveryMultiLine() {
		listener.clear();
		String line1 = "Hello\n";
		String line2 = "World!\n";
		String line3 = "foo bar";
		recorder.printStream("").print(line1 + line2 + line3);
		assertEquals(Arrays.asList(line1, line2, line3), listener.lines());
	}

	@Test
	public void testSplittedLine() {
		listener.clear();
		String part1 = "Hello ";
		String part2 = "World!\n";
		PrintStream stream = recorder.printStream("");
		stream.print(part1);
		assertEquals(Arrays.asList(part1), listener.lines());
		stream.print(part2);
		assertEquals(Arrays.asList(part1, part1 + part2), listener.lines());
	}

	@Test
	public void testReadOldLog() {
		LogRecorder recorder = new LogRecorder();
		LogMessage message = newLogMessage();
		recorder.messageLogged(message);
		assertEquals(Arrays.asList(message), recorder.stream().collect(Collectors
			.toList()));
	}

	@Test
	public void testReadOldLine() {
		LogRecorder recorder = new LogRecorder();
		String text = "Hello World!";
		recorder.printStream("").println(text);
		assertEquals(Arrays.asList(text + "\n"), recordedLines(recorder));
	}

	@Test
	public void testReadOldIncompleteLine() {
		LogRecorder recorder = new LogRecorder();
		recorder.printStream("").print("Hello");
		assertEquals(Arrays.asList("Hello"), recordedLines(recorder));
	}

	@Test
	public void testReadOldIncompleteLines() {
		LogRecorder recorder = new LogRecorder();
		PrintStream stream = recorder.printStream("");
		stream.print("Hello ");
		stream.print("World!");
		assertEquals(Arrays.asList("Hello World!"), recordedLines(recorder));
	}

	@Test
	public void testReadMultipleStreams() {
		LogRecorder recorder = new LogRecorder();
		PrintStream streamA = recorder.printStream("A");
		PrintStream streamB = recorder.printStream("B");
		streamA.print("Hello ");
		streamB.print("Hello ");
		streamA.print("World!");
		streamB.print("World!");
		assertEquals(Arrays.asList("Hello World!", "Hello World!"), recordedLines(
			recorder));
	}

	private List<String> recordedLines(LogRecorder recorder) {
		return recorder.stream().filter(LogRecorder.TaggedLine.class::isInstance)
			.map(LogRecorder.TaggedLine.class::cast).map(LogRecorder.TaggedLine::line)
			.collect(Collectors.toList());
	}

	@Test
	public void testUpdatedReading() {
		// setup
		LogRecorder recorder = new LogRecorder();
		LogMessage msgA = newLogMessage();
		LogMessage msgB = newLogMessage();
		Iterator<Object> iterator = recorder.iterator();
		// process & test
		assertFalse(iterator.hasNext());
		recorder.messageLogged(msgA);
		assertTrue(iterator.hasNext());
		assertSame(msgA, iterator.next());
		assertFalse(iterator.hasNext());
		recorder.messageLogged(msgB);
		assertTrue(iterator.hasNext());
		assertSame(msgB, iterator.next());
	}

	@Test
	public void testUpdatedStream() {
		// setup
		LogRecorder recorder = new LogRecorder();
		LogMessage msgA = newLogMessage();
		LogMessage msgB = newLogMessage();
		Iterator<Object> iterator = recorder.stream().iterator();
		// process & test
		assertFalse(iterator.hasNext());
		recorder.messageLogged(msgA);
		assertTrue(iterator.hasNext());
		assertEquals(msgA, iterator.next());
		assertFalse(iterator.hasNext());
		recorder.messageLogged(msgB);
		assertTrue(iterator.hasNext());
		assertEquals(msgB, iterator.next());
	}

	private static class MyListener implements Runnable {

		private final Iterator<Object> iterator;

		private List<LogMessage> messages = new LinkedList<>();

		private List<String> lines = new LinkedList<>();

		private MyListener(LogRecorder recorder) {
			this.iterator = recorder.iterator();
			recorder.addObservers(this);
		}

		@Override
		public void run() {
			while (iterator.hasNext()) {
				Object item = iterator.next();
				if (item instanceof LogMessage) messages.add((LogMessage) item);
				if (item instanceof LogRecorder.TaggedLine) lines.add(
					((LogRecorder.TaggedLine) item).line());
			}
		}

		public void clear() {
			messages.clear();
			lines.clear();
		}

		public List<LogMessage> messages() {
			return messages;
		}

		public List<String> lines() {
			return lines;
		}

	}

}
