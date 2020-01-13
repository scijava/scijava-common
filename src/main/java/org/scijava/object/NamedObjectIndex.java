package org.scijava.object;

import java.util.WeakHashMap;

/**
 * An {@link ObjectIndex} where each object can have an associated name.
 *
 * @author Jan Eglinger
 */
public class NamedObjectIndex<E> extends ObjectIndex<E> {
	
	private WeakHashMap<Object, String> nameMap;

	public NamedObjectIndex(final Class<E> baseClass) {
		super(baseClass);
		nameMap = new WeakHashMap<>();
	}

	public boolean add(E object, String name) {
		if (name != null)
			nameMap.put(object, name);
		return add(object);
	}

	public boolean add(E object, Class<?> type, String name, boolean batch) {
		if (name != null)
			nameMap.put(object, name);
		return add(object, type, batch);
	}

	public String getName(E object) {
		return nameMap.get(object);
	}
}
