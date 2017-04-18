/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.scijava.script;

import java.util.List;

/**
 * Data structure housing suggestions given by {@link AutoCompleter} helpers.
 * 
 * @author Hadrien Mary
 */
public class AutoCompletionResult {

	private List<String> matches;
	private int startIndex;

	public AutoCompletionResult(final List<String> matches) {
		this(matches, 0);
	}

	public AutoCompletionResult(final List<String> matches,
		final int startIndex)
	{
		this.matches = matches;
		this.startIndex = startIndex;
	}

	public int getStartIndex() {
		return startIndex;
	}

	public List<String> getMatches() {
		return matches;
	}

}
