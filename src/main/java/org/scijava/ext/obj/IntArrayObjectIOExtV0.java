package org.scijava.ext.obj;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.scijava.plugin.Plugin;

@Plugin(type = ObjectIOExternalizer.class)
public class IntArrayObjectIOExtV0 extends AbstractObjectIOExt<int[]> {

	@Override
	public void write(final ObjectOutput oos, final int[] obj) {
		try {
			oos.writeObject(obj);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public int[] read(final ObjectInput ois) {
		try {
			return (int[]) ois.readObject();
		} catch (IOException | ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	protected boolean canExternalize(final Object obj) {
		return obj instanceof int[];
	}
}
