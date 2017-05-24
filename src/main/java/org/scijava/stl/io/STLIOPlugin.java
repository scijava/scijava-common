
package org.scijava.stl.io;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.scijava.Priority;
import org.scijava.io.AbstractIOPlugin;
import org.scijava.io.IOPlugin;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.stl.STLFacet;
import org.scijava.stl.STLService;

/**
 * {@link IOPlugin} for handling STL files
 *
 * @author Richard Domander (Royal Veterinary College, London)
 * @see STLService
 */
@Plugin(type = IOPlugin.class, priority = Priority.LOW_PRIORITY - 1)
public class STLIOPlugin extends AbstractIOPlugin<List<STLFacet>> {

	@Parameter(required = false)
	private STLService stlService;

	@Override
	public Class<List<STLFacet>> getDataType() {
		return (Class) List.class;
	}

	@Override
	public boolean supportsOpen(final String source) {
		return stlService != null && stlService.supports(new File(source));
	}

	@Override
	public boolean supportsSave(final String destination) {
		return stlService != null && stlService.supports(new File(destination));
	}

	@Override
	public void save(final List<STLFacet> data, final String destination)
		throws IOException, NullPointerException
	{
		stlService.write(new File(destination), data);
	}
}
