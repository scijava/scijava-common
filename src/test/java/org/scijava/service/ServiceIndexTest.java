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

package org.scijava.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;
import org.scijava.Context;
import org.scijava.event.DefaultEventService;
import org.scijava.log.StderrLogService;
import org.scijava.options.DefaultOptionsService;
import org.scijava.options.OptionsService;
import org.scijava.plugin.DefaultPluginService;
import org.scijava.plugin.PluginService;
import org.scijava.thread.DefaultThreadService;

/**
 * Tests {@link ServiceIndex}.
 * 
 * @author Curtis Rueden
 */
public class ServiceIndexTest {

	@Test
	public void testGetAll() {
		final Context context = new Context(PluginService.class);
		final ServiceIndex serviceIndex = context.getServiceIndex();
		final List<Service> all = serviceIndex.getAll();
		assertEquals(4, all.size());
		assertSame(DefaultEventService.class, all.get(0).getClass());
		assertSame(DefaultPluginService.class, all.get(1).getClass());
		assertSame(DefaultThreadService.class, all.get(2).getClass());
		assertSame(StderrLogService.class, all.get(3).getClass());
		context.dispose();
	}

	@Test
	public void testMarkerInterfaces() {
		final Context context = new Context();
		for (final Service s : context.getServiceIndex().getAll()) {
			assertTrue(s.getClass().getName(), s instanceof SciJavaService);
		}
		context.dispose();
	}

	/**
	 * Test the {@link ServiceIndex#getPrevService(Class, Class)} operation.
	 */
	@Test
	public void testGetPrevService() {
		final Context context = new Context(SciJavaService.class);

		// Create a service index where the OptionsService hierarchy should be:
		// HigherOptionsService > DefaultOptionsService > LowerOptionsService
		final ServiceIndex serviceIndex = setUpPrivateServices(context);

		// DefaultOptionsService should be the previous service to LowerOptionsService
		assertEquals(DefaultOptionsService.class, serviceIndex.getPrevService(
			OptionsService.class, LowerOptionsService.class).getClass());

		// HigherOptionsService should be the previous service to
		// DefaultOptionsService
		assertEquals(HigherOptionsService.class, serviceIndex.getPrevService(
			OptionsService.class, DefaultOptionsService.class).getClass());

		// There should not be a previous service before HigherOptionsService
		assertNull(serviceIndex.getPrevService(OptionsService.class,
			HigherOptionsService.class));

		context.dispose();
	}

	/**
	 * Test the {@link ServiceIndex#getNextService(Class, Class)} operation.
	 */
	@Test
	public void testGetNextService() {
		final Context context = new Context(SciJavaService.class);

		// Create a service index where the OptionsService hierarchy should be:
		// HigherOptionService > DefaultOptionService > LowerOptionService
		final ServiceIndex serviceIndex = setUpPrivateServices(context);

		// DefaultOptionsService should be the next service to HigherOptionsService
		assertEquals(DefaultOptionsService.class, serviceIndex.getNextService(
			OptionsService.class, HigherOptionsService.class).getClass());

		// HigherOptionsService should be the previous service to
		// DefaultOptionsService
		assertEquals(LowerOptionsService.class, serviceIndex.getNextService(
			OptionsService.class, DefaultOptionsService.class).getClass());

		// There should not be a next service after LowerOptionsService
		assertNull(serviceIndex.getNextService(OptionsService.class,
			LowerOptionsService.class));

		context.dispose();
	}

	// -- Helper methods --

	/**
	 * @return A {@link ServiceIndex} with all private services manually added.
	 */
	private ServiceIndex setUpPrivateServices(final Context context) {
		final ServiceIndex serviceIndex = context.getServiceIndex();
		serviceIndex.add(new HigherOptionsService());
		serviceIndex.add(new LowerOptionsService());
		return serviceIndex;
	}

	// -- Private services --

	private static class HigherOptionsService extends DefaultOptionsService {

		@Override
		public double getPriority() {
			return super.getPriority() + 25;
		}
	}

	private static class LowerOptionsService extends DefaultOptionsService {

		@Override
		public double getPriority() {
			return super.getPriority() - 30;
		}
	}
}
