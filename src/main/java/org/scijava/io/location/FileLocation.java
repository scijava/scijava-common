/*
 * #%L
 * SciJava Common shared library for SciJava software.
 * %%
 * Copyright (C) 2009 - 2023 SciJava developers.
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

package org.scijava.io.location;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * {@link Location} backed by a {@link File} on disk.
 *
 * @author Curtis Rueden
 * @author Gabriel Einsdorf
 */
public class FileLocation extends AbstractLocation implements
	BrowsableLocation
{

	private final File file;

	public FileLocation(final File file) {
		Objects.requireNonNull(file);
		this.file = file;
	}

	public FileLocation(final String path) {
		this(new File(path));
	}

	public FileLocation(final URI path) {
		this(new File(path));
	}

	// -- FileLocation methods --

	/** Gets the associated {@link File}. */
	public File getFile() {
		return file;
	}

	// -- Location methods --

	@Override
	public URI getURI() {
		return getFile().toURI();
	}

	@Override
	public String getName() {
		return file.getName();
	}

	// -- BrowsableLocation methods --

	@Override
	public FileLocation parent() throws IOException {
		return new FileLocation(file.getParentFile());
	}

	@Override
	public Set<BrowsableLocation> children() throws IOException {
		validateDirectory();
		final File[] files = file.listFiles();
		if (files == null) return Collections.emptySet();

		final Set<BrowsableLocation> out = new HashSet<>(files.length);
		for (final File child : files) {
			out.add(new FileLocation(child));
		}
		return out;
	}

	@Override
	public FileLocation sibling(final String path) {
		return new FileLocation(new File(file.getParentFile(), path));
	}

	@Override
	public FileLocation child(final String name) {
		validateDirectory();
		return new FileLocation(new File(file, name));
	}

	@Override
	public boolean isDirectory() {
		return file.isDirectory();
	}

	// -- Helper methods --

	private void validateDirectory() {
		if (isDirectory()) return;
		throw new IllegalArgumentException(
			"This location does not point to a directory!");
	}
}
