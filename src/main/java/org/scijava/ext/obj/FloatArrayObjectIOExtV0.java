package org.scijava.ext.obj;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.scijava.plugin.Plugin;

@Plugin(type = ObjectIOExternalizer.class)
public class FloatArrayObjectIOExtV0 extends AbstractObjectIOExt<float[]> {

	@Override
	public void write(final ObjectOutput oos, float[] obj) {
		try {
			oos.writeObject(obj);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public float[] read(final ObjectInput ois) {
		try {
			return (float[]) ois.readObject();
		} catch (IOException | ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	protected boolean canExternalize(Object obj) {
		return obj instanceof float[];
	}
}
