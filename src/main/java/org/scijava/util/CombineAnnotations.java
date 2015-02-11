/*
 * #%L
 * SciJava Common shared library for SciJava software.
 * %%
 * Copyright (C) 2009 - 2015 Board of Regents of the University of
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

package org.scijava.util;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import org.scijava.annotations.AnnotationCombiner;

/**
 * @deprecated Use {@link org.scijava.annotations.AnnotationCombiner} instead.
 */
@Deprecated
public class CombineAnnotations
{

	/**
	 * @deprecated Use
	 *             {@link org.scijava.annotations.AnnotationCombiner#AnnotationCombiner()}
	 *             instead.
	 * @throws IOException
	 */
	@Deprecated
	public CombineAnnotations() throws IOException {}

	private AnnotationCombiner combiner = new AnnotationCombiner();

	/**
	 * @deprecated Use
	 *             {@link org.scijava.annotations.AnnotationCombiner#combine(File)}
	 *             instead.
	 */
	@Deprecated
	public void combine() throws IOException, ClassNotFoundException {
		try {
			combiner.combine(null);
		}
		catch (final Exception e) {
			if (e instanceof IOException) {
				throw (IOException) e;
			}
			if (e instanceof ClassNotFoundException) {
				throw (ClassNotFoundException) e;
			}
			throw new IOException(e);
		}
	}

	/**
	 * @deprecated Use {@link org.scijava.annotations.AnnotationCombiner#getAnnotationFiles()} instead.
	 */
	@Deprecated
	public Set<String> getAnnotationFiles() throws IOException {
		return combiner.getAnnotationFiles();
	}

	/**
	 * @deprecated Use
	 *             {@link org.scijava.annotations.AnnotationCombiner#main(String[])}
	 *             instead.
	 */
	@Deprecated
	public static void main(final String[] args) throws Exception {
		new org.scijava.annotations.AnnotationCombiner().combine(args.length > 0
			? new File(args[0]) : null);
	}

}
