/*-
 * #%L
 * SciJava Common shared library for SciJava software.
 * %%
 * Copyright (C) 2009 - 2024 SciJava developers.
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

package org.scijava.module.event;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;
import org.scijava.Context;
import org.scijava.event.EventHandler;
import org.scijava.event.EventService;
import org.scijava.module.AbstractModule;
import org.scijava.module.AbstractModuleInfo;
import org.scijava.module.Module;
import org.scijava.module.ModuleException;
import org.scijava.module.ModuleInfo;
import org.scijava.module.ModuleService;

/**
 * Tests {@link ModuleErroredEvent} behavior.
 *
 * @author Gabriel Selzer
 * @author Curtis Rueden
 */
public class ModuleErroredEventTest {

	private EventService es;
	private ModuleService module;

	@Before
	public void setUp() {
		Context ctx = new Context();
		es = ctx.getService(EventService.class);
		module = ctx.getService(ModuleService.class);
	}

	@Test
	public void testModuleErroredEvent() {

		// Must be a final boolean array to be included in the below closure
		final Throwable[] caughtException = { null };

		// Add a new EventHandler to change our state
		final Object interestedParty = new Object() {

			@EventHandler
			void onEvent(final ModuleErroredEvent e) {
				caughtException[0] = e.getException();
				e.consume(); // Prevent exception from being emitted to stderr.
			}
		};
		es.subscribe(interestedParty);

		// Run the module, ensure we get the exception
		assertThrows(Exception.class, //
			() -> module.run(new TestModuleInfo(), false).get());
		assertNotNull(caughtException[0]);
		assertEquals("Yay!", caughtException[0].getMessage());
	}

	static class TestModuleInfo extends AbstractModuleInfo {

		@Override
		public String getDelegateClassName() {
			return this.getClass().getName();
		}

		@Override
		public Class<?> loadDelegateClass() throws ClassNotFoundException {
			return this.getClass();
		}

		@Override
		public Module createModule() throws ModuleException {
			ModuleInfo thisInfo = this;
			return new AbstractModule() {

				@Override
				public ModuleInfo getInfo() {
					return thisInfo;
				}

				@Override
				public void run() {
					throw new RuntimeException("Yay!");
				}
			};
		}
	}
}
