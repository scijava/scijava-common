package org.scijava.event.bushe;

import java.io.File;

public class StrongAnnotatedEventSubscriber {
   static String lastCall = null;
   static int timesCalled = 0;

   public static int getTimesCalled() {
      return timesCalled;
   }

   public static void setTimesCalled(int times) {
      timesCalled = times;
   }

   public static String getLastCall() {
      return lastCall;
   }

   public static void setLastCall(String call) {
      lastCall = call;
   }

   @EventSubscriber(referenceStrength = ReferenceStrength.STRONG)
   public void doStrong(File it) {
      lastCall = "doStrong";
      timesCalled++;
   }
}
