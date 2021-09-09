/*-
 * #%L
 * SciJava Common shared library for SciJava software.
 * %%
 * Copyright (C) 2009 - 2021 SciJava developers.
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

package org.scijava.download;

import java.io.File;
import java.io.IOException;

import org.scijava.io.location.FileLocation;
import org.scijava.io.location.Location;
import org.scijava.util.DigestUtils;
import org.scijava.util.FileUtils;

/**
 * A file-based implementation of {@link LocationCache}.
 *
 * @author Curtis Rueden
 */
public class DiskLocationCache implements LocationCache {

	private File baseDir = new File(System.getProperty("user.home") +
		File.separator + ".scijava" + File.separator + "cache" + File.separator);

	private boolean cacheFileLocations;

	// -- DiskLocationCache methods --

	public File getBaseDirectory() {
		return baseDir;
	}

	public void setBaseDirectory(final File baseDir) {
		if (!baseDir.isDirectory()) {
			throw new IllegalArgumentException("Not a directory: " + baseDir);
		}
		this.baseDir = baseDir;
	}

	public boolean isFileLocationCachingEnabled() {
		return cacheFileLocations;
	}

	public void setFileLocationCachingEnabled(final boolean enabled) {
		// NB: It is possible the input file is stored on a volume which is much
		// slower than the local disk cache, so we make this setting configurable.
		cacheFileLocations = enabled;
	}

	// -- LocationCache methods --

	@Override
	public boolean canCache(final Location source) {
		if (source instanceof FileLocation && !isFileLocationCachingEnabled()) {
			// The cache is not configured to cache files to other files.
			return false;
		}
		return source.getURI() != null;
	}

	@Override
	public Location cachedLocation(final Location source) {
		if (!canCache(source)) {
			throw new IllegalArgumentException("Uncacheable source: " + source);
		}
		return new FileLocation(cachedData(source));
	}

	@Override
	public String loadChecksum(final Location source) throws IOException {
		final File cachedChecksum = cachedChecksum(source);
		if (!cachedChecksum.exists()) return null;
		return DigestUtils.string(FileUtils.readFile(cachedChecksum));
	}

	@Override
	public void saveChecksum(final Location source, final String checksum)
		throws IOException
	{
		final File cachedChecksum = cachedChecksum(source);
		FileUtils.writeFile(cachedChecksum, DigestUtils.bytes(checksum));
	}

	// -- Helper methods --

	private File cachedData(final Location source) {
		return cachedFile(source, ".data");
	}

	private File cachedChecksum(final Location source) {
		return cachedFile(source, ".checksum");
	}

	private File cachedFile(final Location source, final String suffix) {
		final String hexCode = Integer.toHexString(source.hashCode());
		return new File(getBaseDirectory(), hexCode + suffix);
	}
}
