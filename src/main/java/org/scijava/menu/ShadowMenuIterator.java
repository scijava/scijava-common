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

package org.scijava.menu;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.scijava.module.ModuleInfo;

/**
 * Recursive iterator for {@link ShadowMenu} hierarchies.
 * 
 * @author Curtis Rueden
 */
public class ShadowMenuIterator implements Iterator<ModuleInfo> {

	private final ShadowMenu node;
	private final List<ShadowMenuIterator> childIterators;
	private int index;

	public ShadowMenuIterator(final ShadowMenu node) {
		this.node = node;
		final List<ShadowMenu> children = node.getChildren();
		childIterators = new ArrayList<>();
		for (final ShadowMenu child : children) {
			childIterators.add(new ShadowMenuIterator(child));
		}
		index = node.getModuleInfo() == null ? 0 : -1;
	}

	@Override
	public boolean hasNext() {
		return index < childIterators.size();
	}

	@Override
	public ModuleInfo next() {
		if (!hasNext()) throw new NoSuchElementException();
		if (index < 0) {
			index++;
			return node.getModuleInfo();
		}
		final ShadowMenuIterator iter = childIterators.get(index);
		final ModuleInfo next = iter.next();
		if (!iter.hasNext()) index++;
		return next;
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}

}
