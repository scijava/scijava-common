
package org.scijava.stl;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.scijava.plugin.HandlerService;
import org.scijava.service.SciJavaService;

/**
 * Interface for service that works with STL formats.
 *
 * @author Richard Domander (Royal Veterinary College, London)
 */
public interface STLService extends HandlerService<File, STLFormat>,
	SciJavaService
{

	/** Reads the data from the given file into a string. */
	List<STLFacet> read(File file) throws IOException;

	/** Writes the facets into the given file */
	void write(File file, List<STLFacet> facets) throws IOException;

	// -- HandlerService methods --

	/** Gets the STL format which best handles the given file. */
	@Override
	STLFormat getHandler(File file);

	// -- SingletonService methods --

	/** Gets the list of available STL formats. */
	@Override
	List<STLFormat> getInstances();

	// -- Typed methods --

	/** Gets whether the given file contains STL data in a supported format. */
	@Override
	boolean supports(File file);
}
