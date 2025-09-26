package org.scijava.names;

import org.scijava.plugin.HandlerPlugin;

public interface NameProvider extends HandlerPlugin<Object> {
	public String getName(Object thing);

	@Override
	default Class<Object> getType() {
		return Object.class;
	}
}
