
package org.scijava.convert;

import java.io.File;
import java.nio.file.Path;

import org.scijava.plugin.Plugin;

/**
 * A {@link Converter} used to convert {@link Path}s into {@link File}s.
 * 
 * @author Gabriel Selzer
 */
@Plugin(type = Converter.class)
public class PathToFileConverter extends AbstractConverter<Path, File> {

	@SuppressWarnings("unchecked")
	@Override
	public <T> T convert(Object src, Class<T> dest) {
		Path p = (Path) src;
		return (T) p.toFile();
	}

	@Override
	public Class<File> getOutputType() {
		return File.class;
	}

	@Override
	public Class<Path> getInputType() {
		return Path.class;
	}
}
