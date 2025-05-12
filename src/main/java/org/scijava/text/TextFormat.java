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
import java.util.List;

import org.scijava.plugin.HandlerPlugin;
import org.scijava.plugin.Plugin;
import org.scijava.util.FileUtils;

/**
 * {@code TextFormat} is a plugin that provides handling for a text markup
 * language.
 * <p>
 * Text formats discoverable at runtime must implement this interface and be
 * annotated with @{@link Plugin} with attribute {@link Plugin#type()} =
 * {@link TextFormat}.class. While it possible to create a text format merely by
 * implementing this interface, it is encouraged to instead extend
 * {@link AbstractTextFormat}, for convenience.
 * </p>
 * 
 * @author Curtis Rueden
 * @see Plugin
 * @see TextService
 */
public interface TextFormat extends HandlerPlugin<File> {

	/** Gets the list of filename extensions for text in this format. */
	List<String> getExtensions();

	/** Expresses the given text string in HTML format. */
	String asHTML(String text);

	// -- Typed methods --

	@Override
	default boolean supports(final File file) {
		if (!HandlerPlugin.super.supports(file)) return false;
		for (final String ext : getExtensions()) {
			if (FileUtils.getExtension(file).equalsIgnoreCase(ext)) return true;
		}
		return false;
	}

	@Override
	default Class<File> getType() {
		return File.class;
	}

}
