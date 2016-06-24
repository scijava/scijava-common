package org.scijava.ext.obj;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.scijava.plugin.Plugin;

@Plugin(type = ObjectIOExternalizer.class)
public class LongArrayObjectIOExtV0 extends AbstractObjectIOExt<long[]> {

	@Override
	public void write(final ObjectOutput oos, final long[] obj) {
		try {
			oos.writeObject(obj);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public long[] read(final ObjectInput ois) {
		try {
			return (long[]) ois.readObject();
		} catch (IOException | ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	protected boolean canExternalize(final Object obj) {
		return obj instanceof long[];
	}
}
