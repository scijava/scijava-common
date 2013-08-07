/*
 * #%L
 * SciJava Common shared library for SciJava software.
 * %%
 * Copyright (C) 2009 - 2013 Board of Regents of the University of
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
 * 
 * The views and conclusions contained in the software and documentation are
 * those of the authors and should not be interpreted as representing official
 * policies, either expressed or implied, of any organization.
 * #L%
 */

package org.scijava;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.scijava.plugin.Parameter;
import org.scijava.service.AbstractService;
import org.scijava.service.Service;

/**
 * Tests {@link Context} creation with {@link Service} dependencies.
 * 
 * @author Curtis Rueden
 */
public class ContextCreationTest {

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

	// -- Helper classes --

	/** A service which requires a {@link BarService}. */
	public static class FooService extends AbstractService {

		@Parameter
		private BarService barService;

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
