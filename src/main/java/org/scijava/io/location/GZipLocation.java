
package org.scijava.io.location;

import org.scijava.io.handle.DataHandle;
import org.scijava.io.handle.GZipHandle;

/**
 * {@link Location} backed by a {@link DataHandle} that is <code>gzip</code>
 * compressed.
 * 
 * @author Gabriel Einsdorf
 * @see GZipHandle
 */
public class GZipLocation extends AbstractHigherOrderLocation {

	public GZipLocation(Location location) {
		super(location);
	}

}
