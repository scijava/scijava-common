
package org.scijava.path;

/**
 * Base interface for a structure for path traversal, where each node has a rich
 * (annotated) description.
 */
public interface PathNode {

	/**
	 * @return True iff there are more nodes in this path.
	 */
	boolean hasNext();

	/**
	 * TODO use html. Each node added to a path merges its description
	 *
	 * @return Returns the description for this node.
	 */
	String getDescription();

	/**
	 * @param description Description string to be appended to current description
	 *          of this node.
	 *          @see {@link #getDescription}
	 */
	void appendDescription(String description);
}
