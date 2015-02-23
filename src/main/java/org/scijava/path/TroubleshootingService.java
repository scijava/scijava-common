
package org.scijava.path;

import org.scijava.plugin.SingletonService;

/**
 *
 */
public interface TroubleshootingService extends
	SingletonService<Path>
{

	/**
	 * TODO this method needs to take a menu root type,
	 * get the shadow menu and assemble it together with
	 * the labels of each plugin in the given root to create
	 * a decorated path object that can then be consumed.
	 */
	LabeledMap getPath(String root);
}
