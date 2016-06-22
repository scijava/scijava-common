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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.scijava.annotations.legacy.LegacyReader;

/**
 * Reads indexed annotations in JSON format.
 * <p>
 * Example: an annotation {@code @Simple(string1 = "World")} to the
 * {@code Annotated} class would be serialized as
 * 
 * <pre>
 * {
 *     "class":"Annotated",
 *     "values":{
 *         "string1":"World"
 *     }
 * }
 * </pre>
 * 
 * </p>
 * 
 * @author Johannes Schindelin
 */
class IndexReader {

	private final PushbackInputStream in;
	private final String originalISName;

	IndexReader(final InputStream in) {
		this(in, "");
	}

	IndexReader(final InputStream in, final String isName) {
		this.in =
			in instanceof PushbackInputStream ? (PushbackInputStream) in
				: new PushbackInputStream(new BufferedInputStream(in));
		this.originalISName = isName;
	}

	public Object next() throws IOException {
		int c = in.read();
		while (Character.isWhitespace(c)) c = in.read();
		if (c < 0) {
			return null;
		}
		if (c == '{') {
			Map<String, Object> map = new LinkedHashMap<>();
			for (;;) {
				if (expect('"', '}') == 1) {
					return map;
				}
				String key = readString();
				expect(':');
				Object value = next();
				map.put(key, value);
				if (expect(',', '}') == 1) {
					return map;
				}
			}
		}
		if (c == '[') {
			List<Object> list = new ArrayList<>();
			c = in.read();
			if (c == ']') {
				return list;
			}
			in.unread(c);
			for (;;) {
				Object value = next();
				list.add(value);
				if (expect(',', ']') == 1) {
					return list;
				}
			}
		}
		if (c == 't') {
			expect("rue");
			return true;
		}
		if (c == 'f') {
			expect("alse");
			return false;
		}
		if (c == 'n') {
			expect("ull");
			throw new IOException("Invalid stream: contains null");
		}
		if (c == '.' || c == '-' || (c >= '0' && c <= '9')) {
			boolean isInteger = true;
			StringBuilder builder = new StringBuilder();
			for (;;) {
				builder.append((char) c);
				c = in.read();
				if (c == '.' || c == 'e' || c == 'E') {
					isInteger = false;
				}
				else if (c < '0' || c > '9') {
					in.unread(c);
					break;
				}
			}
			c = in.read();
			if (c == 'I' && "-".equals(builder.toString())) {
				expect("nfinity");
				return Double.NEGATIVE_INFINITY;
			}
			in.unread(c);
			if (isInteger) {
				return Long.parseLong(builder.toString());
			}
			return (double) Double.parseDouble(builder.toString());
		}
		if (c == 'I') {
			expect("nfinity");
			return Double.POSITIVE_INFINITY;
		}
		if (c == 'N') {
			expect("aN");
			return Double.NaN;
		}
		if (c == '"') {
			return readString();
		}
		throw new IOException("Unexpected char: '" + (char) c + "'"+
				((originalISName.length()>0) ? " from "+originalISName : ""));
	}

	public void close() throws IOException {
		in.close();
	}

	private String readString() throws IOException {
		StringBuilder builder = new StringBuilder();
		for (;;) {
			int c = in.read();
			if (c == '"') {
				return builder.toString();
			}
			builder.append((char) readCharacter(c));
		}
	}

	private int readCharacter(int first) throws IOException {
		int c = first;
		if (c == '\\') {
			c = in.read();
			if (c == 'u') {
				String hex =
					"" + ((char) in.read()) + ((char) in.read()) + ((char) in.read()) +
						((char) in.read());
				c = Integer.parseInt(hex, 16);
			}
			else if (c != '\\' && c != '"') {
				throw new IOException("Expected '\"' or '\\', got '" + c + "'");
			}
		}
		return c;
	}

	private void expect(char expect) throws IOException {
		int c = in.read();
		if (c != expect) {
			throw new IOException("Expected '" + expect + "', got '" + (char) c + "'");
		}
	}

	private int expect(char a, char b) throws IOException {
		int c = in.read();
		if (c == a) {
			return 0;
		}
		if (c == b) {
			return 1;
		}
		throw new IOException("Expected '" + a + "' or '" + b + "', got '" +
			(char) c + "'"+((originalISName.length()>0) ? " from "+originalISName : ""));
	}

	private void expect(String match) throws IOException {
		for (char c : match.toCharArray()) {
			expect(c);
		}
	}

	private IndexReader() {
		this.in = null;
		this.originalISName="";
	}

	static IndexReader getLegacyReader(final InputStream in) throws IOException {
		final LegacyReader legacy = new LegacyReader(in);
		return new IndexReader() {

			@Override
			public Object next() throws IOException {
				return legacy.readObject();
			}

			@Override
			public void close() throws IOException {
				legacy.close();
			}
		};
	}
}
