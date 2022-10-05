
package org.scijava.convert;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.scijava.Context;
import org.scijava.parse.ParseService;

/**
 * Tests conversion between {@link File}s and {@link Path}s.
 * 
 * @author Gabriel Selzer
 */
public class FileToPathConversionTest {

	private ConvertService convertService;
	private Context context;

	@Before
	public void setUp() {
		context = new Context(ParseService.class, ConvertService.class);
		convertService = context.getService(ConvertService.class);
	}

	@After
	public void tearDown() {
		context.dispose();
	}

	/**
	 * Tests the ability of to convert from {@link File} to {@link Path}.
	 */
	@Test
	public void fileToPathConversion() {
		File f = new File("tmp.java");
		assertTrue(convertService.supports(f, Path.class));
		Path p = convertService.convert(f, Path.class);
		assertEquals(f.toPath(), p);
	}

	@Test
	public void pathToFileConversion() {
		Path p = Paths.get("tmp.java");
		assertTrue(convertService.supports(p, File.class));
		File f = convertService.convert(p, File.class);
		assertEquals(f.toPath(), p);
	}

}
