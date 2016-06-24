package org.scijava.ext.obj;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.scijava.plugin.Plugin;

@Plugin(type = ObjectIOExternalizer.class)
public class DoubleArrayObjectIOExtV0 extends AbstractObjectIOExt<double[]> {

	@Override
	public void write(final ObjectOutput oos, final double[] obj) {
		try {
			oos.writeObject(obj);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public double[] read(final ObjectInput ois) {
		try {
			return (double[]) ois.readObject();
		} catch (IOException | ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	protected boolean canExternalize(Object obj) {
		return obj instanceof double[];
	}
}
