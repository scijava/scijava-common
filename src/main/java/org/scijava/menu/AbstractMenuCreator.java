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

package org.scijava.menu;

/**
 * Abstract helper class for generating a menu structure.
 * <p>
 * The class differentiates between top-level menu components (such as
 * {@link javax.swing.JMenuBar}), and hierarchical menu components (such as
 * {@link javax.swing.JMenu}).
 * </p>
 * 
 * @author Curtis Rueden
 * @param <T> Top-level menu class to populate (e.g.,
 *          {@link javax.swing.JMenuBar} or {@link javax.swing.JMenu} or
 *          {@link java.awt.MenuBar})
 * @param <M> Hierarchical menu class (e.g., {@link javax.swing.JMenu} or
 *          {@link java.awt.Menu})
 */
public abstract class AbstractMenuCreator<T, M> implements MenuCreator<T> {

	// -- MenuCreator methods --

	@Override
	public void createMenus(final ShadowMenu root, final T target) {
		double lastWeight = Double.NaN;
		for (final ShadowMenu child : root.getChildren()) {
			final double weight = child.getMenuEntry().getWeight();
			final double difference = Math.abs(weight - lastWeight);
			if (difference > 1) addSeparatorToTop(target);
			lastWeight = weight;
			if (child.isLeaf()) {
				addLeafToTop(child, target);
			}
			else {
				final M nonLeaf = addNonLeafToTop(child, target);
				populateMenu(child, nonLeaf);
			}
		}
	}

	// -- Internal methods --

	protected abstract void addLeafToMenu(ShadowMenu shadow, M target);

	protected abstract void addLeafToTop(ShadowMenu shadow, T target);

	protected abstract M addNonLeafToMenu(ShadowMenu shadow, M target);

	protected abstract M addNonLeafToTop(ShadowMenu shadow, T target);

	protected abstract void addSeparatorToMenu(M target);

	protected abstract void addSeparatorToTop(T target);

	// -- Helper methods --

	private void populateMenu(final ShadowMenu shadow, final M target) {
		double lastWeight = Double.NaN;
		for (final ShadowMenu child : shadow.getChildren()) {
			final double weight = child.getMenuEntry().getWeight();
			final double difference = Math.abs(weight - lastWeight);
			if (difference > 1) addSeparatorToMenu(target);
			lastWeight = weight;
			if (child.isLeaf()) {
				addLeafToMenu(child, target);
			}
			else {
				final M nonLeaf = addNonLeafToMenu(child, target);
				populateMenu(child, nonLeaf);
			}
		}
	}

}
