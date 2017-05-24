/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.scijava.script.autocompletion;

import java.util.List;

/**
 *
 * @author hadim
 */
public class AutoCompletionResult {

    protected List<String> matches;
    protected int startIndex;

    public AutoCompletionResult(List<String> matches) {
        this(matches, 0);
    }

    public AutoCompletionResult(List<String> matches, int startIndex) {
        this.matches = matches;
        this.startIndex = startIndex;
    }

    public int getStartIndex() {
        return this.startIndex;
    }

    public List<String> getMatches() {
        return this.matches;
    }

}
