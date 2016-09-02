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

package org.scijava;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.scijava.util.ArrayUtils.array;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.PluginIndex;
import org.scijava.plugin.PluginInfo;
import org.scijava.plugin.SciJavaPlugin;
import org.scijava.service.AbstractService;
import org.scijava.service.SciJavaService;
import org.scijava.service.Service;
import org.scijava.thread.ThreadService;

/**
 * Tests {@link Context} creation with {@link Service} dependencies.
 * 
 * @author Curtis Rueden
 */
public class ContextCreationTest {

	/** Tests that a new empty {@link Context} indeed has no {@link Service}s. */
	@Test
	public void testEmpty() {
		final Context context = new Context(true);
		assertTrue(context.getServiceIndex().isEmpty());
		assertFalse(context.getPluginIndex().isEmpty());
	}

	/**
	 * Tests {@link Context#Context(boolean, boolean)} with {@code (true, true)}.
	 */
	@Test
	public void testNoPlugins() {
		final Context context = new Context(true, true);
		assertTrue(context.getServiceIndex().isEmpty());
		assertTrue(context.getPluginIndex().isEmpty());
	}

	/**
	 * Tests that a new fully populated {@link Context} has all available core
	 * {@link Service}s, in the expected priority order.
	 */
	@Test
	public void testFull() {
		final Class<?>[] expected =
			{ org.scijava.event.DefaultEventService.class,
				org.scijava.script.DefaultScriptService.class,
				org.scijava.app.DefaultAppService.class,
				org.scijava.app.DefaultStatusService.class,
				org.scijava.command.DefaultCommandService.class,
				org.scijava.console.DefaultConsoleService.class,
				org.scijava.convert.DefaultConvertService.class,
				org.scijava.display.DefaultDisplayService.class,
				org.scijava.event.DefaultEventHistory.class,
				org.scijava.input.DefaultInputService.class,
				org.scijava.io.DefaultDataHandleService.class,
				org.scijava.io.DefaultIOService.class,
				org.scijava.io.DefaultRecentFileService.class,
				org.scijava.main.DefaultMainService.class,
				org.scijava.menu.DefaultMenuService.class,
				org.scijava.module.DefaultModuleService.class,
				org.scijava.object.DefaultObjectService.class,
				org.scijava.options.DefaultOptionsService.class,
				org.scijava.parse.DefaultParseService.class,
				org.scijava.platform.DefaultPlatformService.class,
				org.scijava.plugin.DefaultPluginService.class,
				org.scijava.prefs.DefaultPrefService.class,
				org.scijava.run.DefaultRunService.class,
				org.scijava.script.DefaultScriptHeaderService.class,
				org.scijava.text.DefaultTextService.class,
				org.scijava.thread.DefaultThreadService.class,
				org.scijava.tool.DefaultToolService.class,
				org.scijava.ui.DefaultUIService.class,
				org.scijava.ui.dnd.DefaultDragAndDropService.class,
				org.scijava.welcome.DefaultWelcomeService.class,
				org.scijava.widget.DefaultWidgetService.class,
				org.scijava.log.StderrLogService.class,
				org.scijava.platform.DefaultAppEventService.class,
				org.scijava.cache.DefaultCacheService.class};

		final Context context = new Context();
		verifyServiceOrder(expected, context);
	}

	/**
	 * Tests that a new fully populated {@link Context} has exactly the same
	 * {@link Service}s available as one created with only {@link SciJavaService}
	 * implementations.
	 * <p>
	 * In other words: tests that all {@link Service}s implemented in SciJava
	 * Common are tagged with the {@link SciJavaService} interface.
	 * </p>
	 */
	@Test
	public void testSciJavaServices() {
		final Context full = new Context();
		final Context sciJava = new Context(SciJavaService.class);
		for (final Service s : full.getServiceIndex()) {
			final Class<? extends Service> c = s.getClass();
			final Service sjs = sciJava.getService(c);
			if (sjs == null) fail("Not a SciJavaService? " + s.getClass().getName());
		}
	}

	/**
	 * Tests that dependent {@link Service}s are automatically created and
	 * populated in downstream {@link Service} classes.
	 */
	@Test
	public void testDependencies() {
		final Context context = new Context(FooService.class);

		// verify that the Foo service is there
		final FooService fooService = context.getService(FooService.class);
		assertNotNull(fooService);
		assertSame(context, fooService.getContext());

		// verify that the Bar service is there
		final BarService barService = context.getService(BarService.class);
		assertNotNull(barService);
		assertSame(context, barService.getContext());
		assertSame(barService, fooService.barService);

		// verify that the *only* two services are Foo and Bar
		assertEquals(2, context.getServiceIndex().size());
	}

	/**
	 * Tests that missing {@link Service}s are handled properly; specifically,
	 * that {@link IllegalArgumentException} gets thrown when attempting to create
	 * a {@link Context} requiring one directly.
	 */
	@Test
	public void testMissingDirect() {
		try {
			new Context(MissingService.class);
			fail("Expected IllegalArgumentException");
		}
		catch (final IllegalArgumentException exc) {
			final String expectedMessage =
				"No compatible service: " + MissingService.class.getName();
			assertEquals(expectedMessage, exc.getMessage());
		}
	}

	/**
	 * Tests that missing {@link Service}s are handled properly; specifically,
	 * that {@link IllegalArgumentException} gets thrown when attempting to create
	 * a {@link Context} requiring one transitively.
	 */
	@Test
	public void testMissingTransitive() {
		try {
			new Context(ServiceRequiringMissingService.class);
			fail("Expected IllegalArgumentException");
		}
		catch (final IllegalArgumentException exc) {
			final String expectedMessage =
				"Invalid service: " + ServiceRequiringMissingService.class.getName();
			assertEquals(expectedMessage, exc.getMessage());
			final String expectedCause =
				"No compatible service: " + MissingService.class.getName();
			assertEquals(expectedCause, exc.getCause().getMessage());
		}
	}

	/**
	 * Tests that missing-but-optional {@link Service}s are handled properly;
	 * specifically, that {@link IllegalArgumentException} gets thrown when
	 * attempting to create a {@link Context} requiring one transitively.
	 * <p>
	 * A service marked {@link Optional}, but annotated without
	 * {@code required = false} from a dependent service, is assumed to be
	 * required for that dependent service.
	 * </p>
	 */
	@Test
	public void testOptionalMissingTransitive() {
		try {
			new Context(ServiceRequiringOptionalMissingService.class);
			fail("Expected IllegalArgumentException");
		}
		catch (final IllegalArgumentException exc) {
			final String expectedMessage =
				"Invalid service: " +
					ServiceRequiringOptionalMissingService.class.getName();
			assertEquals(expectedMessage, exc.getMessage());
			final String expectedCause =
				"No compatible service: " + OptionalMissingService.class.getName();
			assertEquals(expectedCause, exc.getCause().getMessage());
		}
	}

	/**
	 * Tests that missing {@link Service}s are handled properly in non-strict
	 * mode; specifically, that {@link IllegalArgumentException} is <em>not</em>
	 * thrown when attempting to create a {@link Context} requiring one directly.
	 */
	@Test
	public void testNonStrictMissingDirect() {
		final List<Class<? extends Service>> serviceClasses =
			Context.serviceClassList(MissingService.class);
		final Context context = new Context(serviceClasses, false);
		assertEquals(0, context.getServiceIndex().size());
	}

	/**
	 * Tests that missing {@link Service}s are handled properly in non-strict
	 * mode; specifically, that {@link IllegalArgumentException} is <em>not</em>
	 * thrown when attempting to create a {@link Context} requiring one
	 * transitively.
	 */
	@Test
	public void testNonStrictMissingTransitive() {
		final List<Class<? extends Service>> serviceClasses =
			Context.serviceClassList(MissingService.class);
		final Context context = new Context(serviceClasses, false);
		assertEquals(0, context.getServiceIndex().size());
	}

	/**
	 * Tests that missing-but-optional {@link Service}s are handled properly in
	 * non-strict mode; specifically, that {@link IllegalArgumentException} is
	 * <em>not</em> thrown when attempting to create a {@link Context} requiring
	 * one transitively.
	 * <p>
	 * A service marked {@link Optional}, but annotated without
	 * {@code required = false} from a dependent service, is assumed to be
	 * required for that dependent service.
	 * </p>
	 */
	@Test
	public void testNonStrictOptionalMissingTransitive() {
		final List<Class<? extends Service>> serviceClasses =
			Context.serviceClassList(ServiceRequiringOptionalMissingService.class);
		final Context context = new Context(serviceClasses, false);
		final List<Service> services = context.getServiceIndex().getAll();
		assertEquals(1, services.size());
		assertSame(ServiceRequiringOptionalMissingService.class, services.get(0)
			.getClass());
	}

	/**
	 * Verifies that the order plugins appear in the PluginIndex and Service list
	 * does not affect which services are loaded.
	 */
	@Test
	public void testClassOrder() {
		final int expectedSize = 2;

		// Same order, Base first
		Context c = createContext(//
			pluginIndex(BaseImpl.class, ExtensionImpl.class), //
			array(BaseService.class, ExtensionService.class));
		assertEquals(expectedSize, c.getServiceIndex().size());

		// Same order, Extension first
		c = createContext(//
			pluginIndex(ExtensionImpl.class, BaseImpl.class), //
			array(ExtensionService.class, BaseService.class));
		assertEquals(expectedSize, c.getServiceIndex().size());

		// Different order, Extension first
		c = createContext(//
			pluginIndex(ExtensionImpl.class, BaseImpl.class), //
			array(BaseService.class, ExtensionService.class));
		assertEquals(expectedSize, c.getServiceIndex().size());

		// Different order, Base first
		c = createContext(//
			pluginIndex(BaseImpl.class, ExtensionImpl.class), //
			array(ExtensionService.class, BaseService.class));
		assertEquals(expectedSize, c.getServiceIndex().size());
	}

	/**
	 * Verifies that the Service index created when using Abstract classes is the
	 * same as for interfaces.
	 */
	@Test
	public void testAbstractClasslist() {
		final Context cAbstract = createContext(//
			pluginIndex(BaseImpl.class, ExtensionImpl.class), //
			array(AbstractBase.class, AbstractExtension.class));

		final Context cService = createContext(//
			pluginIndex(BaseImpl.class, ExtensionImpl.class), //
			array(BaseService.class, ExtensionService.class));

		assertEquals(cService.getServiceIndex().size(), cAbstract.getServiceIndex()
			.size());
	}

	/**
	 * Verify that if no services are explicitly passed, all subclasses of
	 * Service.class are discovered automatically.
	 */
	@Test
	public void testNoServicesCtor() {
		// create a 2-service context
		final PluginIndex index = pluginIndex(BaseImpl.class, ExtensionImpl.class);
		// Add another service, that is not indexed under Service.class
		index.add(new PluginInfo<>(ThreadService.class.getName(),
			SciJavaPlugin.class));
		final Context c =
			new Context(pluginIndex(BaseImpl.class, ExtensionImpl.class));
		assertEquals(2, c.getServiceIndex().size());
	}

	/**
	 * Tests that missing-but-optional {@link Service}s are handled properly;
	 * specifically, that the {@link Context} is still created successfully when
	 * attempting to include a missing-but-optional service directly.
	 * <p>
	 * A service marked {@link Optional} is assumed to be optional for the context
	 * when requested for inclusion directly. (This behavior is, after all, one
	 * main reason for the {@link Optional} interface.)
	 * </p>
	 */
	@Test
	public void testOptionalMissingDirect() {
		final Context context = new Context(OptionalMissingService.class);

		final OptionalMissingService optionalMissingService =
			context.getService(OptionalMissingService.class);
		assertNull(optionalMissingService);

		// verify that there are *no* services in the context
		assertEquals(0, context.getServiceIndex().size());
	}

	/**
	 * Tests that missing {@link Service}s marked with {@code required = false}
	 * are handled properly; specifically, that the {@link Context} is still
	 * created successfully when attempting to request (but not require) a missing
	 * service transitively.
	 */
	@Test
	public void testNonRequiredMissingService() {
		final Context context = new Context(ServiceWantingMissingService.class);
		assertEquals(1, context.getServiceIndex().size());

		final ServiceWantingMissingService serviceWantingMissingService =
			context.getService(ServiceWantingMissingService.class);
		assertNotNull(serviceWantingMissingService);
		assertNull(serviceWantingMissingService.missingService);

		final MissingService missingService =
			context.getService(MissingService.class);
		assertNull(missingService);

		// verify that the *only* service is ServiceWantingMissing
		assertEquals(1, context.getServiceIndex().size());
	}

	// -- Helper methods --

	/**
	 * Checks the expected order vs. the order in the provided Context's
	 * ServiceIndex
	 */
	private void verifyServiceOrder(final Class<?>[] expected,
		final Context context)
	{
		assertEquals(expected.length, context.getServiceIndex().size());
		int index = 0;
		for (final Service service : context.getServiceIndex()) {
			assertSame(expected[index++], service.getClass());
		}
	}

	/**
	 * Initializes and returns a Context given the provided PluginIndex and array
	 * of services.
	 */
	private Context createContext(final PluginIndex index,
		final Class<? extends Service>[] services)
	{
		return new Context(Arrays.<Class<? extends Service>> asList(services),
			index);
	}

	/**
	 * Creates a PluginIndex and adds all the provided classes as plugins, indexed
	 * under Service.class
	 */
	private PluginIndex pluginIndex(final Class<?>... plugins) {
		final PluginIndex index = new PluginIndex(null);
		for (final Class<?> c : plugins) {
			index.add(new PluginInfo<>(c.getName(), Service.class));
		}
		return index;
	}

	// -- Helper classes --

	/** A service which requires a {@link BarService}. */
	public static class FooService extends AbstractService {

		@Parameter
		private BarService barService;

	}

	/** A service that is extended by {@link ExtensionService}. */
	public static interface BaseService extends Service {
		// NB: No implementation needed.
	}

	/** A service extending {@link BaseService}. */
	public static interface ExtensionService extends BaseService {
		// NB: No implementation needed.
	}

	/** A simple service with no dependencies. */
	public static class BarService extends AbstractService {
		// NB: No implementation needed.
	}

	/** A required service interface with no available implementation. */
	public static interface MissingService extends Service {
		// NB: Marker interface.
	}

	/** A optional service interface with no available implementation. */
	public static interface OptionalMissingService extends Service, Optional {
		// NB: Marker interface.
	}

	/** Abstract implementation of {@link BaseService}. */
	public static abstract class AbstractBase extends AbstractService implements
		BaseService
	{
		// NB: No implementation needed.
	}

	/** Abstract implementation of {@link ExtensionService}. */
	public static abstract class AbstractExtension extends AbstractService
		implements ExtensionService
	{
		// NB: No implementation needed.
	}

	/** Empty {@link BaseService} implementation. */
	public static class BaseImpl extends AbstractBase {
		// NB: No implementation needed.
	}

	/** Empty {@link ExtensionService} implementation. */
	public static class ExtensionImpl extends AbstractExtension {
		// NB: No implementation needed.
	}

	/** A service that is doomed to fail, for depending on a missing service. */
	public static class ServiceRequiringMissingService extends AbstractService {

		@Parameter
		private MissingService missingService;

	}

	/**
	 * Another service that is doomed to fail. It depends on a missing service
	 * which, although optional, is marked with {@code required = true}
	 * (implicitly), and so cannot be filled.
	 */
	public static class ServiceRequiringOptionalMissingService extends
		AbstractService
	{

		@Parameter
		private OptionalMissingService optionalMissingService;

	}

	/**
	 * A service with a missing dependency marked {@code required = false}. This
	 * service should be able to be filled, since its missing service dependency
	 * is optional.
	 */
	public static class ServiceWantingMissingService extends AbstractService {

		@Parameter(required = false)
		private MissingService missingService;

	}

}
