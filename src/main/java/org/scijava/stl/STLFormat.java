
package org.scijava.stl;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.scijava.plugin.HandlerPlugin;

/**
 * {@code STLFormat} plugins provide handling for different kinds of STL files
 * <p>
 * STL files can be saved in binary or ascii
 * </p>
 *
 * @author Richard Domander (Royal Veterinary College, London)
 */
public interface STLFormat extends HandlerPlugin<File> {

	String EXTENSION = "stl";

	/**
	 * Reads the STL facets from the given File which can then be converted into a
	 * mesh
	 */
	List<STLFacet> read(final File stlFile) throws IOException;

	/** Writes the facets into a byte[] that can then be saved into a file */
	byte[] write(final List<STLFacet> facets) throws IOException;
}
