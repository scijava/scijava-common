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

package org.scijava.input;

import java.util.HashSet;

import org.scijava.display.Display;
import org.scijava.display.event.DisplayDeletedEvent;
import org.scijava.display.event.input.KyPressedEvent;
import org.scijava.display.event.input.KyReleasedEvent;
import org.scijava.display.event.input.MsExitedEvent;
import org.scijava.display.event.input.MsMovedEvent;
import org.scijava.display.event.input.MsPressedEvent;
import org.scijava.display.event.input.MsReleasedEvent;
import org.scijava.event.EventHandler;
import org.scijava.event.EventService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.service.AbstractService;
import org.scijava.service.Service;

/**
 * Default implementation of {@link InputService}.
 * 
 * @author Curtis Rueden
 */
@Plugin(type = Service.class)
public class DefaultInputService extends AbstractService implements
	InputService
{

	@Parameter
	private EventService eventService;

	private InputModifiers modifiers;

	private boolean altDown = false;
	private boolean altGrDown = false;
	private boolean ctrlDown = false;
	private boolean metaDown = false;
	private boolean shiftDown = false;

	private HashSet<KeyCode> pressedKeys = new HashSet<>();

	private HashSet<Integer> buttonsDown = new HashSet<>();

	private Display<?> display;
	private int lastX = -1, lastY = -1;

	// -- InputService methods --

	@Override
	public EventService eventService() {
		return eventService;
	}

	@Override
	public InputModifiers getModifiers() {
		return modifiers;
	}

	@Override
	public boolean isAltDown() {
		return altDown;
	}

	@Override
	public boolean isAltGrDown() {
		return altGrDown;
	}

	@Override
	public boolean isCtrlDown() {
		return ctrlDown;
	}

	@Override
	public boolean isMetaDown() {
		return metaDown;
	}

	@Override
	public boolean isShiftDown() {
		return shiftDown;
	}

	@Override
	public boolean isKeyDown(final KeyCode code) {
		return pressedKeys.contains(code);
	}

	@Override
	public Display<?> getDisplay() {
		return display;
	}

	@Override
	public int getX() {
		return lastX;
	}

	@Override
	public int getY() {
		return lastY;
	}

	@Override
	public boolean isButtonDown(final int button) {
		return buttonsDown.contains(button);
	}

	// -- Event handlers --

	@EventHandler
	public void onEvent(final KyPressedEvent evt) {
		modifiers = evt.getModifiers();
		altDown = modifiers.isAltDown();
		altGrDown = modifiers.isAltGrDown();
		ctrlDown = modifiers.isCtrlDown();
		metaDown = modifiers.isMetaDown();
		shiftDown = modifiers.isShiftDown();
		pressedKeys.add(evt.getCode());
	}

	@EventHandler
	public void onEvent(final KyReleasedEvent evt) {
		modifiers = evt.getModifiers();
		altDown = modifiers.isAltDown();
		altGrDown = modifiers.isAltGrDown();
		ctrlDown = modifiers.isCtrlDown();
		metaDown = modifiers.isMetaDown();
		shiftDown = modifiers.isShiftDown();
		pressedKeys.remove(evt.getCode());
	}

	@EventHandler
	protected void onEvent(final MsMovedEvent evt) {
		updateCoords(evt.getDisplay(), evt.getX(), evt.getY());
	}

	@EventHandler
	protected void onEvent(@SuppressWarnings("unused") final MsExitedEvent evt) {
		clearCoords();
	}

	@EventHandler
	protected void onEvent(final MsPressedEvent evt) {
		modifiers = evt.getModifiers();
		buttonsDown.add(evt.getButton());
	}

	@EventHandler
	protected void onEvent(final MsReleasedEvent evt) {
		modifiers = evt.getModifiers();
		buttonsDown.remove(evt.getButton());
	}

	@EventHandler
	protected void onEvent(final DisplayDeletedEvent evt) {
		if (display != evt.getObject()) return;
		clearCoords();
	}

	// -- Helper methods --

	private void updateCoords(final Display<?> d, final int x, final int y) {
		display = d;
		lastX = x;
		lastY = y;
	}

	private void clearCoords() {
		updateCoords(null, -1, -1);
	}

}
