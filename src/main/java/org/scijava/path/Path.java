
package org.scijava.path;

import org.scijava.plugin.SingletonPlugin;

/**
 * Base interface for plugins consumed by the {@link TroubleshootingService}.
 */
public interface Path extends SingletonPlugin {

	public static final String TAIL = "PATH_TAIL";

	/**
	 * All paths of a given pathtype will have a common root and tail node.
	 *
	 * TODO should we make this a TypedPlugin instead..? Seems like maybe unnecessary overhead..
	 *
	 * @return A string identifying the path containing this plugin
	 */
	String getPathType();

	String getPosition();

	/**
	 * @return Thing to run
	 */
	String getModule();
}
