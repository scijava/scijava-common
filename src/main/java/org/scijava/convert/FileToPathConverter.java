
package org.scijava.convert;

import java.io.File;
import java.nio.file.Path;

import org.scijava.plugin.Plugin;

/**
 * A {@link Converter} used to convert {@link File}s into {@link Path}s.
 * 
 * @author Gabriel Selzer
 */
@Plugin(type = Converter.class)
public class FileToPathConverter extends AbstractConverter<File, Path> {

	@SuppressWarnings("unchecked")
	@Override
	public <T> T convert(Object src, Class<T> dest) {
		File f = (File) src;
		return (T) f.toPath();
	}

	@Override
	public Class<Path> getOutputType() {
		return Path.class;
	}

	@Override
	public Class<File> getInputType() {
		return File.class;
	}
}
