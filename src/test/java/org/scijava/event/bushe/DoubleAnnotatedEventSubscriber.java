package org.scijava.event.bushe;

import java.util.List;
import java.util.Collection;
import java.util.Iterator;
import java.io.File;
import java.awt.Color;
import javax.swing.JToggleButton;
import javax.swing.JComponent;

/** Test class for class-based subscriptions */
public class DoubleAnnotatedEventSubscriber {

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

   @EventTopicSubscriber(topic="foo")
   public void foo(String topic, Object o) {
      timesCalled++;
   }
}
