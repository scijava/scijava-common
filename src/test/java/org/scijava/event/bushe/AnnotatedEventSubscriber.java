package org.scijava.event.bushe;

import java.io.File;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.awt.Color;
import javax.swing.JComponent;
import javax.swing.JToggleButton;

/** Test class for class-based subscriptions */
public class AnnotatedEventSubscriber {
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

   @EventSubscriber
   public void doColorChange(Color color) {
      timesColorChanged++;
      timesCalled++;
   }

   @EventSubscriber(eventClass = List.class)
   public void doList(Collection collection) {
      lastCall = "doList";
      timesCalled++;
   }

   @EventSubscriber(eventClass = JToggleButton.class, exact = true)
   public void doJToggleButtonExactly(JComponent list) {
      lastCall = "doJToggleButtonExactly";
      timesCalled++;
   }

   @EventSubscriber(eventClass = Iterator.class,
           eventServiceName = "IteratorService",
           autoCreateEventServiceClass = ThreadSafeEventService.class)
   public void autoCreateEventServiceClass(Iterator it) {
      lastCall = "autoCreateEventServiceClass";
      timesCalled++;
   }

   @EventTopicSubscriber(topic = "File.Open")
   public void simpleTopicOpenFile(String topic, File file) {
      lastCall = "simpleTopicOpenFile";
      timesCalled++;
   }

   @EventTopicSubscriber(topic = "Iterator",
           eventServiceName = "IteratorService",
           autoCreateEventServiceClass = ThreadSafeEventService.class)
   public void autoCreateEventServiceTopic(String topic, Iterator it) {
      lastCall = "autoCreateEventServiceClass";
      timesCalled++;
   }

   @EventTopicPatternSubscriber(topicPattern = "IceCream.*",
           eventServiceName = "IceCreamService")
   public void doIceCream(String topic, String order) {
      lastCall = "doIceCream";
      timesCalled++;
   }

}
