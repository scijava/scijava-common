/*
 * #%L
 * SciJava Common shared library for SciJava software.
 * %%
 * Copyright (C) 2009 - 2022 SciJava developers.
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

package org.scijava.convert;

import org.scijava.Priority;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.util.Types;

import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * A {@link Converter} that specializes in converting {@link String}s to
 * n-dimensional arrays. This {@link Converter} can convert any array whose
 * component types can be created from a {@link String}. By default, this
 * {@link Converter} delimits the {@link String} based on commas.
 * 
 * @author Gabriel Selzer
 */
@Plugin(type = Converter.class, priority=Priority.VERY_LOW)
public class StringToArrayConverter extends AbstractConverter<String, Object> {

	@Parameter
	private ConvertService convertService;

	@Override
	public boolean canConvert(final Class<?> src, final Class<?> dest) {
		if (src == null) return false;
		final Class<?> saneSrc = Types.box(src);
		final Class<?> saneDest = Types.box(dest);
		return saneSrc == String.class && saneDest.isArray();
	}

	@Override
	public boolean canConvert(final Object src, final Class<?> dest) {
		if (!canConvert(src.getClass(), dest)) return false;
		String srcString = (String) src;
		if (!(srcString.startsWith("{") && srcString.endsWith("}"))) return false;
		List<String> components = elements((String) src);
		// NB this check is merely a heuristic. In the case of a heterogeneous
		// array, canConvert may falsely return positive, if later elements in the
		// string-ified array cannot be converted into Objects. We make this
		// compromise in the interest of speed, however, as ensuring correctness
		// would require a premature conversion of the entire array.
		return components.size() == 0 || convertService.supports(components.get(0),
			dest.getComponentType());
	}

	@Override
	public Object convert(Object src, Type dest) {
		final Type componentType = Types.component(dest);
		if (componentType == null) throw new IllegalArgumentException(dest +
			" is not an array type!");
		return convertToArray((String) src, componentType);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T convert(Object src, Class<T> dest) {
		return (T) convert((String) src, (Type) dest);
	}

	@Override
	public Class<Object> getOutputType() {
		return Object.class;
	}

	@Override
	public Class<String> getInputType() {
		return String.class;
	}

	// -- HELPER METHODS -- //

	/**
	 * Converts {@code src} into an array of component type {@code componentType}
	 * 
	 * @param src the {@link String} to convert
	 * @param componentType the component type of the output array
	 * @return an array of {@code componentType} whose elements were created from
	 *         {@code src}
	 */
	private Object convertToArray(String src, final Type componentType) {
		List<String> elements = elements( src);
		Class<?> componentClass = Types.raw(componentType);
		final Object array = Array.newInstance(componentClass, elements.size());
		for (int i = 0; i < elements.size(); i++)
			Array.set(array, i, convertService.convert(elements.get(i),
				componentClass));
		return array;
	}

	/**
	 * Gets the elements of {@code src}.
	 * 
	 * @param src a {@link String} consisting of:
	 *          <ol>
	 *          <li>A leading curly brace</li>
	 *          <li>Some non-negative number of elements, can be a sublist.</li>
	 *          <li>A trailing curly brace</li>
	 *          </ol>
	 * @return the elements of {@code src}
	 */
	private List<String> elements(String src) {
		// trim off the leading curly brace
		if (src.startsWith("{")) src = src.substring(1);
		// trim off the ending curly brace
		if (src.endsWith("}")) src = src.substring(0, src.length() - 1);
		return splitByComma(src);
	}

	/**
	 * Custom method for {@link String} splitting. Splits on TOP-LEVEL commas.
	 * TODO: Is there a regex for which {@link String#split(String)} would work?
	 * 
	 * @param s the {@link String} to split
	 * @return a {@link List} of substrings, split by a comma.
	 */
	private List<String> splitByComma(String s) {
		int openBraces = 0;
		List<String> arrayList = new ArrayList<>();
		int start = 0;
		for (int i = 0; i < s.length(); i++) {
			switch (s.charAt(i)) {
				case '{':
					openBraces++;
					break;
				case '}':
					openBraces--;
					break;
				case ',':
					if (openBraces == 0) {
						addString(arrayList, s.substring(start, i));
						start = i + 1;
					}
			}
		}
		// Get the last substring
		addString(arrayList, s.substring(start));
		return arrayList;
	}

	/**
	 * Helper method used to filter and format the additions to {@code list}
	 * @param list the {@link List} to add to
	 * @param s the {@link String} to (potentially) be added.
	 */
	private void addString(List<String> list, String s) {
		s = s.trim();
		if (!s.equals("")) list.add(s);
	}

}
