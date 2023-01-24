/*
 * #%L
 * SciJava Common shared library for SciJava software.
 * %%
 * Copyright (C) 2009 - 2023 SciJava developers.
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

package org.scijava.command;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.scijava.Context;
import org.scijava.InstantiableException;
import org.scijava.module.Module;
import org.scijava.module.ModuleItem;
import org.scijava.module.MutableModuleItem;
import org.scijava.module.process.AbstractPreprocessorPlugin;
import org.scijava.module.process.PreprocessorPlugin;
import org.scijava.plugin.PluginInfo;
import org.scijava.plugin.PluginService;
import org.scijava.widget.InputHarvester;
import org.scijava.widget.NumberWidget;

/**
 * Tests {@link Inputs}.
 *
 * @author Curtis Rueden
 * @author Deborah Schmidt
 */
public class InputsTest {

	private Context context;

	@Before
	public void setUp() {
		context = new Context();
		context.service(PluginService.class);
	}

	@After
	public void tearDown() {
		context.dispose();
	}

	/** Tests single input, no configuration. */
	@Test
	public void testSingleInput() {
		setExpected(new HashMap<String, Object>() {{
			put("sigma", 3.9f);
		}});
		Inputs inputs = new Inputs(context);
		inputs.getInfo().setName("testSingleInput");//TEMP
		addTempInput(inputs, "sigma", Float.class);
		float sigma = (Float) inputs.harvest().get("sigma");
		assertEquals(3.9f, sigma, 0);
	}

	/** Tests two inputs, no configuration. */
	@Test
	public void testTwoInputs() {
		setExpected(new HashMap<String, Object>() {{
			put("name", "Chuckles");
			put("age", 37);
		}});
		Inputs inputs = new Inputs(context);
		inputs.getInfo().setName("testTwoInputs");//TEMP
		addTempInput(inputs, "name", String.class);
		addTempInput(inputs, "age", Integer.class);
		Map<String, Object> values = inputs.harvest();
		String name = (String) values.get("name");
		int age = (Integer) values.get("age");
		assertEquals("Chuckles", name);
		assertEquals(37, age);
	}

	/** Tests inputs with configuration. */
	@Test
	public void testWithConfiguration() {
		setExpected(new HashMap<String, Object>() {{
			put("word", "brown");
			put("opacity", 0.8);
		}});
		Inputs inputs = new Inputs(context);
		inputs.getInfo().setName("testWithConfiguration");//TEMP
		MutableModuleItem<String> wordInput = addTempInput(inputs, "word",
			String.class);
		wordInput.setLabel("Favorite word");
		wordInput.setChoices(Arrays.asList("quick", "brown", "fox"));
		wordInput.setDefaultValue("fox");
		MutableModuleItem<Double> opacityInput = addTempInput(inputs, "opacity",
			Double.class);
		opacityInput.setMinimumValue(0.0);
		opacityInput.setMaximumValue(1.0);
		opacityInput.setDefaultValue(0.5);
		opacityInput.setWidgetStyle(NumberWidget.SCROLL_BAR_STYLE);
		inputs.harvest();
		String word = wordInput.getValue(inputs);
		double opacity = opacityInput.getValue(inputs);
		assertEquals("brown", word);
		assertEquals(0.8, opacity, 0);
	}

	public void setExpected(final Map<String, Object> expected) {
		final PluginInfo<PreprocessorPlugin> info =
			new PluginInfo<PreprocessorPlugin>(MockInputHarvester.class,
				PreprocessorPlugin.class)
		{
			@Override
			public PreprocessorPlugin createInstance() throws InstantiableException {
				final PreprocessorPlugin pp = super.createInstance();
				((MockInputHarvester) pp).setExpected(expected);
				return pp;
			}
		};
		info.setPriority(InputHarvester.PRIORITY);
		context.service(PluginService.class).addPlugin(info);
	}

	/**
	 * Add a non-persisted input to ensure we are testing with the mock input
	 * harvester.
	 */
	private static <T> MutableModuleItem<T> addTempInput(Inputs inputs,
		String inputName, Class<T> inputType)
	{
		MutableModuleItem<T> input = inputs.addInput(inputName, inputType);
		input.setPersisted(false);
		return input;
	}

	public static class MockInputHarvester extends AbstractPreprocessorPlugin {
		private Map<String, Object> expected;
		public void setExpected(final Map<String, Object> expected) {
			this.expected = expected;
		}

		@Override
		public void process(final Module module) {
			for (final ModuleItem<?> input : module.getInfo().inputs()) {
				if (module.isInputResolved(input.getName())) continue;
				final String name = input.getName();
				if (!expected.containsKey(name)) {
					throw new AssertionError("No value for input: " + input.getName());
				}
				final Object value = expected.get(name);
				module.setInput(name, value);
			}
		}
	}
}
