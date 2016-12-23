/*
 * #%L
 * SciJava Common shared library for SciJava software.
 * %%
 * Copyright (C) 2009 - 2016 Board of Regents of the University of
 * Wisconsin-Madison, Broad Institute of MIT and Harvard, and Max Planck
 * Institute of Molecular Cell Biology and Genetics.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

package org.scijava.parse;

import org.scijava.service.SciJavaService;

/**
 * Interface for service that parses strings.
 *
 * @author Curtis Rueden
 */
public interface ParseService extends SciJavaService {

	/**
	 * Parses a comma-delimited list of data elements.
	 * <p>
	 * Some data elements might be {@code key=value} pairs, while others might be
	 * raw values (i.e., no equals sign).
	 * </p>
	 * 
	 * @param arg The string to parse.
	 * @return A parsed list of {@link Item}s.
	 * @throws IllegalArgumentException If the string does not conform to expected
	 *           syntax.
	 */
	default Items parse(final String arg) {
		return parse(arg, true);
	}

	/**
	 * Parses a comma-delimited list of data elements.
	 * <p>
	 * Some data elements might be {@code key=value} pairs, while others might be
	 * raw values (i.e., no equals sign).
	 * </p>
	 * 
	 * @param arg The string to parse.
	 * @param strict Whether to fail fast when encountering an unassigned variable
	 *          token.
	 * @return A parsed list of {@link Item}s.
	 * @throws IllegalArgumentException If the string does not conform to expected
	 *           syntax.
	 */
	Items parse(String arg, boolean strict);
}
