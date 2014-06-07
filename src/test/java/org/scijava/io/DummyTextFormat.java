package org.scijava.io;

import org.scijava.plugin.Plugin;
import org.scijava.text.AbstractTextFormat;
import org.scijava.text.TextFormat;

import java.util.Collections;
import java.util.List;

@Plugin(type = TextFormat.class)
public class DummyTextFormat  extends AbstractTextFormat {

	@Override
	public List<String> getExtensions() {
		return Collections.singletonList("txt");
	}

	@Override
	public String asHTML(String text) {
		return text;
	}
}
