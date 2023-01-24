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

package org.scijava.plugin;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.scijava.Context;
import org.scijava.convert.AbstractConverter;
import org.scijava.convert.ConvertService;
import org.scijava.convert.Converter;
import org.scijava.plugin.event.PluginsAddedEvent;
import org.scijava.plugin.event.PluginsRemovedEvent;

/**
 * Tests for the {@link SingletonService}
 *
 * @author Gabriel Einsdorf KNIME GmbH
 * @author Stefan Helfrich KNIME GmbH
 */
public class SingletonServiceTest {

	private PluginService pluginService;
	private ConvertService convertService;

	@Before
	public void setUp() {
		final Context context = new Context(PluginService.class,
			ConvertService.class);
		pluginService = context.service(PluginService.class);
		convertService = context.service(ConvertService.class);
	}

	@After
	public void tearDown() {
		pluginService.context().dispose();
	}

	/**
	 * Tests that the {@link AbstractSingletonService} properly handles
	 * {@link PluginsAddedEvent}s originating from the {@link PluginService}.
	 */
	@Test
	public void testSingletonServicePluginsAddedHandling() {
		@SuppressWarnings("rawtypes")
		final PluginInfo<Converter> converterInfo = new PluginInfo<>(
			FoodConverter.class, Converter.class);

		pluginService.addPlugin(converterInfo);

		assertNotNull(pluginService.getPlugin(FoodConverter.class));
		assertTrue(convertService.supports(new Apple() {}, Peach.class));
	}

	/**
	 * Tests that the {@link AbstractSingletonService} properly handles
	 * {@link PluginsAddedEvent}s that replace an instance.
	 */
	@Test
	public void testSingletonServicePluginsAddedHandlingDuplicates() {
		@SuppressWarnings("rawtypes")
		final PluginInfo<Converter> converterInfo = new PluginInfo<>(
			FoodConverter.class, Converter.class);

		pluginService.addPlugin(converterInfo);
		final FoodConverter firstInstance = convertService.getInstance(
			FoodConverter.class);

		pluginService.addPlugin(converterInfo);
		final FoodConverter secondInstance = convertService.getInstance(
			FoodConverter.class);

		assertNotSame(firstInstance, secondInstance);
		assertTrue(convertService.supports(new Apple() {}, Peach.class));
	}

	/**
	 * Tests that the {@link AbstractSingletonService} properly handles
	 * {@link PluginsRemovedEvent}s originating from the {@link PluginService}.
	 */
	@Test
	public void testSingletonServiceManuallyAddedPluginsRemovedHandling() {
		@SuppressWarnings("rawtypes")
		final PluginInfo<Converter> converterInfo = new PluginInfo<>(
			FoodConverter.class, Converter.class);

		pluginService.addPlugin(converterInfo);

		// De-register DummyStringConverter
		pluginService.removePlugin(converterInfo);

		assertNull(pluginService.getPlugin(FoodConverter.class));
		assertFalse(convertService.supports(new Apple() {}, Peach.class));
	}

	/**
	 * Tests that the {@link AbstractSingletonService} properly handles
	 * {@link PluginsRemovedEvent}s originating from the {@link PluginService}.
	 */
	@Test
	public void testSingletonServiceCompileTimePluginsRemovedHandling() {
		final PluginInfo<SciJavaPlugin> pluginInfo = pluginService.getPlugin(
			DiscoveredFoodConverter.class);

		// De-register DiscoveredFoodConverter
		pluginService.removePlugin(pluginInfo);

		assertNull(pluginService.getPlugin(DiscoveredFoodConverter.class));
		assertFalse(convertService.supports(new Orange() {}, Peach.class));
	}

	/**
	 * Dummy {@link Converter}.
	 */
	public static class FoodConverter extends AbstractConverter<Apple, Peach> {

		@Override
		public <T> T convert(final Object src, final Class<T> dest) {
			return null;
		}

		@Override
		public Class<Peach> getOutputType() {
			return Peach.class;
		}

		@Override
		public Class<Apple> getInputType() {
			return Apple.class;
		}
	}

	/**
	 * Dummy {@link Converter} that is added automatically.
	 */
	@Plugin(type = Converter.class)
	public static class DiscoveredFoodConverter extends
		AbstractConverter<Orange, Peach>
	{

		@Override
		public <T> T convert(final Object src, final Class<T> dest) {
			return null;
		}

		@Override
		public Class<Peach> getOutputType() {
			return Peach.class;
		}

		@Override
		public Class<Orange> getInputType() {
			return Orange.class;
		}
	}

	/**
	 * Type interface for conversion
	 */
	public interface Apple {
		// NB
	}

	/**
	 * Type interface for conversion
	 */
	public interface Orange {
		// NB
	}

	/**
	 * Type interface for conversion
	 */
	public interface Peach {
		// NB
	}

	/**
	 * Tests that plugins are added to and removed from the correct singleton
	 * service
	 */
	@Test
	public void testListenToRemove() {

		final Context ctx = new Context(PluginService.class,
			DummySingletonService.class, DummySingletonService2.class);

		final DummySingletonService dss = ctx.getService(
			DummySingletonService.class);

		final DummySingletonService2 dss2 = ctx.getService(
			DummySingletonService2.class);

		final List<DummyPlugin> instances = dss.getInstances();
		final DummyPlugin dummy = instances.get(0);

		assertFalse("Service not correctly initialized", dss2.getInstances()
			.isEmpty());

		// test successful removal
		final PluginService ps = ctx.getService(PluginService.class);
		ps.removePlugin(dummy.getInfo());

		assertFalse("Plugin was removed from wrong service!", dss2.getInstances()
			.isEmpty());
		assertTrue("Plugin was not removed!", dss.getInstances().isEmpty());

		// test successful add
		ps.addPlugin(dummy.getInfo());
		assertEquals("Wrong number of plugins in service:", 1, dss.getInstances()
			.size());
		assertEquals("Wrong number of plugins in independent service:", 1, dss2
			.getInstances().size());
	}

	@Plugin(type = DummyPlugin.class)
	public static class DummyPlugin extends AbstractRichPlugin implements
		SingletonPlugin
	{
		// NB: No implementation needed.
	}

	public static class DummySingletonService extends
		AbstractSingletonService<DummyPlugin>
	{

		@Override
		public Class<DummyPlugin> getPluginType() {
			return DummyPlugin.class;
		}
	}

	@Plugin(type = DummyPlugin2.class)
	public static class DummyPlugin2 extends AbstractRichPlugin implements
		SingletonPlugin
	{
		// NB: No implementation needed.
	}

	public static class DummySingletonService2 extends
		AbstractSingletonService<DummyPlugin2>
	{

		@Override
		public Class<DummyPlugin2> getPluginType() {
			return DummyPlugin2.class;
		}
	}

}
