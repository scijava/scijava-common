/*
 * #%L
 * SciJava Common shared library for SciJava software.
 * %%
 * Copyright (C) 2009 - 2024 SciJava developers.
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

package org.scijava.ui.dnd;

import java.util.Collections;
import java.util.List;

import org.scijava.Context;

/**
 * Default implementation of {@link DragAndDropData}, which provides a
 * UI-agnostic way to bundle an object together with its MIME type.
 * 
 * @author Barry DeZonia
 * @author Curtis Rueden
 */
public class DefaultDragAndDropData extends AbstractDragAndDropData {

	// -- Fields --

	private final MIMEType mime;
	private final Object data;

	// -- Constructor --

	public DefaultDragAndDropData(final Context context, final MIMEType mimeType,
		final Object data)
	{
		setContext(context);
		this.mime = mimeType;
		this.data = data;
	}

	// -- DragAndDropData methods --

	@Override
	public boolean isSupported(final MIMEType mimeType) {
		return mime.equals(mimeType);
	}

	@Override
	public Object getData(final MIMEType mimeType) {
		return isSupported(mimeType) ? data : null;
	}

	@Override
	public List<MIMEType> getMIMETypes() {
		return Collections.singletonList(mime);
	}

}
