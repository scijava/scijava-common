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

package org.scijava.io.nio;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.nio.ByteBuffer;
import java.util.function.Function;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;
import org.scijava.io.ByteBank;
import org.scijava.io.ByteBankTest;

/**
 * Tests {@link ByteBufferByteBank}.
 *
 * @author Curtis Rueden
 * @author Gabriel Einsdorf
 * @see ByteBankTest
 */
@RunWith(Parameterized.class)
public class ByteBufferByteBankTest extends ByteBankTest {

	@Parameter
	public Function<Integer, ByteBuffer> supplier;

	@Parameters
	public static Object[] params() {
		final Function<Integer, ByteBuffer> alloc = ByteBuffer::allocate;
		final Function<Integer, ByteBuffer> allocDirect =
			ByteBuffer::allocateDirect;
		return new Function[] { alloc, allocDirect };
	}

	@Override
	public ByteBank createByteBank() {
		return new ByteBufferByteBank(supplier);
	}

	@Test
	public void testReadOnlyDefault() {
		final ByteBufferByteBank bank = new ByteBufferByteBank();
		assertFalse(bank.isReadOnly());
	}

	@Test
	public void testReadOnlyAllocate() {
		final ByteBufferByteBank bank = new ByteBufferByteBank(
			ByteBuffer::allocate);
		assertFalse(bank.isReadOnly());

		final ByteBufferByteBank readOnlyBank = new ByteBufferByteBank(
			capacity -> ByteBuffer.allocate(capacity).asReadOnlyBuffer());
		assertTrue(readOnlyBank.isReadOnly());
	}

	@Test
	public void testReadOnlyAllocateDirect() {
		final ByteBufferByteBank bank = new ByteBufferByteBank(
			ByteBuffer::allocateDirect);
		assertFalse(bank.isReadOnly());

		final ByteBufferByteBank readOnlyBank = new ByteBufferByteBank(
			capacity -> ByteBuffer.allocateDirect(capacity).asReadOnlyBuffer());
		assertTrue(readOnlyBank.isReadOnly());
	}
}
