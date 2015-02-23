
package org.scijava.path;

import org.scijava.module.Module;
import org.scijava.plugin.SingletonPlugin;

/**
 * Base interface for plugins consumed by the {@link TroubleshootingService}.
 */
public interface Path extends SingletonPlugin {

	public static final String TAIL = "PATH_TAIL";

	/**
	 * Each path plugin can define its own set of labels to use when navigating
	 * its menu path. There should be a number of entries in this array equal
	 * to the number of non-TAIL entries in the plugin's path plus one. The
	 * first label in this array will be attached to the ROOT, while the last
	 * entry will perform the associated module action and lead to the TAIL.
	 *
	 * @return List of navigation labels to use to advance to each menu path
	 */
	String[] getPathLabels();

	/**
	 * @return Thing to run
	 */
	Module getModule();
}
