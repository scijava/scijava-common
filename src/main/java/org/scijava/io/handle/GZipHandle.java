/*
 * #%L
 * SCIFIO library for reading and converting scientific file formats.
 * %%
 * Copyright (C) 2011 - 2016 Board of Regents of the University of
 * Wisconsin-Madison
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

package org.scijava.io.handle;

import java.io.IOException;
import java.util.zip.GZIPInputStream;

import org.scijava.io.location.AbstractCompressedHandle;
import org.scijava.io.location.GZipLocation;
import org.scijava.plugin.Plugin;

/**
 * StreamHandle implementation for reading from gzip-compressed files or byte
 * arrays. Instances of GZipHandle are read-only.
 *
 * @author Melissa Linkert
 * @author Gabriel Einsdorf
 */
@Plugin(type = DataHandle.class)
public class GZipHandle extends AbstractCompressedHandle<GZipLocation> {

	@Override
	public Class<GZipLocation> getType() {
		return GZipLocation.class;
	}

	@Override
	protected void initInputStream() throws IOException {
		inputStream = new GZIPInputStream(new DataHandleInputStream<>(raw()));
	}

//	@Override
//	public boolean isConstructable(final String file) throws IOException {
//		final byte[] b = new byte[2];
//		s.read(b);
//		s.close();
//		return Bytes.toInt(b, true) == GZIPInputStream.GZIP_MAGIC;
//	}

}
