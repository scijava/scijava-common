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

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Date;

import org.scijava.io.location.FileLocation;
import org.scijava.plugin.Plugin;

/**
 * {@link DataHandle} for a {@link FileLocation}.
 * 
 * @author Curtis Rueden
 */
@Plugin(type = DataHandle.class)
public class FileHandle extends AbstractDataHandle<FileLocation> {

	// -- Fields --

	/** The {@link RandomAccessFile} backing this file handle. */
	private RandomAccessFile raf;

	/** The mode of the {@link RandomAccessFile}. */
	private String mode = "rw";

	/** True iff the {@link #close()} has already been called. */
	private boolean closed;

	// -- FileHandle methods --

	/**
	 * Gets the random access file object backing this FileHandle. If the
	 * underlying file does not exist yet, it will be created.
	 */
	public RandomAccessFile getRandomAccessFile() throws IOException {
		return writer();
	}

	public String getMode() {
		return mode;
	}

	public void setMode(final String mode) {
		if (raf != null) {
			throw new IllegalStateException("File already initialized");
		}
		this.mode = mode;
	}

	// -- DataHandle methods --

	@Override
	public boolean isReadable() {
		return getMode().contains("r");
	}

	@Override
	public boolean isWritable() {
		return getMode().contains("w");
	}

	@Override
	public boolean exists() {
		return get().getFile().exists();
	}

	@Override
	public Date lastModified() {
		final long lastModified = get().getFile().lastModified();
		return lastModified == 0 ? null : new Date(lastModified);
	}

	@Override
	public long offset() throws IOException {
		return exists() ? reader().getFilePointer() : 0;
	}

	@Override
	public long length() throws IOException {
		return exists() ? reader().length() : -1;
	}

	@Override
	public void setLength(final long length) throws IOException {
		writer().setLength(length);
	}

	@Override
	public int read() throws IOException {
		return reader().read();
	}

	@Override
	public int read(final byte[] b) throws IOException {
		return reader().read(b);
	}

	@Override
	public int read(final byte[] b, final int off, final int len)
		throws IOException
	{
		return reader().read(b, off, len);
	}

	@Override
	public void seek(final long pos) throws IOException {
		reader().seek(pos);
	}

	// -- DataInput methods --

	@Override
	public boolean readBoolean() throws IOException {
		return reader().readBoolean();
	}

	@Override
	public byte readByte() throws IOException {
		return reader().readByte();
	}

	@Override
	public char readChar() throws IOException {
		return reader().readChar();
	}

	@Override
	public double readDouble() throws IOException {
		return reader().readDouble();
	}

	@Override
	public float readFloat() throws IOException {
		return reader().readFloat();
	}

	@Override
	public void readFully(final byte[] b) throws IOException {
		reader().readFully(b);
	}

	@Override
	public void readFully(final byte[] b, final int off, final int len)
		throws IOException
	{
		reader().readFully(b, off, len);
	}

	@Override
	public int readInt() throws IOException {
		return reader().readInt();
	}

	@Override
	public String readLine() throws IOException {
		return reader().readLine();
	}

	@Override
	public long readLong() throws IOException {
		return reader().readLong();
	}

	@Override
	public short readShort() throws IOException {
		return reader().readShort();
	}

	@Override
	public int readUnsignedByte() throws IOException {
		return reader().readUnsignedByte();
	}

	@Override
	public int readUnsignedShort() throws IOException {
		return reader().readUnsignedShort();
	}

	@Override
	public String readUTF() throws IOException {
		return reader().readUTF();
	}

	@Override
	public int skipBytes(final int n) throws IOException {
		return reader().skipBytes(n);
	}

	// -- DataOutput methods --

	@Override
	public void write(final byte[] b) throws IOException {
		writer().write(b);
	}

	@Override
	public void write(final byte[] b, final int off, final int len)
		throws IOException
	{
		writer().write(b, off, len);
	}

	@Override
	public void write(final int b) throws IOException {
		writer().write(b);
	}

	@Override
	public void writeBoolean(final boolean v) throws IOException {
		writer().writeBoolean(v);
	}

	@Override
	public void writeByte(final int v) throws IOException {
		writer().writeByte(v);
	}

	@Override
	public void writeBytes(final String s) throws IOException {
		writer().writeBytes(s);
	}

	@Override
	public void writeChar(final int v) throws IOException {
		writer().writeChar(v);
	}

	@Override
	public void writeChars(final String s) throws IOException {
		writer().writeChars(s);
	}

	@Override
	public void writeDouble(final double v) throws IOException {
		writer().writeDouble(v);
	}

	@Override
	public void writeFloat(final float v) throws IOException {
		writer().writeFloat(v);
	}

	@Override
	public void writeInt(final int v) throws IOException {
		writer().writeInt(v);
	}

	@Override
	public void writeLong(final long v) throws IOException {
		writer().writeLong(v);
	}

	@Override
	public void writeShort(final int v) throws IOException {
		writer().writeShort(v);
	}

	@Override
	public void writeUTF(final String str) throws IOException {
		writer().writeUTF(str);
	}

	// -- Closeable methods --

	@Override
	public synchronized void close() throws IOException {
		if (raf != null) raf.close();
		closed = true;
	}

	// -- Typed methods --

	@Override
	public Class<FileLocation> getType() {
		return FileLocation.class;
	}

	// -- Helper methods --

	/**
	 * Access method for the internal {@link RandomAccessFile}, that succeeds
	 * independently of the underlying file existing on disk. This allows us to
	 * create a new file for writing.
	 *
	 * @return the internal {@link RandomAccessFile} creating a new file on disk
	 *         if needed.
	 * @throws IOException if the {@link RandomAccessFile} could not be created.
	 */
	private RandomAccessFile writer() throws IOException {
		if (raf == null) initRAF(true);
		return raf;
	}

	/**
	 * Access method for the internal {@link RandomAccessFile}, that only succeeds
	 * if the underlying file exists on disk. This prevents accidental creation of
	 * an empty file when calling read operations on a non-existent file.
	 *
	 * @return the internal {@link RandomAccessFile}.
	 * @throws IOException if the {@link RandomAccessFile} could not be created,
	 *           or the backing file does not exists.
	 */
	private RandomAccessFile reader() throws IOException {
		if (raf == null) initRAF(false);
		return raf;
	}

	/**
	 * Initializes the {@link RandomAccessFile}.
	 *
	 * @param create whether to create the {@link RandomAccessFile} if the
	 *          underlying file does not exist yet.
	 * @throws IOException if the {@link RandomAccessFile} could not be created,
	 *           or the backing file does not exist and the {@code create}
	 *           parameter was set to {@code false}.
	 */
	private synchronized void initRAF(final boolean create) throws IOException {
		if (!create && !exists()) {
			throw new IOException("Trying to read from non-existent file!");
		}
		if (closed) throw new IOException("Handle already closed");
		if (raf != null) return;
		raf = new RandomAccessFile(get().getFile(), getMode());
	}
}
