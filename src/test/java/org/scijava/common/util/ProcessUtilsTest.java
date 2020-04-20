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

package org.scijava.common.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assume.assumeTrue;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.junit.Test;
import org.scijava.common.util.PlatformUtils;
import org.scijava.common.util.ProcessUtils;

/**
 * Tests {@link ProcessUtils}.
 * 
 * @author Johannes Schindelin
 */
public class ProcessUtilsTest {

	@Test
	public void testInterruptible() throws InterruptedException {
		assumePOSIX();
		final SleepThread thread = new SleepThread(5000);
		thread.start();
		Thread.sleep(100);
		thread.interrupt();
		thread.join();
		assertNotNull(thread.getResult());
	}

	@Test
	public void testStdin() {
		assumePOSIX();
		final String value = "Hello, World!\n";
		final InputStream input = new ByteArrayInputStream(value.getBytes());
		final String result = ProcessUtils.exec(null, input, null, null, "cat");
		assertEquals(value, result);
	}

	private void assumePOSIX() {
		assumeTrue(PlatformUtils.isPOSIX());
	}

	/** A class executing a 'sleep' call, to be interrupted. */
	private static class SleepThread extends Thread {
		private int seconds;
		private Throwable result;

		public SleepThread(int seconds) {
			this.seconds = seconds;
		}

		@Override
		public void run() {
			try {
				ProcessUtils.exec(null, null, null, "sleep", "" + seconds);
			} catch (Throwable t) {
				result = t;
			}
		}

		public Throwable getResult() {
			return result;
		}
	}
}
