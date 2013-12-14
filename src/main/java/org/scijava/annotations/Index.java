/*
 * #%L
 * Annotation index (processor and index access library).
 * %%
 * Copyright (C) 2009 - 2013 Board of Regents of the University of Wisconsin-Madison.
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
 * 
 * The views and conclusions contained in the software and documentation are
 * those of the authors and should not be interpreted as representing official
 * policies, either expressed or implied, of any organization.
 * #L%
 */

package org.scijava.annotations;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.URL;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Makes the annotation indexes accessible.
 * <p>
 * You would call it like this:<br />
 * <code>
 * for (IndexItem<MyAnnotation> item : Index.load(MyAnnotation.class)) {<br />
 *    // do something with item.annotation() and/or item.className()<br />
 * }<br />
 * </code>
 * </p>
 * 
 * @author Johannes Schindelin
 */
public class Index<A extends Annotation> implements Iterable<IndexItem<A>> {

	/**
	 * Loads the index of all classes annotated with the specified annotation.
	 * <p>
	 * The specified annotation needs to be annotated with {@link Indexable} for
	 * the annotation indexing to work properly, of course.
	 * </p>
	 * 
	 * @param annotation the annotation type
	 * @return the index
	 * @throws IOException
	 */
	public static <A extends Annotation> Index<A> load(final Class<A> annotation)
	{
		return load(annotation, Thread.currentThread().getContextClassLoader());
	}

	/**
	 * Loads the index of all classes annotated with the specified annotation.
	 * <p>
	 * </p>
	 * 
	 * @param annotation the annotation type
	 * @param loader the class loader to use when loading {@link Class}-type
	 *          annotation fields
	 * @return the index
	 * @throws IOException
	 */
	public static <A extends Annotation> Index<A> load(final Class<A> annotation,
		final ClassLoader loader)
	{
		EclipseHelper.updateAnnotationIndex(loader);
		return new Index<A>(annotation, loader);
	}

	static final String INDEX_PREFIX = "META-INF/json/";
	private static final String LEGACY_INDEX_PREFIX = "META-INF/annotations/";

	private final Class<A> annotation;
	private final ClassLoader loader;

	private Index(final Class<A> annotation, final ClassLoader loader) {
		this.annotation = annotation;
		this.loader = loader;
	}

	private class IndexItemIterator implements Iterator<IndexItem<A>> {

		private Enumeration<URL> urls;
		private IndexReader indexReader;
		private IndexItem<A> next;

		private Map<String, URL> legacyURLs;

		public IndexItemIterator(final Class<A> annotation) {
			try {
				legacyURLs = new LinkedHashMap<String, URL>();
				final Enumeration<URL> legacy =
					loader.getResources(LEGACY_INDEX_PREFIX + annotation.getName());
				final int legacySuffixLength =
					LEGACY_INDEX_PREFIX.length() + annotation.getName().length();
				while (legacy.hasMoreElements()) {
					final URL url = legacy.nextElement();
					final String string = url.toString();
					final String key =
						string.substring(0, string.length() - legacySuffixLength) +
							INDEX_PREFIX + annotation.getName();
					legacyURLs.put(key, url);
				}
				if (legacyURLs.isEmpty()) {
					legacyURLs = null;
				}

				urls = loader.getResources(INDEX_PREFIX + annotation.getName());
				readNext();
			}
			catch (final IOException e) {
				e.printStackTrace();
			}
		}

		private void readNext() throws IOException {
			for (;;) {
				if (indexReader == null) {
					indexReader = getNextReader();
					if (indexReader == null) {
						urls = null;
						next = null;
						return;
					}
				}
				@SuppressWarnings("unchecked")
				final Map<String, Object> map =
					(Map<String, Object>) indexReader.next();
				if (map != null) {
					final String className = (String) map.get("class");
					@SuppressWarnings("unchecked")
					final Map<Object, Object> values =
						(Map<Object, Object>) map.get("values");
					next = new IndexItem<A>(annotation, loader, className, values);
					return;
				}
				indexReader.close();
				indexReader = null;
			}
		}

		private IndexReader getNextReader() throws IOException {
			if (urls == null) {
				return null;
			}
			else if (!urls.hasMoreElements()) {
				if (legacyURLs != null && !legacyURLs.isEmpty()) {
					final Entry<String, URL> entry =
						legacyURLs.entrySet().iterator().next();
					legacyURLs.remove(entry.getKey());
					return IndexReader.getLegacyReader(entry.getValue().openStream());
				}
				return null;
			}
			final URL url = urls.nextElement();
			if (legacyURLs != null) {
				legacyURLs.remove(url.toString());
			}
			return new IndexReader(url.openStream());
		}

		public boolean hasNext() {
			return next != null;
		}

		public IndexItem<A> next() {
			final IndexItem<A> result = next;
			try {
				readNext();
			}
			catch (final IOException e) {
				e.printStackTrace();
				next = null;
			}
			return result;
		}

		public void remove() {
			throw new UnsupportedOperationException();
		}
	}

	public Iterator<IndexItem<A>> iterator() {
		return new IndexItemIterator(annotation);
	}
}
