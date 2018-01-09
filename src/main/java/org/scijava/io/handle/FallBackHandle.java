
package org.scijava.io.handle;

import java.io.IOException;

import org.scijava.Priority;
import org.scijava.io.location.Location;
import org.scijava.plugin.Plugin;

/**
 * Fallback handle to prevent DataHandleService.create() to return
 * <code>null</code>
 * 
 * @author Gabriel Einsdorf
 */
@Plugin(type = DataHandle.class, priority = Priority.VERY_LOW)
public class FallBackHandle extends AbstractDataHandle<Location> {

	@Override
	public boolean isReadable() {
		return false;
	}

	@Override
	public boolean isWritable() {
		return false;
	}

	@Override
	public boolean exists() throws IOException {
		return false;
	}

	@Override
	public long offset() throws IOException {
		throw fail();
	}

	@Override
	public void seek(long pos) throws IOException {
		throw fail();
	}

	@Override
	public long length() throws IOException {
		throw fail();
	}

	@Override
	public void setLength(long length) throws IOException {
		throw fail();
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		throw fail();
	}

	@Override
	public Class<Location> getType() {
		return Location.class;
	}

	@Override
	public byte readByte() throws IOException {
		throw fail();
	}

	@Override
	public void write(int b) throws IOException {
		throw fail();
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		throw fail();
	}

	@Override
	public void close() throws IOException {
		// NO-OP
	}

	private IOException fail() {
		return new IOException("Could not create handle for: " + get().getClass() +
			" created fallback handle does not support any data acces!");
	}

}
