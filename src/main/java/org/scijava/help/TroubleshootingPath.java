
package org.scijava.help;

import org.scijava.plugin.SingletonPlugin;

/**
 * Base interface for plugins consumed by the {@link TroubleshootingService}.
 */
public interface TroubleshootingPath extends SingletonPlugin, Iterable<String> {

	/**
	 * FIXME link to description parser
	 * TODO use html. Need to enhance TextDisplay
	 * so we can link to code from hyperlinks (e.g. to automatically run
	 * "report a bug") and establish a convention for easily converting
	 * to links.
	 *
	 * @return A mapping from each node named in {@link #getPath()} to the rich
	 *         for that node.
	 */
	String getDescription(String name);

}
