
package org.scijava.stl;

import com.google.common.base.Strings;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.scijava.plugin.AbstractHandlerPlugin;
import org.scijava.util.FileUtils;

/**
 * An abstract superclass of binary {@link STLFormat} implementations
 * <p>
 * Binary STL formats include the standard and non-standard colour variants
 * </p>
 *
 * @author Richard Domander (Royal Veterinary College, London)
 */
public abstract class AbstractBinarySTLFormat extends
	AbstractHandlerPlugin<File> implements STLFormat
{

	public static final int HEADER_BYTES = 80;
	public static final String HEADER = Strings.padEnd(
		"Binary STL created with ImageJ", HEADER_BYTES, '.');
	public static final int COUNT_BYTES = 4;
	public static final int FACET_START = HEADER_BYTES + COUNT_BYTES;
	public static final int FACET_BYTES = 50;

	@Override
	public List<STLFacet> read(final File stlFile) throws IOException {
		if (stlFile == null) {
			return Collections.emptyList();
		}

		final byte[] data = Files.readAllBytes(Paths.get(stlFile
			.getAbsolutePath()));

		return readFacets(data);
	}

	@Override
	public boolean supports(final File file) {
		final String extension = FileUtils.getExtension(file);
		if (!EXTENSION.equalsIgnoreCase(extension)) {
			return false;
		}

		try (FileInputStream reader = new FileInputStream(file)) {
			final byte[] dataStart = new byte[5];
			reader.read(dataStart, 0, 5);
			// ASCII STL files begin with the line solid <name> whereas binary files
			// have an arbitrary header
			return !"solid".equals(Arrays.toString(dataStart));
		}
		catch (IOException e) {
			return false;
		}
	}

	@Override
	public Class<File> getType() {
		return File.class;
	}

	public abstract List<STLFacet> readFacets(final byte[] data)
		throws IOException;
}
