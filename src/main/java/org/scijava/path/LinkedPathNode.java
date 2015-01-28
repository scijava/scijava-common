
package org.scijava.path;

/**
 * Interface for a {@link PathNode} that knows its parent.
 */
public interface LinkedPathNode extends PathNode {

	LinkedPathNode getParent();

	boolean hasParent();

}
