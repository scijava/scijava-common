/*
 * #%L
 * SciJava Common shared library for SciJava software.
 * %%
 * Copyright (C) 2009 - 2017 Board of Regents of the University of
 * Wisconsin-Madison, Broad Institute of MIT and Harvard, Max Planck
 * Institute of Molecular Cell Biology and Genetics, University of
 * Konstanz, and KNIME GmbH.
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

package org.scijava.plugin;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import java.util.List;

import org.junit.Test;
import org.scijava.Context;
import org.scijava.InstantiableException;
import org.scijava.Priority;

/**
 * Tests {@link PluginInfo}.
 * 
 * @author Curtis Rueden
 */
public class PluginInfoTest {

	@Test
	public void testNames() throws InstantiableException {
		final Context context = new Context(true);
		final PluginIndex pluginIndex = context.getPluginIndex();

		final List<PluginInfo<?>> infos = pluginIndex.get(IceCream.class);
		assertEquals(3, infos.size());

		assertPlugin(Chocolate.class, IceCream.class, "chocolate", infos.get(0));
		assertPlugin(Vanilla.class, IceCream.class, "vanilla", infos.get(1));
		assertPlugin(Flavorless.class, IceCream.class, "", infos.get(2));
	}

	private void assertPlugin(Class<?> pluginClass, Class<?> pluginType,
		String name, PluginInfo<?> info) throws InstantiableException
	{
		assertSame(pluginClass, info.loadClass());
		assertSame(pluginType, info.getPluginType());
		assertEquals(name, info.getName());
	}

	public static interface IceCream extends SciJavaPlugin {
		// NB: Marker interface.
	}

	@Plugin(type = IceCream.class, priority = Priority.VERY_LOW)
	public static class Flavorless implements SciJavaPlugin {
		// NB: No implementation needed.
	}

	@Plugin(type = IceCream.class, name = "vanilla",
		priority = Priority.LOW)
	public static class Vanilla implements SciJavaPlugin {
		// NB: No implementation needed.
	}

	@Plugin(type = IceCream.class, name = "chocolate",
		priority = Priority.VERY_HIGH)
	public static class Chocolate implements IceCream {
		// NB: No implementation needed.
	}

}
