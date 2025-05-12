/*
 * #%L
 * SciJava Common shared library for SciJava software.
 * %%
 * Copyright (C) 2009 - 2025 SciJava developers.
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

package org.scijava.text;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.scijava.plugin.HandlerService;
import org.scijava.service.SciJavaService;

/**
 * Interface for service that works with text formats.
 * 
 * @author Curtis Rueden
 */
public interface TextService extends HandlerService<File, TextFormat>,
	SciJavaService
{

	/** Reads the data from the given file into a string. */
	String open(File file) throws IOException;

	/** Expresses the given text string as HTML. */
	String asHTML(File file) throws IOException;

	// NB: Javadoc overrides.

	// -- HandlerService methods --

	/** Gets the text format which best handles the given file. */
	@Override
	default TextFormat getHandler(File file) {
		return HandlerService.super.getHandler(file);
	}

	// -- SingletonService methods --

	/** Gets the list of available text formats. */
	@Override
	List<TextFormat> getInstances();

	// -- Typed methods --

	/** Gets whether the given file contains text data in a supported format. */
	@Override
	default boolean supports(final File file) {
		return HandlerService.super.supports(file);
	}
}
