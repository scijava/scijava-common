
package org.scijava.path;

import java.util.Map;

/**
 * Interface for {@code 1-to-many} {@link LinkedPathNode}.
 */
public interface BranchingPathNode extends Map<String, BranchingPathNode>,
	LinkedPathNode
{

	/**
	 * @param path Key of the next desired ndoe in this path.
	 * @return Next node in the path matching the provided key.
	 */
	BranchingPathNode getNext(String path);

	// -- LinkedPathNode API --

	// Return type narrowing
	BranchingPathNode getParent();
}
