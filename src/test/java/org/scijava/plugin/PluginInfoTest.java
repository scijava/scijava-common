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

package org.scijava.plugin;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

import java.util.List;

import org.junit.After;
import org.junit.Before;
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

	private Context context;
	private PluginIndex pluginIndex;

	@Before
	public void setUp() {
		context = new Context(true);
		pluginIndex = context.getPluginIndex();
	}

	@After
	public void tearDown() {
		context.dispose();
		context = null;
		pluginIndex = null;
	}

	@Test
	public void testNames() throws InstantiableException {
		final List<PluginInfo<?>> infos = pluginIndex.get(IceCream.class);
		assertEquals(3, infos.size());

		assertPlugin(Chocolate.class, IceCream.class, "chocolate", infos.get(0));
		assertPlugin(Vanilla.class, IceCream.class, "vanilla", infos.get(1));
		assertPlugin(Flavorless.class, IceCream.class, "", infos.get(2));
	}

	@Test
	public void testGet() throws InstantiableException {
		final PluginInfo<?> chocolateInfo = //
			PluginInfo.get(Chocolate.class, pluginIndex);
		assertPlugin(Chocolate.class, IceCream.class, "chocolate", chocolateInfo);

		final PluginInfo<IceCream> chocolateInfoWithType = //
			PluginInfo.get(Chocolate.class, IceCream.class, pluginIndex);
		assertSame(chocolateInfo, chocolateInfoWithType);

		class Sherbet implements IceCream {}
		assertNull(PluginInfo.get(Sherbet.class, pluginIndex));
	}

	@Test
	public void testCreate() throws InstantiableException {
		final PluginInfo<?> chocolateInfo = PluginInfo.create(Chocolate.class);
		assertPlugin(Chocolate.class, IceCream.class, "chocolate", chocolateInfo);

		final PluginInfo<IceCream> chocolateInfoWithType = //
			PluginInfo.create(Chocolate.class, IceCream.class);
		assertPlugin(Chocolate.class, IceCream.class, "chocolate",
			chocolateInfoWithType);
		assertNotSame(chocolateInfo, chocolateInfoWithType);

		class Sherbet implements IceCream {}
		final PluginInfo<IceCream> sherbetInfoWithType = //
			PluginInfo.create(Sherbet.class, IceCream.class);
		assertPlugin(Sherbet.class, IceCream.class, null, sherbetInfoWithType);

		try {
			final PluginInfo<?> result = PluginInfo.create(Sherbet.class);
			fail("Expected IllegalArgumentException but got: " + result);
		}
		catch (final IllegalArgumentException exc) {
			// NB: Expected.
		}
	}

	@Test
	public void testGetOrCreate() throws InstantiableException {
		final PluginInfo<?> chocolateInfo = //
			PluginInfo.getOrCreate(Chocolate.class, pluginIndex);
		assertPlugin(Chocolate.class, IceCream.class, "chocolate", chocolateInfo);

		final PluginInfo<IceCream> chocolateInfoWithType = //
			PluginInfo.getOrCreate(Chocolate.class, IceCream.class, pluginIndex);
		assertSame(chocolateInfo, chocolateInfoWithType);

		class Sherbet implements IceCream {}
		final PluginInfo<IceCream> sherbetInfoWithType = //
			PluginInfo.getOrCreate(Sherbet.class, IceCream.class, pluginIndex);
		assertPlugin(Sherbet.class, IceCream.class, null, sherbetInfoWithType);

		try {
			final PluginInfo<?> result = //
				PluginInfo.getOrCreate(Sherbet.class, pluginIndex);
			fail("Expected IllegalArgumentException but got: " + result);
		}
		catch (final IllegalArgumentException exc) {
			// NB: Expected.
		}
	}

	private void assertPlugin(Class<?> pluginClass, Class<?> pluginType,
		String name, PluginInfo<?> info) throws InstantiableException
	{
		assertNotNull(info);
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

	@Plugin(type = IceCream.class, name = "vanilla", priority = Priority.LOW)
	public static class Vanilla implements SciJavaPlugin {
		// NB: No implementation needed.
	}

	@Plugin(type = IceCream.class, name = "chocolate",
		priority = Priority.VERY_HIGH)
	public static class Chocolate implements IceCream {
		// NB: No implementation needed.
	}

}
