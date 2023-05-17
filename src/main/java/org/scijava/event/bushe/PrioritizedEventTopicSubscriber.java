package org.scijava.event.bushe;

/**
 * This is a convenience interface, particularly for inner classes, that implements
 * {@link org.scijava.event.bushe.IEventTopicSubscriber} and {@link org.scijava.event.bushe.Prioritized}.
 */
interface PrioritizedEventTopicSubscriber extends IEventTopicSubscriber, Prioritized {
}