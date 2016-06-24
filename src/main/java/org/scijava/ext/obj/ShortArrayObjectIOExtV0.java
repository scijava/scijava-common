package org.scijava.ext.obj;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.scijava.plugin.Plugin;

@Plugin(type = ObjectIOExternalizer.class)
public class ShortArrayObjectIOExtV0 extends AbstractObjectIOExt<short[]> {

	@Override
	public void write(final ObjectOutput oos, final short[] obj) {
		try {
			oos.writeObject(obj);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public short[] read(final ObjectInput ois) {
		try {
			return (short[]) ois.readObject();
		} catch (IOException | ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	protected boolean canExternalize(Object obj) {
		return obj instanceof short[];
	}
}
