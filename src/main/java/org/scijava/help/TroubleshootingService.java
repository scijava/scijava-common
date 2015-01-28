
package org.scijava.help;

import org.scijava.path.BranchingPathNode;
import org.scijava.plugin.SingletonService;

/**
 *
 */
public interface TroubleshootingService extends
	SingletonService<TroubleshootingPath>
{

	/**
	 * @return A root {@link BranchingPathNode} generated from all discovered
	 *         {@link TroubleshootingPath} plugins.
	 */
	BranchingPathNode getRoot();
}
