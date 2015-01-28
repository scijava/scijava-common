
package org.scijava.path;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Abstract superclass for {@link LinkedPathNode} implementations.
 */
public class DefaultBranchingPathNode extends
	HashMap<String, BranchingPathNode> implements BranchingPathNode
{

	// -- Serialization UID --

	private static final long serialVersionUID = -8219740136180873921L;

	// -- Fields --

	private BranchingPathNode parent;

	private String description;

	// -- Constructors --

	/**
	 * Root construction (no parent).
	 */
	public DefaultBranchingPathNode() {
		this((BranchingPathNode) null);
	}

	/**
	 * Root constructor (no parent) with pre-populated children.
	 */
	public DefaultBranchingPathNode(Map<String, BranchingPathNode> children) {
		this(null, children);
	}

	/**
	 * Leaf constructor (has a parent but no children).
	 */
	public DefaultBranchingPathNode(final BranchingPathNode parent) {
		this(parent, Collections.<String, BranchingPathNode> emptyMap());
	}

	/**
	 * Constructs a {@link BranchingPathNode} with the specified parent and given
	 * children.
	 */
	public DefaultBranchingPathNode(final BranchingPathNode parent,
		final Map<String, BranchingPathNode> children)
	{
		this.parent = parent;
		putAll(children);
		description = "";
	}

	// -- BranchingPathNode API --

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public void appendDescription(final String toAppend) {
		//FIXME need to merge HTML here really
		description = description.concat(toAppend);
	}

	// -- LinkedPathNode API --

	@Override
	public BranchingPathNode getParent() {
		return parent;
	}

	@Override
	public boolean hasParent() {
		return parent != null;
	}

	@Override
	public boolean hasNext() {
		return !isEmpty();
	}

	@Override
	public BranchingPathNode getNext(final String path) {
		return get(path);
	}

}
