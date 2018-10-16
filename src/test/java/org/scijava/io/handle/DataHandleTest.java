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

package org.scijava.io.handle;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.function.Supplier;

import org.junit.Before;
import org.junit.Test;
import org.scijava.Context;
import org.scijava.io.handle.DataHandle.ByteOrder;
import org.scijava.io.location.Location;
import org.scijava.util.Bytes;

/**
 * Abstract base class for {@link DataHandle} implementation tests.
 *
 * @author Curtis Rueden
 */
public abstract class DataHandleTest {

	protected static final byte[] BYTES = { //
		'H', 'e', 'l', 'l', 'o', ',', ' ', 'w', 'o', 'r', 'l', 'd', '\n', //
		9, 8, 7, 6, 5, 4, 3, 2, 1, 0, 0, -128, 0, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, //
		125, 127, -127, -125, -3, -2, -1 };

	protected DataHandleService dataHandleService;

	@Before
	public void init() {
		final Context context = new Context(DataHandleService.class);
		dataHandleService = context.service(DataHandleService.class);
	}

	@Test
	public void checkSkip() throws IOException {
		try (DataHandle<? extends Location> handle = createHandle()) {
			handle.seek(0);
			handle.skip(10);
			assertEquals(10, handle.offset());
			handle.skipBytes(11);
			assertEquals(21, handle.offset());
		}
	}

	@Test
	public void testEndianesSettings() throws IOException {

		try (DataHandle<? extends Location> handle = createHandle()) {
			final ByteOrder original = handle.getOrder();

			handle.setOrder(ByteOrder.BIG_ENDIAN);
			assertEquals(ByteOrder.BIG_ENDIAN, handle.getOrder());
			assertTrue(handle.isBigEndian());
			assertFalse(handle.isLittleEndian());

			handle.setOrder(ByteOrder.LITTLE_ENDIAN);
			assertEquals(ByteOrder.LITTLE_ENDIAN, handle.getOrder());
			assertFalse(handle.isBigEndian());
			assertTrue(handle.isLittleEndian());

			handle.setLittleEndian(false);
			assertEquals(ByteOrder.BIG_ENDIAN, handle.getOrder());
			assertTrue(handle.isBigEndian());
			assertFalse(handle.isLittleEndian());

			handle.setLittleEndian(true);
			assertEquals(ByteOrder.LITTLE_ENDIAN, handle.getOrder());
			assertFalse(handle.isBigEndian());
			assertTrue(handle.isLittleEndian());

			handle.setOrder(original);
		}
	}

	@Test
	public void testReading() throws IOException {
		try (final DataHandle<? extends Location> handle = createHandle()) {
			checkBasicReadMethods(handle, true);
			checkEndiannessReading(handle);
		}
	}

	@Test
	public void testWriting() throws IOException {
		try (DataHandle<? extends Location> handle = createHandle()) {
			checkBasicWriteMethods(handle);
			final Location loc = createLocation();
			checkWriteEndianes(() -> dataHandleService.create(loc),
				ByteOrder.LITTLE_ENDIAN);
			checkWriteEndianes(() -> dataHandleService.create(loc),
				ByteOrder.BIG_ENDIAN);
			checkAdvancedStringWriting(() -> dataHandleService.create(loc));
		}
	}

	public abstract Class<? extends DataHandle<?>> getExpectedHandleType();

	public abstract Location createLocation() throws IOException;

	// -- Internal methods --

	/**
	 * Creates a handle for testing
	 */
	public DataHandle<? extends Location> createHandle() {
		Location loc;
		try {
			loc = createLocation();
		}
		catch (final IOException exc) {
			throw new RuntimeException(exc);
		}
		final DataHandle<Location> handle = dataHandleService.create(loc);
		assertEquals(getExpectedHandleType(), handle.getClass());
		return handle;
	}

	/**
	 * Populates the provided {@link OutputStream} with test data.
	 *
	 * @param out the {@link OutputStream} to fill
	 * @throws IOException
	 */
	public void populateData(final OutputStream out) throws IOException {
		out.write(BYTES);
		out.close();
	}

	/**
	 * Checks basic byte reading methods.
	 *
	 * @param handle the handle to test
	 * @param checkLength whether to check the total length of the handle
	 * @throws IOException
	 */
	public <L extends Location> void checkBasicReadMethods(
		final DataHandle<L> handle, boolean checkLength) throws IOException
	{
		assertEquals(0, handle.offset());
		if (checkLength) {
			assertEquals(BYTES.length, handle.length());
		}
		assertEquals("UTF-8", handle.getEncoding());

		// test read()
		for (int i = 0; i < BYTES.length; i++) {
			assertEquals(msg(i), 0xff & BYTES[i], handle.read());
		}
		assertEquals(-1, handle.read());
		handle.seek(10);
		assertEquals(10, handle.offset());
		assertEquals(BYTES[10], handle.read());

		// test read(byte[])
		final byte[] buf = new byte[10];
		handle.seek(1);
		assertBytesMatch(1, handle.read(buf), buf);

		// test readByte()
		handle.seek(0);
		for (int i = 0; i < BYTES.length; i++) {
			assertEquals(msg(i), BYTES[i], handle.readByte());
		}

		// test readUnsignedByte()
		handle.seek(0);
		for (int i = 0; i < BYTES.length; i++) {
			assertEquals(msg(i), BYTES[i] & 0xff, handle.readUnsignedByte());
		}

		// test readFully(byte[])
		Arrays.fill(buf, (byte) 0);
		handle.seek(3);
		handle.readFully(buf);
		assertBytesMatch(3, buf.length, buf);

		// test readCString() - _includes_ the null terminator!
		handle.seek(16);
		assertBytesMatch(16, 7, handle.readCString().getBytes());
		handle.seek(42);
		assertNull(handle.readCString());

		// test readBoolean
		handle.seek(21);
		assertTrue(handle.readBoolean());
		assertFalse(handle.readBoolean());

		// test readLine() - _excludes_ the newline terminator!
		handle.seek(7);
		assertBytesMatch(7, 5, handle.readLine().getBytes());

		// test readString(String) - _includes_ the matching terminator!
		handle.seek(7);
		assertBytesMatch(7, 5, handle.readString("abcdefg").getBytes());

		// test readString()
		handle.seek(7);
		assertBytesMatch(7, 5, handle.readString("d").getBytes());

		// test readString(int
		handle.seek(7);
		assertBytesMatch(7, 5, handle.readString(5).getBytes());

		// test findString(String) - _includes_ the matching terminator!
		handle.seek(1);
		assertBytesMatch(1, 11, handle.findString("world").getBytes());

		handle.seek(0);
		handle.findString(false, "world");
		assertEquals(12, handle.offset());

		handle.seek(0);
		handle.findString(false, "w");
		assertEquals(8, handle.offset());
	}

	/**
	 * Checks reading methods effected by endianness. Tests both
	 * {@link ByteOrder#LITTLE_ENDIAN} and {@link ByteOrder#BIG_ENDIAN}.
	 *
	 * @param handle the handle to check
	 * @throws IOException
	 */
	public void checkEndiannessReading(
		final DataHandle<? extends Location> handle) throws IOException
	{
		checkEndiannessReading(handle, ByteOrder.LITTLE_ENDIAN);
		checkEndiannessReading(handle, ByteOrder.BIG_ENDIAN);
	}

	/**
	 * Checks reading methods effected by endianness.
	 *
	 * @param handle the handle to check
	 * @param order the {@link ByteOrder} to check
	 * @throws IOException
	 */
	public void checkEndiannessReading(
		final DataHandle<? extends Location> handle, final ByteOrder order)
		throws IOException
	{
		handle.setOrder(order);
		handle.seek(0);
		final boolean little = order == ByteOrder.LITTLE_ENDIAN;

		// test readChar()

		handle.seek(0);
		for (int i = 0; i < BYTES.length / 2; i += 2) {
			assertEquals(msg(i), (char) Bytes.toShort(BYTES, i, little), handle
				.readChar());
		}

		// test readShort()
		handle.seek(0);
		for (int i = 0; i < BYTES.length / 2; i += 2) {
			assertEquals(msg(i), Bytes.toShort(BYTES, i, little), handle.readShort());
		}

		// test readInt()
		handle.seek(0);
		for (int i = 0; i < BYTES.length / 4; i += 4) {
			assertEquals(msg(i), Bytes.toInt(BYTES, i, little), handle.readInt());
		}

		// test readLong()
		handle.seek(0);
		for (int i = 0; i < BYTES.length / 8; i += 8) {
			assertEquals(msg(i), Bytes.toLong(BYTES, i, little), handle.readLong());
		}

		// test readFloat()
		handle.seek(0);
		for (int i = 0; i < BYTES.length / 4; i += 4) {
			assertEquals(msg(i), Bytes.toFloat(BYTES, i, little), handle.readFloat(),
				0);
		}

		// test readDouble()
		handle.seek(0);
		for (int i = 0; i < BYTES.length / 8; i += 8) {
			assertEquals(msg(i), Bytes.toDouble(BYTES, i, little), handle
				.readDouble(), 0);
		}
	}

	/**
	 * Check basic write methods for bytes.
	 *
	 * @param handle the handle to write to and read from
	 * @throws IOException
	 */
	public <L extends Location> void checkBasicWriteMethods(
		final DataHandle<L> handle) throws IOException
	{
		checkBasicWrites(handle, handle);
	}

	/**
	 * Tests basic write methods for bytes, both provided handles must point to
	 * the same location!
	 *
	 * @param readHandle the handle to read from
	 * @param writeHandle the handle to write from
	 * @throws IOException
	 */
	public <L extends Location> void checkBasicWrites(
		final DataHandle<L> readHandle, final DataHandle<L> writeHandle)
		throws IOException
	{
		final byte[] copy = BYTES.clone();

		// change the data
		writeHandle.seek(7);
		final String splice = "there";
		for (int i = 0; i < splice.length(); i++) {
			final char c = splice.charAt(i);
			writeHandle.write(c);
			copy[7 + i] = (byte) c;
		}

		writeHandle.writeBoolean(true);
		copy[12] = 1;
		writeHandle.writeBoolean(false);
		copy[13] = 0;

		writeHandle.writeByte(42);
		copy[14] = 42;

		if (writeHandle != readHandle) {
			writeHandle.close(); // to ensure data is flushed
		}

		// verify the changes
		readHandle.seek(0);
		for (int i = 0; i < copy.length; i++) {
			assertEquals(msg(i), 0xff & copy[i], readHandle.read());
		}
	}

	/**
	 * Checks advanced string writing methods.
	 *
	 * @param handleCreator a supplier that creates properly initialized handles
	 *          for reading and writing, all created handles must point to the
	 *          same location!
	 * @throws IOException
	 */
	public <L extends Location> void checkAdvancedStringWriting(
		final Supplier<DataHandle<L>> handleCreator) throws IOException
	{
		checkAdvancedStringWriting(handleCreator, handleCreator);
	}

	/**
	 * Checks advanced string writing methods.
	 *
	 * @param readHandleCreator a supplier that creates properly initialized
	 *          handles for reading, all created handles must point to the same
	 *          location!
	 * @param writeHandleCreator a supplier that creates properly initialized
	 *          handles for reading, all created handles must point to the same
	 *          location!
	 * @throws IOException
	 */
	public <L extends Location> void checkAdvancedStringWriting(
		final Supplier<DataHandle<L>> readHandleCreator,
		final Supplier<DataHandle<L>> writeHandleCreator) throws IOException
	{
		// test writeUTF() / readUTF()
		final String utfTestString = "abcäúöäéëåáðßïœœø¶🤓🍕😋";
		try (DataHandle<L> writeHandle = writeHandleCreator.get()) {
			writeHandle.writeUTF(utfTestString);
		}
		try (DataHandle<L> readHandle = readHandleCreator.get()) {
			assertEquals(utfTestString, readHandle.readUTF());
		}

		// test writeLine()
		final String testString = "The quick brown fox jumps over the lazy dog.";
		try (DataHandle<L> writeHandle = writeHandleCreator.get()) {
			writeHandle.writeLine(testString);
		}
		try (DataHandle<L> readHandle = readHandleCreator.get()) {
			assertEquals(testString, readHandle.readLine());
		}

		// test writeChars / findString
		final String testString2 = "The five boxing wizards jump quickly.";
		try (DataHandle<L> writeHandle = writeHandleCreator.get()) {
			writeHandle.writeChars(testString2);
		}
		try (DataHandle<L> readHandle = readHandleCreator.get()) {
			for (int i = 0; i < testString2.length(); i++) {
				assertEquals(testString2.charAt(i), readHandle.readChar());
			}
		}
	}

	/**
	 * Checks writing methods affected by endianness.
	 *
	 * @param readHandleCreator a supplier that creates properly initialized
	 *          handles for reading, all created handles must point to the same
	 *          location!
	 * @param writeHandleCreator a supplier that creates properly initialized
	 *          handles for reading, all created handles must point to the same
	 *          location!
	 * @throws IOException
	 */
	public <L extends Location> void checkWriteEndianes(
		final Supplier<DataHandle<L>> handleCreator, final ByteOrder order)
		throws IOException
	{
		checkWriteEndianes(handleCreator, handleCreator, order);
	}

	public <L extends Location> void checkWriteEndianes(
		final Supplier<DataHandle<L>> readHandleCreator,
		final Supplier<DataHandle<L>> writeHandleCreator, final ByteOrder order)
		throws IOException
	{
		final boolean little = order == ByteOrder.LITTLE_ENDIAN;

		// test writeChar()
		try (DataHandle<L> writeHandle = writeHandleCreator.get()) {
			writeHandle.setOrder(order);
			for (int i = 0; i < BYTES.length / 2; i += 2) {
				writeHandle.writeChar(Bytes.toInt(BYTES, i, 2, little));
			}
		}

		try (DataHandle<L> readHandle = readHandleCreator.get()) {
			readHandle.setOrder(order);
			for (int i = 0; i < BYTES.length / 2; i += 2) {
				assertEquals(msg(i), Bytes.toShort(BYTES, i, little), readHandle
					.readChar());
			}
		}

		// test writeShort()
		try (DataHandle<L> writeHandle = writeHandleCreator.get()) {
			writeHandle.setOrder(order);
			for (int i = 0; i < BYTES.length / 2; i += 2) {
				writeHandle.writeShort(Bytes.toShort(BYTES, i, little));
			}
		}

		try (DataHandle<L> readHandle = readHandleCreator.get()) {
			readHandle.setOrder(order);
			for (int i = 0; i < BYTES.length / 2; i += 2) {
				assertEquals(msg(i), Bytes.toShort(BYTES, i, little), readHandle
					.readShort());
			}
		}

		// test writeInt()
		try (DataHandle<L> writeHandle = writeHandleCreator.get()) {
			writeHandle.setOrder(order);
			for (int i = 0; i < BYTES.length / 4; i += 4) {
				writeHandle.writeInt(Bytes.toInt(BYTES, i, little));
			}
		}
		try (DataHandle<L> readHandle = readHandleCreator.get()) {
			readHandle.setOrder(order);
			for (int i = 0; i < BYTES.length / 4; i += 4) {
				assertEquals(msg(i), Bytes.toInt(BYTES, i, little), readHandle
					.readInt());
			}
		}

		// test writeLong()
		try (DataHandle<L> writeHandle = writeHandleCreator.get()) {
			writeHandle.setOrder(order);
			for (int i = 0; i < BYTES.length / 8; i += 8) {
				writeHandle.writeLong(Bytes.toLong(BYTES, i, little));
			}
		}
		try (DataHandle<L> readHandle = readHandleCreator.get()) {
			readHandle.setOrder(order);
			for (int i = 0; i < BYTES.length / 8; i += 8) {
				assertEquals(msg(i), Bytes.toLong(BYTES, i, little), readHandle
					.readLong());
			}
		}

		// test writeFloat()
		try (DataHandle<L> writeHandle = writeHandleCreator.get()) {
			writeHandle.setOrder(order);
			for (int i = 0; i < BYTES.length / 4; i += 4) {
				writeHandle.writeFloat(Bytes.toFloat(BYTES, i, little));
			}
		}
		try (DataHandle<L> readHandle = readHandleCreator.get()) {
			readHandle.setOrder(order);
			for (int i = 0; i < BYTES.length / 4; i += 4) {
				assertEquals(msg(i), Bytes.toFloat(BYTES, i, little), readHandle
					.readFloat(), 0);
			}
		}

		// test writeDouble()
		try (DataHandle<L> writeHandle = writeHandleCreator.get()) {
			writeHandle.setOrder(order);
			for (int i = 0; i < BYTES.length / 8; i += 8) {
				writeHandle.writeDouble(Bytes.toDouble(BYTES, i, little));
			}
		}
		try (DataHandle<L> readHandle = readHandleCreator.get()) {
			readHandle.setOrder(order);
			for (int i = 0; i < BYTES.length / 8; i += 8) {
				assertEquals(msg(i), Bytes.toDouble(BYTES, i, little), readHandle
					.readDouble(), 0);
			}
		}
	}

	// -- Internal methods --

	public void assertBytesMatch(final int offset, final int length,
		final byte[] b)
	{
		assertEquals(length, b.length);
		for (int i = 0; i < length; i++) {
			assertEquals(msg(i), BYTES[i + offset], b[i]);
		}
	}

	// -- Test methods --

	public String msg(final int i) {
		return "[" + i + "]:";
	}

}
