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

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.NoSuchElementException;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.scijava.console.OutputEvent;
import org.scijava.console.OutputListener;

/**
 * LogRecorder can be used to record {@link LogMessage}s and text outputted to
 * {@link PrintStream}s at the same time. The recorded {@link LogMessage}s and
 * text are stored in a list. New items are always added to the end of the list.
 * Items in the list are either instances of {@link LogMessage} or
 * {@link TaggedLine}.
 *
 * @author Matthias Arzt
 */
@IgnoreAsCallingClass
public class LogRecorder implements LogListener, OutputListener,
	Iterable<Object>
{

	public static final String STDOUT_TAG = "out";
	public static final String STDERR_TAG = "err";
	public static final String GLOBAL_STDOUT_TAG = "global_out";
	public static final String GLOBAL_STDERR_TAG = "global_err";

	private final PrintStream outStream = printStream(STDOUT_TAG);
	private final PrintStream errStream = printStream(STDERR_TAG);
	private final PrintStream globalOutStream = printStream(GLOBAL_STDOUT_TAG);
	private final PrintStream globalErrStream = printStream(GLOBAL_STDERR_TAG);

	private Container recorded = new Container();

	private List<Runnable> observers = new CopyOnWriteArrayList<>();

	private ConcurrentMap<String, PrintStream> printStreams =
		new ConcurrentSkipListMap<>();

	private boolean recordCallingClass = false;

	/**
	 * The {@link Runnable} observer will be executed, after every new log message
	 * or text recorded. The code executed by the {@link Runnable} must by highly
	 * thread safe and must not use any kind of locks.
	 */
	public void addObservers(Runnable observer) {
		observers.add(observer);
	}

	public void removeObserver(Runnable observer) {
		observers.remove(observer);
	}

	/**
	 * The returned Iterator never fails, and will always be updated. Even if an
	 * element is added after an iterator reached the end of the list,
	 * {@link Iterator#hasNext()} will return true again, and
	 * {@link Iterator#next()} will return the newly added element.
	 */
	public Iterator<Object> iterator() {
		return recorded.iterator();
	}

	public Stream<Object> stream() {
		return recorded.stream();
	}

	/**
	 * The returned Iterator never fails, and will always be updated. The Iterator
	 * will only return log messages and text recorded after the iterator has been
	 * created.
	 */
	public Iterator<Object> iteratorAtEnd() {
		return recorded.iteratorAtEnd();
	}

	public void clear() {
		recorded.clear();
	}

	public PrintStream printStream(String tag) {
		return new PrintStream(new StreamAdapter(tag), true);
	}

	public boolean isRecordCallingClass() {
		return recordCallingClass;
	}

	public void setRecordCallingClass(boolean enable) {
		this.recordCallingClass = enable;
	}

	// -- LogListener methods --

	@Override
	public void messageLogged(LogMessage message) {
		if (recordCallingClass) message.attach(CallingClassUtils.getCallingClass());
		recorded.add(message);
		notifyListeners();
	}

	// -- OutputListener methods --

	@Override
	public void outputOccurred(OutputEvent event) {
		if(event.containsLog()) return;
		PrintStream stream = (event.getSource() == OutputEvent.Source.STDOUT)
			? (event.isContextual() ? outStream : globalOutStream)
			: (event.isContextual() ? errStream : globalErrStream);
		stream.append(event.getOutput());
	}

	// -- Helper methods --

	private void notifyListeners() {
		for (Runnable listener : observers)
			listener.run();
	}

	// -- Helper classes --

	/**
	 * This Container manages a list of items. Items can only be added to end of
	 * the list. It's possible to add items, while iterating over the list.
	 * Iterators never fail, and they will always be updated. Even if an element
	 * is added after an iterator reached the end of the list,
	 * {@link Iterator#hasNext()} will return true again, and
	 * {@link Iterator#next()} will return the newly added element. This Container
	 * is fully thread safe.
	 */
	private static class Container implements Iterable<Object> {

		private final AtomicLong lastKey = new AtomicLong(0);

		private final NavigableMap<Long, Object> map =
			new ConcurrentSkipListMap<>();

		private final Object lock = new Object();

		public Stream<Object> stream() {
			Spliterator<Object> spliterator = Spliterators.spliteratorUnknownSize(
				iterator(), Spliterator.ORDERED);
			return StreamSupport.stream(spliterator, /* parallel */ false);
		}

		public Iterator<Object> iterator() {
			return new MyIterator();
		}

		public Iterator<Object> iteratorAtEnd() {
			return new MyIterator(map.lastEntry());
		}

		private long add(Object value) {
			synchronized (lock) {
				long key = lastKey.incrementAndGet();
				map.put(key, value);
				return key;
			}
		}

		private void remove(long key) {
			map.remove(key);
		}

		public void clear() {
			map.clear();
		}

		private class MyIterator implements Iterator<Object> {

			private Map.Entry<Long, Object> entry;

			private Map.Entry<Long, Object> nextEntry = null;

			public MyIterator() {
				this(null);
			}

			public MyIterator(Map.Entry<Long, Object> entry) {
				this.entry = entry;
			}

			@Override
			public boolean hasNext() {
				nextEntry = (entry == null) ? map.firstEntry() : map.higherEntry(entry
					.getKey());
				return nextEntry != null;
			}

			@Override
			public Object next() {
				if (nextEntry == null) if (!hasNext())
					throw new NoSuchElementException();
				entry = nextEntry;
				nextEntry = null;
				return entry.getValue();
			}
		}

	}

	/**
	 * TaggedLine is a pair of a {@link String} and an {@link Object} used to tag
	 * it.
	 */
	public static class TaggedLine {

		private final String line;

		private final String tag;

		TaggedLine(String line, String tag) {
			this.line = line;
			this.tag = tag;
		}

		public String line() {
			return line;
		}

		public Object tag() {
			return tag;
		}

		public boolean isComplete() {
			return line.endsWith("\n");
		}

	}

	/**
	 * Text written to this {@link StreamAdapter} is split into lines and recorded
	 * by {@link LogRecorder}.
	 */
	private class StreamAdapter extends ByteArrayOutputStream {

		private final String tag;

		long lastKey = -1;

		String remainder = "";

		private StreamAdapter(String tag) {
			this.tag = tag;
		}

		@Override
		public void flush() {
			String text = toString();
			if (text.isEmpty()) return;
			splitToLines(text);
			reset();
		}

		public void splitToLines(String s) {
			int indexBefore = 0;
			while (true) {
				int index = s.indexOf("\n", indexBefore) + 1;
				if (index == 0) break;
				completeLine(remainder + s.substring(indexBefore, index));
				remainder = "";
				indexBefore = index;
			}
			remainder = remainder + s.substring(indexBefore);
			if (!remainder.isEmpty()) incompleteLine(remainder);
		}

		private void incompleteLine(String line) {
			if (lastKey >= 0) recorded.remove(lastKey);
			lastKey = recorded.add(new TaggedLine(line, tag));
			notifyListeners();
		}

		private void completeLine(String line) {
			if (lastKey >= 0) recorded.remove(lastKey);
			lastKey = -1;
			recorded.add(new TaggedLine(line, tag));
			notifyListeners();
		}

	}

}
