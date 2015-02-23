
package org.scijava.path;

import java.util.HashMap;

public class LabeledMap extends HashMap<String, LabeledMap> {

	private String label;

	public LabeledMap(final String label) {
		this.label = label;
	}

	public String getLabel() {
		return label;
	}
}
