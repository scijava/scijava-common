
package org.scijava.path;

import java.util.Map;

import org.scijava.plugin.AbstractSingletonService;
import org.scijava.plugin.Plugin;
import org.scijava.service.Service;

/**
 * Default {@link TroubleshootingService} implementation.
 */
@Plugin(type = Service.class)
public class DefaultTroubleshootingService extends
	AbstractSingletonService<Path> implements
	TroubleshootingService
{

	private Map<String, LabeledMap> paths;

	// -- TroubleshootingPath API --

	@Override
	public Class<Path> getPluginType() {
		return Path.class;
	}

	@Override
	public LabeledMap getPath(final String root) {
		//TODO get shadow menu with the specified root
		//     build appropriate LabeledMap structure.
		//     Cache in paths. Check paths first.
		return null;
	}

}
