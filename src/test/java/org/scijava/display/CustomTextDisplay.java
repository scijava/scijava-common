package org.scijava.display;

public class CustomTextDisplay extends AbstractDisplay<String> implements
		TextDisplay
{

	public CustomTextDisplay() {
		super(String.class);
	}

	@Override
	public void append(final String text) {
		add(text);
	}

}
