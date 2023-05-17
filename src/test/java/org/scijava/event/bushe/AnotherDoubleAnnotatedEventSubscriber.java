package org.scijava.event.bushe;

import java.util.Collection;
import java.util.List;

/** Test class for class-based subscriptions */
public class AnotherDoubleAnnotatedEventSubscriber {

   static int timesCalled = 0;

   public static int getTimesCalled() {
      return timesCalled;
   }

   public static void setTimesCalled(int times) {
      timesCalled = times;
   }

   @EventSubscriber(eventClass = List.class)
   public void doList(Collection collection) {
      timesCalled++;
   }
}
