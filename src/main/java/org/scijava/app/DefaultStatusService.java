/*
 * #%L
 * SciJava Common shared library for SciJava software.
 * %%
 * Copyright (C) 2009 - 2013 Board of Regents of the University of
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
 * 
 * The views and conclusions contained in the software and documentation are
 * those of the authors and should not be interpreted as representing official
 * policies, either expressed or implied, of any organization.
 * #L%
 */

package org.scijava.app;

import org.scijava.app.event.StatusEvent;
import org.scijava.event.EventService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.service.AbstractService;
import org.scijava.service.Service;

/**
 * Default service for status notifications.
 * 
 * @author Curtis Rueden
 */
@Plugin(type = Service.class)
public class DefaultStatusService extends AbstractService implements
	StatusService
{

	@Parameter
	private EventService eventService;

	@Parameter
	private AppService appService;

	// -- StatusService methods --

	@Override
	public void showProgress(final int value, final int maximum) {
		publish(new StatusEvent(value, maximum));
	}

	@Override
	public void showStatus(final String message) {
		publish(new StatusEvent(message));
	}

	@Override
	public void showStatus(final int progress, final int maximum,
		final String message)
	{
		publish(new StatusEvent(progress, maximum, message));
	}

	@Override
	public void warn(final String message) {
		publish(new StatusEvent(message, true));
	}

	@Override
	public void showStatus(final int progress, final int maximum,
		final String message, final boolean warn)
	{
		publish(new StatusEvent(progress, maximum, message, warn));
	}

	@Override
	public void clearStatus() {
		publish(new StatusEvent(""));
	}

	@Override
	public String getStatusMessage(final String appName,
		final StatusEvent statusEvent)
	{
		final String message = statusEvent.getStatusMessage();
		if (!"".equals(message)) return message;
		return appService.getApp(appName).getInfo(false);
	}
	
	/**
	 * Publish the status event to the event service.
	 * The default behavior is to publish status asynchronously.
	 * You can change this behavior by overriding this method
	 * in a derived class.
	 * 
	 * @param statusEvent the event to send to status listeners.
	 */
	protected void publish(final StatusEvent statusEvent)
	{
		eventService.publishLater(statusEvent);
	}

}
