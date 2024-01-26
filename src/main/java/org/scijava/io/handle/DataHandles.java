/*
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

package org.scijava.io.handle;

import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.scijava.io.location.Location;
import org.scijava.task.Task;

/**
 * Utility methods for working with {@link DataHandle}s.
 *
 * @author Curtis Rueden
 * @author Gabriel Einsdorf
 */
public final class DataHandles {

	private static Method utfMethod;

	private DataHandles() {
		// Prevent instantiation of utility class.
	}

	/**
	 * Writes a string to the specified DataOutput using modified UTF-8 encoding
	 * in a machine-independent manner.
	 * <p>
	 * First, two bytes are written to out as if by the {@code writeShort} method
	 * giving the number of bytes to follow. This value is the number of bytes
	 * actually written out, not the length of the string. Following the length,
	 * each character of the string is output, in sequence, using the modified
	 * UTF-8 encoding for the character. If no exception is thrown, the counter
	 * {@code written} is incremented by the total number of bytes written to the
	 * output stream. This will be at least two plus the length of {@code str},
	 * and at most two plus thrice the length of {@code str}.
	 * </p>
	 *
	 * @param str a string to be written.
	 * @param out destination to write to
	 * @return The number of bytes written out.
	 * @throws IOException if an I/O error occurs.
	 */
	public static int writeUTF(final String str, final DataOutput out)
		throws IOException
	{
		// HACK: Strangely, DataOutputStream.writeUTF(String, DataOutput)
		// has package-private access. We work around it via reflection.
		try {
			return (Integer) utfMethod().invoke(null, str, out);
		}
		catch (final IllegalAccessException | IllegalArgumentException
				| InvocationTargetException exc)
		{
			throw new IllegalStateException(
				"Cannot invoke DataOutputStream.writeUTF(String, DataOutput)", exc);
		}
	}

	// -- Helper methods --

	/** Gets the {@link #utfMethod} field, initializing if needed. */
	private static Method utfMethod() {
		if (utfMethod == null) initUTFMethod();
		return utfMethod;
	}

	/** Initializes the {@link #utfMethod} field. */
	private static synchronized void initUTFMethod() {
		if (utfMethod != null) return;
		try {
			final Method m = DataOutputStream.class.getDeclaredMethod("writeUTF",
				String.class, DataOutput.class);
			m.setAccessible(true);
			utfMethod = m;
		}
		catch (final NoSuchMethodException | SecurityException exc) {
			throw new IllegalStateException(
				"No usable DataOutputStream.writeUTF(String, DataOutput)", exc);
		}
	}
	
	protected static IOException readOnlyException() {
		return new IOException("This handle is read-only!");
	}

	protected static IOException writeOnlyException() {
		return new IOException("This handle is write-only!");
	}


	/**
	 * Copies all bytes from the input to the output handle. Reading and writing
	 * start at the current positions of the handles.
	 *
	 * @param in the input handle
	 * @param out the output handle
	 * @return the number of bytes copied
	 * @throws IOException if an I/O error occurs.
	 */
	public static long copy(final DataHandle<Location> in,
		final DataHandle<Location> out) throws IOException
	{
		return copy(in, out, 0l, null);
	}

	/**
	 * Copies all bytes from the input to the output handle, reporting the
	 * progress to the provided task. Reading and writing start at the current
	 * positions of the handles.
	 *
	 * @param in the input handle
	 * @param out the output handle
	 * @param task task to report progress to
	 * @return the number of bytes copied
	 * @throws IOException if an I/O error occurs.
	 */
	public static long copy(final DataHandle<Location> in,
		final DataHandle<Location> out, final Task task) throws IOException
	{
		return copy(in, out, 0l, task);
	}

	/**
	 * Copies up to <code>length</code> bytes from the input to the output handle.
	 * Reading and writing start at the current positions of the handles. Stops
	 * early if there are no more bytes available from the input handle.
	 *
	 * @param in the input handle
	 * @param out the output handle
	 * @param length maximum number of bytes to copy; will copy all bytes if set
	 *          to <code>0</code>
	 * @return the number of bytes copied
	 * @throws IOException if an I/O error occurs.
	 */
	public static long copy(final DataHandle<Location> in,
		final DataHandle<Location> out, final int length) throws IOException
	{
		return copy(in, out, length, null);
	}

	/**
	 * Copies up to <code>length</code> bytes from the input to the output handle,
	 * reporting the progress to the provided task. Reading and writing start at
	 * the current positions of the handles. Stops early if there are no more
	 * bytes available from the input handle.
	 *
	 * @param in input handle
	 * @param out the output handle
	 * @param length maximum number of bytes to copy; will copy all bytes if set
	 *          to <code>0</code>
	 * @param task a task object to use for reporting the status of the copy
	 *          operation. Can be <code>null</code> if no reporting is needed.
	 * @return the number of bytes copied
	 * @throws IOException if an I/O error occurs.
	 */
	public static long copy(final DataHandle<Location> in,
		final DataHandle<Location> out, final long length, final Task task)
		throws IOException
	{
		return copy(in, out, length, task, 64 * 1024);
	}

	/**
	 * Copies up to <code>length</code> bytes from the input to the output handle,
	 * reporting the progress to the provided task. Reading and writing start at
	 * the current positions of the handles. Stops early if there are no more
	 * bytes available from the input handle. Uses a buffer of the provided size,
	 * instead of using the default size.
	 *
	 * @param in input handle
	 * @param out the output handle
	 * @param length maximum number of bytes to copy, will copy all bytes if set
	 *          to <code>0</code>
	 * @param task a task object to use for reporting the status of the copy
	 *          operation. Can be <code>null</code> if no reporting is needed.
	 * @return the number of bytes copied
	 * @throws IOException if an I/O error occurs.
	 */
	public static long copy(final DataHandle<Location> in,
		final DataHandle<Location> out, final long length, final Task task,
		final int bufferSize) throws IOException
	{

		// get length of input
		final long inputlength;
		{
			long i = 0;
			try {
				i = in.length();
			}
			catch (final IOException exc) {
				// Assume unknown length.
				i = 0;
			}
			inputlength = i;
		}

		if (task != null) {
			if (length > 0) task.setProgressMaximum(length);
			else if (inputlength > 0) task.setProgressMaximum(inputlength);
		}

		final byte[] buffer = new byte[bufferSize];
		long totalRead = 0;

		while (true) {
			if (task != null && task.isCanceled()) break;
			final int r;
			// ensure we do not read more than required into the buffer
			if (length > 0 && totalRead + bufferSize > length) {
				int remaining = (int) (length - totalRead);
				r = in.read(buffer, 0, remaining);
			}
			else {
				r = in.read(buffer);
			}
			if (r <= 0) break; // EOF
			if (task != null && task.isCanceled()) break;
			out.write(buffer, 0, r);
			totalRead += r;
			if (task != null) {
				task.setProgressValue(task.getProgressValue() + r);
			}
		}
		return totalRead;
	}
}
