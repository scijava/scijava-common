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

package org.scijava.annotations.legacy;

import java.io.BufferedInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Reads legacy annotation indexes.
 * <p>
 * For backwards-compatibility with SezPoz, this reader falls back to a custom
 * deserializer designed to gracefully handle serialized classes even when the
 * current class path library is incompatible with the one used to serialize the
 * data.
 * </p>
 * 
 * @author Johannes Schindelin
 */
public class LegacyReader {

	private final static short STREAM_MAGIC = (short) 0xaced;
	private final static short STREAM_VERSION = 5;
	private final static byte TC_NULL = (byte) 0x70;
	private final static byte TC_REFERENCE = (byte) 0x71;
	private final static byte TC_CLASSDESC = (byte) 0x72;
	private final static byte TC_OBJECT = (byte) 0x73;
	private final static byte TC_STRING = (byte) 0x74;
	private final static byte TC_BLOCKDATA = (byte) 0x77;
	private final static byte TC_ENDBLOCKDATA = (byte) 0x78;
	private final static byte SC_WRITE_METHOD = 0x01; // if SC_SERIALIZABLE
	private final static byte SC_SERIALIZABLE = 0x02;
	private final static int HANDLE_OFFSET = 0x7e0000;

	private final InputStream in;
	private final List<Object> references;

	public LegacyReader(final InputStream in) throws IOException {
		this.in = new BufferedInputStream(in);
		short signature = (short) read16();
		if (signature != STREAM_MAGIC) {
			throw new IOException("Unrecognized signature: 0x" +
				Integer.toHexString(signature));
		}
		int version = read16();
		if (version != STREAM_VERSION) {
			throw new IOException("Unsupported version: " + version);
		}
		references = new ArrayList<>();
	}

	public void close() throws IOException {
		in.close();
	}

	public Object readObject() throws IOException {
		int c = read8();
		if (c == TC_NULL) {
			return null;
		}
		if (c == TC_STRING) {
			int length = read16();
			byte[] bytes = new byte[length];
			readFully(bytes);
			String s = new String(bytes, "UTF-8");
			references.add(s);
			return s;
		}
		if (c == TC_REFERENCE) {
			int handle = read32() - HANDLE_OFFSET;
			return references.get(handle);
		}
		if (c != TC_OBJECT) {
			throw new IOException("Unexpected token: 0x" + Integer.toHexString(c));
		}

		return readClassDesc().readWithoutClassDesc();
	}

	private ClassDesc readClassDesc() throws IOException {
		int c = read8();
		if (c == TC_REFERENCE) {
			int handle = read32() - HANDLE_OFFSET;
			return (ClassDesc) references.get(handle);
		}
		else if (c == TC_CLASSDESC) {
			return newClassDesc();
		}
		else {
			throw new UnsupportedOperationException("Unexpected token: 0x" +
				Integer.toHexString(c));
		}
	}

	private void expectToken(int token) throws IOException {
		int c = read8();
		if (c != token) {
			throw new UnsupportedOperationException("Unexpected token: 0x" +
				Integer.toHexString(c));
		}
	}

	private int read8() throws IOException {
		return in.read() & 0xff;
	}

	private int read16() throws IOException {
		return (read8() << 8) | read8();
	}

	private int read32() throws IOException {
		return (read16() << 16) | read16();
	}

	private long read64() throws IOException {
		return ((read32() & 0xffffffffl) << 32) | read32();
	}

	private String readString() throws IOException {
		int length = read16();
		byte[] array = new byte[length];
		readFully(array);
		return new String(array);
	}

	private void readFully(byte[] b) throws IOException {
		readFully(b, 0, b.length);
	}

	private void readFully(byte[] b, int offset, int length) throws IOException {
		while (length > 0) {
			int count = in.read(b, offset, length);
			if (count < 0) {
				throw new EOFException("Reached EOF " + length + " bytes too early");
			}
			offset += count;
			length -= count;
		}
	}

	private ClassDesc newClassDesc() throws IOException {
		String className = readString();
		long serialVersionUID = read64();
		String rawName = "L" + className.replace('.', '/') + ";";
		ClassDesc result = classDescs.get(rawName);
		if (result == null) {
			throw new IOException("Could not find class for " + className +
				", serial " + serialVersionUID);
		}
		references.add(result);

		int flags = read8();
		if ((flags & ~(SC_SERIALIZABLE | SC_WRITE_METHOD)) != 0) {
			throw new UnsupportedOperationException("Cannot handle flags: 0x" +
				Integer.toHexString(flags));
		}

		int fieldCount = read16();
		if (fieldCount != result.fields.size()) {
			throw new IOException("Incompatible field count: " + fieldCount + " vs " +
				result.fields.size());
		}
		result.order = new String[fieldCount];
		for (int i = 0; i < fieldCount; i++) {
			int typeChar = read8();
			String fieldName = readString();
			if (!result.fields.containsKey(fieldName)) {
				throw new IOException("Unexpected field " + fieldName);
			}
			result.order[i] = fieldName;
			String type = "" + (char) typeChar;
			if (typeChar == '[' || typeChar == 'L') {
				type = (String) readObject();
			}
			if (!type.equals(result.fields.get(fieldName).className)) {
				throw new IOException(fieldName + " has type " + type +
					" instead of expected " + result.fields.get(fieldName).className);
			}
		}
		expectToken(TC_ENDBLOCKDATA);
		int c = read8();
		@SuppressWarnings("unused")
		Object superClassDesc = null;
		if (c == TC_CLASSDESC) {
			superClassDesc = newClassDesc();
		}
		else if (c == TC_REFERENCE) {
			int handle = read32() - HANDLE_OFFSET;
			superClassDesc = references.get(handle);
		}
		else if (c != TC_NULL) {
			throw new UnsupportedOperationException("Unexpected token: 0x" +
				Integer.toHexString(c));
		}

		return result;
	}

	private abstract class ClassDesc {

		protected String[] order;
		protected final String className;
		protected Map<String, ClassDesc> fields;

		protected ClassDesc(final String className) {
			this.className = className;
			classDescs.put(className, this);
		}

		protected abstract Object read() throws IOException;

		protected Object readWithoutClassDesc() throws IOException {
			return read();
		}
	}

	private class NonPrimitiveClassDesc extends ClassDesc {

		protected NonPrimitiveClassDesc(final String className,
			final Object... fields)
		{
			super(className);
			if ((fields.length % 2) != 0) {
				throw new RuntimeException("That's odd: " + fields.length);
			}
			this.fields = new LinkedHashMap<>();
			for (int i = 0; i < fields.length; i += 2) {
				String name = (String) fields[i];
				ClassDesc classDesc;
				if (fields[i + 1] instanceof ClassDesc) {
					classDesc = (ClassDesc) fields[i + 1];
				}
				else if (fields[i + 1] instanceof String) {
					classDesc = classDescs.get(fields[i + 1]);
					if (classDesc == null) {
						throw new RuntimeException("Could not find class desc for " +
							fields[i + 1]);
					}
				}
				else {
					throw new RuntimeException("Invalid class desc: " + fields[i + 1]);
				}
				this.fields.put(name, classDesc);
			}
		}

		@Override
		protected Object read() throws IOException {
			int c = read8();
			if (c == TC_NULL) {
				return null;
			}
			else if (c != TC_OBJECT) {
				throw new IOException("Unexpected token: " + Integer.toHexString(c));
			}
			final ClassDesc classDesc = readClassDesc();
			if (classDesc != this) {
				throw new IOException("ClassDesc mismatch: " + className +
					" was expected, but got " + classDesc.className);
			}
			return readWithoutClassDesc();
		}

		@Override
		protected final Object readWithoutClassDesc() throws IOException {
			final Map<String, Object> map = new LinkedHashMap<>();
			int index = references.size();
			references.add(map);
			for (final String fieldName : order) {
				final ClassDesc classDesc = fields.get(fieldName);
				map.put(fieldName, classDesc.read());
			}
			Object o = readExtra(map);
			if (o != map) {
				references.set(index, o);
			}
			return o;
		}

		/**
		 * @throws IOException If something goes wrong reading the map.
		 */
		protected Object readExtra(Map<String, Object> o) throws IOException {
			return o;
		}
	}

	private class BoxedPrimitiveClassDesc extends NonPrimitiveClassDesc {

		private final Class<?> clazz;

		public BoxedPrimitiveClassDesc(final Class<?> clazz, final String simpleName)
		{
			super(toSimpleName(clazz), "value", simpleName);
			this.clazz =
				clazz == Double.class || clazz == Float.class ? Double.class
					: clazz == Boolean.class ? Boolean.class : Long.class;
		}

		@Override
		public Object readExtra(final Map<String, Object> map) throws IOException {
			return clazz.cast(map.get("value"));
		}
	}

	private class InterfaceClassDesc extends NonPrimitiveClassDesc {

		protected InterfaceClassDesc(final Class<?> clazz) {
			super(toSimpleName(clazz));
		}

		@Override
		public Object read() throws IOException {
			return readObject();
		}
	}

	public static String toSimpleName(Class<?> clazz) {
		return "L" + clazz.getName().replace('.', '/') + ";";
	}

	private final Map<String, ClassDesc> classDescs =
		new HashMap<>();

	{
		new ClassDesc("B") {

			@Override
			public Object read() throws IOException {
				return (long) (byte) read8();
			}
		};
		new ClassDesc("C") {

			@Override
			public Object read() throws IOException {
				return "" + (char) read16();
			}
		};
		new ClassDesc("D") {

			@Override
			public Object read() throws IOException {
				return Double.longBitsToDouble(read64());
			}
		};
		new ClassDesc("F") {

			@Override
			public Object read() throws IOException {
				return (double) Float.intBitsToFloat(read32());
			}
		};
		new ClassDesc("I") {

			@Override
			public Object read() throws IOException {
				return (long) read32();
			}
		};
		new ClassDesc("J") {

			@Override
			public Object read() throws IOException {
				return read64();
			}
		};
		new ClassDesc("S") {

			@Override
			public Object read() throws IOException {
				return (long) (short) read16();
			}
		};
		new ClassDesc("Z") {

			@Override
			public Object read() throws IOException {
				return read8() != 0;
			}
		};
		new ClassDesc("Ljava/lang/String;") {

			@Override
			public Object read() throws IOException {
				return readObject();
			}
		};
		new BoxedPrimitiveClassDesc(Boolean.class, "Z");
		new BoxedPrimitiveClassDesc(Byte.class, "B");
		new BoxedPrimitiveClassDesc(Short.class, "S");
		new BoxedPrimitiveClassDesc(Integer.class, "I");
		new BoxedPrimitiveClassDesc(Long.class, "J");
		new BoxedPrimitiveClassDesc(Float.class, "F");
		new BoxedPrimitiveClassDesc(Double.class, "D");
		new InterfaceClassDesc(Number.class);
		new InterfaceClassDesc(Comparator.class);
		new NonPrimitiveClassDesc(toSimpleName(Character.class), "value", "C") {

			@Override
			public Object readExtra(final Map<String, Object> map) {
				return map.get("value");
			}
		};
		new NonPrimitiveClassDesc(toSimpleName(TreeMap.class), "comparator",
			"Ljava/util/Comparator;")
		{

			// implements serialVersionUID = 919286545866124006L
			@Override
			public Map<String, Object> readExtra(final Map<String, Object> map)
				throws IOException
			{
				if (map.size() != 1 || !map.containsKey("comparator")) {
					throw new IOException("Unexpected comparator");
				}

				map.clear(); // might just as well re-use it
				expectToken(TC_BLOCKDATA);
				expectToken(4);
				int size = read32();
				for (int i = 0; i < size; i++) {
					final String key = (String) readObject();
					final Object value = readObject();
					map.put(key, value);
				}
				expectToken(TC_ENDBLOCKDATA);
				return map;
			}
		};
		new NonPrimitiveClassDesc(toSimpleName(ArrayList.class), "size", "I") {

			// implements serialVersionUID 8683452581122892189L
			@Override
			public Object readExtra(final Map<String, Object> map) throws IOException
			{
				int size = (int) (long) (Long) map.get("size");

				map.clear(); // might just as well re-use it
				expectToken(TC_BLOCKDATA);
				expectToken(4);
				int capacity = read32();
				final List<Object> list = new ArrayList<>(capacity);
				for (int i = 0; i < size; i++) {
					list.add(readObject());
				}
				expectToken(TC_ENDBLOCKDATA);
				return list;
			}
		};
		new NonPrimitiveClassDesc("Lnet/java/sezpoz/impl/SerAnnotatedElement;",
			"isMethod", "Z", "className", "Ljava/lang/String;", "memberName",
			"Ljava/lang/String;", "values", "Ljava/util/TreeMap;")
		{

			@Override
			public Object readExtra(final Map<String, Object> map) throws IOException
			{
				map.put("class", map.get("className"));
				return map;
			}
		};
		new NonPrimitiveClassDesc("Lnet/java/sezpoz/impl/SerAnnConst;", "name",
			"Ljava/lang/String;", "values", "Ljava/util/TreeMap;")
		{

			@Override
			public Object readExtra(final Map<String, Object> map) throws IOException
			{
				return map.get("values");
			}
		};
		new NonPrimitiveClassDesc("Lnet/java/sezpoz/impl/SerEnumConst;",
			"enumName", "Ljava/lang/String;", "constName", "Ljava/lang/String;")
		{

			@Override
			public Object readExtra(final Map<String, Object> map) throws IOException
			{
				map.put("enum", map.get("enumName"));
				map.put("value", map.get("constName"));
				return map;
			}
		};
		new NonPrimitiveClassDesc("Lnet/java/sezpoz/impl/SerTypeConst;", "name",
			"Ljava/lang/String;")
		{

			@Override
			public Object readExtra(final Map<String, Object> map) throws IOException
			{
				return map.get("name");
			}
		};
	}
}
