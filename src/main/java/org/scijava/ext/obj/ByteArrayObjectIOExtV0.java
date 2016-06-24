package org.scijava.ext.obj;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.scijava.plugin.Plugin;

@Plugin(type = ObjectIOExternalizer.class)
public class ByteArrayObjectIOExtV0 extends AbstractObjectIOExt<byte[]> {

	@Override
	public void write(final ObjectOutput oos, final byte[] obj) {
		try {
			oos.writeObject(obj);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public byte[] read(final ObjectInput ois) {
		try {
			return (byte[]) ois.readObject();
		} catch (IOException | ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	protected boolean canExternalize(final Object obj) {
		return obj instanceof byte[];
	}
}
