/*
 * #%L
 * SciJava Common shared library for SciJava software.
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

package org.scijava.usage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.scijava.Context;
import org.scijava.Identifiable;
import org.scijava.Locatable;

/**
 * Tests {@link UsageService}.
 * 
 * @author Curtis Rueden
 */
public class UsageServiceTest {

	private UsageService usageService;

	@Before
	public void setUp() {
		final Context context = new Context(UsageService.class);
		usageService = context.getService(UsageService.class);
	}

	@After
	public void tearDown() {
		usageService.getContext().dispose();
	}

	/** Tests {@link UsageService#getStats()}. */
	@Test
	public void testUsage() {
		final Thing foo = new Thing("foo", "file:/foo");
		final Thing bar = new Thing("bar", "file:/bar");

		final Map<String, UsageStats> stats = usageService.getStats();
		// no stats at first
		assertTrue(stats.isEmpty());
		assertNull(stats.get(foo));

		// increment foo
		usageService.increment(foo);
		assertEquals(1, stats.size());
		final UsageStats fooStats = stats.get(foo.getIdentifier());
		assertNotNull(fooStats);
		assertSame(fooStats, usageService.getUsage(foo));
		assertEquals(foo.getIdentifier(), fooStats.getIdentifier());
		assertEquals("file:/foo", fooStats.getLocation());
		assertEquals(1, fooStats.getCount());

		// increment bar
		assertFalse(stats.containsKey(bar));
		usageService.increment(bar);
		assertEquals(2, stats.size());
		final UsageStats barStats = stats.get(bar.getIdentifier());
		assertNotNull(barStats);
		assertSame(barStats, usageService.getUsage(bar));
		assertEquals(bar.getIdentifier(), barStats.getIdentifier());
		assertEquals("file:/bar", barStats.getLocation());
		assertEquals(1, barStats.getCount());

		// increment foo again
		usageService.increment(foo);
		assertEquals(2, stats.size());
		assertEquals(2, fooStats.getCount());
		assertEquals(1, barStats.getCount());

		// clear stats, and ensure it doesn't nuke existing references
		usageService.clearStats();
		assertEquals(2, stats.size());
		assertEquals(2, fooStats.getCount());
		assertEquals(1, barStats.getCount());
		final Map<String, UsageStats> newStats = usageService.getStats();
		assertNotSame(stats, newStats);
		assertTrue(newStats.isEmpty());
		assertNotSame(fooStats, usageService.getUsage(foo));
		assertNotSame(barStats, usageService.getUsage(bar));

		// verify that increments affect new stats now, not old ones
		usageService.increment(foo);
		assertEquals(2, fooStats.getCount());
		assertEquals(1, barStats.getCount());
		assertEquals(1, newStats.get(foo.getIdentifier()).getCount());
	}

	// -- Helper classes --

	private static class Thing implements Identifiable, Locatable {

		private String id;
		private String location;

		private Thing(final String id, final String location) {
			this.id = id;
			this.location = location;
		}

		@Override
		public String getLocation() {
			return location;
		}

		@Override
		public String getIdentifier() {
			return id;
		}
		
	}

}
