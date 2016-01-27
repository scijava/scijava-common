package org.scijava.annotations;

import java.util.ArrayList;
import java.util.List;

@Simple(string1 = "adfd")
public class AnnotatedD {

	public AnnotatedD() {
		List<String> list = new ArrayList<>();
		list.stream().reduce(String::concat).get();
	}
}
