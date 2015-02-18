
package org.scijava.path;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

public abstract class AbstractTroubleshootingPath implements
	Path
{

	// -- Fields --

	private final Map<String, String> path = new LinkedHashMap<String, String>();

	// -- TorubleshootingPath API --

	@Override
	public String getDescription(final String name) {
		return path.get(name);
	}

	// -- Iterable API --

	@Override
	public Iterator<String> iterator() {
		return path.keySet().iterator();
	}

	// -- Protected methods --
	/**
	 * @param names A {@code ">"} separated string of node names for this path.
	 * @param descriptions A parallel {@code String} array with descriptions for
	 *          each node of the path.
	 */
	protected void setDescription(final String names,
		final String... descriptions)
	{
		final StringTokenizer stk = new StringTokenizer(names, ">");
		checkParams(stk.countTokens(), descriptions.length);
		int position = 0;
		while (stk.hasMoreTokens()) {
			path.put(stk.nextToken(), descriptions[position++]);
		}
	}

	/**
	 * @param names A {@link List} of node names for this path.
	 * @param descriptions A parallel {@link List} of node descriptions for this
	 *          path.
	 */
	protected void setDescription(final List<String> names,
		final List<String> descriptions)
	{
		checkParams(names.size(), descriptions.size());
		for (int i = 0; i < names.size(); i++) {
			path.put(names.get(i), descriptions.get(i));
		}
	}

	/**
	 * @param nodes A {@link Map} of node names to node descriptions for this
	 *          path.
	 */
	protected void setDescription(final Map<String, String> nodes) {
		path.putAll(nodes);
	}

	// -- Helper methods --

	private void checkParams(int nodeCount, int descriptionCount) {
		if (nodeCount != descriptionCount) throw new IllegalArgumentException(
			"Invalid troubleshooting path - differing number of node names and descriptions: " +
				nodeCount + " nodes, " + descriptionCount + " descriptions.");
	}
}
