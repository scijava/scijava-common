package org.scijava.io;

import org.junit.Test;
import org.scijava.Context;
import org.scijava.plugin.PluginInfo;
import org.scijava.plugin.PluginService;
import org.scijava.plugin.SciJavaPlugin;
import org.scijava.service.SciJavaService;
import org.scijava.text.AbstractTextFormat;
import org.scijava.text.TextFormat;
import org.scijava.text.TextService;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class TypedIOServiceTest {

	@Test
	public void testTextFile() throws IOException {
		// create context, add dummy text format
		final Context ctx = new Context();
		ctx.getPluginIndex().add(new PluginInfo<>(DummyTextFormat.class, TextFormat.class));
		ctx.getPluginIndex().add(new PluginInfo<>(DefaultTextIOService.class, TextIOService.class));
		TextIOService instance = (TextIOService) ctx.getService(PluginService.class).createInstance(ctx.getPluginIndex().get(TextIOService.class).get(0));
		ctx.getServiceIndex().add(instance);

		// try to get the TextIOService
		final TextIOService io = ctx.service(TextIOService.class);
		assertNotNull(io);

		// open text file from resources as String
		String localFile = getClass().getResource("test.txt").getPath();
		String obj = io.open(localFile);
		assertNotNull(obj);
		assertTrue(obj.contains("content"));
	}

	interface TextIOService extends TypedIOService<String> {
	}

	public static class DefaultTextIOService extends AbstractTypedIOService<String> implements TextIOService {
	}

	public static class DummyTextFormat  extends AbstractTextFormat {

		@Override
		public List<String> getExtensions() {
			return Collections.singletonList("txt");
		}

		@Override
		public String asHTML(String text) {
			return text;
		}

	}
}
