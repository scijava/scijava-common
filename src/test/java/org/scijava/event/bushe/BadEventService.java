package org.scijava.event.bushe;

public class BadEventService extends ThreadSafeEventService {


   /** @see org.scijava.event.bushe.EventService#subscribe(String,org.scijava.event.bushe.IEventTopicSubscriber) */
   public boolean subscribe(String topic, IEventTopicSubscriber eh) {
      throw new RuntimeException("For testing");
   }
}
