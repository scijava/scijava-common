
package org.scijava.io.location;

import org.scijava.io.handle.BZip2Handle;
import org.scijava.io.handle.DataHandle;

/**
 * {@link Location} backed by a {@link DataHandle} that is BZip2 compressed.
 * 
 * @author Gabriel Einsdorf
 * @see BZip2Handle
 */
public class BZip2Location extends AbstractHigherOrderLocation {

	/**
	 * Creates a {@link BZip2Location} wrapping the given location
	 *
	 * @param location the location to operate on
	 */
	public BZip2Location(final Location location) {
		super(location);
	}

}
