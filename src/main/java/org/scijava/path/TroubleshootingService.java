
package org.scijava.path;

import org.scijava.path.BranchingPathNode;
import org.scijava.plugin.SingletonService;

/**
 *
 */
public interface TroubleshootingService extends
	SingletonService<Path>
{

	/**
	 * @return A root {@link BranchingPathNode} generated from all discovered
	 *         {@link Path} plugins.
	 */
	BranchingPathNode getRoot();
}
