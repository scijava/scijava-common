package org.scijava.event.bushe;

/**
 * This is a convenience interface, particularly for inner classes, that implements
 * {@link org.scijava.event.bushe.EventTopicSubscriber} and {@link org.scijava.event.bushe.Prioritized}.
 */
public interface PrioritizedEventTopicSubscriber extends EventTopicSubscriber, Prioritized {
}