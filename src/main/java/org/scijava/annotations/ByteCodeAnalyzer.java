/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2014 Board of Regents of the University of
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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

/**
 * An analyzer to parse {@code &#x40;Plugin} annotations inside a {@code .class}
 * file without loading the class. The idea is to inspect the classfile to parse
 * the annotation attributes.
 * 
 * @author Johannes Schindelin
 */
class ByteCodeAnalyzer {

	private byte[] buffer;
	private int[] poolOffsets;
	private int endOffset, interfacesOffset, fieldsOffset, methodsOffset,
			attributesOffset;
	private Interface[] interfaces;
	private Field[] fields;
	private Method[] methods;
	private Attribute[] attributes;

	private ByteCodeAnalyzer(final byte[] buffer) {
		this.buffer = buffer;
		if ((int) getU4(0) != 0xcafebabe) throw new RuntimeException("No class");
		getConstantPoolOffsets();
		getAllInterfaces();
		getAllFields();
		getAllMethods();
		getAllAttributes();
	}

	private String getPathForClass() {
		final int thisOffset = dereferenceOffset(endOffset + 2);
		if (getU1(thisOffset) != 7) throw new RuntimeException("Parse error");
		return getString(dereferenceOffset(thisOffset + 1));
	}

	private String getClassNameConstant(final int index) {
		final int offset = poolOffsets[index - 1];
		if (getU1(offset) != 7) throw new RuntimeException("Constant " + index +
			" does not refer to a class");
		return getStringConstant(getU2(offset + 1)).replace('/', '.');
	}

	private String getStringConstant(final int index) {
		return getString(poolOffsets[index - 1]);
	}

	private long getIntegerConstant(final int index) {
		final int offset = poolOffsets[index - 1];
		if (getU1(offset) != 3) throw new RuntimeException("Constant " + index +
			" does not refer to an integer");
		return getU4(offset + 1);
	}

	private long getLongConstant(final int index) {
		final int offset = poolOffsets[index - 1];
		if (getU1(offset) != 5) throw new RuntimeException("Constant " + index +
			" does not refer to a long");
		return (getU4(offset + 1) << 32) | getU4(offset + 5);
	}

	private float getFloatConstant(final int index) {
		final int offset = poolOffsets[index - 1];
		if (getU1(offset) != 4) throw new RuntimeException("Constant " + index +
			" does not refer to a float");
		return Float.intBitsToFloat((int) getU4(offset + 1));
	}

	private double getDoubleConstant(final int index) {
		final int offset = poolOffsets[index - 1];
		if (getU1(offset) != 6) throw new RuntimeException("Constant " + index +
			" does not refer to a double");
		return Double.longBitsToDouble((getU4(offset + 1) << 32) |
			getU4(offset + 5));
	}

	private boolean containsDebugInfo() {
		for (final Method method : methods)
			if (method.containsDebugInfo()) return true;
		return false;
	}

	private int dereferenceOffset(final int offset) {
		final int index = getU2(offset);
		return poolOffsets[index - 1];
	}

	private void getConstantPoolOffsets() {
		final int poolCount = getU2(8) - 1;
		poolOffsets = new int[poolCount];
		int offset = 10;
		for (int i = 0; i < poolCount; i++) {
			poolOffsets[i] = offset;
			final int tag = getU1(offset);
			if (tag == 7 || tag == 8) offset += 3;
			else if (tag == 9 || tag == 10 || tag == 11 || tag == 3 || tag == 4 ||
				tag == 12) offset += 5;
			else if (tag == 5 || tag == 6) {
				poolOffsets[++i] = offset;
				offset += 9;
			}
			else if (tag == 1) offset += 3 + getU2(offset + 1);
			else throw new RuntimeException("Unknown tag" + " " + tag);
		}
		endOffset = offset;
	}

	private class ClassNameIterator implements Iterator<String> {

		private int index;

		private ClassNameIterator() {
			index = 0;
			findNext();
		}

		private void findNext() {
			while (++index <= poolOffsets.length)
				if (getU1(poolOffsets[index - 1]) == 7) break;
		}

		@Override
		public boolean hasNext() {
			return index <= poolOffsets.length;
		}

		@Override
		public String next() {
			final int current = index;
			findNext();
			return getClassNameConstant(current);
		}

		@Override
		public void remove() throws UnsupportedOperationException {
			throw new UnsupportedOperationException();
		}
	}

	private class InterfaceIterator implements Iterator<String> {

		private int index, count;

		private InterfaceIterator() {
			index = 0;
			count = getU2(endOffset + 6);
		}

		@Override
		public boolean hasNext() {
			return index < count;
		}

		@Override
		public String next() {
			return getClassNameConstant(getU2(endOffset + 8 + index++ * 2));
		}

		@Override
		public void remove() throws UnsupportedOperationException {
			throw new UnsupportedOperationException();
		}
	}

	private Iterable<String> getInterfaces() {
		return new Iterable<String>() {

			@Override
			public Iterator<String> iterator() {
				return new InterfaceIterator();
			}
		};
	}

	private String getSuperclass() {
		final int index = getU2(endOffset + 4);
		if (index == 0) return null;
		return getClassNameConstant(index);
	}

	private String getSourceFile() {
		for (final Attribute attribute : attributes)
			if (getStringConstant(attribute.nameIndex).equals("SourceFile")) return getStringConstant(getU2(attribute.attributeEndOffset - 2));
		return null;
	}

	@Override
	public String toString() {
		String result = "";
		for (int i = 0; i < poolOffsets.length; i++) {
			final int offset = poolOffsets[i];
			result += "index #" + (i + 1) + ": " + format(offset) + "\n";
			final int tag = getU1(offset);
			if (tag == 5 || tag == 6) i++;
		}
		if (interfaces != null) for (int i = 0; i < interfaces.length; i++)
			result += "interface #" + (i + 1) + ": " + interfaces[i] + "\n";
		if (fields != null) for (int i = 0; i < fields.length; i++)
			result += "field #" + (i + 1) + ": " + fields[i] + "\n";
		if (methods != null) for (int i = 0; i < methods.length; i++)
			result += "method #" + (i + 1) + ": " + methods[i] + "\n";
		if (attributes != null) for (int i = 0; i < attributes.length; i++)
			result += "attribute #" + (i + 1) + ": " + attributes[i] + "\n";
		return result;
	}

	private int getU1(final int offset) {
		return getU1(buffer, offset);
	}

	private int getU2(final int offset) {
		return getU2(buffer, offset);
	}

	private long getU4(final int offset) {
		return getU4(buffer, offset);
	}

	private static int getU1(final byte[] buffer, final int offset) {
		return buffer[offset] & 0xff;
	}

	private static int getU2(final byte[] buffer, final int offset) {
		return getU1(buffer, offset) << 8 | getU1(buffer, offset + 1);
	}

	private static long getU4(final byte[] buffer, final int offset) {
		return ((long) getU2(buffer, offset)) << 16 | getU2(buffer, offset + 2);
	}

	private String getString(final int offset) {
		try {
			return new String(buffer, offset + 3, getU2(offset + 1), "UTF-8");
		}
		catch (final Exception e) {
			return "";
		}
	}

	private String format(final int offset) {
		final int tag = getU1(offset);
		final int u2 = getU2(offset + 1);
		final String result = "offset: " + offset + "(" + tag + "), ";
		if (tag == 7) return result + "class #" + u2;
		if (tag == 9) return result + "field #" + u2 + ", #" + getU2(offset + 3);
		if (tag == 10) return result + "method #" + u2 + ", #" + getU2(offset + 3);
		if (tag == 11) return result + "interface method #" + u2 + ", #" +
			getU2(offset + 3);
		if (tag == 8) return result + "string #" + u2;
		if (tag == 3) return result + "integer " + getU4(offset + 1);
		if (tag == 4) return result + "float " + getU4(offset + 1);
		if (tag == 12) return result + "name and type #" + u2 + ", #" +
			getU2(offset + 3);
		if (tag == 5) return result + "long " + getU4(offset + 1) + ", " +
			getU4(offset + 5);
		if (tag == 6) return result + "double " + getU4(offset + 1) + ", " +
			getU4(offset + 5);
		if (tag == 1) return result + "utf8 " + u2 + " " + getString(offset);
		return result + "unknown";
	}

	private void getAllInterfaces() {
		interfacesOffset = endOffset + 6;
		interfaces = new Interface[getU2(interfacesOffset)];
		for (int i = 0; i < interfaces.length; i++)
			interfaces[i] = new Interface(interfacesOffset + 2 + i * 2);
	}

	private class Interface {

		private int nameIndex;

		private Interface(final int offset) {
			nameIndex = getU2(offset);
		}

		@Override
		public String toString() {
			return getClassNameConstant(nameIndex);
		}
	}

	private void getAllFields() {
		fieldsOffset = interfacesOffset + 2 + 2 * interfaces.length;
		fields = new Field[getU2(fieldsOffset)];
		for (int i = 0; i < fields.length; i++)
			fields[i] =
				new Field(i == 0 ? fieldsOffset + 2 : fields[i - 1].getFieldEndOffset());
	}

	private class Field {

		private int accessFlags, nameIndex, descriptorIndex;
		private Attribute[] fieldAttributes;
		private int fieldEndOffset;

		private Field(final int offset) {
			accessFlags = getU2(offset);
			nameIndex = getU2(offset + 2);
			descriptorIndex = getU2(offset + 4);
			fieldAttributes = getAttributes(offset + 6);
			fieldEndOffset =
				fieldAttributes.length == 0 ? offset + 8
					: fieldAttributes[fieldAttributes.length - 1].attributeEndOffset;
		}

		protected Attribute[] getFieldAttributes() {
			return fieldAttributes;
		}

		protected int getFieldEndOffset() {
			return fieldEndOffset;
		}

		@Override
		public String toString() {
			return getStringConstant(nameIndex) +
				ByteCodeAnalyzer.this.toString(fieldAttributes);
		}
	}

	private void getAllMethods() {
		methodsOffset =
			fields.length == 0 ? fieldsOffset + 2 : fields[fields.length - 1]
				.getFieldEndOffset();
		methods = new Method[getU2(methodsOffset)];
		for (int i = 0; i < methods.length; i++)
			methods[i] =
				new Method(i == 0 ? methodsOffset + 2 : methods[i - 1]
					.getFieldEndOffset());
	}

	private class Method extends Field {

		private Method(final int offset) {
			super(offset);
		}

		private boolean containsDebugInfo() {
			for (final Attribute attribute : getFieldAttributes())
				if (attribute.containsDebugInfo()) return true;
			return false;
		}
	}

	private void getAllAttributes() {
		attributesOffset =
			methods.length == 0 ? methodsOffset + 2 : methods[methods.length - 1]
				.getFieldEndOffset();
		attributes = getAttributes(attributesOffset);
	}

	private Attribute[] getAttributes(final int offset) {
		final Attribute[] result = new Attribute[getU2(offset)];
		for (int i = 0; i < result.length; i++)
			result[i] =
				new Attribute(i == 0 ? offset + 2 : result[i - 1].attributeEndOffset);
		return result;
	}

	private class Attribute {

		int nameIndex;
		byte[] attribute;
		int attributeEndOffset;

		private Attribute(final int offset) {
			nameIndex = getU2(offset);
			attribute = new byte[(int) getU4(offset + 2)];
			System.arraycopy(buffer, offset + 6, attribute, 0, attribute.length);
			attributeEndOffset = offset + 6 + attribute.length;
		}

		private String getName() {
			return getStringConstant(nameIndex);
		}

		private boolean containsDebugInfo() {
			if (!getStringConstant(nameIndex).equals("Code")) return false;
			for (final Attribute attribute : getAttributes())
				if (attribute.getName().equals("LocalVariableTable")) return true;
			return false;
		}

		private Attribute[] getAttributes() {
			final int offset = attributeEndOffset - 6 - attribute.length;
			final int codeLength = (int) getU4(offset + 10);
			final int exceptionTableLength = getU2(offset + 14 + codeLength);
			final int attributesOffset =
				offset + 14 + codeLength + 2 + 8 * exceptionTableLength;
			return ByteCodeAnalyzer.this.getAttributes(attributesOffset);
		}

		@Override
		public String toString() {
			if ("Code".equals(getName())) return "Code" +
				ByteCodeAnalyzer.this.toString(getAttributes());
			return getName() + " (length " + attribute.length +
				")";
		}
	}

	private Map<String, Map<String, Object>> getAnnotations() {
		final Map<String, Map<String, Object>> annotations =
			new TreeMap<String, Map<String, Object>>();
		for (final Attribute attr : attributes) {
			if ("RuntimeVisibleAnnotations".equals(attr.getName())) {
				final byte[] buffer = attr.attribute;
				int count = getU2(buffer, 0);
				int offset = 2;
				for (int i = 0; i < count; i++) {
					final String className =
						raw2className(getStringConstant(getU2(buffer, offset)));
					offset += 2;
					final Map<String, Object> values =
						new TreeMap<String, Object>();
					annotations.put(className, values);
					offset = parseAnnotationValues(buffer, offset, values);
				}
			}
		}
		return annotations;
	}

	private int parseAnnotationValues(final byte[] buffer, int offset,
		final Map<String, Object> values)
	{
		int count = getU2(buffer, offset);
		offset += 2;
		for (int i = 0; i < count; i++) {
			final String key = getStringConstant(getU2(buffer, offset));
			offset += 2;
			offset = parseAnnotationValue(buffer, offset, values, key);
		}
		return offset;
	}

	private int parseAnnotationValue(byte[] buffer, int offset,
		Map<String, Object> map, String key)
	{
		Object value;
		switch (getU1(buffer, offset++)) {
			case 'Z':
				value = Boolean.valueOf(getIntegerConstant(getU2(buffer, offset)) != 0);
				offset += 2;
				break;
			case 'B':
				value = Byte.valueOf((byte) getIntegerConstant(getU2(buffer, offset)));
				offset += 2;
				break;
			case 'C':
				value =
					Character.valueOf((char) getIntegerConstant(getU2(buffer, offset)));
				offset += 2;
				break;
			case 'S':
				value =
					Short.valueOf((short) getIntegerConstant(getU2(buffer, offset)));
				offset += 2;
				break;
			case 'I':
				value =
					Integer.valueOf((int) getIntegerConstant(getU2(buffer, offset)));
				offset += 2;
				break;
			case 'J':
				value = Long.valueOf(getLongConstant(getU2(buffer, offset)));
				offset += 2;
				break;
			case 'F':
				value = Float.valueOf(getFloatConstant(getU2(buffer, offset)));
				offset += 2;
				break;
			case 'D':
				value = Double.valueOf(getDoubleConstant(getU2(buffer, offset)));
				offset += 2;
				break;
			case 's':
				value = getStringConstant(getU2(buffer, offset));
				offset += 2;
				break;
			case 'c':
				value = raw2className(getStringConstant(getU2(buffer, offset)));
				offset += 2;
				break;
			case '[': {
				final Object[] array = new Object[getU2(buffer, offset)];
				offset += 2;
				for (int i = 0; i < array.length; i++) {
					offset = parseAnnotationValue(buffer, offset, map, key);
					array[i] = map.get(key);
				}
				value = array;
				break;
			}
			case 'e': {
				final Map<String, Object> enumValue =
					new TreeMap<String, Object>();
				enumValue.put("enum", raw2className(getStringConstant(getU2(buffer,
					offset))));
				offset += 2;
				enumValue.put("value", getStringConstant(getU2(buffer, offset)));
				offset += 2;
				value = enumValue;
				break;
			}
			case '@': {
				// skipping annotation type
				offset += 2;
				final Map<String, Object> values = new TreeMap<String, Object>();
				offset = parseAnnotationValues(buffer, offset, values);
				value = values;
				break;
			}
			default:
				throw new RuntimeException("Unhandled annotation value type: " +
					(char) getU1(buffer, offset - 1));
		}
		if (value == null) {
			throw new NullPointerException();
		}
		map.put(key, value);
		return offset;
	}

	private static String raw2className(final String rawName) {
		if (!rawName.startsWith("L") || !rawName.endsWith(";")) {
			throw new RuntimeException("Invalid raw class name: " + rawName);
		}
		return rawName.substring(1, rawName.length() - 1).replace('/', '.');
	}

	private String toString(final Attribute[] attributes) {
		String result = "";
		for (final Attribute attribute : attributes)
			result += (result.equals("") ? "(" : ";") + attribute;
		return result.equals("") ? "" : result + ")";
	}

	private static byte[] readFile(final File file) throws IOException {
		final InputStream in = new FileInputStream(file);
		final ByteArrayOutputStream out = new ByteArrayOutputStream();
		final byte[] buffer = new byte[16384];
		for (;;) {
			int count = in.read(buffer);
			if (count < 0) break;
			out.write(buffer, 0, count);
		}
		in.close();
		out.close();
		return out.toByteArray();
	}

	static Map<String, Map<String, Object>> getAnnotations(File file)
		throws IOException
	{
		return new ByteCodeAnalyzer(readFile(file)).getAnnotations();
	}
}
