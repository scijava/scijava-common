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

package org.scijava.ui.dnd;

import java.util.List;

import org.scijava.ui.dnd.event.DragEnterEvent;
import org.scijava.ui.dnd.event.DropEvent;

/**
 * Interface for drag-and-drop data.
 * 
 * @author Curtis Rueden
 */
public interface DragAndDropData {

	/**
	 * Gets whether the data can be provided as an object with the given MIME
	 * type.
	 */
	boolean isSupported(MIMEType mimeType);

	/**
	 * Gets whether the data can be provided as an object of the given Java class.
	 */
	default boolean isSupported(final Class<?> type) {
		return getMIMEType(type) != null;
	}

	/**
	 * Gets the data with respect to the given MIME type.
	 * 
	 * @return The data object for the given MIME type. May return null if the
	 *         data is requested too early in the drag-and-drop process, such as
	 *         during a {@link DragEnterEvent} rather than a {@link DropEvent}.
	 * @throws IllegalArgumentException if the MIME type is not supported.
	 */
	Object getData(MIMEType mimeType);

	/** Gets the data as an object of the given Java class. */
	default <T> T getData(final Class<T> type) {
		final MIMEType mimeType = getMIMEType(type);
		if (mimeType == null) return null;
		@SuppressWarnings("unchecked")
		final T data = (T) getData(mimeType);
		return data;
	}

	/** Gets the best supported MIME type matching the given Java class. */
	default MIMEType getMIMEType(final Class<?> type) {
		for (final MIMEType mimeType : getMIMETypes()) {
			if (mimeType.isCompatible(type)) return mimeType;
		}
		return null;
	}

	/** Gets the list of supported MIME types. */
	List<MIMEType> getMIMETypes();

}
