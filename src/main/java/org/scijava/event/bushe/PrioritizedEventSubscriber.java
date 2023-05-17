package org.scijava.event.bushe;

/**
 * This is a convenience interface, particularly for inner classes, that implements
 * {@link IEventSubscriber} and {@link Prioritized}.
 */
interface PrioritizedEventSubscriber extends IEventSubscriber, Prioritized {
}
