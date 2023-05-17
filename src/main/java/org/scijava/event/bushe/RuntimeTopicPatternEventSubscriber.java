package org.scijava.event.bushe;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@interface RuntimeTopicPatternEventSubscriber {
    /**
     * @return name of a method (which should return a String) and whose return value will become the subscription topic.
     */
    String methodName() default "getTopicPatternName";

    /** @return Whether to subscribe weakly or strongly. */
    ReferenceStrength referenceStrength() default ReferenceStrength.WEAK;

    /**
     * @return event service to subscribe to, default to the EventServiceLocator.SERVICE_NAME_EVENT_BUS.
     */
    String eventServiceName() default EventServiceLocator.SERVICE_NAME_EVENT_BUS;

    /** @return Determines the order in which this subscriber is called, default is FIFO.*/
    int priority() default 0;

    /** @return Whether or not to subscribe to the exact class or a class hierarchy, defaults to class hierarchy (false). */
    boolean exact() default false;

    /**
     * @return Whether or not to autocreate the event service if it doesn't exist on subscription, default is true. If the service needs to be created, it must have a default constructor.
     */
    Class<? extends EventService> autoCreateEventServiceClass() default ThreadSafeEventService.class;
}
