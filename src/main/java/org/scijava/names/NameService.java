package org.scijava.names;

import org.scijava.plugin.AbstractHandlerService;
import org.scijava.plugin.HandlerService;
import org.scijava.plugin.Plugin;
import org.scijava.service.SciJavaService;
import org.scijava.service.Service;

@Plugin(type=Service.class)
public class NameService extends AbstractHandlerService<Object, NameProvider> implements HandlerService<Object, NameProvider>, SciJavaService {

	@Override
	public Class<NameProvider> getPluginType() {
		return NameProvider.class;
	}

	@Override
	public Class<Object> getType() {
		return Object.class;
	}

	public String getName(Object thing) {
		NameProvider handler = getHandler(thing);
		if (handler == null) return null;
		return handler.getName(thing);
	}
}
