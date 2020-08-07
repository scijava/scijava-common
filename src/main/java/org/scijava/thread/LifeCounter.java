/*
 * #%L
 * SciJava Common shared library for SciJava software.
 * %%
 * Copyright (C) 2009 - 2020 SciJava developers.
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

package org.scijava.thread;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Keeps the JVM alive, if and only if the life count is greater than zero.
 * <p>
 * This is achieved by adding a shutdown hook that waits for the life count to
 * reach zero.
 *
 * @author Matthias Arzt
 */
class LifeCounter {

	private final AtomicInteger counter = new AtomicInteger();

	private CountDownLatch latch = null;

	/**
	 * Creates a {@link LifeCounter}, with life count initialized to 0.
	 * The life counter, will keep the JVM alive as long as the life count
	 * id greater than zero.
	 */
	public LifeCounter() {
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			latch = new CountDownLatch(1);
			if (counter.get() > 0) try {
				latch.await();
			}
			catch (InterruptedException e) {
				e.printStackTrace();
			}
		}));
	}

	/**
	 * Increases the life count by one.
	 */
	public void increase() {
		counter.incrementAndGet();
	}

	/**
	 * Decrease the life count by one.
	 */
	public void decrease() {
		if (counter.decrementAndGet() <= 0)
			if (latch != null) latch.countDown();
	}
}
