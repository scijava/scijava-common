package org.scijava.ui;

import java.util.function.Consumer;

/**
 * Representation of an outcome after opening some input, be it a file on a drive, URL,
 * object from a drag-and-drop event or alike. The outcome is represented with an object
 * with the data itself, and a method that knows how to present (that means read and display)
 * this data. The class is primarily intended for opening inputs which ImageJ2 is not normally
 * able to open. Example of such opening outcomes are opening of a specific/proprietary data file
 * for GUI-based applications such as BigDataViewer or Mastodon.
 *
 * @param <T> The particular type for the particular data.
 *
 * @author Curtis Rueden, Vladimir Ulman
 */
public class SelfShowableContent<T> {
	private T content;
	private Consumer<T> showAction;

	/**
	 * Binds together a particular piece of data and a method that knows how to open it.
	 * @param content
	 * @param showAction
	 */
	public SelfShowableContent(T content, Consumer<T> showAction) {
		this.content = content;
		this.showAction = showAction;
	}

	/** Getter of the stored data. */
	public T content() {
		return content;
	}

	/**
	 * This starts the actual opening/consuming of the stored data.
	 */
	public void show() {
		showAction.accept(content());
	}
}