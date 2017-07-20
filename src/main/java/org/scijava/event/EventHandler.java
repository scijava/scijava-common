/*
 * #%L
 * SciJava Common shared library for SciJava software.
 * %%
 * Copyright (C) 2009 - 2017 Board of Regents of the University of
 * Wisconsin-Madison, Broad Institute of MIT and Harvard, Max Planck
 * Institute of Molecular Cell Biology and Genetics, University of
 * Konstanz, and KNIME GmbH.
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

package org.scijava.event;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation identifying an event handler method. This annotation allows a
 * class to subscribe to multiple types of events by implementing multiple event
 * handling methods and annotating each with @{@link EventHandler}.
 * <p>
 * Note to developers: This annotation serves exactly the same purpose as
 * EventBus's {@link org.bushe.swing.event.annotation.EventSubscriber}
 * annotation, recapitulating a subset of the same functionality. We do this to
 * avoid third party code depending directly on EventBus. That is, we do not
 * wish to require SciJava developers to {@code import org.bushe.swing.event.*}
 * or similar. In this way, EventBus is isolated as only a transitive dependency
 * of downstream code, rather than a direct dependency. Unfortunately, because
 * Java annotation interfaces cannot utilize inheritance, we have to
 * recapitulate the functionality rather than extend it (as we are able to do
 * with {@link EventSubscriber}).
 * </p>
 * 
 * @author Curtis Rueden
 * @see EventService
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface EventHandler {

	/**
	 * Unique subscription key. If multiple {@code @EventHandler} annotations
	 * exist with the same key, only the first to be subscribed will be respected;
	 * the others will be silently ignored. If no key is specified, the event
	 * handler is always subscribed.
	 * <p>
	 * This feature exists to enable better extensibility of event handling: if
	 * code exists that handles an event in an undesirable way, that logic can be
	 * completely intercepted and overridden by writing more code that handles the
	 * event in a better way, specifying the same key as the original.
	 * </p>
	 */
	String key() default "";

}
