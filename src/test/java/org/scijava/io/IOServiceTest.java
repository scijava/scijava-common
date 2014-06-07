package org.scijava.io;

import org.junit.Test;
import org.scijava.Context;
import org.scijava.io.location.FileLocation;
import org.scijava.plugin.PluginInfo;
import org.scijava.text.TextFormat;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class IOServiceTest {

	@Test
	public void testTextFile() throws IOException {
		// create context, add dummy text format
		final Context ctx = new Context();
		ctx.getPluginIndex().add(new PluginInfo<>(DummyTextFormat.class, TextFormat.class));
		final IOService io = ctx.getService(IOService.class);

		// open text file from resources as String
		String localFile = getClass().getResource("test.txt").getPath();
		Object obj = io.open(localFile);
		assertNotNull(obj);
		String content = obj.toString();
		assertTrue(content.contains("content"));

		// open text file from resources as FileLocation
		obj = io.open(new FileLocation(localFile));
		assertNotNull(obj);
		assertEquals(content, obj.toString());
	}
}
