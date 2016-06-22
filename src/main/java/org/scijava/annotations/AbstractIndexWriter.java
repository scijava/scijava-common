/*
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

package org.scijava.annotations;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentSkipListMap;

import javax.lang.model.element.AnnotationValue;

/**
 * Writes annotations as JSON-formatted files.
 * <p>
 * The file names are the names of the annotations, and the serialized data
 * describe the class which was annotated together with the specific annotation
 * fields.
 * </p>
 * 
 * @author Johannes Schindelin
 */
public abstract class AbstractIndexWriter {

	private final Map<String, Map<String, Object>> map =
		new ConcurrentSkipListMap<>();

	protected synchronized boolean foundAnnotations() {
		return !map.isEmpty();
	}

	protected synchronized void add(final Map<String, Object> annotationValues,
		final String annotationName, final String className)
	{
		Map<String, Object> list = map.get(annotationName);
		if (list == null) {
			list = new LinkedHashMap<>();
			map.put(annotationName, list);
		}
		final Map<String, Object> o = new TreeMap<>();
		o.put("class", className);
		o.put("values", annotationValues);
		list.put(className, o);
	}

	public interface StreamFactory {

		InputStream openInput(String annotationName) throws IOException;

		OutputStream openOutput(String annotationName) throws IOException;

		boolean isClassObsolete(String className);
	}

	protected synchronized void write(final StreamFactory factory)
		throws IOException
	{
		for (Entry<String, Map<String, Object>> entry : map.entrySet()) {
			final String annotationName = entry.getKey();
			merge(annotationName, factory);
			final PrintStream out =
				new PrintStream(factory.openOutput(annotationName));
			for (Object o : entry.getValue().values()) {
				writeObject(out, adapt(o));
			}
			out.close();
		}
		map.clear();
	}

	/**
	 * Merges an existing annotation index into the currently-generated one.
	 * <p>
	 * This method is used to read previously-indexed annotations and reconcile
	 * them with the newly-generated ones just.
	 * </p>
	 * 
	 * @param annotationName the name of the annotation for which the index
	 *          contains the annotated classes
	 * @param factory the factory to generate input and output streams given an
	 *          annotation name
	 * @throws IOException
	 */
	protected synchronized void merge(final String annotationName,
		final StreamFactory factory) throws IOException
	{
		final InputStream in = factory.openInput(annotationName);
		if (in == null) {
			return;
		}
		Map<String, Object> m = map.get(annotationName);
		if (m == null) {
			m = new LinkedHashMap<>();
			map.put(annotationName, m);
		}
		/*
		 * To determine whether the index needs to be written out,
		 * we need to keep track of changed entries.
		 */
		int changedCount = m.size();
		boolean hasObsoletes = false;

		final IndexReader reader =
			new IndexReader(in, annotationName + " from " + in);
		try {
			for (;;) {
				@SuppressWarnings("unchecked")
				final Map<String, Object> entry = (Map<String, Object>) reader.next();
				if (entry == null) {
					break;
				}
				final String className = (String) entry.get("class");
				if (factory.isClassObsolete(className)) {
					hasObsoletes = true;
				}
				else if (m.containsKey(className)) {
					if (!hasObsoletes && entry.equals(m.get(className))) {
						changedCount--;
					}
				}
				else {
					m.put(className, entry);
				}
			}
		}
		finally {
			reader.close();
		}
		// if this annotation index is unchanged, no need to write it out again
		if (changedCount == 0 && !hasObsoletes) {
			map.remove(annotationName);
		}
	}

	protected Object adapt(final Object o) {
		if (o instanceof Annotation) {
			return adapt((Annotation) o);
		}
		else if (o instanceof AnnotationValue) {
			return adapt(((AnnotationValue) o).getValue());
		}
		else if (o instanceof Enum) {
			return adapt((Enum<?>) o);
		}
		return o;
	}

	protected <A extends Annotation> Map<String, Object> adapt(A annotation) {
		Map<String, Object> result = new TreeMap<>();
		for (Method method : annotation.annotationType().getMethods())
			try {
				if (method.getDeclaringClass() == annotation.annotationType()) {
					result.put(method.getName(), adapt(method.invoke(annotation)));
				}
			}
			catch (IllegalArgumentException e) {
				e.printStackTrace();
			}
			catch (IllegalAccessException e) {
				e.printStackTrace();
			}
			catch (InvocationTargetException e) {
				e.printStackTrace();
			}
		return result;
	}

	private static Map<String, Object> adapt(Enum<?> e) {
		Map<String, Object> result = new TreeMap<>();
		result.put("enum", e.getClass().getName());
		result.put("value", e.name());
		return result;
	}

	private void writeObject(final PrintStream out, final Object o)
		throws IOException
	{
		if (o == null) {
			out.print("null");
		}
		else if (o instanceof Boolean) {
			out.print((Boolean) o ? "true" : "false");
		}
		else if (o instanceof Byte) {
			out.print((byte) (Byte) o);
		}
		else if (o instanceof Short) {
			out.print((short) (Short) o);
		}
		else if (o instanceof Integer) {
			out.print((int) (Integer) o);
		}
		else if (o instanceof Long) {
			out.print((long) (Long) o);
		}
		else if (o instanceof Float) {
			out.print((float) (Float) o);
		}
		else if (o instanceof Double) {
			out.print((double) (Double) o);
		}
		else if (o instanceof Character) {
			writeString(out, "" + o);
		}
		else if (o instanceof String) {
			writeString(out, (String) o);
		}
		else if (o instanceof Class) {
			writeString(out, ((Class<?>) o).getName());
		}
		else if (o instanceof List) {
			writeArray(out, (List<?>) o);
		}
		else if (o.getClass().isArray()) {
			writeArray(out, o);
		}
		else if (o instanceof Map) {
			writeMap(out, (Map<?, ?>) o);
		}
		else {
			throw new IOException("Cannot handle object of type " + o.getClass());
		}
	}

	protected void writeMap(final PrintStream out, final Object... pairs)
		throws IOException
	{
		if ((pairs.length % 2) != 0) {
			throw new IOException("Key without value!");
		}
		out.write('{');
		for (int i = 0; i < pairs.length; i += 2) {
			if (i > 0) {
				out.write(',');
			}
			writeString(out, (String) pairs[i]);
			out.write(':');
			writeObject(out, pairs[i + 1]);
		}
		out.write('}');
	}

	private void writeMap(final PrintStream out, final Map<?, ?> m)
		throws IOException
	{
		out.write('{');
		boolean first = true;
		for (Map.Entry<?, ?> entry : m.entrySet()) {
			if (first) {
				first = false;
			}
			else {
				out.write(',');
			}
			writeString(out, entry.getKey().toString());
			out.write(':');
			writeObject(out, entry.getValue());
		}
		out.write('}');
	}

	private void writeArray(final PrintStream out, final List<?> list)
		throws IOException
	{
		out.write('[');
		boolean first = true;
		for (Object o : list) {
			if (first) {
				first = false;
			}
			else {
				out.write(',');
			}
			o = adapt(o);
			writeObject(out, o);
		}
		out.write(']');
	}

	private void writeArray(final PrintStream out, final Object o)
		throws IOException
	{
		out.write('[');
		int length = Array.getLength(o);
		for (int i = 0; i < length; i++) {
			if (i > 0) {
				out.write(',');
			}
			writeObject(out, adapt(Array.get(o, i)));
		}
		out.write(']');
	}

	private void writeString(final PrintStream out, final String string) {
		out.write('"');
		for (char c : string.toCharArray()) {
			if (c == '"' || c == '\\') {
				out.write('\\');
				out.write(c);
			}
			else if (c >= ' ' && c <= 0x7f) {
				out.write(c);
			}
			else {
				String hex = Integer.toHexString(c);
				out.print("\\u");
				if (hex.length() < 4) {
					out.print("0000".substring(hex.length()));
				}
				out.print(hex);
			}
		}
		out.write('"');
	}

}
