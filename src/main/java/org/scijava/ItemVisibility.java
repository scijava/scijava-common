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

package org.scijava;

/**
 * Defines the "visibility" of a parameter.
 * 
 * @author Curtis Rueden
 */
public enum ItemVisibility {

	/**
	 * Item is included in the history for purposes of data provenance, and
	 * included as a parameter when recording scripts.
	 */
	NORMAL,

	/**
	 * Item is excluded from the history for the purposes of data provenance, but
	 * still included as a parameter when recording scripts.
	 */
	TRANSIENT,

	/**
	 * Item is excluded from the history for the purposes of data provenance, and
	 * also excluded as a parameter when recording scripts. This option should
	 * only be used for items with no effect on the final output, such as a
	 * "verbose" flag.
	 */
	INVISIBLE,

	/**
	 * As {@link #INVISIBLE}, and further indicating that the item's value is
	 * intended as a message to the user (e.g., in the input harvester panel)
	 * rather than an actual parameter to the module execution.
	 */
	MESSAGE

}
