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

package org.scijava.parse;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.scijava.plugin.Plugin;
import org.scijava.service.AbstractService;
import org.scijava.service.Service;
import org.scijava.sjep.Variable;
import org.scijava.sjep.eval.DefaultEvaluator;
import org.scijava.util.ObjectArray;

/**
 * Default service for parsing strings.
 * 
 * @author Curtis Rueden
 */
@Plugin(type = Service.class)
public class DefaultParseService extends AbstractService implements
	ParseService
{

	@Override
	public Items parse(final String arg, final boolean strict) {
		if (arg == null) throw new NullPointerException("arg must not be null");
		return new ItemsList(arg, strict);
	}

	// -- Helper classes --

	/**
	 * {@link Items} implementation backed by the
	 * <a href="https://github.com/scijava/scijava-expression-parser">SciJava
	 * Expression Parser</a>.
	 */
	private static class ItemsList extends ObjectArray<Item> implements Items {

		public ItemsList(final String arg, final boolean strict) {
			super(Item.class);
			parseItems(arg, strict);
		}

		@Override
		public Map<String, Object> asMap() {
			final LinkedHashMap<String, Object> map =
				new LinkedHashMap<>();
			for (final Item item : this) {
				map.put(item.name(), item.value());
			}
			return map;
		}

		@Override
		public boolean isMap() {
			for (final Item item : this) {
				if (item.name() == null) return false;
			}
			return true;
		}

		@Override
		public boolean isList() {
			for (final Item item : this) {
				if (item.name() != null) return false;
			}
			return true;
		}

		private void parseItems(final String arg, final boolean strict) {
			final DefaultEvaluator e = new DefaultEvaluator();
			e.setStrict(strict);
			final Object result = e.evaluate("(" + arg + ")");
			if (result == null) {
				throw new IllegalStateException("Error parsing string: '" + arg + "'");
			}
			final List<?> list;
			if (result instanceof List) list = (List<?>) result;
			else list = Collections.singletonList(result);

			for (final Object o : list) {
				final String name;
				final Object value;
				if (o instanceof Variable) {
					final Variable v = (Variable) o;
					name = v.getToken();
					value = e.value(v);
				}
				else {
					name = null;
					value = o;
				}
				add(new Item() {

					@Override
					public String name() {
						return name;
					}

					@Override
					public Object value() {
						return value;
					}

				});
			}
		}

	}

}
