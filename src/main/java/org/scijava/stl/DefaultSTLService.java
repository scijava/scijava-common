
package org.scijava.stl;

import java.io.File;
import java.io.IOException;
import java.util.List;

import com.google.common.io.Files;
import org.scijava.plugin.AbstractHandlerService;
import org.scijava.plugin.Plugin;
import org.scijava.service.Service;

/**
 * Default service for working with STL formats
 *
 * @author Richard Domander (Royal Veterinary College, London)
 */
@Plugin(type = Service.class)
public class DefaultSTLService extends AbstractHandlerService<File, STLFormat>
	implements STLService
{

	@Override
	public List<STLFacet> read(final File file) throws IOException {
		final STLFormat format = getHandler(file);
		if (format == null) return null;
		return format.read(file);
	}

	@Override
	public void write(final File file, final List<STLFacet> facets) throws IOException {
		final STLFormat format = getHandler(file);
		if (format == null) return;
		final byte[] bytes = format.write(facets);

		Files.write(bytes, file);
	}

	// -- PTService methods --

	@Override
	public Class<STLFormat> getPluginType() {
		return STLFormat.class;
	}

	// -- Typed methods --

	@Override
	public Class<File> getType() {
		return File.class;
	}
}
