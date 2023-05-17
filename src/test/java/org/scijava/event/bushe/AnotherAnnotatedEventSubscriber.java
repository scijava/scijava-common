package org.scijava.event.bushe;

import java.util.List;
import java.util.Collection;
import java.util.Iterator;
import java.io.File;
import java.awt.Color;
import javax.swing.JToggleButton;
import javax.swing.JComponent;

/** Test class for class-based subscriptions */
public class AnotherAnnotatedEventSubscriber {
   static int timesColorChanged = 0;
   static String lastCall = null;
   static int timesCalled = 0;

   public static int getTimesColorChanged() {
      return timesColorChanged;
   }

   public static void setTimesColorChanged(int times) {
      timesColorChanged = times;
   }

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

   @EventSubscriber(eventClass = List.class, referenceStrength = ReferenceStrength.STRONG)
   public void doList(Collection collection) {
      lastCall = "doList";
      timesCalled++;
   }
}
