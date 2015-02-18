
package org.scijava.path;

import org.scijava.path.BranchingPathNode;
import org.scijava.path.DefaultBranchingPathNode;
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

	private BranchingPathNode root;

	// -- TroubleshootingPath API --

	@Override
	public BranchingPathNode getRoot() {
		if (root == null) {
			synchronized (this) {
				// Initialize the root node if it does not already exist
				if (root == null) {
					root = new DefaultBranchingPathNode();
					// For each path we start from the root node and build the
					// specified path.
					for (final Path path : getInstances()) {
						BranchingPathNode currentNode = root;
						for (final String name : path) {
							currentNode.appendDescription(path.getDescription(name));
							BranchingPathNode nextNode = getNextNode(currentNode, name);
							currentNode.put(name, nextNode);
							currentNode = nextNode;
						}
					}
				}
			}
		}

		return root;
	}

	// -- SingletonService API --

	@Override
	public Class<Path> getPluginType() {
		return Path.class;
	}

	// -- Helper methods --

	/**
	 * Gets the next node along the specified path from the the current node,
	 * creating it if necessary.
	 *
	 * @param currentNode Base node to search
	 * @param name Path in the base node to traverse
	 * @return The node in {@code currentNode} mapped with key {@code name}
	 */
	private BranchingPathNode getNextNode(final BranchingPathNode currentNode,
		final String name)
	{
		BranchingPathNode nextNode = currentNode.get(name);
		if (nextNode == null) nextNode = new DefaultBranchingPathNode(currentNode);
		return nextNode;
	}
}
